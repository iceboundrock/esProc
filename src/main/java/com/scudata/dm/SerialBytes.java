package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * 排号，由最长8个字节构成的整数，用于表示键值，可比较，不能运算
 * 第一个字节保存在value1的最高字节上
 * value1					value2
 * b1 b2 b3 b4 b5 b6 b7 b8	b9 b10 b11 b12口口口口
 *
 * @author WangXiaoJun
 */
public class SerialBytes implements Externalizable, Comparable<SerialBytes> {
	private static final long serialVersionUID = 0x02613003;
	private static final long LONGSIGN = 0xFFFFFFFFFFFFFFFFL;
	private static final String []ZEROSTRS;
	static {
		ZEROSTRS = new String[16];
		String str = "";
		for (int i = 0; i < 16; ++i) {
			ZEROSTRS[i] = str;
			str += '0';
		}
	}
	
	private long value1;
	private long value2;
	//private int len;
	
	// 用于序列化
	public SerialBytes() {
	}
	
	public SerialBytes(byte []bytes, int len) {
		int index = 0;
		for (byte b : bytes) {
			++index;
			if (index <= 8) {
				value1 |= (0xFFL & b) << (8 - index) * 8;
			} else {
				value2 |= (0xFFL & b) << (16 - index) * 8;
			}
		}
	}
	
	public SerialBytes(long value1, long value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
	
	/**
	 * 构建排号对象
	 * @param num 数
	 * @param len 字节数，范围是[1,16]
	 */
	public SerialBytes(Number num, int len) {
		if (len > 16) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
		}
		
		if (len <= 8) {
			value1 = num.longValue() << (8 - len) * 8;
		} else {
			BigInteger bi = Variant.toBigInteger(num);
			byte []bytes = bi.toByteArray();
			int blen = bytes.length;
			if (blen > len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("serialbytes.biLenMismatch"));
			}
			
			// 如果实际字节数少于给定的字节数则用0补足
			int index = len - blen;
			for (byte b : bytes) {
				++index;
				if (index <= 8) {
					value1 |= (0xFFL & b) << (8 - index) * 8;
				} else {
					value2 |= (0xFFL & b) << (16 - index) * 8;
				}
			}
		}
	}
	
	/**
	 * 构建排号对象
	 * @param vals 数数组
	 * @param lens 每个数的字节数数组，总字节数不能超过16
	 */
	public SerialBytes(Number []vals, int []lens) {
		int len = 0;
		for (int i = 0; i < vals.length; ++i) {
			int curLen = lens[i];
			if (curLen <= 8) {
				// 如果长度小于8则把高位置0，由低类型转过来的数可能是负值
				long curVal = vals[i].longValue() & (LONGSIGN >>> 8 - curLen);
				
				if (len < 8) {
					len += curLen;
					if (len <= 8) {
						value1 |= curVal << (8 - len) * 8;
					} else {
						value1 |= curVal >>> (len - 8) * 8;
						value2 = curVal << (16 - len) * 8;
					}
				} else {
					len += curLen;
					if (len > 16) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
					}
					
					value2 |= curVal << (16 - len) * 8;
				}
			} else {
				BigInteger bi = Variant.toBigInteger(vals[i]);
				byte []bytes = bi.toByteArray();
				int blen = bytes.length;
				if (blen > curLen) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("serialbytes.biLenMismatch"));
				}
				
				// 如果实际字节数少于给定的字节数则用0补足
				len += curLen - blen;
				
				for (byte b : bytes) {
					++len;
					if (len <= 8) {
						value1 |= (0xFFL & b) << (8 - len) * 8;
					} else {
						value2 |= (0xFFL & b) << (16 - len) * 8;
					}
				}
				
				if (len > 16) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
				}
			}
		}
	}
	
	/**
	 * 返回排号的长度
	 * @return int
	 */
	public int length() {
		return 16;
	}
	
	/**
	 * 返回排号的哈希值
	 * @return int
	 */
    public int hashCode() {
    	return HashUtil.hashCode(value1 + value2);
    }
	
    /**
     * 转成字符串，用于显示
	 * @return String
     */
	public String toString() {
		String str1 = Long.toHexString(value1);
		int strLen = str1.length();
		
		if (strLen < 16) {
			str1 = ZEROSTRS[16 - strLen] + str1;
		}
		
		String str2 = Long.toHexString(value2);
		strLen = str2.length();
		if (strLen < 16) {
			return str1 + ZEROSTRS[16 - strLen] + str2;
		} else {
			return str1 + str2;
		}
	}
	
	/**
	 * 把排号转成字节数组，用于存储
	 * @return 字节数组
	 */
	public byte[] toByteArray() {
		byte []bytes = new byte[16];
		int i = 16;
		for (; i > 8; --i) {
			bytes[i - 1] = (byte)(value2 >>> (16 - i) * 8);
		}
		
		for (; i > 0; --i) {
			bytes[i - 1] = (byte)(value1 >>> (8 - i) * 8);
		}
		
		return bytes;
	}
	
	/**
	 * 取排号指定字节的值
	 * @param q 字节号，从1开始计数
	 * @return long
	 */
	public long getByte(int q) {
		if (q < 1 || q > 16) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(q + mm.getMessage("engine.indexOutofBound"));
		} else if (q <= 8) {
			return (value1 >>> (8 - q) * 8) & 0xFFL;
		} else {
			return (value2 >>> (16 - q) * 8) & 0xFFL;
		}
	}
	
	/**
	 * 取排号指定字节区间的值
	 * @param start 起始字节号，从1开始计数（包含）
	 * @param end 结束字节号，从1开始计数（包含）
	 * @return long
	 */
	public long getBytes(int start, int end) {
		if (start < 1 || end < start || end > 16) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(start + "," + end + mm.getMessage("engine.indexOutofBound"));
		}
		
		if (start <= 8) {
			if (end <= 8) {
				return (value1 << (start - 1) * 8) >>> (7 + start - end) * 8;
			} else {
				long result = (value1 << (start - 1) * 8) >>> (start - 1) * 8;
				return (result << (end - 8) * 8) | (value2 >>> (16 - end) * 8);
			}
		} else {
			return (value2 << (start - 9) * 8) >>> (7 + start - end) * 8;
		}
	}
	
	/**
	 * 比较两个排号的大小
	 * @param value1 排号1的值1
	 * @param value2 排号1的值2
	 * @param otherValue1 另一个排号的值1
	 * @param otherValue2 另一个排号的值2
	 * @return 1：排号1大，0：一样的，-1：排号2大
	 */
	public static int compare(long value1, long value2, long otherValue1, long otherValue2) {
		if (value1 == otherValue1) {
			if (value2 == otherValue2) {
				return 0;
			} else if (value2 < 0) {
				if (otherValue2 >= 0) {
					return 1;
				} else {
					return value2 > otherValue2 ? 1 : -1;
				}
			} else if (otherValue2 < 0) {
				return -1;
			} else {
				return value2 > otherValue2 ? 1 : -1;
			}
		} else {
			if (value1 < 0) {
				if (otherValue1 >= 0) {
					return 1;
				} else {
					return value1 > otherValue1 ? 1 : -1;
				}
			} else if (otherValue1 < 0) {
				return -1;
			} else {
				return value1 > otherValue1 ? 1 : -1;
			}
		}
	}
	
	/**
	 * 比较两个排号的大小，用于排序
	 * @param o
	 * @return int
	 */
	public int compareTo(SerialBytes o) {
		return compare(value1, value2, o.value1, o.value2);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SerialBytes) {
			return equals((SerialBytes)obj);
		} else {
			return false;
		}
	}
	
	public long getValue1() {
		return value1;
	}

	public long getValue2() {
		return value2;
	}

	/**
	 * 判断两个排号是否相等
	 * @param other
	 * @return
	 */
	public boolean equals(SerialBytes other) {
		return value1 == other.value1 && value2 == other.value2;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(value1);
		out.writeLong(value2);
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value1 = in.readLong();
		value2 = in.readLong();
	}
}