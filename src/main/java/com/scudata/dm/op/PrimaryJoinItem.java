package com.scudata.dm.op;

import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.Current;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.Node;
import com.scudata.util.Variant;

class PrimaryJoinItem {
	private Context ctx; // 当前表计算用的上下文
	private ICursor cursor;
	private Expression []keyExps;
	private Expression []newExps;
	private Node []gathers = null; // 统计表达式中的统计函数
	private int joinType; // 关联类型，0:内连接, 1:左连接, 2：差运算，保留匹配不上的
	
	private Current current; // 当前计算对象，用于压栈
	private Sequence data; // 游标取出的数据
	private Object []keyValues;
	private Object []cacheKeyValues; // 上一条记录的键值，用于带时间键或汇总的关联
	
	private int keyCount = 0;
	private int newCount = 0; // new字段数
	private int seq;	// 当前记录在data中的序号

	private boolean isGather = false; // 关联的表的计算表达式是否是汇总表达式
	private boolean isPrevMatch = false; // 当前记录是键值是否跟上一条左侧记录的键值匹配
	
	public PrimaryJoinItem(ICursor cursor, Expression []keyExps, Expression []newExps, int joinType, Context ctx) {
		this.ctx = new Context(ctx);
		this.cursor = cursor;
		this.keyExps = keyExps;
		this.newExps = newExps;
		this.joinType = joinType;
		
		if (newExps != null) {
			newCount = newExps.length;
			for (Expression exp : newExps) {
				if (exp.getHome() instanceof Gather) {
					gathers = Sequence.prepareGatherMethods(newExps, ctx);
					isGather = true;
					break;
				}
			}
		}
		
		keyCount = keyExps.length;
		keyValues = new Object[keyCount];
		cacheKeyValues = new Object[keyCount];
		cacheData();
	}
	
	private void cacheData() {
		data = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
		if (data != null && data.length() > 0) {
			ComputeStack stack = ctx.getComputeStack();
			if (current != null) {
				stack.pop();
			}
			
			current = new Current(data, 1);
			stack.push(current);
			seq = 1;
			
			for (int i = 0; i < keyCount; ++i) {
				keyValues[i] = keyExps[i].calculate(ctx);
			}
		} else {
			seq = -1;
		}
	}
	
	public Object[] getCurrentKeyValues() {
		if (seq > 0) {
			return keyValues;
		} else {
			return null;
		}
	}
	
	private void calcNewValues(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		int newCount = this.newCount;
		if (newCount == 0) {
			return;
		}
		
		Context ctx = this.ctx;
		Node []gathers = this.gathers;
		
		if (isGather) {
			isPrevMatch = false;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].gather(ctx);
			}
			
			Expression []keyExps = this.keyExps;
			Object []keyValues = this.keyValues;
			int keyCount = this.keyCount;
			
			while (true) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						break;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				if (Variant.compareArrays(srcKeyValues, keyValues, keyCount) == 0) {
					for (int i = 0; i < newCount; ++i) {
						resultValues[fieldIndex + i] = gathers[i].gather(resultValues[fieldIndex + i], ctx);
					}
				} else {
					break;
				}
			}
			
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].finish(resultValues[fieldIndex + i]);
			}
		} else {
			Expression []newExps = this.newExps;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = newExps[i].calculate(ctx);
			}
		}
	}
	
	public void resetNewValues(Object []resultValues, int fieldIndex) {
		for (int i = 0, count = newCount; i < count; ++i) {
			resultValues[fieldIndex++] = null;
		}
	}
	
	public void popTop(Object []resultValues, int fieldIndex) {
		int newCount = this.newCount;
		Context ctx = this.ctx;
		Node []gathers = this.gathers;
		
		if (isGather) {
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].gather(ctx);
			}
			
			Expression []keyExps = this.keyExps;
			Object []keyValues = this.keyValues;
			Object []prevKeyValues = this.cacheKeyValues;
			int keyCount = this.keyCount;
			System.arraycopy(keyValues, 0, prevKeyValues, 0, keyCount);
			
			while (true) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						break;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				if (Variant.compareArrays(prevKeyValues, keyValues, keyCount) == 0) {
					for (int i = 0; i < newCount; ++i) {
						resultValues[fieldIndex + i] = gathers[i].gather(resultValues[fieldIndex + i], ctx);
					}
				} else {
					break;
				}
			}
			
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = gathers[i].finish(resultValues[fieldIndex + i]);
			}
		} else {
			Expression []newExps = this.newExps;
			for (int i = 0; i < newCount; ++i) {
				resultValues[fieldIndex + i] = newExps[i].calculate(ctx);
			}
			
			seq++;
			if (seq > data.length()) {
				cacheData();
			} else {
				current.setCurrent(seq);
				for (int i = 0; i < keyCount; ++i) {
					keyValues[i] = keyExps[i].calculate(ctx);
				}
			}
		}
	}
	
	public boolean join(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		if (seq == -1) {
			return joinType != 0;
		}

		Expression []keyExps = this.keyExps;
		Object []keyValues = this.keyValues;
		int keyCount = this.keyCount;
		
		if (isPrevMatch) {
			isPrevMatch = false;
			seq++;
			
			if (seq > data.length()) {
				cacheData();
				if (seq == -1) {
					resetNewValues(resultValues, fieldIndex);
					return joinType != 0;
				}
			} else {
				current.setCurrent(seq);
				for (int i = 0; i < keyCount; ++i) {
					keyValues[i] = keyExps[i].calculate(ctx);
				}
			}
		}
		
		while (true) {
			int cmp = Variant.compareArrays(srcKeyValues, keyValues, keyCount);
			if (cmp == 0) {
				isPrevMatch = true;
				if (joinType == 2) {
					resetNewValues(resultValues, fieldIndex);
					return false;
				} else {
					calcNewValues(srcKeyValues, resultValues, fieldIndex);
					return true;
				}
			} else if (cmp > 0) {
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						resetNewValues(resultValues, fieldIndex);
						return joinType != 0;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
			} else {
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			}
		}
	}
	
	public boolean timeKeyJoin(Object []srcKeyValues, Object []resultValues, int fieldIndex) {
		if (seq == -1) {
			resetNewValues(resultValues, fieldIndex);
			return joinType != 0;
		}
		
		int keyCount = this.keyCount;
		int timeIndex = keyCount - 1;
		Expression []keyExps = this.keyExps;
		Object []keyValues = this.keyValues;
		
		while (true) {
			// 先比较除去时间键之外其它字段的大小
			int cmp = Variant.compareArrays(srcKeyValues, keyValues, timeIndex);
			if (cmp < 0) {
				// 没有能关连上的
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			} else if (cmp > 0) {
				// 维表值小，跳到下一条记录
				seq++;
				if (seq > data.length()) {
					cacheData();
					if (seq == -1) {
						resetNewValues(resultValues, fieldIndex);
						return joinType != 0;
					}
				} else {
					current.setCurrent(seq);
					for (int i = 0; i < keyCount; ++i) {
						keyValues[i] = keyExps[i].calculate(ctx);
					}
				}
				
				continue;
			}
			
			// 能匹配上，比较时间键的大小
			cmp = Variant.compare(srcKeyValues[timeIndex], keyValues[timeIndex], true);
			if (cmp < 0) {
				// 时间键没有能匹配的
				resetNewValues(resultValues, fieldIndex);
				return joinType != 0;
			} else if (cmp == 0) {
				if (joinType == 2) {
					resetNewValues(resultValues, fieldIndex);
					return false;
				} else {
					calcNewValues(srcKeyValues, resultValues, fieldIndex);
					return true;
				}
			}
			
			// 找到时间最近的
			Next:
			while (true) {
				int len = data.length();
				for (int q = seq + 1; q <= len; ++q) {
					current.setCurrent(q);
					for (int i = 0; i < timeIndex; ++i) {
						if (!Variant.isEquals(keyValues[i], keyExps[i].calculate(ctx))) {
							seq = q - 1;
							current.setCurrent(seq);
							break Next;
						}
					}
					
					Object time = keyExps[timeIndex].calculate(ctx);
					cmp = Variant.compare(srcKeyValues[timeIndex], time, true);
					if (cmp < 0) {
						seq = q - 1;
						current.setCurrent(seq);
						break Next;
					} else if (cmp == 0) {
						seq = q;
						keyValues[timeIndex] = time;
						break Next;
					} else {
						keyValues[timeIndex] = time;
					}
				}
				
				Sequence prevData = data;
				int prevSeq = len;
				System.arraycopy(keyValues, 0, cacheKeyValues, 0, keyCount);
				cacheData();
				
				if (seq == -1) {
					data = prevData;
					seq = prevSeq;
					System.arraycopy(cacheKeyValues, 0, keyValues, 0, keyCount);
					break;
				} else {
					cmp = Variant.compareArrays(srcKeyValues, keyValues, keyCount);
					if (cmp < 0) {
						data.insert(1, prevData.getMem(prevSeq));
						System.arraycopy(cacheKeyValues, 0, keyValues, 0, keyCount);
						break;
					} else if (cmp == 0) {
						if (joinType == 2) {
							resetNewValues(resultValues, fieldIndex);
							return false;
						} else {
							calcNewValues(srcKeyValues, resultValues, fieldIndex);
							return true;
						}
					}
				}
			}

			if (joinType == 2) {
				resetNewValues(resultValues, fieldIndex);
				return false;
			} else {
				calcNewValues(srcKeyValues, resultValues, fieldIndex);
				return true;
			}
		}
	}
}
