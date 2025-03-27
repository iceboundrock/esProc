package com.scudata.dm;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.scudata.array.IArray;
import com.scudata.common.RQException;

/**
 * 用于向输出流写出对象，对应的读为ObjectReader
 * ObjectWriter和ObjectReader的方法是一一对应的，比如writeInt和readInt对应，writeInt32和readInt32对应。
 * 读的时候一定要调用和写的方法相对应的方法，不能写的时候用writeInt32而读的时候用readInt。
 * 此输出流有自己的写缓冲区
 * @author WangXiaoJun
 *
 */
public class ObjectWriter extends OutputStream implements ObjectOutput {
	// 以下常量为数据的类型编码
	static final int MARK0 = 0x00;
	static final int NULL = 0x00;
	static final int TRUE = 0x01;
	static final int FALSE = 0x02;
	static final int LONG0 = 0x03;
	static final int FLOAT0 = 0x04;
	static final int DECIMAL0 = 0x05;

	static final int MARK1 = 0x10;
	static final int INT16 = 0x10; // 16bit无符号正数
	static final int INT32 = 0x11; // Integer
	static final int LONG16 = 0x12; // 16bit无符号正数
	static final int LONG32 = 0x13; // 32bit无符号正数
	static final int LONG64 = 0x14; // Long
	static final int FLOAT16 = 0x15;
	static final int FLOAT32 = 0x16;
	static final int FLOAT64 = 0x17;

	static final int MARK2 = 0x20;
	static final int DECIMAL = 0x20;
	static final int STRING = 0x21;
	static final int SEQUENCE = 0x22;
	static final int TABLE = 0x23;
	static final int BLOB = 0x24;
	static final int AVG = 0x25; // 分组运算中求平均值的中间结果
	static final int RECORD = 0x27;

	static final int MARK3 = 0x30;
	static final int DATE16 = 0x30; // 2000年之后的日期
	static final int DATE32 = 0x31; // 2000年之前的日期
	static final int TIME16 = 0x32;
	static final int TIME17 = 0x33;
	static final int DATETIME32 = 0x34;
	static final int DATETIME33 = 0x35;
	static final int DATETIME64 = 0x36;
	static final int TIME32 = 0x37;
	static final int DATE24 = 0x38; // 2000年之后的日期
	static final int DATE64 = 0x39; // 超界表示不了都得日期用64位保存
	
	static final int SERIALBYTES = 0x40; // 排号
	static final int REPEAT3 = 0x70;
	static final int REPEAT11 = 0x78;
	
	static final int INT4 = 0x80; // 4bit无符号正数
	static final int INT12 = 0x90; // 12bit无符号正数
	static final int HEX4 = 0xA0; // 长度为1的十六进制数码字串，字母大写，一般用于标志
	static final int DIGIT4 = 0xB0; // 长度小于等于30的数字串
	static final int STRING4 = 0xC0;
	static final int STRING5 = 0xD0;
	static final int STRING4_ASSIC = 0xE0;//
	static final int STRING5_ASSIC = 0xF0;


	// 块开始标志，连续16个0xFF
	public static final byte []BLOCKMARKS = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

	private static final double MINFLOAT = 0.000001;
	private static final int MAX_DIGIT_LEN = 30;

	static final long BASEDATE; // 1992年之前有的日期不能被86400000整除
	static final long BASETIME;
	static {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(1970, java.util.Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(java.util.Calendar.MILLISECOND, 0);
		BASETIME = calendar.getTimeInMillis();

		calendar.set(java.util.Calendar.YEAR, 2000);
		BASEDATE = calendar.getTimeInMillis();
	}

	protected OutputStream out; // 输出流
	protected byte []buf; // 写缓冲区
	protected int count = 0; // 写缓冲区当前字节数

	/**
	 * 构建写对象
	 * @param out 输出流
	 */
	public ObjectWriter(OutputStream out) {
		this.out = out;
		buf = new byte[Env.FILE_BUFSIZE > 1024 ? Env.FILE_BUFSIZE : 1024];
	}

	/**
	 * 构建写对象
	 * @param out 输出流
	 * @param bufSize 缓冲区大小
	 */
	public ObjectWriter(OutputStream out, int bufSize) {
		this.out = out;
		buf = new byte[bufSize];
	}
	
	// 把缓存写到输出流
    private void flushBuffer() throws IOException {
        if (count > 0) {
		    out.write(buf, 0, count);
		    count = 0;
        }
    }

    /**
     * 强制写出缓存到永久存储
 	 * @throws IOException
    */
	public void flush() throws IOException {
		flushBuffer();
		out.flush();
	}

	/**
	 * 关闭写
	 * @throws IOException
	 */
	public void close() throws IOException {
		flush();
		out.close();
	}

	/**
	 * 写出一个字节
	 * @param b 字节值
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		if (count >= buf.length) {
		    flushBuffer();
		}
		
		buf[count++] = (byte)b;
	}

	/**
	 * 写出一个字节数组
	 * @param b 字节数组
	 * @throws IOException
	 */
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	/**
	 * 写出一个字节数组
	 * @param b 字节数组
	 * @param off 起始位置
	 * @param len 长度
	 * @throws IOException
	 */
	public void write(byte b[], int off, int len) throws IOException {
		if (len >= buf.length) {
			flushBuffer();
			out.write(b, off, len);
		} else {
			if (len > buf.length - count) {
				flushBuffer();
			}
			
			System.arraycopy(b, off, buf, count, len);
			count += len;
		}
	}

	/**
	 * 写出一个字节
	 * @param v 字节值
	 * @throws IOException
	 */
	public void writeByte(int v) throws IOException {
		if (count >= buf.length) {
		    flushBuffer();
		}
		
		buf[count++] = (byte)v;
	}

	/**
	 * 写出一个布尔值
	 * @param v 布尔值
	 * @throws IOException
	 */
	public void writeBoolean(boolean v) throws IOException {
		if (count >= buf.length) {
		    flushBuffer();
		}
		
		buf[count++] = v ? (byte)1 : (byte)0;
	}

	/**
	 * 写出一个短整数
	 * @param v 短整数
	 * @throws IOException
	 */
	public void writeShort(int v) throws IOException {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	/**
	 * 写出一个字符
	 * @param v 字符
	 * @throws IOException
	 */
	public void writeChar(int v) throws IOException {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	/**
	 * 写出一个布尔值
	 * @param v 布尔值
	 * @throws IOException
	 */
	public void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	/**
	 * 把字符串的每个字符当byte值写出
	 * @param s 字符串
	 * @throws IOException
	 */
	public void writeBytes(String s) throws IOException {
		for (char c : s.toCharArray()) {
			write(c);
		}
	}

	/**
	 * 把字符串的每个字符当char值写出
	 * @param s 字符串
	 * @throws IOException
	 */
	public void writeChars(String s) throws IOException {
		for (char c : s.toCharArray()) {
			writeChar(c);
		}
	}

	/**
	 * 写出字符串的utf编码
	 * @param s 字符串
	 * @throws IOException
	 */
	public void writeUTF(String str) throws IOException {
		writeString(str);
	}

	/**
	 * 写出字节数组
	 * @param v 字节数组
	 * @throws IOException
	 */
	public void writeBytes(byte[] v) throws IOException {
		if (v == null) {
			writeInt(-1);
		} else {
			int len = v.length;
			writeInt(len);
			write(v, 0, len);
		}
	}

	/**
	 * 写出字符串数组
	 * @param strs 字符串数组
	 * @throws IOException
	 */
	public void writeStrings(String[] strs) throws IOException {
		if (strs == null) {
			writeInt(-1);
		} else {
			writeInt(strs.length);
			for (String str : strs) {
				writeObject(str);
			}
		}
	}

	// 长度小于等于30的数字串
	private boolean isDigit(char []charr, int len) {
		if (len > MAX_DIGIT_LEN) return false;

		for (int i = 0; i < len; ++i) {
			if (charr[i] < '0' || charr[i] > '9') return false;
		}

		return true;
	}

	// 长度小于等于MAX_DIGIT_LEN的数字串
	private void writeDigit(char []charr, int len) throws IOException {
		byte []writeBuffer = this.buf;
		if (writeBuffer.length - count < MAX_DIGIT_LEN) {
			flushBuffer();
		}
		
		int seq = count;
		if (len % 2 == 0) {
			writeBuffer[seq++] = (byte)(DIGIT4 | (len / 2));
			for (int i = 0; i < len; ) {
				int d1 = charr[i++] - '0';
				int d2 = charr[i++] - '0';
				writeBuffer[seq++] = (byte)((d1 << 4) | d2);
			}
		} else {
			writeBuffer[seq++] = (byte)(DIGIT4 | (len / 2 + 1));
			len--;
			for (int i = 0; i < len; ) {
				int d1 = charr[i++] - '0';
				int d2 = charr[i++] - '0';
				writeBuffer[seq++] = (byte)((d1 << 4) | d2);
			}
			
			writeBuffer[seq++] = (byte)((charr[len] - '0' << 4) | 0x0F);
		}
		
		count = seq;
	}

	private void writeString(String str) throws IOException {
		int strlen = str.length();
		if (strlen == 0) {
			write(STRING4);
			return;
		} else if (strlen == 1) {
			char c = str.charAt(0);
			if (c >= '0' && c <= '9') {
				write(HEX4 | (c - '0'));
				return;
			} else if (c >= 'A' && c <= 'F') {
				write(HEX4 | (c - 'A' + 10));
				return;
			}
		}

		char[] charr = new char[strlen];
		str.getChars(0, strlen, charr, 0);
		if (isDigit(charr, strlen)) {
			writeDigit(charr, strlen);
			return;
		}

		int utflen = 0;
		int c, count = 0;
		for (int i = 0; i < strlen; i++) {
			c = charr[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}

		byte[] bytearr = new byte[utflen];
		for (int i = 0; i < strlen; i++) {
			c = charr[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte)c;
			} else if (c > 0x07FF) {
				bytearr[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
			} else {
				bytearr[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
			}
		}

		if (utflen <= 0x1F) {
			write(STRING4 | utflen);
			write(bytearr);
		} else {
			write(STRING);
			writeInt(utflen);
			write(bytearr);
		}
	}

	private void writeDecimal(BigDecimal bd) throws IOException {
		byte []bts = bd.unscaledValue().toByteArray();
		int scale = bd.scale();
		if (scale == 0 && bts[0] == 0 && bts.length == 1) {
			write(DECIMAL0);
		} else {
			write(DECIMAL);

			write(scale);
			write(bts.length);
			write(bts);
		}
	}

	private void writeDecimal(BigInteger bi) throws IOException {
		byte []bts = bi.toByteArray();
		if (bts[0] == 0 && bts.length == 1) {
			write(DECIMAL0);
		} else {
			write(DECIMAL);

			write(0);
			write(bts.length);
			write(bts);
		}
	}

	private void writeDouble(double d, long v, int scale) throws IOException {
		if (v <= 0x3FFF) {
			int n = (int)v;
			write(FLOAT16);
			write((n >>> 8) | scale);
			write(n & 0xFF);
		} else if (v <= 0x3FFFFFFF) {
			int n = (int)v;
			write(FLOAT32);
			write((n >>> 24) | scale);
			write((n >>> 16) & 0xFF);
			write((n >>>  8) & 0xFF);
			write(n & 0xFF);
		} else {
			writeDouble64(d);
		}
	}

	private void writeDouble64(double d) throws IOException {
		write(FLOAT64);
		long v = Double.doubleToLongBits(d);
		writeLong64(v);
	}

	/**
	 * 写出一个double
	 * @param d double值
	 * @throws IOException
	 */
	public void writeDouble(double d) throws IOException {
		if (d > 0.0 && d <= 0x3FFFFFFF) {
			double v = Math.ceil(d);
			if (v - d < MINFLOAT) {
				long l = (long)v;
				if (l % 100 == 0) {
					writeDouble(d, l / 100, 0xC0);
				} else {
					writeDouble(d, (long)v, 0x00);
				}
			} else {
				double d1 = d * 100;
				v = Math.ceil(d1);
				if (v - d1 < MINFLOAT) {
					writeDouble(d, (long)v, 0x40);
				} else {
					d1 = d * 10000;
					v = Math.ceil(d1);
					if (v - d1 < MINFLOAT) {
						writeDouble(d, (long)v, 0x80);
					} else {
						writeDouble64(d);
					}
				}
			}
		} else if (d == 0.0) {
			write(FLOAT0);
		} else {
			writeDouble64(d);
		}
	}

	/**
	 * 写出一个长整数
	 * @param v 长整数
	 * @throws IOException
	 */
	public void writeLong(long v) throws IOException {
		if (v == 0L) {
			write(LONG0);
		} else if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
			int n = (int)v;
			if (n >= 0) {
				if (n <= 0xFFFF) {
					write(LONG16);
					write(n >>> 8);
					write(n & 0xFF);
				} else {
					write(LONG32);
					write(n >>> 24);
					write((n >>> 16) & 0xFF);
					write((n >>>  8) & 0xFF);
					write(n & 0xFF);
				}
			} else {
				write(LONG32);
				write(n >>> 24);
				write((n >>> 16) & 0xFF);
				write((n >>>  8) & 0xFF);
				write(n & 0xFF);
			}
		} else {
			write(LONG64);
			writeLong64(v);
		}
	}
	
	/**
	 * 写出一个4字节整数
	 * @param n 整数
	 * @throws IOException
	 */
	public void writeInt32(int n) throws IOException {
		write(n >>> 24);
		write((n >>> 16) & 0xFF);
		write((n >>>  8) & 0xFF);
		write(n & 0xFF);
	}

	/**
	 * 写出一个5字节长整数
	 * @param v 长整数
	 * @throws IOException
	 */
	public void writeLong40(long v) throws IOException {
		byte []writeBuffer = this.buf;
		if (writeBuffer.length - count < 5) {
			flushBuffer();
		}
		
		int seq = count;
		writeBuffer[seq++] = (byte)(v >>> 32);
		writeBuffer[seq++] = (byte)(v >>> 24);
		writeBuffer[seq++] = (byte)(v >>> 16);
		writeBuffer[seq++] = (byte)(v >>>  8);
		writeBuffer[seq++] = (byte)(v >>>  0);
		count = seq;
	}
	
	/**
	 * 写出一个8字节长整数
	 * @param v 长整数
	 * @throws IOException
	 */
	public void writeLong64(long v) throws IOException {
		byte []writeBuffer = this.buf;
		if (writeBuffer.length - count < 8) {
			flushBuffer();
		}
		
		int seq = count;
		writeBuffer[seq++] = (byte)(v >>> 56);
		writeBuffer[seq++] = (byte)(v >>> 48);
		writeBuffer[seq++] = (byte)(v >>> 40);
		writeBuffer[seq++] = (byte)(v >>> 32);
		writeBuffer[seq++] = (byte)(v >>> 24);
		writeBuffer[seq++] = (byte)(v >>> 16);
		writeBuffer[seq++] = (byte)(v >>>  8);
		writeBuffer[seq++] = (byte)(v >>>  0);
		count = seq;
	}

	/**
	 * 写出一个整数
	 * @param n 整数
	 * @throws IOException
	 */
	public void writeInt(int n) throws IOException {
		if (n >= 0) {
			if (n <= 0x0F) {
				write(INT4 | n);
			} else if (n <= 0x0FFF) {
				write(INT12 | (n >>> 8));
				write(n & 0xFF);
			} else if (n <= 0xFFFF) {
				write(INT16);
				write(n >>> 8);
				write(n & 0xFF);
			} else {
				write(INT32);
				write(n >>> 24);
				write((n >>> 16) & 0xFF);
				write((n >>>  8) & 0xFF);
				write(n & 0xFF);
			}
		} else {
			write(INT32);
			write(n >>> 24);
			write((n >>> 16) & 0xFF);
			write((n >>>  8) & 0xFF);
			write(n & 0xFF);
		}
	}

	private void writeTimestamp(java.util.Date dt) throws IOException {
		long t = dt.getTime();
		if (t % 1000 == 0) {
			long v = t / 1000;
			if (v < 0) {
				v = -v;
				if (v <= 0xFFFFFFFFL) {
					write(DATETIME33);
					write((int)(v >>> 24));
					write((int)(v >>> 16));
					write((int)(v >>>  8));
					write((int)(v >>>  0));
					return;
				}
			} else {
				if (v <= 0xFFFFFFFFL) {
					write(DATETIME32);
					write((int)(v >>> 24));
					write((int)(v >>> 16));
					write((int)(v >>>  8));
					write((int)(v >>>  0));
					return;
				}
			}
		}

		write(DATETIME64);
		writeLong64(t);
	}

	private void writeDate(java.sql.Date date) throws IOException {
		long v = date.getTime();
		if (v >= BASEDATE) {
			// 精确到天
			int d = (int)((v - BASEDATE) / 86400000);
			if (d > 0xFFFF) {
				if (d > 0xFFFFFF) {
					//throw new RQException("Invalid date: " + date);
					write(DATE64);
					writeLong64(v);
				} else {
					write(DATE24);
					write((d >>> 16));
					write((d >>>  8) & 0xFF);
					write(d & 0xFF);
				}
			} else {
				write(DATE16);
				write(d >>> 8);
				write(d & 0xFF);
			}
		} else {
			// 精确到秒
			long d = (BASEDATE - v) / 1000;
			if (d > 0xFFFFFFFFL) {
				//throw new RQException("Invalid date: " + date);
				write(DATE64);
				writeLong64(v);
			} else {
				write(DATE32);
				writeInt32((int)d);
			}
		}
	}

	private void writeTime(java.sql.Time time) throws IOException {
		int t = (int)((time.getTime() - BASETIME) % 86400000);
		if (t < 0) t += 86400000;

		if (t % 1000 == 0) {
			t /= 1000;
			if (t > 0xFFFF) {
				write(TIME17);
				write((t >>> 8) & 0xFF);
				write(t & 0xFF);
			} else {
				write(TIME16);
				write(t >>> 8);
				write(t & 0xFF);
			}
		} else {
			write(TIME32);
			write(t >>> 24);
			write((t >>> 16) & 0xFF);
			write((t >>>  8) & 0xFF);
			write(t & 0xFF);
		}
	}
	
	private void writeRecord(BaseRecord r) throws IOException {
		write(RECORD);
		String []names = r.getFieldNames();
		int fcount = names.length;
		writeInt(fcount);
		for (int i = 0; i < fcount; ++i) {
			writeString(names[i]);
		}
		
		Object []vals = r.getFieldValues();
		for (int f = 0; f < fcount; ++f) {
			writeObject(vals[f]);
		}
	}

	private void writeSequence(Sequence seq) throws IOException {
		IArray mems = seq.getMems();
		int len = mems.size();

		DataStruct ds = seq.dataStruct();
		if (ds == null) {
			write(SEQUENCE);
			writeInt(len);
			for (int i = 1; i <= len; ++i) {
				writeObject(mems.get(i));
			}
		} else {
			write(TABLE);
			String []names = ds.getFieldNames();
			int fcount = names.length;
			writeInt(fcount);
			for (int i = 0; i < fcount; ++i) {
				writeString(names[i]);
			}

			writeInt(len);
			for (int i = 1; i <= len; ++i) {
				BaseRecord r = (BaseRecord)mems.get(i);
				Object []vals = r.getFieldValues();
				for (int f = 0; f < fcount; ++f) {
					writeObject(vals[f]);
				}
			}
		}
	}

	/**
	 * 写出一个对象
	 * @param obj 对象
	 * @throws IOException
	 */
	public void writeObject(Object obj) throws IOException {
		if (obj == null) {
			write(NULL);
		} else if (obj instanceof String) {
			writeString((String)obj);
		} else if (obj instanceof Integer) {
			writeInt(((Number)obj).intValue());
		} else if (obj instanceof Double) {
			writeDouble(((Number)obj).doubleValue());
		} else if (obj instanceof BigDecimal) {
			writeDecimal((BigDecimal)obj);
		} else if (obj instanceof Long) {
			writeLong(((Number)obj).longValue());
		} else if (obj instanceof java.sql.Date) {
			writeDate((java.sql.Date)obj);
		} else if (obj instanceof java.sql.Time) {
			writeTime((java.sql.Time)obj);
		} else if (obj instanceof java.util.Date) { // java.sql.Timestamp
			writeTimestamp((java.util.Date)obj);
		} else if (obj instanceof Boolean) {
			if (((Boolean)obj).booleanValue()) {
				write(TRUE);
			} else {
				write(FALSE);
			}
		} else if (obj instanceof BigInteger) {
			writeDecimal((BigInteger)obj);
		} else if (obj instanceof Float) {
			writeDouble(((Number)obj).doubleValue());
		} else if (obj instanceof Number) { // Byte  Short
			writeInt(((Number)obj).intValue());
		} else if (obj instanceof Sequence) {
			writeSequence((Sequence)obj);
		} else if (obj instanceof BaseRecord) {
			writeRecord((BaseRecord)obj);
		} else if (obj instanceof byte[]) {
			write(BLOB);
			writeBytes((byte[])obj);
		} else if (obj instanceof SerialBytes) {
			SerialBytes sb = (SerialBytes)obj;
			int len = sb.length();
			if (len < 16) {
				write(SERIALBYTES | len);
			} else {
				// 0表示长度16
				write(SERIALBYTES);
			}
			
			write(sb.toByteArray());
		} else if (obj instanceof AvgValue) {
			write(AVG);
			((AvgValue)obj).writeData(this);
		} else {
			throw new RQException("error type: " + obj.getClass().getName());
		}
	}
}
