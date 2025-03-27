package com.scudata.expression.operator;

import java.util.Date;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Operator;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * 负号运算符：-
 * @author RunQian
 *
 */
public class Negative extends Operator {
	public Negative() {
		priority = PRI_NEGT;
	}

	/**
	 * 检查表达式的有效性，无效则抛出异常
	 */
	public void checkValidity() {
		if (right == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"-\"" + mm.getMessage("operator.missingRightOperation"));
		}
		
		right.checkValidity();
	}
	
	public Object calculate(Context ctx) {
		Object rightResult = right.calculate(ctx);
		if (rightResult instanceof Number) {
			return Variant.negate((Number)rightResult);
		} else if (rightResult == null) {
			return null;
		} else if (rightResult instanceof Date) {
			return Variant.negate((Date)rightResult);
		} else if (rightResult instanceof String) {
			return Variant.negate((String)rightResult);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"-\"" +mm.getMessage("operator.numberRightOperation"));
		}
	}
	
	/**
	 * 判断是否可以计算全部的值，有赋值运算时只能一行行计算
	 * @return
	 */
	public boolean canCalculateAll() {
		return right.canCalculateAll();
	}

	/**
	 * 计算出所有行的结果
	 * @param ctx 计算上行文
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = right.calculateAll(ctx);
		return array.negate();
	}

	/**
	 * 计算signArray中取值为sign的行
	 * @param ctx
	 * @param signArray 行标识数组
	 * @param sign 标识
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		IArray array = right.calculateAll(ctx, signArray, sign);
		return array.negate();
	}
}
