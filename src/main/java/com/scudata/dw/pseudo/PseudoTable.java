package com.scudata.dw.pseudo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.scudata.common.RQException;
import com.scudata.common.Types;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MergeCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.op.Conj;
import com.scudata.dm.op.Group;
import com.scudata.dm.op.Join;
import com.scudata.dm.op.New;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.Select;
import com.scudata.dm.op.Switch;
import com.scudata.dw.ColPhyTable;
import com.scudata.dw.Cursor;
import com.scudata.dw.IFilter;
import com.scudata.dw.IPhyTable;
import com.scudata.dw.RowCursor;
import com.scudata.dw.RowPhyTable;
import com.scudata.dw.PhyTable;
import com.scudata.expression.Constant;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.ParamParser;
import com.scudata.expression.UnknownSymbol;
import com.scudata.expression.VarParam;
import com.scudata.expression.mfn.sequence.Contain;
import com.scudata.expression.operator.And;
import com.scudata.expression.operator.DotOperator;
import com.scudata.expression.operator.Equals;
import com.scudata.expression.operator.NotEquals;
import com.scudata.expression.operator.Or;

public class PseudoTable extends Pseudo {
	//创建游标需要的参数
	protected String []fkNames;
	protected Sequence []codes;
	protected int pathCount;
	
	protected ArrayList<Operation> extraOpList = new ArrayList<Operation>();//其它情况产生的延迟计算（不是主动调用select添加）
	protected ArrayList<PseudoColumn> joinColumnList = new ArrayList<PseudoColumn>();//外键计算列
	protected PseudoTable mcsTable;
	
	protected boolean hasPseudoColumns = false;//是否需要根据伪字段转换（枚举、二值）表达式
	
	public PseudoTable() {
	}
	
	/**
	 * 产生虚表对象
	 * @param rec 定义记录
	 * @param hs 分机序列
	 * @param n 并行数
	 * @param ctx
	 */
	public PseudoTable(BaseRecord rec, int n, Context ctx) {
		pd = new PseudoDefination(rec, ctx);
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}

	public PseudoTable(PseudoDefination pd, int n, Context ctx) {
		this.pd = pd;
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}
	
	public PseudoTable(BaseRecord rec, PseudoTable mcs, Context ctx) {
		this(rec, 0, ctx);
		mcsTable = mcs;
	}
	
	public static PseudoTable create(BaseRecord rec, int n, Context ctx) {
		PseudoDefination pd = new PseudoDefination(rec, ctx);
		if (pd.isBFile()) {
			return new PseudoBFile(pd, n, ctx);
		} else {
			return new PseudoTable(pd, n, ctx);
		}
	}
	
	protected void init() {
		if (getPd() != null) {
			allNameList = new ArrayList<String>();
			String []names = getPd().getAllColNames();
			for (String name : names) {
				allNameList.add(name);
			}
			
			if (getPd().getColumns() != null) {
				List<PseudoColumn> columns = getPd().getColumns();
				for (PseudoColumn column : columns) {
					//如果存在枚举伪字段和二值伪字段，要记录下来，在接下来的处理中会用到
					if (column.getPseudo() != null) {
						hasPseudoColumns = true;
					}
					if (column.getBits() != null) {
						hasPseudoColumns = true;
					}
					
					if (column.getDim() != null) {
						if (column.getFkey() == null) {
							addColName(column.getName());
						} else {
							for (String key : column.getFkey()) {
								addColName(key);
							}
						}
						
						Sequence dim = (Sequence) column.getDim();
						if (column.getTkey() != null && dim.dataStruct().getTimeKeyCount() > 0) {
							addColName(column.getTkey());
						}
					}
				}
			}
		}
	}

	public void addPKeyNames() {
		addColNames(getPd().getAllSortedColNames());
	}
	
	public void addColNames(String []nameArray) {
		for (String name : nameArray) {
			addColName(name);
		}
	}
	
	public void addColName(String name) {
		if (name == null) return; 
		if (allNameList.contains(name) && !extraNameList.contains(name)) {
			extraNameList.add(name);
		}
	}
	
	/**
	 * 设置取出字段
	 * @param exps 取出表达式
	 * @param fields 取出别名
	 */
	protected void setFetchInfo(Expression []exps, String []fields) {
		this.exps = null;
		this.names = null;
		boolean needNew = extraNameList.size() > 0;
		Expression newExps[] = null;
		
		extraOpList.clear();
		joinColumnList.clear();
		
		//set FK codes info
		if (fkNameList != null) {
			int size = fkNameList.size();
			fkNames = new String[size];
			fkNameList.toArray(fkNames);
			
			codes = new Sequence[size];
			codeList.toArray(codes);
		}
		
		if (exps == null) {
			if (fields == null) {
				return;
			} else {
				int len = fields.length;
				exps = new Expression[len];
				for (int i = 0; i < len; i++) {
					exps[i] = new Expression(fields[i]);
				}
			}
		}
		
		newExps = exps.clone();//备份一下
		
		/**
		 * 有取出表达式也有取出字段,则检查extraNameList里是否包含exps里的字段
		 * 如果包含就去掉
		 */
		ArrayList<String> tempList = new ArrayList<String>();
		for (String name : extraNameList) {
			if (!tempList.contains(name)) {
				tempList.add(name);
			}
		}
		for (Expression exp : exps) {
			String expName = exp.getIdentifierName();
			if (tempList.contains(expName)) {
				tempList.remove(expName);
			}
		}
		
		ArrayList<String> tempNameList = new ArrayList<String>();
		ArrayList<Expression> tempExpList = new ArrayList<Expression>();
		int size = exps.length;
		for (int i = 0; i < size; i++) {
			Expression exp = exps[i];
			String name = fields[i];
			Node node = exp.getHome();
			
			if (node instanceof UnknownSymbol || node instanceof VarParam) {
				String expName = exp.getIdentifierName();
				if (!allNameList.contains(expName)) {
					/**
					 * 如果是伪字段则做转换
					 */
					PseudoColumn col = pd.findColumnByPseudoName(expName);
					if (col != null) {
						if (col.getDim() != null) {
							if (col.getFkey() == null) {
								String colName = col.getName();
								if (!tempNameList.contains(colName)) {
									tempExpList.add(new Expression(colName));
									tempNameList.add(colName);
								}
							} else {
								for (String colName : col.getFkey()) {
									if (!tempNameList.contains(colName)) {
										tempExpList.add(new Expression(colName));
										tempNameList.add(colName);
									}
								}
							}
							joinColumnList.add(col);
						} else if (col.getExp() != null) {
							//有表达式的伪列
							newExps[i] = new Expression(col.getExp());
							needNew = true;
							
							ArrayList<String> list = new ArrayList<String>();
							newExps[i].getUsedFields(ctx, list);
							for(String field : list) {
								if (!tempNameList.contains(field)) {
									tempExpList.add(new Expression(field));
									tempNameList.add(field);
								}
							}
							
						} else if (col.get_enum() != null) {
							/**
							 * 枚举字段做转换
							 */
							String var = "pseudo_enum_value_" + i;
							ctx.setParamValue(var, col.get_enum());
							name = col.getName();
							newExps[i] = new Expression(var + "(" + name + ")");
							exp = new Expression(name);
							needNew = true;
							if (!tempNameList.contains(name)) {
								tempExpList.add(exp);
								tempNameList.add(name);
							}
						} else if (col.getBits() != null) {
							/**
							 * 二值字段做转换
							 */
							name = col.getName();
							String pname = ((UnknownSymbol) node).getName();
							Sequence seq;
							seq = col.getBits();
							int idx = seq.firstIndexOf(pname) - 1;
							int bit = 1 << idx;
							String str = "and(" + col.getName() + "," + bit + ")!=0";//改为真字段的位运算
							newExps[i] = new Expression(str);
							exp = new Expression(name);
							needNew = true;
							tempExpList.add(exp);
							tempNameList.add(name);
						}
					}
				} else {
					if (!tempNameList.contains(name)) {
						tempExpList.add(exp);
						tempNameList.add(name);
					}
				}
				
//			} else if (node instanceof DotOperator) {
//				Node left = node.getLeft();
//				if (left != null && left instanceof UnknownSymbol) {
//					PseudoColumn col = getPd().findColumnByName( ((UnknownSymbol)left).getName());
//					if (col != null) {
//						Derive derive = new Derive(new Expression[] {exp}, new String[] {name}, null);
//						extraOpList.add(derive);
//					}
//				}
			}
		}

		//如果存在date则必须取出首字段ugrp
		String date = pd.getDate();
		String ugrp = pd.getUgrp();
		if (date != null && !tempNameList.contains(ugrp)) {
			needNew = true;
			tempList.add(ugrp);
		}
		
		for (String name : tempList) {
			tempExpList.add(new Expression(name));
			tempNameList.add(name);
		}
		
		size = tempExpList.size();
		this.exps = new Expression[size];
		tempExpList.toArray(this.exps);
		
		this.names = new String[size];
		tempNameList.toArray(this.names);
	
		if (needNew) {
			New _new = new New(newExps, fields, null);
			extraOpList.add(_new);
		}
		return;
	}
	
	public String[] getFetchColNames(String []fields) {
		ArrayList<String> tempList = new ArrayList<String>();
		if (fields != null) {
			for (String name : fields) {
				tempList.add(name);
			}
		}
		for (String name : extraNameList) {
			if (!tempList.contains(name)) {
				tempList.add(name);
			}
		}
		
		int size = tempList.size();
		if (size == 0) {
			return null;
		}
		String []newFields = new String[size];
		tempList.toArray(newFields);
		return newFields;
	}
	
	/**
	 * 得到虚表的每个实体表的游标构成的数组
	 * @return
	 */
	public ICursor[] getCursors(boolean isColumn) {
		List<IPhyTable> tables = getPd().getTables();
		int size = tables.size();
		ICursor cursors[] = new ICursor[size];
		
		for (int i = 0; i < size; i++) {
			cursors[i] = getCursor(tables.get(i), null, true, isColumn);
		}
		return cursors;
	}
	
	/**
	 * 添加可能用到的外键连接
	 */
	protected void addJoin(ICursor cursor) {
		List<PseudoColumn> list = getFieldSwitchColumns(this.names);
		if (list != null) {
			for (PseudoColumn col : joinColumnList) {
				list.add(col);
			}
		} else {
			list = joinColumnList;
		}
		if (getPd() != null && list != null) {
			for (PseudoColumn column : list) {
				if (column.getDim() != null) {//如果存在外键，则添加一个switch的延迟计算
					Sequence dim;
					if (column.getDim() instanceof Sequence) {
						dim = (Sequence) column.getDim();
					} else {
						dim = ((IPseudo) column.getDim()).cursor(null, null, false).fetch();
					}
					boolean hasTimeKey = column.getTkey() != null && dim.dataStruct().getTimeKeyCount() == 1;
					
					String fkey[] = column.getFkey();
					if (fkey == null) {
						/**
						 * 此时name就是外键字段
						 */
						String[] fkNames = new String[] {column.getName()};
						String[] timeFkNames =hasTimeKey ? new String[] {column.getTkey()} : null;
						Sequence[] codes = new Sequence[] {dim};
						Switch s = new Switch(
								null,
								fkNames, 
								timeFkNames,
								codes,
								null,
								null,
								null);
						cursor.addOperation(s, ctx);
					} else {
						int size = fkey.length;
						
						/**
						 * 如果定义了时间字段,就把时间字段拼接到fkey末尾
						 */
						if (hasTimeKey) {
							size++;
							fkey = new String[size];
							System.arraycopy(column.getFkey(), 0, fkey, 0, size - 1);
							fkey[size - 1] = column.getTkey();
						}
						
						Expression[][] exps = new Expression[1][];
						exps[0] = new Expression[size];
						for (int i = 0; i < size; i++) {
							exps[0][i] = new Expression(fkey[i]);
						}
						Expression[][] newExps = new Expression[1][];
						newExps[0] = new Expression[] {new Expression("~")};
						
						String newName = column.getName();
						if (newName == null && column.getFkey() != null) {
							newName = column.getFkey()[0];
						}
						String[][] newNames = new String[1][];
						newNames[0] = new String[] {newName};
						
						Expression[][] dimKeyExps = new Expression[1][];
						String[] dimKey = column.getDimKey();
						if (dimKey == null) {
							dimKeyExps[0] = null;
						} else {
							Expression[] dimKeyExp = new Expression[size];
							for (int i = 0; i < size; i++) {
								dimKeyExp[i] = new Expression(dimKey[i]);
							}
							dimKeyExps[0] = dimKeyExp;
						}
						
						Join join = new Join(null, null, exps, new Sequence[] {dim}, dimKeyExps, newExps, newNames, null);
						cursor.addOperation(join, ctx);
					}
				}
			}
		}
	}
	
	/**
	 * 得到table的游标
	 * @param table
	 * @param mcs
	 * @param addOpt 是否把附加计算添加
	 * @param isColumn 是否返回列式游标
	 * @return
	 */
	private ICursor getCursor(IPhyTable table, ICursor mcs, boolean addOpt, boolean isColumn) {
		ICursor cursor = null;

		if (fkNames != null) {
			if (mcs != null ) {
				if (mcs instanceof MultipathCursors) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, (MultipathCursors)mcs, null, ctx);
				} else {
					if (exps == null) {
						cursor = table.cursor(null, this.names, filter, fkNames, codes, null, null, ctx);
					} else {
						cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, null, ctx);
					}
				}
			} else if (pathCount > 1) {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, pathCount, null, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, pathCount, null, ctx);
				}
			} else {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, null, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, null, ctx);
				}
			}
		} else {
			if (mcs != null ) {
				if (mcs instanceof MultipathCursors) {
					cursor = table.cursor(null, this.names, filter, null, null, null, (MultipathCursors)mcs, null, ctx);
				} else {
					if (exps == null) {
						cursor = table.cursor(this.names, filter, ctx);
					} else {
						cursor = table.cursor(this.exps, this.names, filter, null, null, null, null, ctx);
					}
				}
			} else if (pathCount > 1) {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, null, null, null, pathCount, null, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, null, null, null, pathCount, null, ctx);
				}
			} else {
				if (exps == null) {
					cursor = table.cursor(this.names, filter, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, null, null, null, null, ctx);
				}
			}
		}
	
		
		addJoin(cursor);
	
		if (addOpt) {
			if (opList != null) {
				for (Operation op : opList) {
					cursor.addOperation(op, ctx);
				}
			}
			if (extraOpList != null) {
				for (Operation op : extraOpList) {
					cursor.addOperation(op, ctx);
				}
			}
		}

		return cursor;
	
	}
	
	/**
	 * 按user排序 （返回单个游标时使用）
	 * @param cursors
	 * @param pd
	 * @param ctx
	 * @return
	 */
	static ICursor sortCursor(ICursor cursor, PseudoDefination pd, Context ctx) {
//		if (pd.getDate() == null) {
//			return cursor;
//		}
		
		String ugrp = pd.getUgrp();
		String user = pd.getUser();
		DataStruct ds = cursor.getDataStruct();
		if (user == null || user.equals(ugrp) || ds.getFieldIndex(user) == -1) {
			return cursor;
		}
		
		return new GroupSortCursor(cursor, ugrp, user, ctx);
	}
	
	/**
	 * 归并或者连接游标
	 * @param cursors
	 * @return
	 */
	static ICursor mergeCursor(ICursor cursors[], PseudoDefination pd, Context ctx) {
		if (pd.getDate() == null) {
			return new ConjxCursor(cursors);
		}
		
		String ugrp = pd.getUgrp();
		DataStruct ds = cursors[0].getDataStruct();
		int index = ds.getFieldIndex(ugrp); 
		if (index == -1) {
			return new ConjxCursor(cursors);
		}
		
		ICursor cs = new MergeCursor(cursors, new int[] {index}, null, ctx);
		
		//如果user不存在或等于ugrp
		String user = pd.getUser();
		if (user == null || user.equals(ugrp) || ds.getFieldIndex(user) == -1) {
			return cs;
		}
		
		//user不是首字段，在归并后同值首字段内的数据按user排序
		return new GroupSortCursor(cs, ugrp, user, ctx);
	}
	
	static ICursor mergeCursor(ICursor cursors[], Context ctx) {
		return new ConjxCursor(cursors);
	}
	
	/**
	 * 如果存在user（账户属性）则group(首字段).conj(~.group(user)),
	 * 如果不存在user，则使用首字段归并。
	 */
	static void groupByUser(ICursor cursor, String user, String ugrp, Context ctx) {
		if (user != null && ugrp != null && user.equals(ugrp)) {
			Expression[] exps = new Expression[1];
			exps[0] = new Expression(ugrp);
			cursor.addOperation(new Group(exps, null), ctx);
			cursor.addOperation(new Conj(new Expression("~.group("+user+")")), ctx);
		}
	}
	
	private ICursor addOptionToCursor(ICursor cursor) {
		if (opList != null) {
			for (Operation op : opList) {
				cursor.addOperation(op, ctx);
			}
		}
		if (extraOpList != null) {
			for (Operation op : extraOpList) {
				cursor.addOperation(op, ctx);
			}
		}
		return cursor;
	}
	
	private List<IPhyTable> filterTables(List<IPhyTable> tables) {
		if (filter != null && pd.getDate() != null) {
			String dateName = pd.getDate();
			PseudoColumn dateCol = pd.findColumnByPseudoName(dateName);
			if (dateCol != null && dateCol.getExp() != null) {
				dateName = dateCol.getName();
			}
			IPhyTable table = tables.get(0);
			
			//判断是否有关于date的过滤
			Object obj;
			if (table instanceof ColPhyTable)
				obj = Cursor.parseFilter((ColPhyTable) table, filter, ctx);
			else 
				obj = RowCursor.parseFilter((RowPhyTable) table, filter.getHome(), ctx);
			
			IFilter dateFilter = null;
			if (obj instanceof IFilter) {
				if (((IFilter)obj).getColumnName().equals(dateName)) {
					dateFilter = (IFilter)obj;
				}
			} else if (obj instanceof ArrayList) {
				@SuppressWarnings("unchecked")
				ArrayList<Object> list = (ArrayList<Object>)obj;
				for (Object f : list) {
					if (f instanceof IFilter) {
						if (((IFilter)f).getColumnName().equals(dateName)) {
							dateFilter = (IFilter)f;
							break;
						}
					}
				}
			}
			
			if (dateFilter != null) {
				int count = tables.size();
				List<IPhyTable> list = new ArrayList<IPhyTable>(count);
				List<Object> max = pd.getMaxValues();
				List<Object> min = pd.getMinValues();			
				for (int i = 0; i < count; i++) {
					PhyTable t = (PhyTable) tables.get(i);
					if (t.getTotalRecordCount() == 0) {
						continue;
					}
					if (dateFilter.match(min.get(i), max.get(i))) {
						list.add(t);
					}
				}
				return list;
			}
		}

		int count = tables.size();
		List<IPhyTable> list = new ArrayList<IPhyTable>(count);
		for (int i = 0; i < count; i++) {
			PhyTable t = (PhyTable) tables.get(i);
			if (t.getTotalRecordCount() == 0) {
				continue;
			}
			list.add(t);
		}
		return list;
	}
	
	public ICursor cursor(Expression []exps, String []names) {
		return cursor(exps, names, false);
	}
	
	//返回虚表的游标
	public ICursor cursor(Expression []exps, String []names, boolean isColumn) {
		setFetchInfo(exps, names);//把取出字段添加进去，里面可能会对extraOpList赋值
		
		//每个实体文件生成一个游标
		List<IPhyTable> tables = getPd().getTables();
		int size = tables.size();
		ICursor cursors[] = new ICursor[size];
		
		/**
		 * 对得到游标进行归并，分为情况
		 * 1 只有一个游标则返回；
		 * 2 有多个游标且不并行时，进行归并
		 * 3 有多个游标且并行时，先对第一个游标分段，然后其它游标按第一个同步分段，最后把每个游标的每个段进行归并
		 */
		if (pathCount == 1 && size == 1) {//只有一个游标直接返回
			ICursor cs = getCursor(tables.get(0), null, false, isColumn);
			cs = sortCursor(cs, pd, ctx);
			return addOptionToCursor(cs);
		} else {
			tables = filterTables(tables);
			size = tables.size();
			if (size == 0) {
				return new MemoryCursor(null);
			}
			
			if (pathCount > 1) {//指定了并行数，此时忽略mcsTable
				cursors[0] = getCursor(tables.get(0), null, false, isColumn);
				for (int i = 1; i < size; i++) {
					cursors[i] = getCursor(tables.get(i), cursors[0], false, isColumn);
				}
			} else {//没有指定并行数
				if (mcsTable == null) {//没有指定分段参考虚表mcsTable
					for (int i = 0; i < size; i++) {
						cursors[i] = getCursor(tables.get(i), null, false, isColumn);
					}
					return addOptionToCursor(mergeCursor(cursors, pd, ctx));
				} else {//指定了分段参考虚表mcsTable
					ICursor mcs = mcsTable.cursor();
					for (int i = 0; i < size; i++) {
						cursors[i] = getCursor(tables.get(i), mcs, false, isColumn);
					}
					mcs.close();
				}
			}
			
			//对cursors按段归并或连接:把所有游标的第N路归并,得到N个游标,再把这N个游标做成多路游标返回
			int mcount = ((MultipathCursors)cursors[0]).getPathCount();//分段数
			ICursor mcursors[] = new ICursor[mcount];//结果游标
			for (int m = 0; m < mcount; m++) {
				if (size == 1) {
					mcursors[m] = sortCursor(((MultipathCursors)cursors[0]).getCursors()[m], pd, ctx);
				} else {
					ICursor cursorArray[] = new ICursor[size];
					for (int i = 0; i < size; i++) {
						cursorArray[i] = ((MultipathCursors)cursors[i]).getCursors()[m];
					}
					mcursors[m] = mergeCursor(cursorArray, pd, ctx);
				}
			}
			return addOptionToCursor(new MultipathCursors(mcursors, ctx));
		}
	}
	
	//用于获取多路游标
	private ICursor cursor() {
		List<IPhyTable> tables = getPd().getTables();
		return tables.get(0).cursor(null, null, null, null, null, null, pathCount, null, ctx);
	}

	public Object clone(Context ctx) throws CloneNotSupportedException {
		PseudoTable obj = new PseudoTable();
		obj.hasPseudoColumns = hasPseudoColumns;
		obj.pathCount = pathCount;
		obj.mcsTable = mcsTable;
		obj.fkNames = fkNames == null ? null : fkNames.clone();
		obj.codes = codes == null ? null : codes.clone();
		cloneField(obj);
		obj.ctx = ctx;
		return obj;
	}

	public Pseudo setPathCount(int pathCount) {
		PseudoTable table = null;
		try {
			table = (PseudoTable) clone(ctx);
			table.pathCount = pathCount;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}

	public Pseudo setMcsTable(Pseudo mcsTable) {
		PseudoTable table = null;
		try {
			table = (PseudoTable) clone(ctx);
			table.mcsTable = (PseudoTable) mcsTable;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * 转换二值伪字段节点为真字段
	 * 转换普通伪字段节点为真字段
	 * @param node
	 * @return
	 */
	private Node bitsToBoolean(Node node) {
		String pname = ((UnknownSymbol) node).getName();
		PseudoColumn col = getPd().findColumnByPseudoName(pname);
		
		if (col == null) {
			return null;
		}
		
		if (col.getBits() != null) {
			/**
			 * 把一个UnknownSymbol的二值节点转换为一个Boolean节点
			 */
			Sequence seq;
			seq = col.getBits();
			int idx = seq.firstIndexOf(pname) - 1;
			int bit = 1 << idx;
			String str = "and(" + col.getName() + "," + bit + ")!=0";//改为真字段的位运算
			return new Expression(str).getHome();
		} else if (col.get_enum() != null) {
			return null;//枚举的不在这里处理
		} else if (col.getExp() != null) {
			return new Expression(col.getExp()).getHome();
		} else if (col.getFkey() != null) {
			return null;//new UnknownSymbol(col.getFkey()[0]);//有Fkey时 Fkey[0]是真字段
		} else {
			return new UnknownSymbol(col.getName());//处理普通伪字段
		}
	}
	
	/**
	 * 处理二值伪字段
	 */
	private void replaceFilter(Node node) {
		if (node == null) {
			return;
		}
		
		if (node.getLeft() instanceof UnknownSymbol) {
			Node left = bitsToBoolean(node.getLeft());
			if (left != null) {
				node.setLeft(left);
			}
		} else {
			replaceFilter(node.getLeft());
		}
		
		if (node.getRight() instanceof UnknownSymbol) {
			Node right = bitsToBoolean(node.getRight());
			if (right != null) {
				node.setRight(right);
			}
		} else {
			replaceFilter(node.getRight());
		}
	}
	
	/**
	 * 把表达式里涉及伪字段的枚举运算进行转换
	 * @param node
	 */
	private void parseFilter(Node node) {
		if (node instanceof And || node instanceof Or) {
			/**
			 * 逻辑与、或时，递归处理
			 */
			parseFilter(node.getLeft());
			parseFilter(node.getRight());
		} else if (node instanceof Equals || node instanceof NotEquals) {
			/**
			 * 对伪字段的==、!=进行处理
			 */
			if (node.getLeft() instanceof UnknownSymbol) {
				//判断是否是伪字段
				String pname = ((UnknownSymbol) node.getLeft()).getName();
				PseudoColumn col = getPd().findColumnByPseudoName(pname);
				if (col != null) {
					Sequence seq;
					//判断是否是对枚举伪字段进行运算
					if (col.get_enum() != null) {
						seq = col.get_enum();
						node.setLeft(new UnknownSymbol(col.getName()));//改为真字段
						Integer obj = seq.firstIndexOf(node.getRight().calculate(ctx));
						node.setRight(new Constant(obj));//把枚举值改为对应的真的值
					}
				}
			} else if (node.getRight() instanceof UnknownSymbol) {
				//处理字段名在右边的情况，左右交换一下再处理，逻辑跟上面一样
				Node right = node.getRight();
				node.setRight(node.getLeft());
				node.setLeft(right);
				parseFilter(node);
			}
		} else if (node instanceof DotOperator) {
			//对有枚举列表的伪字段的contain进行处理
			if (node.getRight() instanceof Contain) {
				Contain contain = (Contain)node.getRight();
				IParam param = contain.getParam();
				if (param == null || !param.isLeaf()) {
					return;
				}
				
				//判断是否是对伪字段进行contain运算
				PseudoColumn col = null;
				Node containNode = param.getLeafExpression().getHome();
				if (containNode instanceof UnknownSymbol) {
					UnknownSymbol un = (UnknownSymbol) param.getLeafExpression().getHome();
					col = getPd().findColumnByPseudoName(un.getName());
				}
				if (col != null && col.get_enum() != null) {
					Object val = node.getLeft().calculate(ctx);
					if (val instanceof Sequence) {
						//把contain右边的字段名改为真字段
						IParam newParam = ParamParser.parse(col.getName(), null, ctx);
						contain.setParam(newParam);
						
						//把contain左边的枚举值序列改为对应的真的值的序列
						Sequence value = (Sequence) val;
						Sequence newValue = new Sequence();
						int size = value.length();
						for (int i = 1; i <= size; i++) {
							Integer obj = col.get_enum().firstIndexOf(value.get(i));
							newValue.add(obj);
						}
						node.setLeft(new Constant(newValue));
					}
				}
			}
		}
	}
	
	public Operable addOperation(Operation op, Context ctx) {
		if (op == null) {
			return this;
		}
		
		if (hasPseudoColumns) {
			/**
			 * 处理伪字段，二值字段，枚举字段
			 */
			Expression exp;
			if (op instanceof Select) {
				exp = ((Select) op).getFilterExpression();
			} else if (op instanceof New) {
				return super.addOperation(op, ctx);
			} else {
				exp = op.getFunction().getParam().getLeafExpression();
			}
			Node node = exp.getHome();
			if (node instanceof UnknownSymbol) {
				/**
				 * node是伪字段
				 */
				Node n = bitsToBoolean(node);
				if (n != null) {
					op = new Select(new Expression(n), null);
					
					ArrayList<String> tempList = new ArrayList<String>();
					n.getUsedFields(ctx, tempList);
					for (String name : tempList) {
						addColName(name);
					}
				}
			} else {
				/**
				 * node是普通字段
				 */
				replaceFilter(node);
				parseFilter(node);
			}
		}
		
		return super.addOperation(op, ctx);
	}
	
	/**
	 * 把游标的伪列转换为真字段，用于update和append
	 * @param cursor
	 * @param columns
	 * @param fields
	 */
	private void convertPseudoColumn(ICursor cursor, List<PseudoColumn> columns, String fields[]) {
		//先把不是伪字段的赋值过来
		DataStruct ds = new DataStruct(fields);
		int size = ds.getFieldCount();
		Expression []exps = new Expression[size];
		String []names = new String[size];
		for (int c = 0; c < size; c++) {
			exps[c] = new Expression(fields[c]);
			names[c] = fields[c];
		}
		
		//转换游标里的伪字段
		size = columns.size();
		for (int c = 0; c < size; c++) {
			PseudoColumn column = columns.get(c);
			String pseudoName = column.getPseudo();
			Sequence bitNames = column.getBits();
			int idx = ds.getFieldIndex(column.getName());
			
			if (column.getExp() != null) {
				//有表达式的伪列
				exps[idx] = new Expression(column.getExp());
				names[idx] = column.getName();
			} else if (pseudoName != null && column.get_enum() != null) {
				//枚举伪列
				String var = "pseudo_enum_value_" + c;
				Context context = cursor.getContext();
				if (context == null) {
					context = new Context();
					cursor.setContext(context);
					context.setParamValue(var, column.get_enum());
				} else {
					context.setParamValue(var, column.get_enum());
				}
				exps[idx] = new Expression(var + ".pos(" + pseudoName + ")");
				names[idx] = column.getName();
			} else if (bitNames != null) {
				//处理二值伪字段(多个伪字段按位转换为一个真字段)
				String exp = "0";
				int len = bitNames.length();
				for (int i = 1; i <= len; i++) {
					String field = (String) bitNames.get(i);
					//转换为bit值,并累加
					int bit = 1 << (i - 1);
					exp += "+ if(" + field + "," + bit + ",0)";
				}
				exps[idx] = new Expression(exp);
				names[idx] = column.getName();
			}
		}
		
		New _new = new New(exps, names, null);
		cursor.addOperation(_new, null);
	}
	
	public void append(ICursor cursor, String option) {
		//把数据追加到file
		List<IPhyTable> tables = getPd().getTables();
		IPhyTable table = null;
		boolean flag = true;//立即写入标志
		int size = tables.size();
		if (size == 0) {
			return;
		} else if (option != null && option.indexOf("y") != -1) {
			table = tables.get(0);//有@y时更新到第一个表
			flag = false;
		} else if (size == 1) {
			table = tables.get(0);
		} else {
			/**
			 * 文件组追加时要处理分布：单路游标自动加@x，多路不用处理
			 */
			if (!(cursor instanceof MultipathCursors)) {
				option = (option == null) ? "x" : option + "x";
			}
			table = getPd().getTableMetaDataGroup();
		}
		
		List<PseudoColumn> columns = pd.getColumns();
		if (columns != null) {
			String fields[] = table.getAllColNames();
			convertPseudoColumn(cursor, columns, fields);
		}
		
		try {
			if (flag) {
				option = (option == null) ? "i" : option + "i";
			}
			table.append(cursor, option);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public Sequence update(Sequence data, String opt) {
		//更新到最后一个file
		List<IPhyTable> tables = getPd().getTables();
		int size = tables.size();
		if (size == 0) {
			return null;
		}
		IPhyTable table = tables.get(size - 1);

		List<PseudoColumn> columns = pd.getColumns();
		if (columns != null) {
			String fields[] = table.getAllColNames();
			ICursor cursor = new MemoryCursor(data);
			convertPseudoColumn(cursor, columns, fields);
			data = cursor.fetch();
		}
		
		try {
			return table.update(data, opt);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public Sequence delete(Sequence data, String opt) {
		List<IPhyTable> tables = getPd().getTables();
		int size = tables.size();
		if (size == 0) {
			return null;
		}
		
		Sequence result = null;
		for (IPhyTable table : tables) {
			List<PseudoColumn> columns = pd.getColumns();
			if (columns != null) {
				String fields[] = table.getAllColNames();
				ICursor cursor = new MemoryCursor(data);
				convertPseudoColumn(cursor, columns, fields);
				data = cursor.fetch();
			}
			
			try {
				result = table.delete(data, opt);
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		return result;
	}
	
	/**
	 * 添加外键
	 * @param fkName	外键名
	 * @param fieldNames 外键字段
	 * @param code	外表
	 * @return
	 */
	public Pseudo addForeignKeys(String fkName, String []fieldNames, Object code, String[] codeKeys, boolean clone) {
		PseudoTable table = null;
		try {
			table = clone ? (PseudoTable) clone(ctx) : this;
			table.getPd().addPseudoColumn(new PseudoColumn(fkName, fieldNames, code, codeKeys));
//			if (fieldNames == null) {
//				table.addColName(fkName);
//			} else {
//				for (String key : fieldNames) {
//					table.addColName(key);
//				}
//			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	// 主子表按主表主键做有序连接
//	public static ICursor join(PseudoTable masterTable, PseudoTable subTable) {
//		String[] keys = masterTable.getPrimaryKey();
//		if (keys != null) {
//			int size = keys.length;
//			Expression[] exps = new Expression[size];
//			for (int i = 0; i < size; i++) {
//				exps[i] = new Expression(keys[i]);
//			}
//			Expression [][]joinExps = new Expression [][] {exps, exps};//使用主表的主键做join
//			
//			ICursor cursors[] = new ICursor[]{masterTable.cursor(null, null), subTable.cursor(null, null)};
//			ICursor cursor = CursorUtil.joinx(cursors, null, joinExps, null, masterTable.getContext());
//			return cursor;
//		}
//		return null;
//	}
	
	/**
	 * 获得虚表对应的组表的字段
	 * @return
	 */
	public String[] getFieldNames() {
		return getPd().getAllColNames();
	}
	
	protected static byte getProperDataType(Object value) {
		if (value instanceof String) {
			return Types.DT_STRING;
		}
		if (value instanceof java.lang.Double || value instanceof java.lang.Float) {
			return Types.DT_DOUBLE;
		}
		if (value instanceof java.lang.Integer) {
				return Types.DT_INT;
			}
		
		if (value instanceof java.lang.Integer || value instanceof java.lang.Long ||
			value instanceof java.math.BigInteger) {
			return Types.DT_LONG;
		}

		if (value instanceof java.sql.Time) {
			return Types.DT_TIME;
		}

		if (value instanceof java.sql.Timestamp) {
			return Types.DT_DATETIME;
		}

		if (value instanceof java.sql.Date) {
			return Types.DT_DATE;
		}

		if (value instanceof java.math.BigDecimal) {
			return Types.DT_DECIMAL;
		}
		if (value instanceof java.lang.Boolean) {
			return Types.DT_BOOLEAN;
		}
		return Types.DT_STRING;
	}
	
	/**
	 * 获得虚表对应的组表的每列的数据类型
	 * 注意：返回的类型是以第一条记录为准
	 * @return
	 */
	public byte[] getFieldTypes() {
		List<IPhyTable> tables = getPd().getTables();
		ICursor cursor = tables.get(0).cursor(null, null, null, null, null, null, 1, null, ctx);
		Sequence data = cursor.fetch(1);
		cursor.close();
		
		if (data == null || data.length() == 0) {
			return null;
		}
		
		BaseRecord record = (BaseRecord) data.getMem(1);
		Object[] objs = record.getFieldValues();
		int len = objs.length;
		byte[] types = new byte[len];
		
		for (int i = 0; i < len; i++) {
			types[i] = getProperDataType(objs[i]);
		}
		return types;
	}
	
	/**
	 * 返回虚表的所有外键列的定义
	 * 没有外键时返回NULL
	 * @return
	 */
	public List<PseudoColumn> getDimColumns() {
		List<PseudoColumn> dims = new ArrayList<PseudoColumn>();
		List<PseudoColumn> columns = getPd().getColumns();
		for (PseudoColumn col : columns) {
			if (col.getDim() != null) {
				dims.add(col);
			}
		}
		if (dims.size() == 0) {
			return null;
		}
		return dims;
	}
}

