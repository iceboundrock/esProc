package com.scudata.excel;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.scudata.common.CellLocation;
import com.scudata.common.Matrix;
import com.scudata.common.RQException;
import com.scudata.dm.BaseRecord;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.resources.AppMessage;
import com.scudata.util.Variant;

/**
 * Sheet object of memory mode
 *
 */
public class SheetXls extends SheetObject {
	/**
	 * FileXls object
	 */
	private FileXls xlsFile;
	/**
	 * Sheet object
	 */
	private Sheet sheet = null;
	/**
	 * DataFormat
	 */
	private DataFormat dataFormat;
	/**
	 * FormulaEvaluator
	 */
	private FormulaEvaluator evaluator;
	/**
	 * Whether to write the title line
	 */
	private boolean writeTitle;
	/**
	 * Whether to append data
	 */
	private boolean isAppend;
	/**
	 * Mapping of graph coordinates and graph data
	 */
	private Map<String, byte[]> graphMap = null;

	/**
	 * Row and cell styles
	 */
	private RowAndCellStyle dataStyle;
	/**
	 * Column styles
	 */
	private CellStyle[] colStyles;
	/**
	 * Styles used when exporting
	 */
	private HashMap<Integer, CellStyle> styles = new HashMap<Integer, CellStyle>();
	/**
	 * After the first row of data is written out, save the style and use it
	 * directly later.
	 */
	private boolean resetDataStyle = true;

	/**
	 * Constructor
	 * 
	 * @param xlsFile
	 *            FileXls
	 * @param sheet
	 *            Sheet
	 * @param dataFormat
	 *            DataFormat
	 * @param isXls
	 *            Whether xls format
	 * @param evaluator
	 *            FormulaEvaluator
	 */
	public SheetXls(FileXls xlsFile, Sheet sheet, DataFormat dataFormat,
			boolean isXls, FormulaEvaluator evaluator) {
		this.xlsFile = xlsFile;
		this.sheet = sheet;
		this.dataFormat = dataFormat;
		this.isXls = isXls;
		this.evaluator = evaluator;
		sheetInfo = new SheetInfo(sheet.getSheetName());
	}

	/**
	 * Get the maximum number of rows
	 * 
	 * @return
	 */
	public int getMaxRowCount() {
		if (isXls) {
			return IExcelTool.MAX_XLS_LINECOUNT;
		}
		return IExcelTool.MAX_XLSX_LINECOUNT;
	}

	public int getMaxColCount() {
		if (isXls) {
			return IExcelTool.MAX_XLS_COLCOUNT;
		}
		return IExcelTool.MAX_XLSX_COLCOUNT;
	}

	/**
	 * Move line
	 * 
	 * @param startRow
	 *            Start row
	 * @param endRow
	 *            End row
	 * @param n
	 *            Number of lines
	 * @param copyRowHeight
	 *            Whether to copy row height
	 * @param resetOriginalRowHeight
	 *            Whether to reset the original row height
	 */
	public synchronized void shiftRows(int startRow, int endRow, int n,
			boolean copyRowHeight, boolean resetOriginalRowHeight) {
		sheet.shiftRows(startRow, endRow, n, copyRowHeight,
				resetOriginalRowHeight);
	}

	/**
	 * Write a row of data
	 * 
	 * @param row
	 *            Row number
	 * @param line
	 *            Row datas
	 */
	public void writeLine(int row, Object[] line) {
		writeLine(row, line, 0, line == null ? 0 : line.length);
	}

	/**
	 * Write a row of data
	 * 
	 * @param currRow
	 *            Row number
	 * @param items
	 *            Row datas
	 * @param startCol
	 *            Start column
	 * @param endCol
	 *            End column
	 */
	public void writeLine(int currRow, Object[] items, int startCol, int endCol) {
		if (sheet == null || items == null || items.length == 0)
			return;
		Row row = null;
		if (currRow <= sheet.getLastRowNum())
			row = sheet.getRow(currRow);
		if (row == null)
			row = sheet.createRow(currRow);
		RowAndCellStyle rowAndCellStyle = null;
		CellStyle[] cellStyles = null;
		CellStyle rowStyle = null;
		if (isAppend)
			if (writeTitle) {
				rowAndCellStyle = getRowStyle(currRow);
			} else {
				rowAndCellStyle = dataStyle;
			}
		if (rowAndCellStyle != null) {
			rowStyle = rowAndCellStyle.rowStyle;
			cellStyles = rowAndCellStyle.cellStyles;
		}
		writeRowData(row, items, startCol, endCol, rowStyle, cellStyles);
		if (writeTitle) {
			writeTitle = false;
		} else {
			if (isAppend && resetDataStyle) {
				resetDataStyle(row);
			}
		}
		currRow++;
	}

	/**
	 * Read datas of a row
	 * 
	 * @param currRow
	 *            Current row
	 * @return
	 */
	public Object[] readLine(int currRow) {
		if (currRow > sheet.getLastRowNum())
			return null;
		Row row = sheet.getRow(currRow);
		currRow++;
		return ExcelUtils.getRowData(row, dataFormat, evaluator);
	}

	public int totalCount() {
		return sheet.getLastRowNum() + 1;
	}

	public int getStartRow(boolean hasTitle) {
		try {
			int lastRow = sheet.getLastRowNum();
			if (lastRow < 0) {
				return 0;
			}
			/*
			 * Find the last line with content. If @t this line is used as the
			 * header line. If there is no @t: there is a blank line after it,
			 * use the blank line style, otherwise use this line style.
			 */
			int lastContentRow = -1;
			Row row;
			int colCount = 0;
			for (int r = lastRow; r >= 0; r--) {
				row = sheet.getRow(r);
				if (row == null)
					continue;
				int lastCol = row.getLastCellNum();
				colCount = Math.max(lastCol, colCount);
				if (!ExcelUtils.isEmptyRow(row, lastCol)) {
					lastContentRow = r;
					break;
				}
			}
			int startRow;
			// Determine the header row and data row.
			if (hasTitle) {
				if (lastContentRow == -1) { // No rows with content found
					lastContentRow = 0;
				}
				startRow = lastContentRow;
				dataStyle = getRowStyle(lastContentRow + 1);
			} else {
				if (lastContentRow == -1) { // No rows with content found
					startRow = 0;
				} else {
					/*
					 * Start writing on the next line of the first line with
					 * content
					 */
					startRow = lastContentRow + 1;
				}
				if (lastContentRow < lastRow) {
					/*
					 * If there is a row after the content row, use the next
					 * blank row format as the data row format.
					 */
					dataStyle = getRowStyle(lastContentRow + 1);
				} else {
					/*
					 * The last row with content is the data row.
					 */
					dataStyle = getRowStyle(lastContentRow);
				}
			}
			colStyles = new CellStyle[colCount];
			for (int c = 0; c < colCount; c++) {
				colStyles[c] = sheet.getColumnStyle(c);
			}
			return startRow;
		} catch (Exception e) {
			/*
			 * Even if you can't read it, ensure that the export is normal and
			 * type out the error message.
			 */
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Import the excel sheet and return the sequence object.
	 * 
	 * @param fields
	 *            Field names
	 * @param startRow
	 *            Start row
	 * @param endRow
	 *            End row
	 * @param opt
	 *            Options
	 * @return
	 * @throws Exception
	 */
	public Object xlsimport(String[] fields, int startRow, int endRow,
			String opt) throws IOException {
		IXlsImporter xlsImporter = new IXlsImporter() {

			public Object[] readLine(int row, boolean isN, boolean isW)
					throws IOException {
				Object[] line = SheetXls.this.readLine(row);
				if (line == null)
					return null;
				if (isN) {
					for (int i = 0; i < line.length; i++) {
						line[i] = ExcelUtils.trim(line[i], isW);
					}
				}
				return line;
			}

			public int totalCount() {
				return SheetXls.this.totalCount();
			}

			public void setStartRow(int startRow) {
			}

		};
		return ExcelTool.fileXlsImport(fields, startRow, endRow, opt,
				xlsImporter);
	}

	/**
	 * Use option @w when importing excel file.
	 * 
	 * @param startRow
	 *            Start row
	 * @param endRow
	 *            End row
	 * @param opt
	 *            The options
	 * @return
	 * @throws IOException
	 */
	// public Sequence xlsImportW(int startRow, int endRow, String opt)
	// throws IOException {
	// boolean isP = opt != null && opt.indexOf("p") > -1;
	// boolean isN = opt != null && opt.indexOf("n") > -1;
	// Sequence seq = new Sequence();
	// Object[] line;
	// while (startRow <= endRow) {
	// line = readLine(startRow, isN, true);
	// if (line == null)
	// break;
	// startRow++;
	// Sequence subSeq = new Sequence(line.length);
	// for (Object data : line) {
	// subSeq.add(data);
	// }
	// seq.add(subSeq);
	// }
	// if (isP)
	// seq = ExcelUtils.transpose(seq);
	// return seq;
	// }

	/**
	 * Use option @s when importing excel file.
	 * 
	 * @param startRow
	 *            Start row
	 * @param endRow
	 *            End row
	 * @param opt
	 *            The option
	 * @return
	 * @throws IOException
	 */
	// public String xlsImportS(int startRow, int endRow, String opt)
	// throws IOException {
	// boolean isN = opt != null && opt.indexOf("n") != -1;
	// StringBuffer buf = new StringBuffer();
	// Object[] line;
	// boolean firstLine = true;
	// while (startRow <= endRow) {
	// line = readLine(startRow, isN, false);
	// if (line == null)
	// break;
	// startRow++;
	// if (firstLine) {
	// firstLine = false;
	// } else {
	// buf.append(ExcelTool.ROW_SEP);
	// }
	// for (int c = 0; c < line.length; c++) {
	// if (c > 0) {
	// buf.append(ExcelTool.COL_SEP);
	// }
	// buf.append(line[c] == null ? "" : line[c].toString());
	// }
	// }
	// return buf.toString();
	// }

	/**
	 * Read a row of data
	 * 
	 * @param isN
	 * @n
	 * @param isW
	 * @w
	 * @return
	 * @throws IOException
	 */
	// private Object[] readLine(int row, boolean isN, boolean isW)
	// throws IOException {
	// Object[] line = readLine(row);
	// if (line == null)
	// return null;
	// if (isN) {
	// for (int i = 0; i < line.length; i++) {
	// line[i] = ExcelUtils.trim(line[i], isW);
	// }
	// }
	// return line;
	// }

	/**
	 * Export excel sheet
	 * 
	 * @param series
	 *            Sequence to export
	 * @param exps
	 *            Field expressions
	 * @param names
	 *            Field names
	 * @param bTitle
	 *            Has title line
	 * @param isAppend
	 *            Whether to append
	 * @param startRow
	 *            Start row
	 * @param ctx
	 *            Context
	 * @throws IOException
	 */
	public void xlsexport(Sequence series, Expression[] exps, String[] names,
			int startRow, String opt, Context ctx) throws IOException {
		this.writeTitle = opt != null && opt.indexOf("t") != -1;
		this.isAppend = opt != null && opt.indexOf("a") != -1;

		IXlsExporter xlsExporter = new IXlsExporter() {

			public void writeLine(int row, Object[] items) throws IOException {
				SheetXls.this.writeLine(row, items);
			}
		};
		int[] rc = ExcelTool.fileXlsExport(series, exps, names, startRow, opt,
				ctx, xlsExporter);
		if (rc != null) {
			sheetInfo.setRowCount(Math.max(sheetInfo.getRowCount(), rc[0]));
			sheetInfo.setColCount(Math.max(sheetInfo.getColCount(), rc[1]));
		}
	}

	/**
	 * Export excel sheet
	 * 
	 * @param cursor
	 *            The cursor to export
	 * @param exps
	 *            Field expreessions
	 * @param names
	 *            Field names
	 * @param bTitle
	 *            Has title line
	 * @param isAppend
	 *            Whether to append export
	 * @param startRow
	 *            Start row
	 * @param ctx
	 *            Context
	 * @throws IOException
	 */
	public void xlsexport(ICursor cursor, Expression[] exps, String[] names,
			int startRow, String opt, Context ctx) throws IOException {
		this.writeTitle = opt != null && opt.indexOf("t") != -1;
		this.isAppend = opt != null && opt.indexOf("a") != -1;
		IXlsExporter xlsExporter = new IXlsExporter() {

			public void writeLine(int row, Object[] items) throws IOException {
				SheetXls.this.writeLine(row, items);
			}
		};
		int[] rc = ExcelTool.fileXlsExport(cursor, exps, names, startRow, opt,
				ctx, xlsExporter);
		if (rc != null) {
			sheetInfo.setRowCount(Math.max(sheetInfo.getRowCount(), rc[0]));
			sheetInfo.setColCount(Math.max(sheetInfo.getColCount(), rc[1]));
		}
	}

	/**
	 * Get cell ID and graph mapping
	 * 
	 * @return Key: Cell ID; Value: graph byte array
	 */
	private synchronized Map<String, byte[]> getGraphMap() {
		if (graphMap == null) {
			graphMap = new HashMap<String, byte[]>();
			if (sheet != null) {
				if (isXls) {
					Iterator it = sheet.getDrawingPatriarch().iterator();
					while (it.hasNext()) {
						Shape shape = (Shape) it.next();
						if (shape instanceof Picture) {
							Picture picture = (Picture) shape;
							PictureData pdata = picture.getPictureData();
							if (pdata != null) {
								ClientAnchor cAnchor = picture
										.getClientAnchor();
								String key = cAnchor.getRow1() + ROW_COL_SEP
										+ cAnchor.getCol1();
								graphMap.put(key, pdata.getData());
							}
						}
					}
				} else {
					if (sheet instanceof XSSFSheet) {
						ExcelVersionCompatibleUtilGetter.getInstance()
								.getSheetPictures((XSSFSheet) sheet, graphMap);
					}
				}
			}
		}
		return graphMap;
	}

	/**
	 * The separator of row and column in cell ID (Key of Graph mapping).
	 */
	private static final String ROW_COL_SEP = "_";

	/**
	 * Read data from excel cells
	 * 
	 * @param pos1
	 *            Excel grid position 1
	 * @param pos2
	 *            Excel grid position 2
	 * @param isGraph
	 *            Whether graph cell
	 * @param isW
	 *            Whether option @w
	 * @param isP
	 *            Whether option @p
	 * @param isN
	 *            Whether option @n
	 * @return
	 */
	public Object getCells(CellLocation pos1, CellLocation pos2,
			boolean isGraph, boolean isW, boolean isP, boolean isN) {
		try {
			int startRow = pos1.getRow() - 1;
			int startCol = pos1.getCol() - 1;
			if (isGraph) {
				return getCellGraph(startRow, startCol);
			}
			int endRow, endCol;
			if (pos2 != null) {
				endRow = pos2.getRow() - 1;
				endCol = pos2.getCol() - 1;
			} else { // pos2应该总不是null了
				endRow = startRow;
				endCol = startCol;
			}
			if (isW)
				return getCellsW(startRow, startCol, endRow, endCol, isP, isN);
			return getCells(startRow, startCol, endRow, endCol, isN);
		} catch (Exception ex) {
			throw new RQException(ex.getMessage(), ex);
		}
	}

	/**
	 * Read data from excel cells
	 * 
	 * @param startRow
	 *            Start row
	 * @param startCol
	 *            Start column
	 * @param endRow
	 *            End Row
	 * @param endCol
	 *            End column
	 * @param isN
	 *            Option @n
	 * @return
	 */
	private Object getCells(int startRow, int startCol, int endRow, int endCol,
			boolean isN) {
		Object[] line;
		int colCount = -1;
		StringBuffer buf = new StringBuffer();
		Object[] cutLine;
		for (int i = startRow; i <= endRow; i++) {
			if (i >= getMaxRowCount()) {
				break;
			}
			if (i > startRow) {
				buf.append(ExcelTool.ROW_SEP);
			}
			line = readLine(i);
			if (line == null || line.length == 0)
				continue;
			if (colCount == -1) {
				colCount = endCol - startCol + 1;
			}
			cutLine = new Object[colCount];
			for (int c = 0; c < colCount; c++) {
				if (startCol + c < line.length) {
					cutLine[c] = line[startCol + c];
					if (isN) {
						cutLine[c] = ExcelUtils.trim(cutLine[c], false);
					}
				}
			}
			for (int c = 0; c < cutLine.length; c++) {
				if (c > 0)
					buf.append(ExcelTool.COL_SEP);
				Object val = Variant.toExportString(cutLine[c]);
				if (val == null)
					val = "";
				buf.append(val);
			}
		}
		return buf.toString();
	}

	/**
	 * Read data from excel cells
	 * 
	 * @param startRow
	 *            Start row
	 * @param startCol
	 *            Start column
	 * @param endRow
	 *            End Row
	 * @param endCol
	 *            End column
	 * @param isP
	 *            Option @p
	 * @param isN
	 *            Option @n
	 * @return
	 */
	private Object getCellsW(int startRow, int startCol, int endRow,
			int endCol, boolean isP, boolean isN) {
		Object[] line;
		int colCount = -1;
		Sequence seq = new Sequence();
		Object[] cutLine;
		for (int i = startRow; i <= endRow; i++) {
			line = readLine(i);
			Sequence subSeq = new Sequence();
			if (line == null || line.length == 0) {
				seq.add(subSeq);
				continue;
			}
			if (colCount == -1) {
				colCount = endCol - startCol + 1;
			}
			cutLine = new Object[colCount];
			for (int c = 0; c < colCount; c++) {
				if (startCol + c < line.length) {
					cutLine[c] = line[startCol + c];
					if (isN) {
						cutLine[c] = ExcelUtils.trim(cutLine[c], true);
					}
				}
			}
			for (int c = 0; c < cutLine.length; c++) {
				subSeq.add(cutLine[c]);
			}
			seq.add(subSeq);
		}
		if (isP) {
			seq = ExcelUtils.transpose(seq);
		}
		return seq;
	}

	/**
	 * Write data to excel cells
	 * 
	 * @param pos1
	 *            Excel grid position 1
	 * @param pos2
	 *            Excel grid position 2
	 * @param content
	 *            Data to be exported
	 * @param isRowInsert
	 *            Insert export
	 * @param isGraph
	 *            Whether graph cell
	 */
	public void setCells(CellLocation pos1, CellLocation pos2, Object content,
			boolean isRowInsert, boolean isGraph) {
		int startRow = pos1.getRow() - 1;
		int startCol = pos1.getCol() - 1;
		if (isGraph) {
			setCellGraph(startRow, startCol, (byte[]) content);
			return;
		}
		int totalCount = totalCount();
		if (content instanceof Sequence) {
			Sequence seq = (Sequence) content;
			int rowCount = seq.length();
			if (isRowInsert) {
				// ：插入行后超出行数限制：{0}
				if (totalCount + rowCount - 1 > getMaxRowCount()) {
					throw new RQException("xlscell"
							+ AppMessage.get().getMessage(
									"filexls.morethanmax", getMaxRowCount()));
				}
				if (startRow < totalCount - 1) {
					shiftRows(startRow + 1, totalCount - 1, rowCount, false,
							false);
				}
				startRow += 1;
			}
			int endRow = startRow + rowCount;
			int lastCol = startCol;
			if (pos2 != null) {
				endRow = Math.min(pos2.getRow(), endRow);
			}
			int endCol;
			for (int r = startRow; r < endRow; r++) {
				if (r >= getMaxRowCount()) {
					break;
				}
				Object rowData = seq.get(r - startRow + 1);
				if (rowData == null) {
					continue;
				}
				Object[] line;
				if (rowData instanceof Sequence) {
					Sequence rowSeq = (Sequence) rowData;
					int colCount = rowSeq.length();
					if (colCount == 0)
						continue;
					endCol = startCol + colCount;
					line = new Object[colCount];
					for (int c = 0; c < colCount; c++) {
						line[c] = rowSeq.get(c + 1);
					}
				} else if (rowData instanceof BaseRecord) {
					BaseRecord record = (BaseRecord) rowData;
					line = record.getFieldValues();
					if (line == null || line.length == 0)
						continue;
					endCol = startCol + line.length;
				} else {
					line = new Object[1];
					line[0] = rowData;
					endCol = 1;
				}
				lastCol = Math.max(lastCol, endCol);
				writeLine(r, line, startCol, endCol);
			}
			sheetInfo.setRowCount(sheetInfo.getRowCount() + rowCount);
			sheetInfo.setColCount(Math.max(sheetInfo.getColCount(), lastCol));
		} else if (content instanceof Matrix) {
			Matrix matrix = (Matrix) content;
			int rowCount = matrix.getRowSize();
			if (isRowInsert) {
				if (totalCount + rowCount - 1 > getMaxRowCount()) {
					throw new RQException("xlscell"
							+ AppMessage.get().getMessage(
									"filexls.morethanmax", getMaxRowCount()));
				}
				if (startRow < totalCount - 1) {
					shiftRows(startRow + 1, totalCount - 1, rowCount, false,
							false);
				}
				startRow += 1;
			}
			int endRow = startRow + rowCount;
			int endCol = startCol + matrix.getColSize();
			if (pos2 != null) {
				int pos2Row = pos2.getRow();
				if (isRowInsert) {
					pos2Row++;
				}
				endRow = Math.min(pos2Row, endRow);
				endCol = Math.min(pos2.getCol(), endCol);
			}
			for (int r = startRow; r < endRow; r++) {
				if (r >= getMaxRowCount()) {
					break;
				}
				Object[] line = matrix.getRow(r - startRow);
				writeLine(r, line, startCol, endCol);
			}
			sheetInfo.setRowCount(sheetInfo.getRowCount() + rowCount);
			sheetInfo.setColCount(Math.max(sheetInfo.getColCount(), endCol));
		}
	}

	/**
	 * Rename excel sheet
	 * 
	 * @param sheetName
	 */
	public void rename(String sheetName) {
		Workbook wb = xlsFile.getWorkbook();
		int sheetIndex = wb.getSheetIndex(sheet);
		wb.setSheetName(sheetIndex, sheetName);
		sheetInfo.setSheetName(sheetName);
	}

	/**
	 * Close
	 */
	public void close() {
	}

	/**
	 * Get cell graph in the map
	 * 
	 * @param row
	 *            Row number
	 * @param col
	 *            Column number
	 * @return
	 */
	private Object getCellGraph(int row, int col) {
		Map<String, byte[]> map = getGraphMap();
		byte[] data = map.get(row + ROW_COL_SEP + col);
		return data;
	}

	/**
	 * Set cell graph
	 * 
	 * @param row
	 *            Row number
	 * @param col
	 *            Column number
	 * @param data
	 *            Graph data
	 */
	private void setCellGraph(int row, int col, byte[] data) {
		if (sheet == null)
			return;
		Workbook wb = xlsFile.getWorkbook();
		if (isXls) {
			ClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255,
					(short) col, row, (short) col, row);
			HSSFPatriarch hssfPatriarch = ((HSSFSheet) sheet)
					.getDrawingPatriarch();
			if (hssfPatriarch == null)
				hssfPatriarch = ((HSSFSheet) sheet).createDrawingPatriarch();
			hssfPatriarch.createPicture(anchor,
					wb.addPicture(data, Workbook.PICTURE_TYPE_PNG));
		} else {
			ClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, col,
					row, col + 1, row + 1);
			XSSFDrawing xssfPatriarch = ((XSSFSheet) sheet)
					.getDrawingPatriarch();
			if (xssfPatriarch == null)
				xssfPatriarch = ((XSSFSheet) sheet).createDrawingPatriarch();
			xssfPatriarch.createPicture(anchor,
					wb.addPicture(data, Workbook.PICTURE_TYPE_PNG));
		}
		Map<String, byte[]> map = getGraphMap();
		map.put(row + ROW_COL_SEP + col, data);
	}

	/**
	 * Get row style by row number
	 * 
	 * @param r
	 *            Row number
	 * @return
	 */
	private RowAndCellStyle getRowStyle(int r) {
		Row hr = sheet.getRow(r);
		if (hr == null)
			return null;
		RowAndCellStyle style = new RowAndCellStyle();
		style.rowStyle = hr.getRowStyle();
		short lastCol = hr.getLastCellNum();
		if (lastCol > 0) {
			CellStyle[] cellStyles = new CellStyle[lastCol];
			for (int c = 0; c < lastCol; c++) {
				Cell cell = hr.getCell(c);
				if (cell != null)
					cellStyles[c] = cell.getCellStyle();
			}
			style.cellStyles = cellStyles;
		}
		style.rowHeight = hr.getHeightInPoints();
		return style;
	}

	/**
	 * Write a row of data
	 * 
	 * @param row
	 *            Row
	 * @param items
	 *            Row datas
	 * @param startCol
	 *            Start row
	 * @param endCol
	 *            End column
	 * @param rowStyle
	 *            Row style
	 * @param cellStyles
	 *            Cell styles
	 */
	private void writeRowData(Row row, Object[] items, int startCol,
			int endCol, CellStyle rowStyle, CellStyle[] cellStyles) {
		if (items == null || items.length == 0)
			return;
		CellStyle cellStyle, rowOrColStyle = null;
		for (int currCol = startCol; currCol < endCol; currCol++) {
			Cell cell = row.getCell(currCol);
			if (cell == null) {
				cellStyle = null;
				cell = row.createCell(currCol);
				if (cellStyles != null && cellStyles.length > currCol) {
					cellStyle = cellStyles[currCol];
					cell.setCellStyle(cellStyle);
				}
			} else {
				cellStyle = cell.getCellStyle();
			}

			if (cellStyle == null) {
				// When the grid has no style, use the row style setting.
				if (rowStyle != null) {
					cell.setCellStyle(rowStyle);
					rowOrColStyle = rowStyle;
				} else if (colStyles != null) {
					/*
					 * When there is no format for grids and rows, use column
					 * style settings.
					 */
					if (currCol < colStyles.length) {
						if (colStyles[currCol] != null) {
							cell.setCellStyle(colStyles[currCol]);
							rowOrColStyle = colStyles[currCol];
						}
					}
				}
			}
			Object value = items[currCol - startCol];
			if (value instanceof Date) {
				cell.setCellValue((Date) value);
				// The cell is not styled and is not in a date and time format.
				DataFormat dFormat = xlsFile.getWorkbook().createDataFormat();
				if (cellStyle == null
						&& !ExcelUtils.isCellDateFormatted(cell, dFormat)) {
					CellStyle style = null;
					short format = 49;
					if (value instanceof Timestamp) {
						format = dFormat.getFormat(Env.getDateTimeFormat());
					} else if (value instanceof Time) {
						format = dFormat.getFormat(Env.getTimeFormat());
					} else {
						format = dFormat.getFormat(Env.getDateFormat());
					}
					style = styles.get(new Integer(currCol));
					if (style == null) {
						style = xlsFile.getWorkbook().createCellStyle();
						if (rowOrColStyle != null)
							style.cloneStyleFrom(rowOrColStyle);
						style.setDataFormat(format);
						styles.put(new Integer(currCol), style);
					}
					cell.setCellStyle(style);
				}
			} else if (value instanceof String) {
				String sValue = (String) value;
				if (ExcelUtils.isNumeric(sValue)) {
					cell.setCellType(CellType.STRING);
				}
				cell.setCellValue(sValue);
			} else if (value instanceof Boolean) {
				cell.setCellValue(((Boolean) value).booleanValue());
			} else if (value == null) {
			} else {
				String s = value.toString();
				try {
					double d = Double.parseDouble(s);
					cell.setCellValue(d);
				} catch (Throwable e1) {
					cell.setCellValue(s);
				}
			}
		}
		if (rowStyle != null) {
			row.setRowStyle(rowStyle);
		}
	}

	/**
	 * Reset cell styles
	 * 
	 * @param row
	 */
	private void resetDataStyle(Row row) {
		if (dataStyle == null) {
			dataStyle = new RowAndCellStyle();
		}
		int lastCol = row.getLastCellNum();
		if (lastCol > 0) {
			CellStyle[] cellStyles = new CellStyle[lastCol];
			Cell cell;
			for (int c = 0; c < lastCol; c++) {
				cell = row.getCell(c);
				if (cell != null)
					cellStyles[c] = cell.getCellStyle();
			}
			dataStyle.cellStyles = cellStyles;
		}
		resetDataStyle = false;
	}

}