package com.scudata.expression.mfn.file;

import java.io.File;
import java.io.IOException;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.dw.ColComTable;
import com.scudata.dw.ComTable;
import com.scudata.dw.RowComTable;
import com.scudata.expression.Expression;
import com.scudata.expression.FileFunction;
import com.scudata.expression.IParam;
import com.scudata.parallel.ClusterFile;
import com.scudata.resources.EngineMessage;

/**
 * 创建组表文件
 * f.create(C,…;x;b)
 * @author RunQian
 *
 */
public class Create extends FileFunction {
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("create" + mm.getMessage("function.missingParam"));
		}
		
		IParam colParam = param;
		Expression distributeExp = null; // 分布表达式
		String distribute = null;
		Integer blockSize = null;
		
		if (param.getType() == IParam.Semicolon) {
			int size = param.getSubSize();
			if (size != 2 && size != 3) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("create" + mm.getMessage("function.invalidParam"));
			}
			
			colParam = param.getSub(0);
			if (colParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("create" + mm.getMessage("function.invalidParam"));
			}
			
			IParam expParam = param.getSub(1);
			if (expParam != null) {
				distributeExp = expParam.getLeafExpression();
				distribute = distributeExp.toString();
			}
			
			if (size == 3) {
				IParam blockSizeParam = param.getSub(2);
				if (blockSizeParam != null) {
					String b = blockSizeParam.getLeafExpression().calculate(ctx).toString();
					try {
						blockSize = Integer.parseInt(b);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		
		String []cols;
		if (colParam.isLeaf()) {
			cols = new String[]{colParam.getLeafExpression().getIdentifierName()};
		} else {
			int size = colParam.getSubSize();
			cols = new String[size];
			for (int i = 0; i < size; ++i) {
				IParam sub = colParam.getSub(i);
				if (sub == null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("create" + mm.getMessage("function.invalidParam"));
				}
				
				cols[i] = sub.getLeafExpression().getIdentifierName();
			}
		}

		if (file.isRemoteFile()) {
			// 远程文件
			String host = file.getIP();
			int port = file.getPort();
			String fileName = file.getFileName();
			Integer partition = file.getPartition();
			int p = partition == null ? -1 : partition.intValue();
			ClusterFile cf = new ClusterFile(host, port, fileName, p, ctx);
			return cf.createGroupTable(cols, distributeExp, option, ctx);
		}
		
		FileObject fo = file;
		String opt = option;
		File file = fo.getLocalFile().file();
		Integer partition = fo.getPartition();

		if ((opt == null || opt.indexOf('y') == -1) && file.exists()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("file.fileAlreadyExist", fo.getFileName()));
		} else if (opt != null && opt.indexOf('y') != -1 && file.exists()) {
			ComTable table = null;
			try {
				table = ComTable.open(file, ctx);
				table.delete();
			} catch (Exception e) {
				try {
					if (table != null) {
						table.close();
					}
					file.delete();
				} catch (Exception e1) {
					throw new RQException(e1.getMessage(), e1);
				}
			}
		}

		try {
			ComTable table;
			if (opt != null && opt.indexOf('r') != -1) {
				table = new RowComTable(file, cols, distribute, opt, blockSize, ctx);
			} else {
				table = new ColComTable(file, cols, distribute, opt, blockSize, ctx);
			}
			
			table.setFileObject(fo);
			table.setPartition(partition);
			return table.getBaseTable();
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public boolean isLeftTypeMatch(Object obj) {
		if (obj instanceof FileObject) {
			return !((FileObject)obj).getFile().isCloudFile();
		}
		return false;
	}
}
