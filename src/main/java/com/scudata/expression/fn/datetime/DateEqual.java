package com.scudata.expression.fn.datetime;

import java.util.Date;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * deq(datetimeExp1,datetimeExp2) 将dateExp1和dateExp2两个日期参数比较，判断是否相同
 * @author runqian
 *
 */
public class DateEqual extends Function {
	private Expression exp1;
	private Expression exp2;

	/**
	 * 检查表达式的有效性，无效则抛出异常
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("deq" + mm.getMessage("function.missingParam"));
		} else if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("deq" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub1 = param.getSub(0);
		IParam sub2 = param.getSub(1);
		if (sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("deq" + mm.getMessage("function.invalidParam"));
		}
		
		exp1 = sub1.getLeafExpression();
		exp2 = sub2.getLeafExpression();
	}

	public Object calculate(Context ctx) {
		Object result1 = exp1.calculate(ctx);
		Object result2 = exp2.calculate(ctx);
		if (result1 == null) {
			return result2 == null ? Boolean.TRUE : Boolean.FALSE;
		} else if (result2 == null) {
			return Boolean.FALSE;
		}

		if (result1 instanceof String) {
			result1 = Variant.parseDate((String)result1);
			if (!(result1 instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("deq" + mm.getMessage("function.paramTypeError"));
			}
		}

		if (result2 instanceof String) {
			result2 = Variant.parseDate((String)result2);
			if (!(result2 instanceof Date)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("deq" + mm.getMessage("function.paramTypeError"));
			}
		}

		if (!(result1 instanceof Date) || !(result2 instanceof Date)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("deq" + mm.getMessage("function.paramTypeError"));
		}

		Date date1 = (Date)result1;
		Date date2 = (Date)result2;
		return Variant.isEquals(date1, date2, option) ? Boolean.TRUE : Boolean.FALSE;
	}
}
