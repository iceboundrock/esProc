package com.scudata.dm.cursor;

import java.io.IOException;
import com.scudata.array.IArray;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.DataStruct;
import com.scudata.dm.LineImporter;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dw.BufferReader;

/**
 * 文件排序结果的游标
 * 用于f.sortx
 * @author LW
 *
 */
public class BFileSortxCursor extends ICursor {
	private int bytesIndex;
	private DataStruct fileDataStruct;
	private ICursor cursor;
	private boolean isClosed = false;
	private LineImporter importer;
	
	/**
	 * 集文件sortx结果游标
	 * @param cursor
	 * @param bytesIndex
	 * @param fileDataStruct
	 */
	public BFileSortxCursor(ICursor cursor, int bytesIndex, DataStruct fileDataStruct) {
		this(cursor, bytesIndex, fileDataStruct, null);
	}

	/**
	 * 文本文件sortx结果游标
	 * @param cursor
	 * @param bytesIndex
	 * @param fileDataStruct
	 * @param importer
	 */
	public BFileSortxCursor(ICursor cursor, int bytesIndex, DataStruct fileDataStruct, LineImporter importer) {
		this.cursor = cursor;
		this.bytesIndex = bytesIndex;
		this.fileDataStruct = fileDataStruct;
		this.importer = importer;
	}
	
	protected Sequence get(int n) {
		if (isClosed || n < 1) {
			return null;
		}
		
		ICursor cursor = this.cursor;
		DataStruct ds = this.fileDataStruct;
		int fcount = ds.getFieldCount(); 
		int bytesIndex = this.bytesIndex;
		
		Sequence data = cursor.fetch(n);
		if (data == null || data.length() == 0) {
			isClosed = true;
			return null;
		}
		
		int len = data.length();
		Sequence result = new Sequence(len);
		IArray mems = data.getMems();
		BufferReader reader = new BufferReader(null, new byte[] {0});
		
		try {
			for (int i = 1; i <= len; i++) {
				BaseRecord record = (BaseRecord) mems.get(i);
				byte[] bytes = (byte[]) record.getNormalFieldValue(bytesIndex);
				reader.buffer = bytes;
				reader.index = 0;
				reader.count = bytes.length;
				
				if (importer != null) {
					Object[] values = importer.readLine(bytes);
					Record rec = new Record(ds, values);
					result.add(rec);
				} else {
					Record rec = new Record(ds);
					Object[] values = rec.getFieldValues();
					for (int f = 0; f < fcount; ++f) {
						values[f] = reader.readObject();
					}
					result.add(rec);
				}
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
		
		return result;
	}

	protected long skipOver(long n) {
		return cursor.skipOver(n);
	}
	
	public void close() {
		super.close();
		cursor.close();
		isClosed = true;
		
		if (importer != null) {
			try {
				importer.close();
			} catch (IOException e) {
			}
			importer = null;
		}
	}
}
