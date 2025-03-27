package com.scudata.expression.mfn.file;

import java.util.ArrayList;

import com.scudata.cellset.ICellSet;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Types;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.FileObject;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.FileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.query.SimpleSQL;
import com.scudata.expression.Expression;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.expression.ParamInfo3;
import com.scudata.resources.EngineMessage;


/**
 * 创建文件游标，文件可以是文本文件、集文件
 * f.cursor(Fi:type:fmt,…;k:n,s)
 * @author RunQian
 *
 */
public class CreateCursor extends FileFunction {
	public Object calculate(Context ctx) {
		return createCursor("cursor", file, cs, param, option, ctx);
	}
	
	private static ICursor createSimpleSQLCursor(FileObject fo, ICellSet cs, IParam param, String option, Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.missingParam"));
		} else if (param.getType() == IParam.Normal) { // 没有参数
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
			}

			SimpleSQL lq = new SimpleSQL(cs, (String)obj, null, ctx);
			Object val = lq.execute();
			if (val instanceof ICursor) {
				return (ICursor)val;
			} else if (val instanceof Sequence) {
				return new MemoryCursor((Sequence)val);
			} else if (val == null) {
				return null;
			} else {
				Sequence seq = new Sequence(1);
				seq.add(val);
				return new MemoryCursor(seq);
			}
		} else if (param.getType() == IParam.Comma) {
			IParam sub0 = param.getSub(0);
			if (sub0 == null || !sub0.isLeaf()) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
			}

			Object obj = sub0.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
			}
			
			String strSql = (String)obj;
			int paramSize = param.getSubSize() - 1;
			ArrayList<Object> list = new ArrayList<Object>(paramSize);
			for (int i = 0; i < paramSize; ++i) {
				IParam sub = param.getSub(i + 1);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
				}

				Object p = sub.getLeafExpression().calculate(ctx);
				list.add(p);
			}

			SimpleSQL lq = new SimpleSQL(cs, strSql, list, ctx);
			Object val = lq.execute();
			if (val instanceof ICursor) {
				return (ICursor)val;
			} else if (val instanceof Sequence) {
				return new MemoryCursor((Sequence)val);
			} else if (val == null) {
				return null;
			} else {
				Sequence seq = new Sequence(1);
				seq.add(val);
				return new MemoryCursor(seq);
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
		}
	}
	
	public static ICursor createCursor(String fnName, FileObject fo, ICellSet cs, IParam param, String option, Context ctx) {
		if (fo.getIsSimpleSQL()) {
			return createSimpleSQLCursor(fo, cs, param, option, ctx);
		}
		
		boolean isBinary = false, isMultiThread = false;
		if (option != null) {
			if (option.indexOf('m') != -1) isMultiThread = true;
			if (option.indexOf('b') != -1 || option.indexOf('z') != -1) isBinary = true;
		}
		
		IParam fieldParam = param;
		IParam segParam = null;
		IParam sParam = null;
		
		if (param != null && param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.invalidParam"));
			}

			fieldParam = param.getSub(0);
			IParam p = param.getSub(1);
			if (p == null) {
			} else if (p.getType() == IParam.Comma) {
				if (p.getSubSize() != 2) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fnName + mm.getMessage("function.invalidParam"));
				}
				
				segParam = p.getSub(0);				
				IParam sub1 = p.getSub(1);
				if (sub1 != null) {
					if (!sub1.isLeaf()) {
						MessageManager mm = EngineMessage.get();
						//throw new RQException(function + mm.getMessage("function.invalidParam"));
						throw new RQException(fnName + mm.getMessage("function.paramChanged"));
					}
					
					sParam = sub1;
				}
			} else {
				segParam = p;
			}
		}

		String []fields = null;
		byte []types = null;
		String []fmts = null;
		int []fieldLens = null;
		int segSeq = 1;
		int segCount = 1;
		String s = null;

		if (fieldParam != null) {
			ParamInfo3 pi = ParamInfo3.parse(fieldParam, "cursor", false, false, false);
			fields = pi.getExpressionStrs1();
			String []typeNames = pi.getExpressionStrs2();
			
			int fcount = fields.length;
			Expression []exps = pi.getExpressions3();
			boolean isFixedLength = option != null && option.indexOf('y') != -1;
			if (isFixedLength) {
				types = new byte[fcount];
				fmts = new String[fcount];
				fieldLens = new int[fcount];
			}
			
			for (int i = 0; i < fcount; ++i) {
				if (!isFixedLength && (fields[i] == null || fields[i].length() == 0)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fnName + mm.getMessage("function.invalidParam"));
				}
				
				if (types == null) {
					types = new byte[fcount];
				}
				
				String type = typeNames[i];
				if (type == null) {
				} else if (type.equals("string")) {
					types[i] = Types.DT_STRING;
				} else if (type.equals("int")) {
					types[i] = Types.DT_INT;
				} else if (type.equals("float")) {
					types[i] = Types.DT_DOUBLE;
				} else if (type.equals("long")) {
					types[i] = Types.DT_LONG;
				} else if (type.equals("decimal")) {
					types[i] = Types.DT_DECIMAL;
				} else if (type.equals("date")) {
					types[i] = Types.DT_DATE;
				} else if (type.equals("datetime")) {
					types[i] = Types.DT_DATETIME;
				} else if (type.equals("time")) {
					types[i] = Types.DT_TIME;
				} else if (type.equals("bool")) {
					types[i] = Types.DT_BOOLEAN;
				} else {
					try {
						int len = Integer.parseInt(type);
						if (len < 1) {
							MessageManager mm = EngineMessage.get();
							throw new RQException(type + mm.getMessage("engine.unknownType"));
						}
						
						types[i] = Types.DT_SERIALBYTES;
						if (fmts == null) {
							fmts = new String[fcount];
						}
						
						fmts[i] = type;
					} catch (NumberFormatException e) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(type + mm.getMessage("engine.unknownType"));
					}
				}
				
				if (exps[i] != null) {
					Object obj = exps[i].calculate(ctx);
					if (fmts == null) {
						fmts = new String[fcount];
					}
					
					if (obj instanceof String) {
						fmts[i] = (String)obj;
						if (isFixedLength) {
							fieldLens[i] = fmts[i].length();
						}
					} else if (obj instanceof Number && isFixedLength) {
						fieldLens[i] = ((Number)obj).intValue();
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException(fnName + mm.getMessage("function.paramTypeError"));
					}
				}
			}
		}
		
		if (segParam == null) {
			if (isMultiThread) {
				segCount = Env.getCursorParallelNum();
			}
		} else if (segParam.isLeaf()) {
			if (!isMultiThread) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = segParam.getLeafExpression().calculate(ctx);
			if (!(obj instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.paramTypeError"));
			}

			segCount = ((Number)obj).intValue();
		} else {
			if (segParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = segParam.getSub(0);
			IParam sub1 = segParam.getSub(1);
			if (sub0 != null && sub1 != null) {
				Object obj = sub0.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fnName + mm.getMessage("function.paramTypeError"));
				}

				segSeq = ((Number)obj).intValue();
				
				obj = sub1.getLeafExpression().calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(fnName + mm.getMessage("function.paramTypeError"));
				}

				segCount = ((Number)obj).intValue();
				isMultiThread = false;
			} else if (sub0 != null || sub1 != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.invalidParam"));
			}
		}
		
		if (sParam != null) {
			Object obj = sParam.getLeafExpression().calculate(ctx);
			if (obj != null && !(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fnName + mm.getMessage("function.paramTypeError"));
			}

			s = (String)obj;
		}
		
		if (isMultiThread && segCount > 1) {
			ICursor []cursors = new ICursor[segCount];
			if (isBinary) {
				for (int i = 0; i < segCount; ++i) {
					cursors[i] = new BFileCursor(fo, fields, i + 1, segCount, option, ctx);
				}
			} else {
				for (int i = 0; i < segCount; ++i) {
					FileCursor cursor = new FileCursor(fo, i + 1, segCount, fields, types, s, option, ctx);
					cursor.setFieldLens(fieldLens);
					cursor.setFormats(fmts);
					cursors[i] = cursor;
				}
			}
			
			return new MultipathCursors(cursors, ctx);
		} else {
			if (isBinary) {
				return new BFileCursor(fo, fields, segSeq, segCount, option, ctx);
			} else {
				FileCursor cursor = new FileCursor(fo, segSeq, segCount, fields, types, s, option, ctx);
				cursor.setFieldLens(fieldLens);
				cursor.setFormats(fmts);
				return cursor;
			}
		}
	}
}
