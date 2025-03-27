package com.scudata.expression.mfn.sequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.SequenceFunction;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * 对序列做模式匹配，如果有拆出项则进行拆分返回成序表
 * A.regex(rs,F)
 * A.regex(rs,F;Fi,…)
 * @author RunQian
 *
 */
public class Regex extends SequenceFunction {
	/**
	 * 检查表达式的有效性，无效则抛出异常
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("regex" + mm.getMessage("function.missingParam"));
		}
	}

	public Object calculate(Context ctx) {
		int flags = 0;
		if (option != null) {
			if (option.indexOf('c') != -1) flags |= Pattern.CASE_INSENSITIVE;
			if (option.indexOf('u') != -1) flags |= Pattern.UNICODE_CASE;
		}

		String strPattern;
		String []names = null;
		Expression exp = null;
		IParam sub0;
		IParam sub1 = null;
		
		if (param.getType() == IParam.Semicolon) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.invalidParam"));
			}
			
			sub0 = param.getSub(0);
			if (sub0 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.missingParam"));
			}
			
			sub1 = param.getSub(1);
		} else {
			sub0 = param;
		}
		
		if (sub0.isLeaf()) {
			Object obj = sub0.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.paramTypeError"));
			}

			strPattern = (String)obj;
		} else {
			if (sub0.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = sub0.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = sub.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex" + mm.getMessage("function.paramTypeError"));
			}

			strPattern = (String)obj;
			sub = sub0.getSub(1);
			if (sub != null) exp = sub.getLeafExpression();
		}

		if (sub1 == null) {
		} else if (sub1.isLeaf()) {
			names = new String[1];
			names[0] = sub1.getLeafExpression().getIdentifierName();
		} else {
			int size = sub1.getSubSize();
			names = new String[size];
			for (int f = 0; f < size; ++f) {
				IParam sub = sub1.getSub(f);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("regex" + mm.getMessage("function.invalidParam"));
				}

				names[f] = sub.getLeafExpression().getIdentifierName();
			}
		}

		Pattern pattern = Pattern.compile(strPattern, flags);
		Matcher m = pattern.matcher("");
		int fcount = m.groupCount();
		if (fcount > 0) {
			if (names == null) {
				names = new String[fcount];
			} else if (names.length != fcount) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("regex: " + mm.getMessage("engine.dsNotMatch"));
			}
		} else {
			names = null;
		}
		
		int len = srcSequence.length();
		if (len == 0) {
			return null;
		}

		Sequence strs;
		if (exp != null) {
			strs = srcSequence.calc(exp, "o", ctx);
		} else {
			strs = srcSequence;
		}
		
		IArray strMems = strs.getMems();
		if (names == null) {
			IArray srcMems = srcSequence.getMems();
			Sequence result = new Sequence(len);
			
			if (option == null || option.indexOf('w') == -1) {
				for (int i = 1; i <= len; ++i) {
					Object obj = strMems.get(i);
					if (obj instanceof String) {
						m = pattern.matcher((String)obj);
						if (m.find()) {
							result.add(srcMems.get(i));
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needStringExp"));
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					Object obj = strMems.get(i);
					if (obj instanceof String) {
						m = pattern.matcher((String)obj);
						if (m.matches()) {
							result.add(srcMems.get(i));
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needStringExp"));
					}
				}
			}
			
			return result;
		} else {
			boolean doParse = option != null && option.indexOf('p') != -1;
			int gcount = names.length;
			Table table = new Table(names, len);
			
			if (doParse) {
				for (int i = 1; i <= len; ++i) {
					Object obj = strMems.get(i);
					if (obj instanceof String) {
						m = pattern.matcher((String)obj);
						if (m.find()) {
							BaseRecord r = table.newLast();
							for (int g = 1; g <= gcount; ++g) {
								String s = m.group(g);
								r.setNormalFieldValue(g - 1, Variant.parse(s));
							}
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needStringExp"));
					}
				}
			} else {
				for (int i = 1; i <= len; ++i) {
					Object obj = strMems.get(i);
					if (obj instanceof String) {
						m = pattern.matcher((String)obj);
						if (m.find()) {
							BaseRecord r = table.newLast();
							for (int g = 1; g <= gcount; ++g) {
								r.setNormalFieldValue(g - 1, m.group(g));
							}
						}
					} else if (obj != null) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needStringExp"));
					}
				}
			}

			return table;
		}
	}
}

