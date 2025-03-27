package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * 游标或管道的延迟派生字段处理类
 * @author RunQian
 *
 */
public class Derive extends Operation  {
	private Expression[] exps; // 派生字段的表达式数组
	private String[] names; // 派生字段的字段名数组
	private String opt; // 选项
	
	private int oldColCount; // 源字段数
	private DataStruct newDs; // 结果集数据结构
	//private boolean containNull; // 是否保留null
	
	private int level = 0;
	
	public Derive(Expression []exps, String []names, String opt) {
		this(null, exps, names, opt, 0);
	}
	
	public Derive(Function function, Expression []exps, String []names, String opt, int level) {
		super(function);
		this.exps = exps;
		this.names = names;
		this.opt = opt;
		//this.containNull = opt == null || opt.indexOf('i') == -1;
		this.level = level;
	}
	
	/**
	 * 复制运算用于多线程计算，因为表达式不能多线程计算
	 * @param ctx 计算上下文
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		Expression []dupExps = dupExpressions(exps, ctx);
		return new Derive(function, dupExps, names, opt, level);
	}
	
	private DataStruct getNewDataStruct(Sequence seq) {
		if (newDs == null) {
			DataStruct ds = seq.dataStruct();
			if (ds == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needPurePmt"));
			}
			
			Expression[] exps = this.exps;
			int colCount = exps.length;
			for (int i = 0; i < colCount; ++i) {
				if (names[i] == null || names[i].length() == 0) {
					if (exps[i] == null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException("derive" + mm.getMessage("function.invalidParam"));
					}
	
					names[i] = exps[i].getFieldName(ds);;
				} else {
					if (exps[i] == null) {
						exps[i] = Expression.NULL;
					}
				}
			}
			
			String []oldNames = ds.getFieldNames();
			oldColCount = oldNames.length;
			
			// 合并字段
			int newColCount = oldColCount + colCount;
			String []totalNames = new String[newColCount];
			System.arraycopy(oldNames, 0, totalNames, 0, oldColCount);
			System.arraycopy(names, 0, totalNames, oldColCount, colCount);
			newDs = ds.create(totalNames);
		}
		
		return newDs;
	}
	
	/**
	 * 处理游标或管道当前推送的数据
	 * @param seq 数据
	 * @param ctx 计算上下文
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		if (exps == null) {
			return seq.derive(opt);
		} else if (level > 1) {
			return seq.derive(names, exps, opt, ctx, level);
		} else {
			DataStruct newDs = getNewDataStruct(seq);
			return seq.derive(newDs, exps, opt, ctx);
		}
	}
}
