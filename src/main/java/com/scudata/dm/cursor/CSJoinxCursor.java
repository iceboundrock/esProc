package com.scudata.dm.cursor;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.*;
import com.scudata.dm.op.Derive;
import com.scudata.dm.op.DiffJoin;
import com.scudata.dm.op.FilterJoin;
import com.scudata.dm.op.Join;
import com.scudata.dm.op.New;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.resources.EngineMessage;
import com.scudata.util.CursorUtil;
import com.scudata.util.Variant;

/**
 * �α�joinx�࣬�����ǹ鲢��
 * �α���һ���ɷֶμ��ļ�f��ʵ��T��join���㣬f/T�����ж���
 * ���α갴��f/T�ķֶ���Ϣд����������ʱ�ļ�
 * @author 
 *
 */
public class CSJoinxCursor extends ICursor {
	public static final String SEQ_FIELDNAME = "rq_csjoinx_seq_";
	private boolean hasU;//true:������ԭ��
	private boolean hasO;//true:������ԭ��
	private boolean isEnd;
	private boolean hasSeq = false;//�Ƿ�׷�ӹ�����ֶ�
	private boolean needOrgName = false;//�Ƿ���fname����
	private int orgFieldsCount;//fnameԭ��¼���ֶ���
	private int seqIndex;//���λ��
	private ICursor srcCursor;//Դ�α�
	private SyncReader []fileOrTable;//ά��
	private Expression [][]fields;//��ʵ���ֶ�
	private Expression [][]keys;//ά���ֶ�
	private Expression [][]newExps;//�µı��ʽ
	private String [][]newNames;//�µı��ʽ����
	private String option;
	private String fname;
	private int n;//����������
	
	//���ڼ���
	private transient ICursor cursor;//ά���α�
	private transient int fileIndex = 0;
	private transient int fileSize;
	private transient ICursor fileCursor;//��ʱ�ļ��α�
	private transient ICursor []fileCursors;//��ʱ�ļ��α�����
	private transient Sequence []table;//��ǰ����
	private transient ICursor sortCursor;
	
	//�������һ�μ���
	private SyncReader lastReader;
	private transient Expression [][]lastFields;//��ʵ���ֶ�
	private transient Expression [][]lastKeys;//ά���ֶ�
	private transient Expression [][]lastNewExps;//�µı��ʽ
	private transient String [][]lastNewNames;//�µı��ʽ����

	/**
	 * 
	 * @param cursor 		��ʵ��Դ�α�
	 * @param fileOrTable	ά��
	 * @param fields		��ʵ���ֶ�
	 * @param keys			ά���ֶ�
	 * @param exps			�µı��ʽ
	 * @param names			�µı��ʽ���ֶ���
	 * @param ctx
	 * @param option
	 * @param n				ά��ÿ�εĻ�������С
	 */
	public CSJoinxCursor(ICursor cursor, SyncReader []fileOrTable, Expression [][]fields, Expression [][]keys, 
			Expression [][]exps, String [][]names, String fname, Context ctx, String option, int n) {
		srcCursor = cursor;
		this.fileOrTable = fileOrTable;
		this.fields = fields;
		this.keys = keys;
		this.newExps = exps;
		this.newNames = names;
		this.ctx = ctx;
		this.option = option;
		this.n = n;
		this.fname = fname;
		
		//���newNames����null������newExps���
		for (int i = 0, len = newExps.length; i < len; i++) {
			String[] arr = newNames[i];
			for (int j = 0, len2 = arr.length; j < len2; j++) {
				if (arr[j] == null) {
					arr[j] = newExps[i][j].getFieldName();
				}
			}
		}
		
		if (option != null) {
			if (option.indexOf('u') != -1) hasU = true;
			if (option.indexOf('o') != -1) hasO = true;
		}
		
		table = new Sequence[1];
	}

	/**
	 * ���м���ʱ��Ҫ�ı�������
	 * �̳�������õ��˱��ʽ����Ҫ�������������½������ʽ
	 */
	public void resetContext(Context ctx) {
		if (this.ctx != ctx) {
			newExps = Operation.dupExpressions(newExps, ctx);
			super.resetContext(ctx);
		}
	}

	private void init() {
		ICursor cs = srcCursor;
		int fcount = fileOrTable.length;
		
		if (hasO) {
			if (fcount > 1 || !hasU) {
				//������ֻ���û��@u
				option = option.replace("o", "");
				needOrgName = true;
			}
		}
		
		//backup oplist
		ArrayList<Operation> opListBk = opList;
		opList = null;
		
		for (int i = 0; i < fcount; i++) {
			Sequence values = fileOrTable[i].getValues();//ÿ�ε�����ֵ

			boolean needSeq = hasU ? false : i == 0;
			if (needSeq && (!hasSeq)) {
				hasSeq = true;
				Derive newOp = new Derive( new Expression[]{new Expression("0L")}, new String[]{CSJoinxCursor.SEQ_FIELDNAME}, null);
				cs.addOperation(newOp, ctx);
				Sequence seq = cs.peek(1);
				if (seq == null || seq.length() == 0) {
					isEnd = true;
					return;
				}
				seqIndex = seq.dataStruct().getFieldCount() - 1;//������ŵ�λ��
			}
			
			if (needOrgName && i == 0) {
				//�����Ҫ��fname
				if (hasSeq) {
					orgFieldsCount = seqIndex;
				} else {
					Sequence seq = cs.peek(1);
					if (seq == null || seq.length() == 0) {
						isEnd = true;
						return;
					}
					orgFieldsCount = seq.dataStruct().getFieldCount();
				}
			}
			
			boolean hasData = cursorToFiles(cs, fields[i], values, needSeq);
			if (!hasData) {
				isEnd = true;
				return;
			}
			
			//����join
			FileObject tempFile = null;
			BFileCursor fCursor = null;
			try {
				if (i == fcount - 1) {
					fileIndex = 0;
					fileSize = fileCursors.length;
					lastReader = fileOrTable[i];
					lastFields = new Expression[][] { this.fields[i] };
					lastKeys = new Expression[][] { this.keys[i] };
					lastNewExps = new Expression[][] { this.newExps[i] };
					lastNewNames = new String[][] { this.newNames[i] };
					fileCursor = null;
					if (!hasU) {
						processSeqField(lastNewNames[0]);
						sortCursor = sortBySeq();
						if (sortCursor == null) {
							isEnd = true;
							return;
						} else {
							isEnd = false;
						}
					} else {
						if (needOrgName) {
							sortCursor = toFileCursor();
						}
					}
					opList = opListBk;
					return;//���һ������������join������������дһ��
				}
				
				tempFile = FileObject.createTempFileObject();
				cs = new BFileCursor(tempFile, null, "x", ctx);
				tempFile.setFileSize(0);
				Sequence[] seqs = new Sequence[1];
				Expression[][] fields = new Expression[][] { this.fields[i] };
				Expression[][] keys = new Expression[][] { this.keys[i] };
				Expression[][] newExps = new Expression[][] { this.newExps[i] };
				String[][] newNames = new String[][] { this.newNames[i] };
				
				boolean hasNewExps = false;
				if (newExps[0] != null && newExps[0].length > 0) {
					hasNewExps = true;
				}
				boolean isIsect = false, isDiff = false;
				if (!hasNewExps && option != null) {
					if (option.indexOf('i') != -1) {
						isIsect = true;
					} else if (option.indexOf('d') != -1) {
						isDiff = true;
					}
				}
				
				for (int j = 0, filesCount = fileCursors.length; j < filesCount; j++) {
					ICursor c = fileCursors[j];
					if (c == null) {
						fileOrTable[i].getData(j);
						continue;
					}
					seqs[0] = fileOrTable[i].getData(j);
					fCursor = (BFileCursor) c;
					
					Operation op;
					if (isIsect) {
						op = new FilterJoin(null, fields, seqs, keys, option);
					} else if (isDiff) {
						op = new DiffJoin(null, fields, seqs, keys, option);
					} else {
						op = new Join(null, fields, seqs, keys, newExps, newNames, option);
					}
					fCursor.addOperation(op, ctx);

					Sequence table = fCursor.fetch(FETCHCOUNT);
					while (table != null && table.length() != 0) {
						tempFile.exportSeries(table, "ab", null);
						table = fCursor.fetch(FETCHCOUNT);
					}
				} 
			} catch (Exception e) {
				if (fCursor != null) {
					fCursor.close();
				}
				if (tempFile != null && tempFile.isExists()) {
					tempFile.delete();
				}
				if (e instanceof RQException) {
					throw (RQException)e;
				} else {
					throw new RQException(e.getMessage(), e);
				}
			}
			
			if (tempFile.size() == 0) {
				tempFile.delete();
				isEnd = true;
				return;
			}
		}
	}
	
	/**
	 * ��ȡһ����ʱ�ļ������ݳ�������join������
	 * @return
	 */
	private boolean loadFile() {
		boolean hasNewExps = false;
		if (lastNewExps[0] != null && lastNewExps[0].length > 0) {
			hasNewExps = true;
		}
		boolean isIsect = false, isDiff = false;
		if (!hasNewExps && option != null) {
			if (option.indexOf('i') != -1) {
				isIsect = true;
			} else if (option.indexOf('d') != -1) {
				isDiff = true;
			}
		}
		
		while (fileIndex < fileSize) {
			if (fileCursors[fileIndex] == null) {
				lastReader.getData(fileIndex);
				fileIndex++;
				continue;
			}
			table[0] = null;
			table[0] = lastReader.getData(fileIndex);
			fileCursor = fileCursors[fileIndex];
			
			Operation op;
			if (isIsect) {
				op = new FilterJoin(null, lastFields, table, lastKeys, option);
			} else if (isDiff) {
				op = new DiffJoin(null, lastFields, table, lastKeys, option);
			} else {
				op = new Join(fname, lastFields, table, lastKeys, lastNewExps, lastNewNames, option);
			}
			fileCursor.addOperation(op, ctx);
			fileIndex++;
			return true;
		}
		isEnd = true;
		return false;
		
	}
	
	protected Sequence get(int n) {
		if (fileCursors == null && sortCursor == null) {
			init();
		}
		if (isEnd || n < 1) return null;
		
		if (sortCursor != null) {
			if (needOrgName) {
				Sequence data = sortCursor.fetch(n);
				if (data == null || data.length() == 0) {
					return null;
				}
				DataStruct ds = data.dataStruct();
				DataStruct newDs;
				DataStruct subDs;
				String []field = ds.getFieldNames();
				String []newField = new String[field.length + 1 - orgFieldsCount];
				String []subField = new String[orgFieldsCount];
				
				newField[0] = fname;
				System.arraycopy(field, orgFieldsCount, newField, 1, newField.length - 1);
				System.arraycopy(field, 0, subField, 0, orgFieldsCount);
				newDs = new DataStruct(newField);
				subDs = new DataStruct(subField);
				
				int newFieldLen = newField.length;
				int subFieldLen = orgFieldsCount;
				int len = data.length();
				Sequence result = new Sequence(len);
				for (int i = 1; i <= len; ++i) {
					com.scudata.dm.Record rec = (com.scudata.dm.Record) data.get(i);
					com.scudata.dm.Record newRec = new com.scudata.dm.Record(newDs);
					com.scudata.dm.Record subRec = new com.scudata.dm.Record(subDs);
					for (int j = 0; j < subFieldLen; j++) {
						subRec.setNormalFieldValue(j, rec.getNormalFieldValue(j));
					}
					for (int j = 0; j < newFieldLen - 1; j++) {
						newRec.setNormalFieldValue(j + 1, rec.getNormalFieldValue(j + subFieldLen));
					}
					newRec.setNormalFieldValue(0, subRec);
					result.add(newRec);
				}
				return result;
			} else {
				return sortCursor.fetch(n);
			}
		}
		

		Sequence newTable = null;
		if (fileCursor == null) {
			if (!loadFile()) {
				return null;
			}
		}

		Sequence data = null;
		int count = 0;
		try {
			while (count < n) {
				data = fileCursor.fetch(n - count);
				
				if (data == null || data.length() == 0) {
					fileCursor.close();
					if (!loadFile()) {
						return newTable;
					}
				} else {
					if (newTable == null) {
						newTable = data;
					} else {
						newTable.addAll(data);
					}
					count += data.length();
				}

			} 
		} catch (Exception e) {
			close();
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		if (newTable.length() > 0) {
			return newTable;
		} else {
			return null;
		}
	}

	protected long skipOver(long n) {
		if (sortCursor != null) {
			return sortCursor.skip(n);
		}
		if (isEnd || n < 1) return 0;

		if (fileCursor == null) {
			if (!loadFile()) {
				return 0;
			}
		}

		long count = 0;
		while(count < n) {
			long c = fileCursor.skip(n - count);
			if (c == 0) {
				if (!loadFile()) {
					return count;
				}
			} else {
				count += c;
			}
			
		}
		
		return count;
	}

	public synchronized void close() {
		super.close();
		if (fileCursor != null) {
			fileCursor.close();
			fileCursor = null;
		}
		
		fileCursors = null;
		
		if (sortCursor != null) {
			sortCursor.close();
			sortCursor = null;
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		srcCursor.close();
		isEnd = true;
	}
	
	/**
	 * �����α�
	 * @return �����Ƿ�ɹ���true���α���Դ�ͷ����ȡ����false�������Դ�ͷ����ȡ��
	 */
	public boolean reset() {
		super.close();
		if (fileCursor != null) {
			fileCursor.close();
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		if (sortCursor != null) {
			sortCursor.close();
			sortCursor = null;
		}
		
		srcCursor.reset();
		isEnd = false;
		init();
		return true;
	}
	
	/**
	 * ���α������д������ʱ�ļ�
	 * @param cursor �α�
	 * @param fields д���ֶ�
	 * @param values �ֶ���Ϣ
	 * @param needSeq �Ƿ񱣳������Ϣ��Ϊ��ʵ��ԭ��
	 * @return
	 */
	private boolean cursorToFiles(ICursor cursor, Expression []fields, Sequence values, boolean needSeq) {
		final int fetchCount = n;
		Sequence table = cursor.fetch(fetchCount);
		if (table == null || table.length() == 0) {
			return false;
		}
		DataStruct ds = table.dataStruct();
		setDataStruct(ds);
		int seqIndex = this.seqIndex;

		//��ȡ�ֶ�index
		int fcount = fields.length;
		int []findex = new int[fcount];
		for (int f = 0; f < fcount; ++f) {
			findex[f] = ds.getFieldIndex(fields[f].toString());
			if (findex[f] == -1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(fields[f] + mm.getMessage("ds.fieldNotExist"));
			}
		}
		
		Object []curVals = new Object[fcount];
		int segCount = values.length();
		Sequence []outSeqs = new Sequence[segCount + 1];
		FileObject []files = new FileObject[segCount + 1];
		fileCursors = new BFileCursor[segCount + 1];
		for (int i = 0; i <= segCount; i++) {
			files[i] = FileObject.createTempFileObject();
			files[i].setFileSize(0);
			fileCursors[i] = new BFileCursor(files[i], null, "x", ctx);
			outSeqs[i] = new Sequence();
		}
		
		long seq = 0;
		try {
			while (table != null && table.length() > 0) {
				//����ȡ�����ļ�¼���͵�ÿ����ʱoutSeq��
				int len = table.length();
				for (int i = 1; i <= len; ++i) {
					BaseRecord record = (BaseRecord) table.getMem(i);
					for (int f = 0; f < fcount; ++f) {
						curVals[f] = record.getNormalFieldValue(findex[f]);
					}
					
					if (needSeq) {
						record.setNormalFieldValue(seqIndex, ++seq);
					}
					
					int low = 1, high = segCount, middle = 0;
					if (segCount != 0) {
						while (low <= high) {
							middle = (low + high) / 2;
							Object[] vals = (Object[]) values.getMem(middle);
							int cmp = Variant.compareArrays(vals, curVals);
							if(cmp > 0) {
								high = middle - 1;
							} else if(cmp < 0) {
								low = middle + 1;
							} else {
								break;
							}
						}
						Object[] vals = (Object[]) values.getMem(middle);
						if (Variant.compareArrays(vals, curVals) > 0) middle--;
					} else {
						middle = 0;
					}
					outSeqs[middle].add(record);
					record = null;
					
					
				}
				
				//��outSeqs��ļ�¼д���ļ�
				for (int i = 0; i <= segCount; i++) {
					files[i].exportSeries(outSeqs[i], "ab", null);
					outSeqs[i].clear();
				}
				table = null;
				table = cursor.fetch(fetchCount);

			}
		} catch (Exception e) {
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		for (int i = 0; i <= segCount; i++) {
			FileObject f = files[i];
			if (f.size() == 0) {
				fileCursors[i].close();
				files[i] = null;
				fileCursors[i] = null;
			}
		}
		return true;
	}

	private ICursor toFileCursor() {
		ICursor cursor = this;
		Sequence table = cursor.fetch(ICursor.FETCHCOUNT);
		if (table == null || table.length() == 0) {
			return null;
		}

		FileObject file = FileObject.createTempFileObject();
		try {
			while (table != null && table.length() > 0) {
				file.exportSeries(table, "ab", null);
				table = cursor.fetch(ICursor.FETCHCOUNT);
			}
		} catch (Exception e) {
			if (e instanceof RQException) {
				throw (RQException)e;
			} else {
				throw new RQException(e.getMessage(), e);
			}
		}
		return new BFileCursor(file, null, "x", ctx);
	}

	private void processSeqField(String []newNames) {
		String []oldNames = dataStruct.getFieldNames();
		int oldColCount = oldNames.length;
		
		// �ϲ����ֶ�
		int newColCount = oldColCount + newNames.length;
		String []totalNames = new String[newColCount];
		System.arraycopy(oldNames, 0, totalNames, 0, oldColCount);
		System.arraycopy(newNames, 0, totalNames, oldColCount, newNames.length);
		setDataStruct(dataStruct.create(totalNames));

	}
	
	private ICursor sortBySeq() {
		int seqIndex = this.seqIndex;
		DataStruct ds = getDataStruct();
		
		//������
		Expression []exps = new Expression[]{new Expression(CSJoinxCursor.SEQ_FIELDNAME)};
		ICursor cs = CursorUtil.sortx(this, exps, ctx, this.n, null);
		
		//ȥ�����
		String []oldFields = ds.getFieldNames();
		int oldLen = oldFields.length;
		int newLen = oldLen - 1;
		String []newFields = new String[newLen];
		Expression []expressions = new Expression[newLen];
		int c = 0;
		for (int i = 0; i < oldLen; i++) {
			if (i == seqIndex) {
				continue;
			} else {
				expressions[c] = new Expression(ctx, "#" + (i + 1));
				newFields[c] = oldFields[i];
			}
			c++;
		}
		New newOp = new New(expressions, newFields, null);
		if (cs != null) cs.addOperation(newOp, ctx);
		return cs;
	}
	
	protected void finalize() throws Throwable {
		close();
	}
}
