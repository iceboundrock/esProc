package com.scudata.expression.fn.algebra;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * 两个向量之间的欧氏距离dis(A, B)
 * @author bd
 */
public class Distance extends Function {
	/**
	 * 检查表达式的有效性，无效则抛出异常
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("dis" + mm.getMessage("function.missingParam"));
		}
	}
	
	public Object calculate(Context ctx) {
		boolean manhattan = false;
		boolean standard = false;
		if (option != null) {
			if (option.indexOf('a') > -1) {
				manhattan = true;
			}
			if (option.indexOf('m') > -1) {
				standard = true;
			}
		}
		Sequence s1 = null;
		Sequence s2 = null;
		if (param.isLeaf()) {
			Object o = param.getLeafExpression().calculate(ctx);
			if (o instanceof Sequence) {
				s1 = (Sequence) o;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.invalidParam"));
			}

			IParam sub1 = param.getSub(0);
			IParam sub2 = param.getSub(1);
			if (sub1 == null || sub2 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.invalidParam"));
			}
			Object o1 = sub1.getLeafExpression().calculate(ctx);
			Object o2 = sub2.getLeafExpression().calculate(ctx);
			if (o1 instanceof Sequence && o2 instanceof Sequence) {
				// edited by bd, 2021.11.17, 在dis函数中，单层序列认为是横向量
				s1 = (Sequence) o1;
				s2 = (Sequence) o2;
			}
		}
		if (s1 != null) {
			Matrix A = new Matrix(s1);
			Object o11 = s1.length() > 0 ? s1.get(1) : null;
			if (!(o11 instanceof Sequence)) {
				// A为单序列定义的向量，转成横向量
				A = A.transpose();
			}
			Matrix B = null;
			if (s2 != null) {
				B = new Matrix(s2);
				Object o21 = s2.length() > 0 ? s2.get(1) : null;
				if (!(o21 instanceof Sequence)) {
					// A为单序列定义的向量，转成横向量
					B = B.transpose();
				}
			}
			else {
				double[][] bs = new double[1][A.getCols()];
				B = new Matrix(bs);
			}
			
			if (A.getCols() == 0 && A.getRows() == 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.paramTypeError"));
			}
			else if (B.getCols() == 0 && B.getRows() == 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.paramTypeError"));
			}
			Matrix X = new Matrix(A.getRows(), B.getRows());
			double[][] xs = X.getArray();
			for (int r = 0; r < A.getRows(); r++) {
				for (int c = 0; c < B.getRows(); c++) {
					double res = 0;
					for (int i = 0; i < A.getCols(); i++) {
						if (manhattan) {
							res += Math.abs(A.get(r, i)-B.get(c, i));
						}
						else {
							res += Math.pow((A.get(r, i) - B.get(c, i)), 2);
						}
					}
					if (standard) {
						res = res / A.getCols();
					}
					if (manhattan) {
						xs[r][c] = res;
					}
					else {
						xs[r][c] = Math.sqrt(res);
					}
				}
			}
			return X.toSequence(option, false);
		} 
	    else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("dis" + mm.getMessage("function.paramTypeError"));
		}
	}

}
