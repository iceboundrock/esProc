package com.scudata.dm;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MergeCursor;
import com.scudata.dm.cursor.UpdateIdCursor;
import com.scudata.dm.cursor.UpdateMergeCursor;
import com.scudata.dw.ColComTable;
import com.scudata.dw.ColumnMetaData;
import com.scudata.dw.ColPhyTable;
import com.scudata.dw.ComTable;
import com.scudata.dw.IPhyTable;
import com.scudata.dw.RowComTable;
import com.scudata.dw.RowPhyTable;
import com.scudata.dw.PhyTable;
import com.scudata.dw.PhyTableGroup;
import com.scudata.resources.EngineMessage;

/**
 * 文件组
 * file(fn:z) z数列
 * @author RunQian
 *
 */
public class FileGroup implements Externalizable {
	private String fileName;
	private int []partitions;
	private FileObject[] files;
	
	public FileGroup(String fileName, int []partitions) {
		this.fileName = fileName;
		this.partitions = partitions;
	}
	
	public FileGroup(FileObject[] files, String fileName, int []partitions) {
		this.fileName = fileName;
		this.partitions = partitions;
		this.files = files;
	}
	
	/**
	 * 把当前对象写到输出流
	 * @param out 输出流
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileName);
		out.writeInt(partitions.length);
		for (int p : partitions) {
			out.writeInt(p);
		}
	}
	
	/**
	 * 从输入流读出文件组对象
	 * @param in 输入流
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		fileName = (String)in.readObject();
		int count = in.readInt();
		partitions = new int[count];
		for (int i = 0; i < count; ++i) {
			partitions[i] = in.readInt();
		}
	}
	
	/**
	 * 取文件名
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * 取分表号
	 * @return
	 */
	public int[] getPartitions() {
		return partitions;
	}
	
	/**
	 * 打开组表
	 * @param opt 选项
	 * @param ctx 计算上下文
	 * @return
	 */
	public PhyTableGroup open(String opt, Context ctx) {
		int pcount = partitions.length;
		PhyTable []tables = new PhyTable[pcount];
		if (files == null) {
			for (int i = 0; i < pcount; ++i) {
				File file = Env.getPartitionFile(partitions[i], fileName);
				tables[i] = ComTable.openBaseTable(file, ctx);
				tables[i].getGroupTable().setPartition(partitions[i]);
			}
		} else {
			for (int i = 0; i < pcount; ++i) {
				tables[i] = ComTable.openBaseTable(files[i], ctx);
				tables[i].getGroupTable().setPartition(partitions[i]);
			}
		}
		
		return new PhyTableGroup(fileName, tables, partitions, opt, ctx);
	}
	
	/**
	 * 创建组表
	 * @param colNames 字段名数组
	 * @param distribute 分布表达式
	 * @param opt 选项
	 * @param blockSize 区块大小
	 * @param ctx 计算上下文
	 * @return
	 * @throws IOException
	 */
	public PhyTableGroup create(String []colNames, String distribute, String opt, Integer blockSize, Context ctx) throws IOException {
		int pcount = partitions.length;
		PhyTable []tables = new PhyTable[pcount];
		boolean yopt = opt != null && opt.indexOf('y') != -1;
		boolean ropt = opt != null && opt.indexOf('r') != -1;
		
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			if (file.exists()) {
				if (yopt) {
					try {
						ComTable table = ComTable.open(file, ctx);
						table.delete();
					} catch (IOException e) {
						throw new RQException(e.getMessage(), e);
					}
				} else {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("file.fileAlreadyExist", fileName));
				}
			}
			
			ComTable table;
			if (ropt) {
				table = new RowComTable(file, colNames, distribute, opt, blockSize, ctx);
			} else {
				table = new ColComTable(file, colNames, distribute, opt, blockSize, ctx);
			}
			
			table.setPartition(partitions[i]);
			tables[i] = table.getBaseTable();
		}
		
		return new PhyTableGroup(fileName, tables, partitions, opt, ctx);
	}
	
	/**
	 * 建立当前文件组的归并游标
	 * @param tableGroup 当前复组表
	 * @param hasW 更新方式归并
	 * @param cursor 要归并的游标 
	 * @return
	 */
	private ICursor makeCursor(PhyTableGroup tableGroup, boolean hasW, ICursor cursor, Context ctx) {
		IPhyTable[] tables = tableGroup.getTables();
		int tableCount = tables.length;
		
		if (tableCount == 0) {
			return cursor;
		}
		
		ICursor []cursors;
		if (cursor != null) {
			cursors = new ICursor[tableCount + 1];
		} else {
			cursors = new ICursor[tableCount];
		}
		
		for (int i = 0; i < tableCount; ++i) {
			cursors[i] = tables[i].cursor();
		}
		DataStruct ds1 = cursors[0].getDataStruct();
				
		if (cursor != null) {
			cursors[tableCount] = cursor;			
		}
		
		ICursor cs;
		if (hasW) {
			int deleteField = tableGroup.getDeleteFieldIndex(null, ds1.getFieldNames());
			cursor = new UpdateIdCursor(cursor, ds1.getPKIndex(), deleteField);
			cs = new UpdateMergeCursor(cursors, ds1.getPKIndex(), deleteField, ctx);
		} else {
			cs = new MergeCursor(cursors, ds1.getPKIndex(), null, ctx);
		}
		return cs;
	}
	
	/**
	 * 整理组表数据
	 * @param opt 选项
	 * @param blockSize 区块大小
	 * @param ctx计算上下文
	 * @param 归并的游标
	 * @return true：成功，false：失败
	 */
	public boolean resetGroupTable(String opt, Integer blockSize, ICursor cursor, Context ctx) {
		if (cursor != null) {
			FileGroup tempFileGroup = createResetTempFileGroup(opt, blockSize, ctx);//得到一个同构的临时文件组
			resetGroupTable(tempFileGroup, opt, null, blockSize, cursor, ctx);//把当前文件组reset到临时文件组
			delete(ctx);//删除当前
			tempFileGroup.rename(fileName, ctx);//改名
		} else {
			int pcount = partitions.length;
			for (int i = 0; i < pcount; ++i) {
				File file = Env.getPartitionFile(partitions[i], fileName);
				PhyTable tmd = ComTable.openBaseTable(file, ctx);
				boolean result = tmd.getGroupTable().reset(null, opt, ctx, null, blockSize, null);
				tmd.close();
				if (!result) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("reset" + mm.getMessage("file.deleteFailed"));
				}
			}
		}
		return true;
	}
	
	/**
	 * 把复组表整理成单组表
	 * @param newFile 新组表对应的文件
	 * @param opt 选项
	 * @param blockSize 区块大小
	 * @param 归并的游标
	 * @param ctx计算上下文
	 * @return true：成功，false：失败
	 */
	public boolean resetGroupTable(File newFile, String opt, Integer blockSize, ICursor cursor, Context ctx) {
		PhyTableGroup tableGroup = open(null, ctx);
		PhyTable baseTable = (PhyTable) tableGroup.getTables()[0];
		
		boolean isCol = baseTable.getGroupTable() instanceof ColComTable;
		boolean hasN = false;
		boolean hasW = false;
		boolean compress = false; // 压缩
		boolean uncompress = false; // 不压缩
		
		if (opt != null) {
			if (opt.indexOf('q') != -1) {
				return conj(newFile, ctx);
			}
			
			if (opt.indexOf('r') != -1) {
				isCol = false;
			} else if (opt.indexOf('c') != -1) {
				isCol = true;
			}
			
			if (opt.indexOf('u') != -1) {
				uncompress = true;
			}
			
			if (opt.indexOf('z') != -1) {
				compress = true;
			}
			
			if (opt.indexOf('w') != -1) {
				hasW = true;
			}
			
			if (compress && uncompress) {
				tableGroup.close();
				MessageManager mm = EngineMessage.get();
				throw new RQException(opt + mm.getMessage("engine.optConflict"));
			}
			
			if (newFile == null) {
				tableGroup.close();
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.invalidParam"));
			}
		}
		
		if ((opt == null || opt.indexOf('y') == -1) && newFile.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileAlreadyExist", newFile.getName()));
		} else if (opt != null && opt.indexOf('y') != -1 && newFile.exists()) {
			try {
				ComTable table = ComTable.open(newFile, ctx);
				table.delete();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		String []srcColNames = baseTable.getColNames();
		int len = srcColNames.length;
		String []colNames = new String[len];
		
		if (baseTable instanceof ColPhyTable) {
			for (int i = 0; i < len; i++) {
				ColumnMetaData col = ((ColPhyTable)baseTable).getColumn(srcColNames[i]);
				if (col.isDim()) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		} else {
			boolean[] isDim = ((RowPhyTable)baseTable).getDimIndex();
			for (int i = 0; i < len; i++) {
				if (isDim[i]) {
					colNames[i] = "#" + srcColNames[i];
				} else {
					colNames[i] = srcColNames[i];
				}
			}
		}

		// 生成分段选项，是否按第一字段分段
		String newOpt = "";
		String segmentCol = baseTable.getSegmentCol();
		if (segmentCol != null) {
			newOpt += "p";
		}
		if (baseTable.getGroupTable().hasDeleteKey()) {
			newOpt += "d";
		}
		
		ComTable newGroupTable = null;
		try {
			//生成新组表文件
			if (isCol) {
				newGroupTable = new ColComTable(newFile, colNames, null, newOpt, blockSize, ctx);
				if (compress) {
					newGroupTable.setCompress(true);
				} else if (uncompress) {
					newGroupTable.setCompress(false);
				} else {
					newGroupTable.setCompress(baseTable.getGroupTable().isCompress());
				}
			} else {
				newGroupTable = new RowComTable(newFile, colNames, null, newOpt, blockSize, ctx);
			}
			
			//处理分段
			boolean needSeg = baseTable.getSegmentCol() != null;
			if (needSeg) {
				newGroupTable.getBaseTable().setSegmentCol(baseTable.getSegmentCol(), baseTable.getSegmentSerialLen());
			}
			
			if (hasN) {
				newGroupTable.close();
				return Boolean.TRUE;
			}
			
			//新基表
			PhyTable newBaseTable = newGroupTable.getBaseTable();
			ICursor cs;
			if (cursor != null) {
				cs = makeCursor(tableGroup, hasW, cursor, ctx);
			} else if (hasW) {
				cs = tableGroup.cursor(null, null, null, null, null, null, "w", ctx);
			} else {
				cs = tableGroup.merge(ctx);
			}

			newBaseTable.append(cs);
			newBaseTable.appendCache();
			
			//基表的子表
			ArrayList<PhyTable> tableList = baseTable.getTableList();
			for (PhyTable t : tableList) {
				colNames = t.getColNames();
				len = colNames.length;
				if (t instanceof ColPhyTable) {
					for (int i = 0; i < len; i++) {
						ColumnMetaData col = ((ColPhyTable)t).getColumn(colNames[i]);
						if (col.isDim()) {
							colNames[i] = "#" + colNames[i];
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						boolean[] isDim = ((RowPhyTable)t).getDimIndex();
						if (isDim[i]) {
							colNames[i] = "#" + colNames[i];
						}
					}
				}
				PhyTable newTable = newBaseTable.createAnnexTable(colNames, t.getSerialBytesLen(), t.getTableName());
				cs = ((PhyTableGroup) tableGroup.getAnnexTable(t.getTableName())).merge(ctx);
				newTable.append(cs);
			}
		
		} catch (Exception e) {
			if (newGroupTable != null) newGroupTable.close();
			newFile.delete();
			throw new RQException(e.getMessage(), e);
		} finally {
			tableGroup.close();
		}

		newGroupTable.close();

		try{
			newGroupTable = ComTable.open(newFile, ctx);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		//重建索引文件和cuboid
		newGroupTable.getBaseTable().resetIndex(ctx);
		newGroupTable.getBaseTable().resetCuboid(ctx);
		ArrayList<PhyTable> newTableList = newGroupTable.getBaseTable().getTableList();
		for (PhyTable table : newTableList) {
			table.resetIndex(ctx);
			table.resetCuboid(ctx);
		}
		newGroupTable.close();
		
		return Boolean.TRUE;
	}
	
	/**
	 * 把复组表数据整理到另一个复组表，如果有分布表达式更改数据的分布
	 * @param newFileGroup 新文件组
	 * @param opt 选项
	 * @param distribute 分布表达式
	 * @param blockSize 区块大小
	 * @param 归并的游标
	 * @param ctx计算上下文
	 * @return true：成功，false：失败
	 */
	public boolean resetGroupTable(FileGroup newFileGroup, String opt, String distribute, Integer blockSize, ICursor cursor, Context ctx) {
		if ((distribute == null || distribute.length() == 0) && cursor == null) {
			// 分布不变
			int pcount = partitions.length;
			if (pcount != newFileGroup.partitions.length) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("reset" + mm.getMessage("function.paramCountNotMatch"));
			}
			
			for (int i = 0; i < pcount; ++i) {
				File file = getPartitionFile(i);
				File newFile = newFileGroup.getPartitionFile(i);
				
				PhyTable tmd = ComTable.openBaseTable(file, ctx);
				boolean result = tmd.getGroupTable().reset(newFile, opt, ctx, null);
				tmd.close();
				if (!result) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("reset" + mm.getMessage("file.deleteFailed"));
				}
			}
		} else {
			// 更换分布表达式
			PhyTableGroup tableGroup = open(null, ctx);
			PhyTable baseTable = (PhyTable) tableGroup.getTables()[0];
			boolean isCol = baseTable.getGroupTable() instanceof ColComTable;
			boolean uncompress = false; // 不压缩
			boolean hasW = false;
			if (opt != null) {
				if (opt.indexOf('r') != -1) {
					isCol = false;
				} else if (opt.indexOf('c') != -1) {
					isCol = true;
				}
				
				if (opt.indexOf('u') != -1) {
					uncompress = true;
				}
				
				if (opt.indexOf('z') != -1) {
					uncompress = false;
				}
				
				if (opt.indexOf('w') != -1) {
					hasW = true;
				}
			}
			
			String []srcColNames = baseTable.getColNames();
			int len = srcColNames.length;
			String []colNames = new String[len];
			
			if (baseTable instanceof ColPhyTable) {
				for (int i = 0; i < len; i++) {
					ColumnMetaData col = ((ColPhyTable)baseTable).getColumn(srcColNames[i]);
					if (col.isDim()) {
						colNames[i] = "#" + srcColNames[i];
					} else {
						colNames[i] = srcColNames[i];
					}
				}
			} else {
				boolean[] isDim = ((RowPhyTable)baseTable).getDimIndex();
				for (int i = 0; i < len; i++) {
					if (isDim[i]) {
						colNames[i] = "#" + srcColNames[i];
					} else {
						colNames[i] = srcColNames[i];
					}
				}
			}
			
			// 生成分段选项，是否按第一字段分段
			String newOpt = "y";
			String segmentCol = baseTable.getSegmentCol();
			if (segmentCol != null) {
				newOpt = "p";
			}
			
			if (isCol) {
				newOpt += 'c';
			} else {
				newOpt += 'r';
			}
			
			if (uncompress) {
				newOpt += 'u';
			}
			
			if (baseTable.getGroupTable().hasDeleteKey()) {
				newOpt += "d";
			}
			
			try {
				//写基表
				PhyTableGroup newTableGroup = newFileGroup.create(colNames, distribute, newOpt, blockSize, ctx);
				ICursor cs = this.makeCursor(tableGroup, hasW, cursor, ctx);//tableGroup.merge(ctx);
				newTableGroup.append(cs, "xi");
				
				//写子表
				ArrayList<PhyTable> tableList = baseTable.getTableList();
				for (PhyTable t : tableList) {
					len = t.getColNames().length;
					colNames = Arrays.copyOf(t.getColNames(), len);
					if (t instanceof ColPhyTable) {
						for (int i = 0; i < len; i++) {
							ColumnMetaData col = ((ColPhyTable)t).getColumn(colNames[i]);
							if (col.isDim()) {
								colNames[i] = "#" + colNames[i];
							}
						}
					} else {
						boolean[] isDim = ((RowPhyTable)t).getDimIndex();
						for (int i = 0; i < len; i++) {
							if (isDim[i]) {
								colNames[i] = "#" + colNames[i];
							}
						}
					}
					IPhyTable newTable = newTableGroup.createAnnexTable(colNames, t.getSerialBytesLen(), t.getTableName());
					
					//附表的游标，取出字段里要包含基表所有字段，这是因为需要计算分布
					String[] allColNames = Arrays.copyOf(srcColNames, srcColNames.length + t.getColNames().length);
					System.arraycopy(t.getColNames(), 0, allColNames, srcColNames.length, t.getColNames().length);
					cs = tableGroup.getAnnexTable(t.getTableName()).cursor(allColNames);
					newTable.append(cs, "xi");
				}

				tableGroup.close();
				newTableGroup.close();
				return Boolean.TRUE;
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		return true;
	}
	
	/**
	 * 取指定序号对应的分表文件
	 * @param index 序号，从0开始计数
	 * @return
	 */
	public File getPartitionFile(int index) {
		return Env.getPartitionFile(partitions[index], fileName);
	}
	
	/**
	 * 取分表数
	 * @return 分表数
	 */
	public int getPartitionCount() {
		return partitions.length;
	}
	
	public void delete(Context ctx) {
		int pcount = partitions.length;
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			if (file.exists()) {
				try {
					ComTable table = ComTable.open(file, ctx);
					table.delete();
				} catch (IOException e) {
					throw new RQException(e.getMessage(), e);
				}
			
			}
		}
	}
	
	public boolean isExist() {
		int pcount = partitions.length;
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			if (!file.exists()) {
				return false;
			}
		}
		return true;
	}
	
	public void rename(String newName, Context ctx) {
		int pcount = partitions.length;
		
		for (int i = 0; i < pcount; ++i) {
			FileObject fo = new FileObject(fileName);
			fo.setPartition(partitions[i]);
			
			FileObject file = new FileObject(newName);
			file.setPartition(partitions[i]);

			if (fo.isExists() && !file.isExists()) {
				String path = file.getFileName();
				fo.move(path, null);
			} else {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * 根据当前文件组，得到一个临时文件组
	 * @return
	 */
	private FileGroup createResetTempFileGroup(String opt, Integer blockSize, Context ctx) {
		FileObject fo = new FileObject(fileName);
		int pcount = partitions.length;
		String tempFileName;
		while (true) {
			FileObject newFileObj = new FileObject(fo.createTempFile());
			IFile f = newFileObj.getFile();
			boolean exist = false;
			
			for (int i = 0; i < pcount; ++i) {
				newFileObj.setPartition(partitions[i]);
				if (newFileObj.isExists()) {
					exist = true;
					break;
				}
			}
			
			String name = newFileObj.getFileName();
			f.delete();
			
			if (!exist) {
				tempFileName = name;
				break;
			}
		}
		
		String option = opt == null ? "" : opt;
		option += "n";
		for (int i = 0; i < pcount; ++i) {
			File file = Env.getPartitionFile(partitions[i], fileName);
			File newFile = Env.getPartitionFile(partitions[i], tempFileName);
			PhyTable tmd = ComTable.openBaseTable(file, ctx);
			boolean result = tmd.getGroupTable().reset(newFile, option, ctx, null, blockSize, null);
			tmd.close();
			if (!result) {
				return null;
			}
		}
		return new FileGroup(tempFileName, partitions);
	}
	
	private boolean conj(File newFile, Context ctx) {
		int pcount = partitions.length;
		if (newFile.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileAlreadyExist", newFile.getName()));
		}
		
		File file = getPartitionFile(0);
		LocalFile.copyFile(file, newFile);
		if (pcount == 1) {
			return false;
		}
		
		PhyTable resultTable = ComTable.openBaseTable(newFile, ctx);
		PhyTable table = null;
		try {
			for (int i = 1; i < pcount; ++i) {
				File f = getPartitionFile(i);
				table = ComTable.openBaseTable(f, ctx);
				resultTable.append(table);
				table.close();
			}
		} catch (IOException e) {
			if (table != null) {
				table.close();
			}
			newFile.delete();
			return false;
		} finally {
			resultTable.close();
		}
		return true;
	}
}