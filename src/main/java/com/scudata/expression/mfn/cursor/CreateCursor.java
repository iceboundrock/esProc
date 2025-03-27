package com.scudata.expression.mfn.cursor;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.cursor.PrereadCursor;
import com.scudata.dm.cursor.SinglepathCursor;
import com.scudata.dm.cursor.SyncCursor;
import com.scudata.expression.CursorFunction;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

// 
/**
 * 把多路游标转成路数更少的多路游标或单路游标
 * mcs.cursor(n)
 * @author RunQian
 *
 */
public class CreateCursor extends CursorFunction {
	public Object calculate(Context ctx) {
		if (cursor instanceof MultipathCursors) {
			return createCursor((MultipathCursors)cursor, param, ctx);
		} else if (option != null && option.indexOf('m') != -1) {
			int n = 0;
			if (param != null) {
				Object obj = param.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					n = ((Number)obj).intValue();
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
				}
			} else {
				n = Env.getCursorParallelNum();
			}
			
			if (n > 1) {
				ICursor []cursors = new ICursor[n];
				for (int i = 0; i < n; ++i) {
					cursors[i] = new SyncCursor(cursor);
				}
				
				return new MultipathCursors(cursors, ctx);
			} else {
				return new PrereadCursor(cursor);
			}
		} else {
			return cursor;
		}
	}
	
	private static ICursor createCursor(MultipathCursors mcs, IParam param, Context ctx) {
		int newPathCount = 1;
		if (param == null) {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException("cursor" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			if (!(obj instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("cursor" + mm.getMessage("function.paramTypeError"));
			}
	
			newPathCount = ((Number)obj).intValue();
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("cursor" + mm.getMessage("function.invalidParam"));
		}
		
		int oldPathCount = mcs.getPathCount();
		if (newPathCount >= oldPathCount) {
			return mcs;
		} else if (newPathCount <= 1) {
			return new SinglepathCursor(mcs);
		} else {
			int avg = oldPathCount / newPathCount;
			int mod = oldPathCount % newPathCount;
			ICursor []newCursors = new ICursor[newPathCount];
			ICursor []oldCursors = mcs.getCursors();
			int index = 0;
			
			for (int i = 0; i < newPathCount; ++i) {
				if (mod > 0) {
					mod--;
					int curLen = avg + 1;
					ICursor []tmp = new ICursor[curLen];
					System.arraycopy(oldCursors, index, tmp, 0, curLen);
					newCursors[i] = new MultipathCursors(tmp, ctx);
					index += curLen;
				} else {
					if (avg == 1) {
						newCursors[i] = oldCursors[index];
						index++;
					} else {
						ICursor []tmp = new ICursor[avg];
						System.arraycopy(oldCursors, index, tmp, 0, avg);
						newCursors[i] = new MultipathCursors(tmp, ctx);
						index += avg;
					}
				}
			}
			
			return new MultipathCursors(newCursors, ctx);
		}
	}
}
