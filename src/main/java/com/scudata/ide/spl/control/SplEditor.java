package com.scudata.ide.spl.control;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.scudata.cellset.IColCell;
import com.scudata.cellset.INormalCell;
import com.scudata.cellset.IRowCell;
import com.scudata.cellset.IStyle;
import com.scudata.cellset.datamodel.CellSet;
import com.scudata.cellset.datamodel.ColCell;
import com.scudata.cellset.datamodel.NormalCell;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.cellset.datamodel.PgmNormalCell;
import com.scudata.cellset.datamodel.RowCell;
import com.scudata.common.Area;
import com.scudata.common.ByteMap;
import com.scudata.common.CellLocation;
import com.scudata.common.Escape;
import com.scudata.common.IByteMap;
import com.scudata.common.Matrix;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.ide.common.ConfigOptions;
import com.scudata.ide.common.GC;
import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;
import com.scudata.ide.common.IAtomicCmd;
import com.scudata.ide.common.control.CellRect;
import com.scudata.ide.common.control.CellSelection;
import com.scudata.ide.common.control.IEditorListener;
import com.scudata.ide.common.dialog.DialogInputText;
import com.scudata.ide.spl.AtomicCell;
import com.scudata.ide.spl.AtomicSpl;
import com.scudata.ide.spl.GCSpl;
import com.scudata.ide.spl.GMSpl;
import com.scudata.ide.spl.GVSpl;
import com.scudata.ide.spl.SheetSpl;
import com.scudata.ide.spl.UndoManager;
import com.scudata.ide.spl.chart.DialogPlotEdit;
import com.scudata.ide.spl.dialog.DialogCopyPresent;
import com.scudata.ide.spl.dialog.DialogTextEditor;
import com.scudata.ide.spl.dialog.DialogZoom;
import com.scudata.ide.spl.etl.DialogFuncEdit;
import com.scudata.ide.spl.etl.EtlConsts;
import com.scudata.ide.spl.etl.ObjectElement;
import com.scudata.ide.spl.resources.IdeSplMessage;

/**
 * 网格编辑器
 *
 */
public class SplEditor {
	/** CTRL-ENTER事件，插入行 */
	public static final byte HK_CTRL_ENTER = 0;
	/** CTRL-INSERT事件，右移单元格 */
	public static final byte HK_CTRL_INSERT = 2;
	/** ALT-INSERT事件，下移单元格 */
	public static final byte HK_ALT_INSERT = 3;

	/** 粘贴选项，常规 */
	public static final byte PASTE_OPTION_NORMAL = 0;
	/** 粘贴选项，插入空行 */
	public static final byte PASTE_OPTION_INSERT_ROW = 1;
	/** 粘贴选项，插入空列 */
	public static final byte PASTE_OPTION_INSERT_COL = 2;
	/** 粘贴选项，目标区域格子下移 */
	public static final byte PASTE_OPTION_PUSH_BOTTOM = 3;
	/** 粘贴选项，目标区域格子右移 */
	public static final byte PASTE_OPTION_PUSH_RIGHT = 4;

	/**
	 * 网格控件
	 */
	protected EditControl control;

	/**
	 * 撤销重做管理器
	 */
	public UndoManager undoManager;

	/**
	 * 编辑监听器
	 */
	private IEditorListener listener;

	/**
	 * 选择状态
	 */
	public byte selectState = GCSpl.SELECT_STATE_CELL;

	/**
	 * 集算器资源管理器
	 */
	private MessageManager mm = IdeSplMessage.get();

	/**
	 * 数据是否变化了
	 */
	private boolean isDataChanged = false;

	/**
	 * 选中的格子矩形
	 */
	public Vector<CellRect> selectedRects = new Vector<CellRect>();

	/**
	 * 选中的列号
	 */
	public Vector<Integer> selectedCols = new Vector<Integer>();

	/**
	 * 选中的行号
	 */
	public Vector<Integer> selectedRows = new Vector<Integer>();

	/**
	 * 页面对象
	 */
	protected SheetSpl sheet;

	/**
	 * 构造函数
	 * 
	 * @param sheet
	 *            页面对象
	 * @param context
	 *            上下文
	 */
	public SplEditor(SheetSpl sheet) {
		this.sheet = sheet;
		control = newEditControl(ConfigOptions.iRowCount.intValue(),
				ConfigOptions.iColCount.intValue());
		initDefaultProperty(control.cellSet);
		control.draw();
		SplControlListener qcl = new SplControlListener(this);
		control.addEditorListener(qcl);
		undoManager = new UndoManager(this);
	};

	/**
	 * Initialize the grid with initial properties
	 * 
	 * @param cs
	 *            CellSet
	 */
	public static void initDefaultProperty(CellSet cs) {
		int rows, r, cols, c;
		rows = cs.getRowCount();
		cols = cs.getColCount();
		for (r = 1; r <= rows; r++) {
			IRowCell rc = cs.getRowCell(r);
			initDefaultCell(rc);
		}
		for (c = 1; c <= cols; c++) {
			IColCell cc = cs.getColCell(c);
			initDefaultCell(cc);
		}
	}

	/**
	 * Initialize cell
	 * 
	 * @param ic
	 */
	private static void initDefaultCell(Object ic) {
		if (ic instanceof RowCell) {
			RowCell rc = (RowCell) ic;
			rc.setHeight(ConfigOptions.fRowHeight.floatValue());
		} else if (ic instanceof ColCell) {
			ColCell cc = (ColCell) ic;
			cc.setWidth(ConfigOptions.fColWidth.floatValue());
		}
	}

	/**
	 * 构造编辑控件
	 * 
	 * @param rows
	 *            行数
	 * @param cols
	 *            列数
	 * @return EditControl
	 */
	protected EditControl newEditControl(int rows, int cols) {
		EditControl control = new EditControl(rows, cols);
		control.setSheet(sheet);
		return control;
	}

	/**
	 * 设置网格数据是否变化了
	 * 
	 * @param isDataChanged
	 */
	public void setDataChanged(boolean isDataChanged) {
		this.isDataChanged = isDataChanged;
		GMSpl.enableSave(isDataChanged);
	}

	/**
	 * 取网格数据是否变化了
	 * 
	 * @return
	 */
	public boolean isDataChanged() {
		return isDataChanged;
	}

	/**
	 * 设置集算器网格
	 * 
	 * @param cellSet
	 *            集算器网格对象
	 * @return
	 * @throws Exception
	 */
	public boolean setCellSet(PgmCellSet cellSet) throws Exception {
		control.setCellSet(cellSet);
		control.draw();
		return true;
	}

	/**
	 * 选择第一个格子
	 */
	public void selectFirstCell() {
		if (control.cellSet.getRowCount() < 1
				|| control.cellSet.getColCount() < 1)
			return;
		selectState = GC.SELECT_STATE_CELL;
		CellRect rect = new CellRect(1, 1, 1, 1);
		selectedRects.clear();
		selectedRects.add(rect);
		selectedCols.clear();
		selectedRows.clear();

		control.m_selectedCols.clear();
		control.m_selectedRows.clear();
		control.m_cornerSelected = false;
		control.contentView.rememberedRow = 1;
		control.contentView.rememberedCol = 1;
		control.setActiveCell(new CellLocation(1, 1));
		control.setSelectedArea(new Area(1, 1, 1, 1));
	}

	/**
	 * 选择格子
	 * 
	 * @param row
	 *            行号
	 * @param col
	 *            列号
	 */
	public void selectCell(int row, int col) {
		if (control.cellSet.getRowCount() < row
				|| control.cellSet.getColCount() < col)
			return;
		selectState = GC.SELECT_STATE_CELL;
		CellRect rect = new CellRect(row, col, 1, 1);
		selectedRects.clear();
		selectedRects.add(rect);
		selectedCols.clear();
		selectedRows.clear();

		control.m_selectedCols.clear();
		control.m_selectedRows.clear();
		control.m_cornerSelected = false;
		control.contentView.rememberedRow = row;
		control.contentView.rememberedCol = col;
		control.setActiveCell(new CellLocation(row, col));
		control.setSelectedArea(new Area(row, col, row, col));
	}

	/**
	 * 增加网格编辑监听器
	 * 
	 * @param listener
	 */
	public void addSplListener(IEditorListener listener) {
		this.listener = listener;
	}

	/**
	 * 取网格编辑监听器
	 * 
	 * @return
	 */
	public IEditorListener getSplListener() {
		return listener;
	}

	/**
	 * 取网格控件
	 * 
	 * @return
	 */
	public SplControl getComponent() {
		return control;
	}

	/**
	 * 执行原子命令集
	 * 
	 * @param cmds
	 *            原子命令集
	 * @return
	 */
	public boolean executeCmd(Vector<IAtomicCmd> cmds) {
		undoManager.doing(cmds);
		control.getContentPanel().initEditor(ContentPanel.MODE_HIDE);
		return true;
	}

	/**
	 * 执行原子命令
	 * 
	 * @param aCell
	 *            原子命令
	 * @return
	 */
	public boolean executeCmd(IAtomicCmd aCell) {
		undoManager.doing(aCell);
		control.getContentPanel().initEditor(ContentPanel.MODE_HIDE);
		return true;
	}

	/**
	 * 撤销
	 * 
	 * @return
	 */
	public boolean undo() {
		if (undoManager.canUndo()) {
			undoManager.undo();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否可以撤销
	 * 
	 * @return
	 */
	public boolean canUndo() {
		return undoManager.canUndo();
	}

	/**
	 * 重做
	 * 
	 * @return
	 */
	public boolean redo() {
		if (undoManager.canRedo()) {
			undoManager.redo();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否可以重做
	 * 
	 * @return
	 */
	public boolean canRedo() {
		return undoManager.canRedo();
	}

	/**
	 * 设置编辑控件的文本
	 * 
	 * @param text
	 */
	public void setEditingText(String text) {
		control.getContentPanel().setEditorText(text);
	}

	/**
	 * 绘图编辑
	 */
	public void dialogChartEditor() {
		if (isNothingSelected()) {
			return;
		}
		CellRect cr = (CellRect) selectedRects.get(0);
		int row = cr.getBeginRow();
		int col = cr.getBeginCol();

		NormalCell nc = (NormalCell) control.cellSet.getCell(row, col);
		String exp = nc.getExpString();
		if (exp == null) {
			exp = "";
		}
		List<String> gNames = getCanvasNames();
		DialogPlotEdit dpe = new DialogPlotEdit(GV.appFrame, exp, gNames);
		dpe.setVisible(true);
		if (dpe.getOption() != JOptionPane.OK_OPTION) {
			return;
		}
		exp = "=" + dpe.getPlotFunction();
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();

		Vector<CellLocation> cells = ControlUtils
				.listSelectedCells(selectedRects);
		for (int i = 0; i < cells.size(); i++) {
			CellLocation cp = (CellLocation) cells.get(i);
			nc = (NormalCell) control.cellSet.getCell(cp.getRow(), cp.getCol());

			AtomicCell ac = new AtomicCell(control, nc);
			ac.setProperty(AtomicCell.CELL_EXP);
			ac.setValue(exp);
			cmds.add(ac);

			ac = SplControlListener.getCellHeightCmd(control, cp.getRow(),
					cp.getCol(), exp);
			if (ac != null) {
				cmds.add(ac);
			}
		}

		executeCmd(cmds);
		return;
	}

	/**
	 * 取画布的名称列表
	 * 
	 * @return
	 */
	private List<String> getCanvasNames() {
		List<String> gNames = new ArrayList<String>();
		PgmCellSet cellSet = control.cellSet;
		PgmNormalCell cell;
		Pattern p = Pattern
				.compile("\\s*(\\S*\\s*=)?\\s*canvas\\s*\\(\\s*\\)\\s*");
		Matcher m;
		CellRect cr = (CellRect) selectedRects.get(0);
		int row = cr.getBeginRow();
		int col = cellSet.getColCount();
		CellSetParser parser = new CellSetParser(control.getCellSet());

		for (int r = 1, rowCount = row; r <= rowCount; r++) {
			for (int c = 1, colCount = col; c <= colCount; c++) {
				cell = (PgmNormalCell) cellSet.getCell(r, c);
				String exp = cell.getExpString();
				if (exp == null)
					continue;
				byte cellType = parser.getCellDispType(r, c);
				if (cellType == CellSetParser.TYPE_NOTE) {
					continue;
				}
				if (r == rowCount && c == cr.getBeginCol()) {
					break;
				}

				switch (cell.getType()) {
				case NormalCell.TYPE_CALCULABLE_CELL:
				case NormalCell.TYPE_EXECUTABLE_CELL:
					exp = exp.substring(1);
					break;
				case NormalCell.TYPE_CALCULABLE_BLOCK:
				case NormalCell.TYPE_EXECUTABLE_BLOCK:
					exp = exp.substring(2);
					break;
				default:
					continue;
				}
				exp = exp.trim();
				m = p.matcher(exp);
				if (m.matches()) {
					String paramName = m.group(1);
					if (!StringUtils.isValidString(paramName))
						paramName = cell.getCellId();
					else
						paramName = paramName.substring(0,
								paramName.length() - 1);
					if (!gNames.contains(paramName))
						gNames.add(paramName);
				}
			}
		}
		return gNames;
	}

	/**
	 * 取函数对象
	 * 
	 * @param row
	 *            行号
	 * @param col
	 *            列号
	 * @param oes
	 *            函数对象映射
	 * 
	 * @return
	 */
	private ObjectElement getFuncObj(int row, int col,
			HashMap<String, ObjectElement> oes) {
		INormalCell nc = control.cellSet.getCell(row, col);
		return ObjectElement.parseString(nc.getExpString(), oes);
	}

	/**
	 * 函数对象是否选择了
	 * 
	 * @return
	 */
	public boolean isObjectElementSelected() {
		if (isNothingSelected()) {
			return false;
		}
		CellRect cr = (CellRect) selectedRects.get(0);
		int row = cr.getBeginRow();
		int col = cr.getBeginCol();
		ObjectElement oe = getFuncObj(row, col, getObjectElementMap());
		return oe != null;
	}

	/**
	 * 函数编辑对话框
	 */
	public void dialogFuncEditor() {
		if (isNothingSelected()) {
			return;
		}
		CellRect cr = (CellRect) selectedRects.get(0);
		int row = cr.getBeginRow();
		int col = cr.getBeginCol();

		HashMap<String, ObjectElement> elementMap = getObjectElementMap();
		ObjectElement oe = getFuncObj(row, col, elementMap);
		DialogFuncEdit dfe = new DialogFuncEdit(elementMap, oe);
		dfe.setVisible(true);
		if (dfe.getOption() != JOptionPane.OK_OPTION) {
			return;
		}
		oe = dfe.getObjectElement();
		String exp = null;
		try {
			exp = oe.toExpressionString();
		} catch (Exception x) {
			GM.showException(GV.appFrame, x);
			return;
		}

		INormalCell nc = control.cellSet.getCell(row, col);
		AtomicCell ac = new AtomicCell(control, nc);
		ac.setProperty(AtomicCell.CELL_EXP);
		ac.setValue(exp);
		executeCmd(ac);
	}

	/**
	 * 缩放对话框
	 */
	public void dialogZoom() {
		DialogZoom dz = new DialogZoom();
		dz.setScale(control.scale);
		dz.setVisible(true);
		if (dz.getOption() == JOptionPane.OK_OPTION) {
			control.setScale(dz.getScale());
			if (GVSpl.splEditor != null)
				GVSpl.splEditor.setDataChanged(true);
		}
	}

	/**
	 * 取函数对象映射表
	 * 
	 * @return
	 */
	private HashMap<String, ObjectElement> getObjectElementMap() {
		HashMap<String, ObjectElement> elementMap = new HashMap<String, ObjectElement>();
		PgmCellSet cellSet = control.cellSet;
		CellSetParser parser = new CellSetParser(control.getCellSet());

		CellRect cr = (CellRect) selectedRects.get(0);
		int row = cr.getBeginRow();
		int col = cellSet.getColCount();

		for (int r = 1, rowCount = row; r <= rowCount; r++) {
			for (int c = 1, colCount = col; c <= colCount; c++) {
				byte cellType = parser.getCellDispType(r, c);
				if (cellType == CellSetParser.TYPE_NOTE) {
					continue;
				}
				if (r == rowCount && c == cr.getBeginCol()) {
					break;
				}
				ObjectElement oe = getFuncObj(r, c, elementMap);
				if (oe == null)
					continue;
				if (oe.getReturnType() == EtlConsts.TYPE_EMPTY) {
					continue;
				}
				INormalCell cell = cellSet.getCell(r, c);
				String cellName = cell.getCellId();
				elementMap.put(cellName, oe);
			}
		}
		return elementMap;
	}

	/**
	 * 插入/追加行
	 * 
	 * @param insertBefore
	 *            是否插入行。true行前插入，false追加行。
	 * @return
	 */
	public boolean insertRow(boolean insertBefore) {
		CellRect rect;
		int cc = control.cellSet.getColCount();
		if (isNothingSelected()) {
			rect = new CellRect(1, (int) 1, 1, (int) cc);
		} else {
			if (isMultiRectSelected()) {
				GM.messageDialog(GV.appFrame,
						mm.getMessage("dfxeditor.insertmore"));
				return false;
			}
			rect = getSelectedRect();
		}
		return insertRow(rect, insertBefore);
	}

	/**
	 * 插入/追加行
	 * 
	 * @param rect
	 *            选择区域
	 * @param insertBefore
	 *            是否插入行。true行前插入，false追加行。
	 * @return
	 */
	public boolean insertRow(CellRect rect, boolean insertBefore) {
		executeCmd(getInsertRow(insertBefore, rect));
		if (insertBefore) {
			// 调整断点位置
			ArrayList<CellLocation> breaks = control.getBreakPoints();
			for (int i = 0; i < breaks.size(); i++) {
				CellLocation cp = (CellLocation) breaks.get(i);
				if (cp.getRow() >= rect.getBeginRow()) {
					cp.setRow(cp.getRow() + rect.getRowCount());
				}
			}
		}
		return true;
	}

	/**
	 * 是否选中了多个格子矩阵区域
	 * 
	 * @return
	 */
	private boolean isMultiRectSelected() {
		return selectedRects.size() > 1;
	}

	/**
	 * 获取选择的格子矩阵
	 * 
	 * @return
	 */
	public CellRect getSelectedRect() {
		if (selectedRects == null || selectedRects.isEmpty()) {
			return null;
		}
		return (CellRect) selectedRects.get(0);
	}

	/**
	 * 获取所有选择区域的Vector，里面的每个元素都是一个CellRect对象
	 * 
	 * @return
	 */
	public Vector<CellRect> getSelectedRects() {
		return selectedRects;
	}

	/**
	 * 取插入/追加行的原子命令
	 * 
	 * @param insertBefore
	 *            是否插入行。true行前插入，false追加行。
	 * @param rect
	 *            选中的格子矩阵
	 * @return
	 */
	public AtomicSpl getInsertRow(boolean insertBefore, CellRect rect) {
		AtomicSpl aq = new AtomicSpl(control);
		if (insertBefore) {
			aq.setType(AtomicSpl.INSERT_ROW);
		} else {
			aq.setType(AtomicSpl.ADD_ROW);
		}

		ArrayList<RowCell> oneRowCells = getApprCopiedRowCells(rect
				.getBeginRow());
		aq.setValue(oneRowCells);
		aq.setRect(rect);
		return aq;
	}

	/**
	 * 复制指定行外观属性的格子,包含行首格
	 * 
	 * @param row
	 *            行号
	 * @return
	 */
	private ArrayList<RowCell> getApprCopiedRowCells(int row) {
		ArrayList<RowCell> oneRowCells = new ArrayList<RowCell>();
		RowCell rc = (RowCell) control.cellSet.getRowCell(row).deepClone();
		oneRowCells.add(rc);
		return oneRowCells;
	}

	/**
	 * 是否没有选择区域
	 * 
	 * @return
	 */
	private boolean isNothingSelected() {
		return selectedRects.isEmpty();
	}

	/**
	 * 设置单元格属性
	 * 
	 * @param type
	 *            选择类型
	 * @param property
	 *            格子属性类型
	 * @param value
	 *            值
	 * @return
	 */
	public boolean setProperty(byte type, byte property, Object value) {
		if (isNothingSelected()) {
			return false;
		}
		CellLocation cp;
		Object cell;
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		Vector<CellLocation> cells = ControlUtils
				.listSelectedCells(selectedRects);
		CellSetParser parser = new CellSetParser(control.getCellSet());
		switch (type) {
		case GCSpl.SELECT_STATE_CELL:
		case GCSpl.SELECT_STATE_DM:
			for (int i = 0; i < cells.size(); i++) {
				cp = (CellLocation) cells.get(i);
				if (isCellIgnoreable(parser, cp)) {
					continue;
				}
				AtomicCell ac = new AtomicCell(control,
						control.cellSet.getCell(cp.getRow(), cp.getCol()));
				ac.setProperty(property);
				ac.setValue(value);
				cmds.add(ac);

				if (property == AtomicCell.CELL_EXP
						|| property == AtomicCell.CELL_VALUE) {
					ac = SplControlListener.getCellHeightCmd(control,
							cp.getRow(), cp.getCol(), String.valueOf(value));
					if (ac != null) {
						cmds.add(ac);
					}
				}
			}
			break;
		case GCSpl.SELECT_STATE_ROW:
			for (int r = 0; r < selectedRows.size(); r++) {
				cell = control.getCellSet().getRowCell(
						((Integer) selectedRows.get(r)).intValue());
				if (cell == null) {
					continue;
				}
				AtomicCell ac = new AtomicCell(control, cell);
				ac.setProperty(property);
				ac.setValue(value);
				cmds.add(ac);
			}
			break;
		case GCSpl.SELECT_STATE_COL:
			for (int c = 0; c < selectedCols.size(); c++) {
				cell = control.getCellSet().getColCell(
						((Integer) selectedCols.get(c)).intValue());
				if (cell == null) {
					continue;
				}
				AtomicCell ac = new AtomicCell(control, cell);
				ac.setProperty(property);
				ac.setValue(value);
				cmds.add(ac);
			}
			break;
		}
		executeCmd(cmds);
		return true;
	}

	/**
	 * 取格子属性
	 * 
	 * @return
	 */
	public IByteMap getProperty() {
		IByteMap bm = null;
		switch (selectState) {
		case GCSpl.SELECT_STATE_CELL:
			bm = cloneAMap((IByteMap) getCellByteMap(GCSpl.SELECT_STATE_CELL));
			break;
		case GCSpl.SELECT_STATE_ROW:
			bm = cloneAMap((IByteMap) getCellByteMap(GCSpl.SELECT_STATE_CELL));
			if (bm != null) {
				bm.putAll(getCellByteMap(GCSpl.SELECT_STATE_ROW));
			}
			break;
		case GCSpl.SELECT_STATE_COL:
			bm = cloneAMap((IByteMap) getCellByteMap(GCSpl.SELECT_STATE_CELL));
			if (bm != null) {
				bm.putAll(getCellByteMap(GCSpl.SELECT_STATE_COL));
			}
			break;
		case GCSpl.SELECT_STATE_DM:
			bm = cloneAMap((IByteMap) getCellByteMap(GCSpl.SELECT_STATE_CELL));
			if (bm != null) {
				bm.putAll(getCellByteMap(GCSpl.SELECT_STATE_ROW));
				bm.putAll(getCellByteMap(GCSpl.SELECT_STATE_COL));
			}
			break;
		}
		return bm;
	}

	/**
	 * 插入/追加列
	 * 
	 * @param rect
	 *            选择格子区域
	 * @param insertBefore
	 *            是否插入列。true列前插入，false追加列。
	 * @return
	 */
	public boolean insertCol(CellRect rect, boolean insertBefore) {
		executeCmd(getInsertCol(insertBefore, rect));
		if (insertBefore) {
			// 调整断点位置
			ArrayList<CellLocation> breaks = control.getBreakPoints();
			for (int i = 0; i < breaks.size(); i++) {
				CellLocation cp = (CellLocation) breaks.get(i);
				if (cp.getCol() >= rect.getBeginCol()) {
					cp.setCol(cp.getCol() + rect.getColCount());
				}
			}
		}
		control.getHorizontalScrollBar().setValue(
				control.getHorizontalScrollBar().getValue());
		control.getHorizontalScrollBar().repaint();
		return true;
	}

	/**
	 * 追加列
	 * 
	 * @param cols
	 *            追加的列数
	 */
	public void appendCols(int cols) {
		insertCol(new CellRect(1, (int) control.cellSet.getColCount(), 1,
				(int) cols), false);
	}

	/**
	 * 取追加列的原子命令
	 * 
	 * @param cols
	 *            追加的列数
	 * @return
	 */
	public IAtomicCmd getAppendCols(int cols) {
		return getInsertCol(false,
				new CellRect(1, (int) control.cellSet.getColCount(), 1,
						(int) cols));
	}

	/**
	 * 追加指定的行数到末尾，用于块操作溢出时的添加行
	 * 
	 * @param rows
	 *            追加的行数
	 */
	public void appendRows(int rows) {
		executeCmd(getAppendRows(rows));
	}

	/**
	 * 取追加行的原子命令
	 * 
	 * @param rows
	 *            追加的行数
	 * @return
	 */
	public IAtomicCmd getAppendRows(int rows) {
		return getInsertRow(false, new CellRect(control.cellSet.getRowCount(),
				(int) 1, rows, (int) 1));
	}

	/**
	 * 把当前选中的列设为统一的列宽度
	 * 
	 * @param newWidth
	 *            新的列宽
	 */
	public void setColumnWidth(float newWidth) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		CellSetParser parser = new CellSetParser(control.getCellSet());
		for (int c = 0; c < selectedCols.size(); c++) {
			int col = ((Integer) selectedCols.get(c)).intValue();
			if (!parser.isColVisible(col)) {
				continue;
			}
			ColCell cell = (ColCell) control.getCellSet().getColCell(col);
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.COL_WIDTH);
			ac.setValue(new Float(newWidth));
			cmds.add(ac);
		}
		SheetSpl sheet = null;
		if (GVSpl.appSheet instanceof SheetSpl)
			sheet = (SheetSpl) GVSpl.appSheet;
		if (sheet != null)
			sheet.scrollActiveCellToVisible = false;
		executeCmd(cmds);
	}

	/**
	 * 设置当前选中列的可视属性
	 * 
	 * @param visible
	 *            是否可视
	 */
	public void setColumnVisible(boolean visible) {
		if (selectedCols == null || selectedCols.size() == 0) {
			return;
		}

		ArrayList<Integer> list = new ArrayList<Integer>();
		if (visible && selectedCols.size() == 1) {
			int col = ((Number) selectedCols.get(0)).intValue(); // 选中的第一列
			if (col > 1) { // 取消隐藏时，如果第一列之前都是隐藏列，取消隐藏
				// 判断第一行之前是否都是隐藏行
				boolean allHideBefore = true;
				for (int i = 1; i < col; i++) {
					ColCell cc = (ColCell) control.cellSet.getColCell(i);
					if (cc.getVisible() != ColCell.VISIBLE_ALWAYSNOT) {
						allHideBefore = false;
						break;
					}
				}
				if (allHideBefore) {
					for (int i = 1; i < col; i++) {
						list.add(i);
					}
				}
			}

			int endCol = control.cellSet.getColCount();
			if (col < endCol) { // 取消隐藏时，如果最后一列之后都是隐藏列，取消隐藏
				// 判断最后一列之后是否都是隐藏行
				boolean allHideBehind = true;
				for (int i = col + 1; i <= endCol; i++) {
					ColCell cc = (ColCell) control.cellSet.getColCell(i);
					if (cc.getVisible() != ColCell.VISIBLE_ALWAYSNOT) {
						allHideBehind = false;
						break;
					}
				}
				if (allHideBehind) {
					for (int i = col + 1; i <= endCol; i++) {
						list.add(i);
					}
				}
			}

		}

		for (int c = 0; c < selectedCols.size(); c++) {
			int col = ((Number) selectedCols.get(c)).intValue();
			list.add(col);
		}
		setColumnsVisible(list, visible);
	}

	/**
	 * 设置当前选中列的可视属性
	 * 
	 * @param columns
	 *            指定的列
	 * @param visible
	 *            是否可视
	 */
	public void setColumnsVisible(ArrayList<Integer> columns, boolean visible) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int i = 0; i < columns.size(); i++) {
			int col = columns.get(i);
			ColCell cell = (ColCell) control.getCellSet().getColCell(col);
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.COL_VISIBLE);
			ac.setValue(new Boolean(visible));
			cmds.add(ac);
		}
		executeCmd(cmds);
		control.getColumnHeader().revalidate();
		control.repaint();
	}

	/**
	 * 把当前选中的行设为统一的行高度
	 * 
	 * @param newHeight
	 *            新行高
	 */
	public void setRowHeight(float newHeight) {
		CellSetParser parser = new CellSetParser(control.getCellSet());
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int r = 0; r < selectedRows.size(); r++) {
			int row = ((Number) selectedRows.get(r)).intValue();
			if (!parser.isRowVisible(row)) {
				continue;
			}
			RowCell cell = (RowCell) control.getCellSet().getRowCell(row);

			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.ROW_HEIGHT);
			ac.setValue(new Float(newHeight));
			cmds.add(ac);
		}
		SheetSpl sheet = null;
		if (GVSpl.appSheet instanceof SheetSpl)
			sheet = (SheetSpl) GVSpl.appSheet;
		if (sheet != null)
			sheet.scrollActiveCellToVisible = false;
		executeCmd(cmds);
	}

	/**
	 * 调整为合适行高
	 */
	public void adjustRowHeight() {
		if (selectedRows == null || selectedRows.size() == 0) {
			return;
		}
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		CellSetParser parser = new CellSetParser(control.getCellSet());
		for (int i = 0; i < selectedRows.size(); i++) {
			int row = ((Number) selectedRows.get(i)).intValue();
			if (!parser.isRowVisible(row)) {
				continue;
			}
			float height = GMSpl.getMaxRowHeight(control.getCellSet(), row,
					control.scale);
			RowCell cell = (RowCell) control.getCellSet().getRowCell(row);
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.ROW_HEIGHT);
			ac.setValue(new Float(height));
			cmds.add(ac);
		}
		executeCmd(cmds);
	}

	/**
	 * 调整为合适列宽
	 */
	public void adjustColWidth() {
		if (selectedCols == null || selectedCols.size() == 0) {
			return;
		}
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		CellSetParser parser = new CellSetParser(control.getCellSet());
		for (int i = 0; i < selectedCols.size(); i++) {
			int col = ((Number) selectedCols.get(i)).intValue();
			if (!parser.isColVisible(col)) {
				continue;
			}
			float width = GMSpl.getMaxColWidth(control.getCellSet(), col,
					control.scale);
			ColCell cell = (ColCell) control.getCellSet().getColCell(col);
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.COL_WIDTH);
			ac.setValue(new Float(width));
			cmds.add(ac);
		}
		executeCmd(cmds);
	}

	/**
	 * 设置当前选中行的可视属性
	 * 
	 * @param visible
	 *            是否可视
	 */
	public void setRowVisible(boolean visible) {
		if (selectedRows == null || selectedRows.size() == 0) {
			return;
		}
		ArrayList<Integer> list = new ArrayList<Integer>();
		if (visible && selectedRows.size() == 1) { // 选中一行显示
			int row = ((Number) selectedRows.get(0)).intValue();
			if (row > 1) { // 取消隐藏时，如果第一行之前都是隐藏行，取消隐藏
				// 判断第一行之前是否都是隐藏行
				boolean allHideBefore = true;
				for (int i = 1; i < row; i++) {
					RowCell rc = (RowCell) control.cellSet.getRowCell(i);
					if (rc.getVisible() != RowCell.VISIBLE_ALWAYSNOT) {
						allHideBefore = false;
						break;
					}
				}
				if (allHideBefore) {
					for (int i = 1; i < row; i++) {
						list.add(i);
					}
				}
			}
			int endRow = control.cellSet.getRowCount();
			if (row < endRow) { // 取消隐藏时，如果最后一行之后都是隐藏行，取消隐藏
				// 判断最后一行之后是否都是隐藏行
				boolean allHideBehind = true;
				for (int i = row + 1; i <= endRow; i++) {
					RowCell rc = (RowCell) control.cellSet.getRowCell(i);
					if (rc.getVisible() != RowCell.VISIBLE_ALWAYSNOT) {
						allHideBehind = false;
						break;
					}
				}
				if (allHideBehind) {
					for (int i = row + 1; i <= endRow; i++) {
						list.add(i);
					}
				}
			}
		}

		for (int i = 0; i < selectedRows.size(); i++) {
			int row = ((Number) selectedRows.get(i)).intValue();
			list.add(row);
		}
		setRowsVisible(list, visible);
	}

	/**
	 * 设置指定行的可视属性
	 * 
	 * @param rows
	 *            指定的行号列表
	 * @param visible
	 *            是否可视
	 */
	public void setRowsVisible(ArrayList<Integer> rows, boolean visible) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int i = 0; i < rows.size(); i++) {
			int row = rows.get(i);
			RowCell cell = (RowCell) control.getCellSet().getRowCell(row);
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.ROW_VISIBLE);
			ac.setValue(new Boolean(visible));
			cmds.add(ac);
		}
		executeCmd(cmds);
		control.getRowHeader().revalidate();
		control.repaint();
	}

	/**
	 * 按照热键执行相应的插入操作， 相对比较复杂，有数据块的移动等
	 * 
	 * @param key
	 *            byte
	 */
	public void hotKeyInsert(byte key) {
		CellLocation activeCell = control.getActiveCell();
		if (activeCell == null) {
			return;
		}
		CellRect rect = null;
		if (control.getSelectedAreas().size() > 0) {
			Area a = control.getSelectedArea(0);
			rect = new CellRect(a);
		}
		hotKeyInsert(key, rect);
	}

	/**
	 * 执行热键
	 * 
	 * @param key
	 *            定义的HK常量类型
	 * @param rect
	 *            选择的格子矩阵
	 */
	private void hotKeyInsert(byte key, CellRect rect) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();

		int curCol = rect.getBeginCol();
		int curRow = rect.getBeginRow();

		CellSet ics = control.getCellSet();

		CellRect srcRect, tarRect;
		switch (key) {
		case HK_CTRL_ENTER:
			/* 这个动作是本格以及右边的格子到下一行，左边格子不变。之前做的有问题，重新整理了一下 */
			int newRow = -1;
			if (curCol == 1) { // 如果当前格是第一列，直接插入行
				newRow = curRow + rect.getRowCount();
				cmds.add(getInsertRow(true,
						new CellRect(curRow, curCol, rect.getRowCount(), 1)));
			} else {// 第一步先在下面插入或者追加一行，第二步把当前格以及右边格子下移
				if (curRow < ics.getRowCount()) {
					cmds.add(getInsertRow(true, new CellRect(curRow + 1,
							curCol, 1, 1)));
				} else {
					cmds.add(getInsertRow(false, new CellRect(curRow, curCol,
							1, 1)));
				}

				int colCount = ics.getColCount();
				srcRect = new CellRect(curRow, (int) curCol, 1, (int) (colCount
						- curCol + 1));
				tarRect = new CellRect(curRow + 1, (int) curCol, 1,
						(int) (colCount - curCol + 1));
				Vector<IAtomicCmd> tmp = GMSpl.getMoveRectCmd(this, srcRect,
						tarRect);
				if (tmp != null)
					cmds.addAll(tmp);
			}
			// 这个构造函数特殊，现在还没有插入行
			AtomicCell ac = new AtomicCell(control, curRow + 1);
			ac.setProperty(AtomicCell.ROW_HEIGHT);
			ac.setValue(new Float(control.cellSet.getRowCell(curRow)
					.getHeight()));
			cmds.add(ac);
			executeCmd(cmds);

			// 调整断点位置
			ArrayList<CellLocation> breaks = control.getBreakPoints();

			for (int i = breaks.size(); i < breaks.size(); i++) {
				CellLocation cp = (CellLocation) breaks.get(i);
				if (cp.getRow() >= rect.getBeginRow()) {
					cp.setRow(cp.getRow() + rect.getRowCount());
				}
			}
			if (newRow > 0) {
				control.scrollToArea(control.setActiveCell(new CellLocation(
						newRow, control.getActiveCell().getCol())));
			} else {
				control.scrollToArea(control.toDownCell());
			}
			break;
		case HK_ALT_INSERT: {
			int maxUsedRow = 0;
			for (int c = rect.getBeginCol(); c <= rect.getEndCol(); c++) {
				int usedRow = getCellSelectListener().getUsedRows(c);
				if (usedRow > maxUsedRow) {
					maxUsedRow = usedRow;
				}
			}
			if (maxUsedRow >= rect.getBeginRow()) {
				srcRect = new CellRect(rect.getBeginRow(), rect.getBeginCol(),
						maxUsedRow - rect.getBeginRow() + 1, rect.getColCount());
				tarRect = new CellRect(rect.getEndRow() + 1,
						rect.getBeginCol(),
						maxUsedRow - rect.getBeginRow() + 1, rect.getColCount());
				getCellSelectListener().moveRect(srcRect, tarRect, false);
			}
		}
			break;
		case HK_CTRL_INSERT: {
			int maxUsedCol = 0;
			for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
				int usedCol = getCellSelectListener().getUsedCols(r);
				if (usedCol > maxUsedCol) {
					maxUsedCol = usedCol;
				}
			}
			if (maxUsedCol >= rect.getBeginCol()) {
				srcRect = new CellRect(rect.getBeginRow(), rect.getBeginCol(),
						rect.getRowCount(), (int) (maxUsedCol
								- rect.getBeginCol() + 1));
				tarRect = new CellRect(rect.getBeginRow(),
						(int) (rect.getEndCol() + 1), rect.getRowCount(),
						(int) (maxUsedCol - rect.getBeginCol() + 1));
				getCellSelectListener().moveRect(srcRect, tarRect, false);
			}
		}
			break;
		}
	}

	/**
	 * 移动复制
	 * 
	 * @param key
	 *            GCSpl中定义的常量
	 */
	public void moveCopy(short key) {
		if (this.isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.copymore"));
			return;
		}
		CellLocation activeCell = control.getActiveCell();
		if (activeCell == null) {
			return;
		}
		// 提交当前编辑
		control.getContentPanel().submitEditor();
		CellSet ics = control.getCellSet();
		CellSetParser parser = new CellSetParser(ics);
		CellRect rect = getSelectedRect();
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		switch (key) {
		case GCSpl.iMOVE_COPY_UP:
			boolean hasPreRow = false;
			for (int r = rect.getBeginRow() - 1; r >= 1; r--) {
				if (parser.isRowVisible(r)) {
					hasPreRow = true;
					break;
				}
			}
			if (!hasPreRow)
				return;
			break;
		case GCSpl.iMOVE_COPY_DOWN:
			boolean hasNextRow = false;
			for (int r = rect.getEndRow() + 1; r <= ics.getRowCount(); r++) {
				if (parser.isRowVisible(r)) {
					hasNextRow = true;
					break;
				}
			}
			if (!hasNextRow) {
				cmds.add(getAppendRows(1));
			}
			break;
		case GCSpl.iMOVE_COPY_LEFT:
			boolean hasPreCol = false;
			for (int c = rect.getBeginCol() - 1; c >= 1; c--) {
				if (parser.isColVisible(c)) {
					hasPreCol = true;
					break;
				}
			}
			if (!hasPreCol)
				return;
			break;
		case GCSpl.iMOVE_COPY_RIGHT:
			boolean hasNextCol = false;
			for (int c = rect.getEndCol() + 1; c <= ics.getColCount(); c++) {
				if (parser.isColVisible(c)) {
					hasNextCol = true;
					break;
				}
			}
			if (!hasNextCol)
				return;
			break;
		}

		CellSelection cs = new CellSelection(null, rect, control.cellSet);
		AtomicSpl ad = new AtomicSpl(control);
		ad.setType(AtomicSpl.MOVE_COPY);
		ad.setRect(rect);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AtomicSpl.CELL_SELECTION, cs);
		map.put(AtomicSpl.MOVE_TYPE, key);
		ad.setValue(map);
		cmds.add(ad);
		this.executeCmd(cmds);
	}

	/**
	 * 取单元格选择监听器
	 * 
	 * @return
	 */
	private CellSelectListener getCellSelectListener() {
		return (CellSelectListener) control.getContentPanel()
				.getMouseListeners()[0];
	}

	/**
	 * 插入/追加列
	 * 
	 * @param insertBefore
	 *            是否插入列。true插入列，false追加列
	 * @return
	 */
	public boolean insertCol(boolean insertBefore) {
		CellRect rect;
		if (isNothingSelected()) {
			rect = new CellRect(1, (int) 1, control.cellSet.getRowCount(),
					(int) 1);
		} else {
			if (isMultiRectSelected()) {
				GM.messageDialog(GV.appFrame,
						mm.getMessage("dfxeditor.insertmore"));
				return false;
			}
			rect = getSelectedRect();
		}
		return insertCol(rect, insertBefore);
	}

	/**
	 * 克隆行
	 * 
	 * @param isAdjust
	 *            克隆时是否调整表达式
	 */
	public void dupRow(boolean isAdjust) {
		if (isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.insertmore"));
			return;
		}
		CellRect rect = getSelectedRect();
		if (rect == null)
			return;
		Matrix matrix = GMSpl.getMatrixCells(control.cellSet,
				new CellRect(rect.getBeginRow(), 1, rect.getRowCount(),
						control.cellSet.getColCount()), true);
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		AtomicSpl insertCmd = new AtomicSpl(control);
		if (rect.getEndRow() != control.cellSet.getRowCount()) {
			insertCmd.setType(AtomicSpl.INSERT_ROW);
		} else {
			insertCmd.setType(AtomicSpl.ADD_ROW);
		}
		CellRect newRect = new CellRect(rect.getEndRow() + 1, 1,
				rect.getRowCount(), control.cellSet.getColCount());
		ArrayList<RowCell> oneRowCells = getApprCopiedRowCells(rect
				.getBeginRow());
		insertCmd.setValue(oneRowCells);
		insertCmd.setRect(newRect);
		cmds.add(insertCmd);

		AtomicSpl setCmd = new AtomicSpl(control);
		setCmd.setType(AtomicSpl.SET_RECTCELLS);
		setCmd.setRect(rect);
		CellSelection cs = new CellSelection(matrix, newRect, control.cellSet);
		cs.setAdjustSelf(isAdjust);
		setCmd.setValue(cs);
		cmds.add(setCmd);
		this.executeCmd(cmds);
	}

	/**
	 * 显示格值
	 */
	public void showCellValue() {
		CellRect cr = getSelectedRect();
		if (cr == null)
			return;
		// 显示结果并自动钉住、不移动光标
		NormalCell nc = (NormalCell) control.cellSet.getCell(cr.getBeginRow(),
				cr.getBeginCol());
		if (nc != null) {
			Object value = nc.getValue();
			GVSpl.panelValue.tableValue.setValue1(value, nc.getCellId());
			GVSpl.panelValue.tableValue.setLocked(true);
		}
	}

	/**
	 * 取插入/追加列的原子命令
	 * 
	 * @param insertBefore
	 *            是否插入列。true插入列，false追加列
	 * @param rect
	 *            选择的格子矩阵
	 * @return
	 */
	public IAtomicCmd getInsertCol(boolean insertBefore, CellRect rect) {
		AtomicSpl aq = new AtomicSpl(control);
		if (insertBefore) {
			aq.setType(AtomicSpl.INSERT_COL);
		} else {
			aq.setType(AtomicSpl.ADD_COL);
		}
		aq.setRect(rect);

		ArrayList<ColCell> oneColCells = getApprCopiedColCells(rect
				.getBeginCol());
		aq.setValue(oneColCells);

		return aq;
	}

	/**
	 * 复制指定列外观属性的格子,包含列首格
	 * 
	 * @param col
	 *            列号
	 * @return
	 */
	private ArrayList<ColCell> getApprCopiedColCells(int col) {
		ArrayList<ColCell> oneColCells = new ArrayList<ColCell>();
		CellSet ics = control.cellSet;
		ColCell cc = (ColCell) ics.getColCell(col).deepClone();
		oneColCells.add(cc);
		return oneColCells;
	}

	/**
	 * 克隆格子属性映射表
	 * 
	 * @param aMap
	 * @return
	 */
	private IByteMap cloneAMap(IByteMap aMap) {
		if (aMap == null) {
			return null;
		}
		return (IByteMap) aMap.deepClone();
	}

	/**
	 * 取格子属性映射表
	 * 
	 * @param type
	 *            选择的状态，GC中定义的常量
	 * @return
	 */
	private IByteMap getCellByteMap(byte type) {
		IByteMap bm = new ByteMap();
		switch (type) {
		case GCSpl.SELECT_STATE_CELL:
			NormalCell nc = getDisplayCell();
			if (nc == null) {
				return null;
			}

			bm.put(AtomicCell.CELL_VALUE, nc.getValue());
			bm.put(AtomicCell.CELL_EXP, nc.getExpString());
			break;
		case GCSpl.SELECT_STATE_COL:
			if (!selectedCols.isEmpty()) {
				ColCell cc = (ColCell) control.cellSet
						.getColCell(((Integer) selectedCols.get(0)).intValue());
				bm.put(AtomicCell.COL_WIDTH, new Float(cc.getWidth()));
			}
			break;
		case GCSpl.SELECT_STATE_ROW:
			if (!selectedRows.isEmpty()) {
				RowCell rc = (RowCell) control.cellSet
						.getRowCell(((Integer) selectedRows.get(0)).intValue());
				bm.put(AtomicCell.ROW_HEIGHT, new Float(rc.getHeight()));
			}
			break;
		}
		return bm;
	}

	/**
	 * 取当前显示的单元格
	 * 
	 * @return
	 */
	public NormalCell getDisplayCell() {
		if (isNothingSelected()) {
			return null;
		}

		CellRect cr = (CellRect) selectedRects.get(0);
		NormalCell nc = null;
		for (int r = cr.getBeginRow(); r <= cr.getEndRow(); r++) {
			for (int c = cr.getBeginCol(); c <= cr.getEndCol(); c++) {
				try {
					nc = (NormalCell) control.cellSet.getCell(r, c);
				} catch (Exception x) {
				}
				if (nc != null) {
					return nc;
				}
			}
		}
		return null;
	}

	/**
	 * 删除
	 * 
	 * @param cmd
	 *            GCSpl中定义的常量
	 * @return
	 */
	public boolean delete(short cmd) {
		CellSet cellSet = control.cellSet;
		int TOTAL_ROWS = cellSet.getRowCount();
		int TOTAL_COLS = (int) cellSet.getColCount();
		if (cmd == GCSpl.iDELETE_COL) {
			if (TOTAL_COLS == selectedCols.size()) {
				GM.messageDialog(GV.appFrame,
						mm.getMessage("dfxeditor.notdelallcol"),
						mm.getMessage("public.prompt"),
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		} else if (cmd == GCSpl.iDELETE_ROW) {
			if (TOTAL_ROWS == selectedRows.size()) {
				GM.messageDialog(GV.appFrame,
						mm.getMessage("dfxeditor.notdelallrow"),
						mm.getMessage("public.prompt"),
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		deleteRowOrCol(cmd);
		return true;
	}

	/**
	 * 单元格文本编辑
	 */
	public void textEditor() {
		control.getContentPanel().submitEditor();
		NormalCell cell = getDisplayCell();
		String exp = cell.getExpString();
		DialogTextEditor dte = new DialogTextEditor(GV.appFrame);
		dte.setText(exp);
		try {
			dte.setVisible(true);
		} catch (Exception e) {
		}
		if (dte.getOption() == JOptionPane.OK_OPTION) {
			exp = dte.getText();
			AtomicCell ac = new AtomicCell(control, cell);
			ac.setProperty(AtomicCell.CELL_EXP);
			ac.setValue(exp);
			executeCmd(ac);
		}
	}

	/**
	 * 注释
	 * 
	 * @return
	 */
	public boolean note() {
		if (isMultiRectSelected()) {
			// 不能注释多片区域。
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.notemore"),
					mm.getMessage("public.prompt"), JOptionPane.WARNING_MESSAGE);
			return false;
		}
		control.getContentPanel().submitEditor();
		CellRect rect = getSelectedRect();
		INormalCell cell;
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int row = rect.getBeginRow(); row <= rect.getEndRow(); row++) {
			for (int col = rect.getBeginCol(); col <= rect.getEndCol(); col++) {
				cell = control.cellSet.getCell(row, col);
				AtomicCell ac = new AtomicCell(control, cell);
				ac.setProperty(AtomicCell.CELL_EXP);
				String exp = cell.getExpString();
				if (exp == null) {
					continue;
				}
				if (exp.startsWith("//")) {
					exp = exp.substring(2);
				} else {
					exp = "/" + exp;
				}
				ac.setValue(exp);
				cmds.add(ac);
			}
		}
		if (cmds.isEmpty()) {
			return false;
		}
		return executeCmd(cmds);
	}

	/**
	 * 设置单元格提示
	 */
	public void setTips() {
		Vector<CellRect> rects = getSelectedRects();
		if (rects == null) {
			return;
		}
		control.getContentPanel().submitEditor();
		DialogInputText dit = new DialogInputText(true);
		dit.setText(getDisplayCell().getTip());
		dit.setTitle(mm.getMessage("dfxeditor.tip"));
		dit.setVisible(true);
		if (dit.getOption() != JOptionPane.OK_OPTION) {
			return;
		}
		String tips = dit.getText();
		CellRect rect;
		INormalCell cell;
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int i = 0; i < rects.size(); i++) {
			rect = (CellRect) rects.get(i);
			for (int row = rect.getBeginRow(); row <= rect.getEndRow(); row++) {
				for (int col = rect.getBeginCol(); col <= rect.getEndCol(); col++) {
					cell = control.cellSet.getCell(row, col);
					AtomicCell ac = new AtomicCell(control, cell);
					ac.setProperty(AtomicCell.CELL_TIPS);
					ac.setValue(tips);
					cmds.add(ac);
				}
			}
		}
		executeCmd(cmds);
	}

	/**
	 * 剪切
	 * 
	 * @return
	 */
	public boolean cut() {
		if (isMultiRectSelected()) {
			// 不能剪切多片区域。
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.cutmore"),
					mm.getMessage("public.prompt"), JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return copy(true, false);
	}

	/**
	 * 复制选中的格子
	 * 
	 * @return boolean,没有格子时返回false
	 */
	public boolean copy() {
		return copy(false, false);
	}

	/**
	 * 复制格子，同时生产系统剪贴板串
	 * 
	 * @param isCutStatus
	 *            boolean,是否剪切状态
	 * @param valueCopy
	 *            boolean，是否生成值串化的系统剪贴板
	 * @return boolean
	 */
	public boolean copy(boolean isCutStatus, boolean valueCopy) {
		if (isNothingSelected()) {
			return false;
		}
		if (isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.copymore"));
			return false;
		}
		CellRect cr = getSelectedRect();
		Matrix matrix = getSelectedMatrix(cr);
		GVSpl.cellSelection = new CellSelection(matrix, cr,
				control.getCellSet(), valueCopy);
		CellSet cellSet = control.cellSet;
		ArrayList<IRowCell> rowHeaders = new ArrayList<IRowCell>();
		CellSetParser parser = new CellSetParser(control.getCellSet());
		if (selectState == GCSpl.SELECT_STATE_ROW) {
			for (int r = cr.getBeginRow(); r <= cr.getEndRow(); r++) {
				if (parser.isRowVisible(r)) {
					rowHeaders.add(cellSet.getRowCell(r));
				}
			}
			GVSpl.cellSelection.rowHeaderList = rowHeaders;
		}
		ArrayList<IColCell> colHeaders = new ArrayList<IColCell>();
		if (selectState == GCSpl.SELECT_STATE_COL) {
			for (int c = cr.getBeginCol(); c <= cr.getEndCol(); c++) {
				if (parser.isColVisible(c)) {
					colHeaders.add(cellSet.getColCell(c));
				}
			}
			GVSpl.cellSelection.colHeaderList = colHeaders;
		}

		if (isCutStatus) {
			GVSpl.cellSelection.setCutStatus();
		}

		GVSpl.cellSelection.setSelectState(selectState);
		Clipboard cb;
		try {
			cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		} catch (HeadlessException e) {
			cb = null;
		}
		String strCS = GMSpl.getCellSelectionString(matrix, valueCopy);
		if (cb != null) {
			cb.setContents(new StringSelection(strCS), null);
		}
		GVSpl.cellSelection.systemClip = strCS;

		control.resetCellSelection(GVSpl.cellSelection);
		return true;
	}

	/**
	 * 是否可以复制可呈现代码
	 * 
	 * @return
	 */
	public boolean canCopyPresent() {
		if (isNothingSelected()) {
			return false;
		}
		if (isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.copymore"));
			return false;
		}
		CellRect cr = getSelectedRect();
		if (cr.getRowCount() == 0 || cr.getColCount() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 复制可呈现代码对话框
	 * 
	 * @return
	 */
	public boolean copyPresentDialog() {
		if (!canCopyPresent())
			return false;
		DialogCopyPresent dcp = new DialogCopyPresent();
		dcp.setVisible(true);
		if (dcp.getOption() != JOptionPane.OK_OPTION)
			return false;
		return copyPresent();
	}

	/**
	 * 复制可呈现代码
	 * 
	 * @return
	 */
	public boolean copyPresent() {
		String str = getCopyPresentString();
		if (!StringUtils.isValidString(str))
			return false;
		GM.clipBoard(str);
		return true;
	}

	/**
	 * 获取复制可呈现代码字符串
	 * 
	 * @return
	 */
	public String getCopyPresentString() {
		CellRect cr = getSelectedRect();
		CellSetParser parser = new CellSetParser(control.cellSet);
		final String LINE_SEP = GM.getLineSeparator();
		boolean copyHeader = ConfigOptions.bCopyPresentHeader.booleanValue();
		StringBuffer buf = new StringBuffer();
		if (ConfigOptions.iCopyPresentType == ConfigOptions.COPY_HTML) {
			final int headerWidth = control.getRowHeaderPanel().getWidth();
			final int headerHeight = control.getColHeaderPanel().getHeight();
			final int headerFontSize = 12;
			final String COL_SEP = "\t";
			boolean isFirstRow = true;
			buf.append("<table class=\"table\">");
			if (copyHeader) {
				buf.append(LINE_SEP);
				buf.append(COL_SEP + "<tr height=" + headerHeight + "px>");
				buf.append(LINE_SEP);
				buf.append(COL_SEP + COL_SEP + "<td");
				buf.append(" width=" + headerWidth + "px");
				buf.append(" bgcolor=" + color2Html(Color.lightGray));
				buf.append(">");
				buf.append("</td>");
				for (int col = cr.getBeginCol(); col <= cr.getEndCol(); col++) {
					buf.append(LINE_SEP);
					buf.append(COL_SEP + COL_SEP + "<td");
					// 设置列宽
					buf.append(" width="
							+ parser.getColWidth(col, control.scale) + "px");
					buf.append(" align=\"center\"");
					buf.append(" valign=\"center\"");
					buf.append(" bgcolor=" + color2Html(Color.lightGray));
					buf.append(" style=\"font-size:" + headerFontSize + "px");
					buf.append(";color:" + color2Html(Color.BLACK));
					buf.append("\">");
					buf.append(StringUtils.toExcelLabel(col));
					buf.append("</td>");
				}
				buf.append(LINE_SEP);
				buf.append(COL_SEP + "</tr>");
				isFirstRow = false;
			}
			for (int row = cr.getBeginRow(); row <= cr.getEndRow(); row++) {
				if (!parser.isRowVisible(row))
					continue;
				buf.append(LINE_SEP);
				buf.append(COL_SEP + "<tr height="
						+ parser.getRowHeight(row, control.scale) + "px>");
				if (copyHeader) {
					buf.append(LINE_SEP);
					buf.append(COL_SEP + COL_SEP + "<td");
					if (isFirstRow) { // 设置表头列宽
						buf.append(" width=" + headerWidth + "px");
					}
					buf.append(" align=\"center\"");
					buf.append(" valign=\"center\"");
					buf.append(" bgcolor=" + color2Html(Color.lightGray));
					buf.append(" style=\"font-size:" + headerFontSize + "px");
					buf.append(";color:" + color2Html(Color.BLACK));
					buf.append("\">");
					buf.append(row);
					buf.append("</td>");
				}
				for (int col = cr.getBeginCol(); col <= cr.getEndCol(); col++) {
					if (!parser.isColVisible(col)) {
						continue;
					}
					buf.append(LINE_SEP);
					buf.append(COL_SEP + COL_SEP + "<td");
					if (isFirstRow) { // 设置列宽
						buf.append(" width="
								+ parser.getColWidth(col, control.scale) + "px");
					}
					int halign = parser.getHAlign(row, col);
					buf.append(" align=");
					if (halign == IStyle.HALIGN_LEFT) {
						buf.append("\"left\"");
					} else if (halign == IStyle.HALIGN_RIGHT) {
						buf.append("\"right\"");
					} else {
						buf.append("\"center\"");
					}
					int valign = parser.getVAlign(row, col);
					buf.append(" valign=");
					if (valign == IStyle.VALIGN_TOP) {
						buf.append("\"top\"");
					} else if (valign == IStyle.VALIGN_BOTTOM) {
						buf.append("\"bottom\"");
					} else {
						buf.append("\"center\"");
					}
					Color bc = parser.getBackColor(row, col);
					Color fc = parser.getForeColor(row, col);
					buf.append(" bgcolor=" + color2Html(bc));
					buf.append(" style=\"font-size:" + GC.font.getSize() + "px");
					buf.append(";color:" + color2Html(fc));
					buf.append("\">");
					String text = parser.getDispText(row, col);
					boolean isBold = parser.isBold(row, col);
					boolean isItalic = parser.isItalic(row, col);
					boolean isUnderline = parser.isUnderline(row, col);
					if (isBold)
						buf.append("<b>");
					if (isItalic)
						buf.append("<i>");
					if (isUnderline)
						buf.append("<u>");
					if (text == null) {
						buf.append("");
					} else {
						buf.append(text);
					}
					if (isBold)
						buf.append("</b>");
					if (isItalic)
						buf.append("</i>");
					if (isUnderline)
						buf.append("</u>");
					buf.append("</td>");
				}
				if (isFirstRow)
					isFirstRow = false;
				buf.append(LINE_SEP);
				buf.append(COL_SEP + "</tr>");
			}
			buf.append(LINE_SEP);
			buf.append("</table>");
		} else { // text
			final String COL_SEP = ConfigOptions.sCopyPresentSep;
			if (copyHeader) {
				buf.append("");
				for (int col = cr.getBeginCol(); col <= cr.getEndCol(); col++) {
					buf.append(COL_SEP);
					buf.append(StringUtils.toExcelLabel(col));
				}
				buf.append(LINE_SEP);
			}
			for (int row = cr.getBeginRow(); row <= cr.getEndRow(); row++) {
				if (!parser.isRowVisible(row))
					continue;
				if (copyHeader) {
					buf.append(row);
					buf.append(COL_SEP);
				}
				for (int col = cr.getBeginCol(); col <= cr.getEndCol(); col++) {
					if (!parser.isColVisible(col)) {
						continue;
					}
					if (col > cr.getBeginCol()) {
						buf.append(COL_SEP);
					}
					String text = parser.getDispText(row, col);
					if (text == null) {
						buf.append("");
					} else {
						buf.append(text);
					}
				}
				if (row < cr.getEndRow())
					buf.append(LINE_SEP);
			}
		}
		return buf.toString();
	}

	/**
	 * 将颜色转为html格式
	 * 
	 * @param color
	 *            颜色
	 * @return
	 */
	private String color2Html(Color color) {
		String R = Integer.toHexString(color.getRed());
		String G = Integer.toHexString(color.getGreen());
		String B = Integer.toHexString(color.getBlue());
		if (R.length() == 1)
			R = "0" + R;
		if (G.length() == 1)
			G = "0" + G;
		if (B.length() == 1)
			B = "0" + B;
		return "#" + R + G + B;
	}

	/**
	 * 代码复制
	 * 
	 * @return
	 */
	public boolean codeCopy() {
		if (isNothingSelected()) {
			return false;
		}
		if (isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.copymore"));
			return false;
		}
		CellRect cr = getSelectedRect();
		CellSetParser parser = new CellSetParser(control.cellSet);
		StringBuffer buf = new StringBuffer();
		for (int row = cr.getBeginRow(); row <= cr.getEndRow(); row++) {
			if (!parser.isRowVisible(row))
				continue;
			if (buf.length() > 0)
				buf.append("\n");
			for (int col = cr.getBeginCol(); col <= cr.getEndCol(); col++) {
				if (col > cr.getBeginCol())
					buf.append("\t");
				String text = parser.getDispText(row, col);
				if (text == null) {
					buf.append("");
				} else {
					buf.append(text);
				}
			}
		}
		if (buf.length() == 0)
			return false;
		GM.clipBoard(Escape.addEscAndQuote(buf.toString()));
		return true;
	}

	/**
	 * 取选择的格子矩阵
	 * 
	 * @param rect
	 *            选择的格子矩形
	 * @return
	 */
	private Matrix getSelectedMatrix(CellRect rect) {
		return GMSpl.getMatrixCells(control.cellSet, rect);
	}

	/**
	 * 展开行
	 * 
	 * @param row
	 *            行号
	 * @return
	 */
	public boolean expandRow(int row) {
		CellSetParser parser = new CellSetParser(control.cellSet);
		int subEnd = parser.getSubEnd(row);
		if (subEnd <= row)
			return false;
		boolean isExpand = parser.isSubExpand(row, subEnd);
		RowCell rc;
		for (int r = row + 1; r <= subEnd; r++) {
			rc = (RowCell) control.cellSet.getRowCell(r);
			rc.setVisible(isExpand ? RowCell.VISIBLE_ALWAYSNOT
					: RowCell.VISIBLE_ALWAYS);
		}
		setDataChanged(true);
		return true;
	}

	/**
	 * 从剪贴板粘贴格子或者文本数据
	 * 
	 * @return boolean 粘贴后是否调整自身表达式
	 */
	public boolean paste(boolean isAdjustSelf) {
		return paste(isAdjustSelf, PASTE_OPTION_NORMAL);
	}

	/**
	 * 从剪贴板粘贴格子或者文本数据
	 * 
	 * @param isAdjustSelf
	 *            粘贴后是否调整自身表达式
	 * @param pasteOption
	 *            粘贴时目标区域清空方式，当有清空方式时，按选择区域循环粘贴功能无效
	 * @return
	 */
	public boolean paste(boolean isAdjustSelf, byte pasteOption) {
		// 有清空方式时，选择区域无效
		int curRow = 1;
		int curCol = 1;
		if (pasteOption != PASTE_OPTION_NORMAL) {
			CellRect cr = getSelectedRect();
			selectedRects.clear();
			curRow = cr.getBeginRow();
			curCol = cr.getBeginCol();
			selectedRects.add(new CellRect(curRow, curCol, 1, (int) 1));
		}

		if (isMultiRectSelected()) {
			GM.messageDialog(GV.appFrame, mm.getMessage("dfxeditor.pastemore"));
			return false;
		}

		CellRect targetArea = new CellRect(curRow, curCol, 1, (int) 1);
		Vector<IAtomicCmd> cmds = null;
		String sysclip = GM.clipBoard();
		if (GVSpl.cellSelection != null
				&& GVSpl.cellSelection.srcCellSet instanceof PgmCellSet) {
			Object clip = GVSpl.cellSelection.systemClip;
			if (clip.equals(sysclip)) {
				if (pasteOption != PASTE_OPTION_NORMAL) {
					targetArea.setRowCount(GVSpl.cellSelection.matrix
							.getRowSize());
					targetArea.setColCount(GVSpl.cellSelection.matrix
							.getColSize());
					try {
						cmds = executePasteOption(targetArea, pasteOption);
					} catch (Exception e) {
						GM.messageDialog(GV.appFrame, e.getMessage());
						return false;
					}
				}
				return pasteCell(isAdjustSelf, cmds);
			}
		}

		if (StringUtils.isValidString(sysclip)) {
			if (pasteOption != PASTE_OPTION_NORMAL) {
				String data = sysclip;
				if (StringUtils.isValidString(data)) {
					Matrix matrix = GMSpl.string2Matrix(data);
					targetArea.setRowCount(matrix.getRowSize());
					targetArea.setColCount(matrix.getColSize());

					try {
						cmds = executePasteOption(targetArea, pasteOption);
					} catch (Exception e) {
						GM.messageDialog(GV.appFrame, e.getMessage());
						return false;
					}
				}
			}
			return pasteValue(cmds);
		}

		return false;
	}

	/**
	 * 执行选项粘贴
	 * 
	 * @param rect
	 *            选择的格子矩形
	 * @param option
	 *            粘贴选项
	 * @return
	 */
	private Vector<IAtomicCmd> executePasteOption(CellRect rect, byte option)
			throws Exception {
		Vector<IAtomicCmd> cmds;
		CellSelection cs = GV.cellSelection;
		switch (option) {
		case PASTE_OPTION_INSERT_ROW:
			cmds = new Vector<IAtomicCmd>();
			cmds.add(getInsertRow(
					true,
					new CellRect(rect.getBeginRow(), rect.getBeginCol(), rect
							.getRowCount(), 1)));
			return cmds;
		case PASTE_OPTION_INSERT_COL:
			cmds = new Vector<IAtomicCmd>();
			cmds.add(getInsertCol(true,
					new CellRect(rect.getBeginRow(), rect.getBeginCol(), 1,
							rect.getColCount())));
			return cmds;
		case PASTE_OPTION_PUSH_RIGHT:
			try {
				hotKeyInsert(HK_CTRL_INSERT, rect);
			} catch (Exception x) {
				// 当前位置的行数不够！
				throw new RQException(mm.getMessage("dfxeditor.notenoughrows"));
			}
			break;
		case PASTE_OPTION_PUSH_BOTTOM:
			try {
				hotKeyInsert(HK_ALT_INSERT, rect);
			} catch (Exception x) {
				// 当前位置的列数不够！
				throw new RQException(mm.getMessage("dfxeditor.notenoughcols"));
			}
			break;
		}
		control.resetCellSelection(cs);
		return null;
	}

	/**
	 * 粘贴格子
	 * 
	 * @param isAdjustSelf
	 *            粘贴后是否调整自身表达式
	 * @param cmds
	 *            粘贴时需要执行的命令，可能为null
	 * @return
	 */
	private boolean pasteCell(boolean isAdjustSelf, Vector<IAtomicCmd> cmds) {
		if (cmds == null) {
			cmds = new Vector<IAtomicCmd>();
		}
		CellRect targetRect = getSelectedRect();
		if (targetRect == null) {
			GM.messageDialog(GV.appFrame,
					mm.getMessage("dfxeditor.notselecttarget"));
			return false;
		}
		CellSelection cs = GVSpl.cellSelection;
		if (cs == null) {
			return false;
		}
		if (cs.isCutStatus()) {
			if (cs.srcCellSet != control.getCellSet()) {
				GM.messageDialog(GV.appFrame,
						mm.getMessage("spleditor.notcutothersheet"));
				return false;
			}
		}
		CellSetParser parser = new CellSetParser(control.getCellSet());
		cs.setAdjustSelf(isAdjustSelf);

		targetRect.setColCount((int) cs.matrix.getColSize());
		targetRect.setRowCount(cs.matrix.getRowSize());

		boolean isCut = (cs.srcCellSet == control.getCellSet())
				&& cs.isCutStatus();
		if (isCut) {
			// 剪切时用移动矩形, 不是同一个sheet,不支持剪切操作
			cmds = GMSpl.getMoveRectCmd(this, cs.rect, targetRect);
			GM.clipBoard(null);
		} else {
			int hideRowCount = 0;
			for (int r = targetRect.getBeginRow(); r <= control.cellSet
					.getRowCount(); r++) {
				if (!parser.isRowVisible(r)) {
					hideRowCount++;
				}
			}
			if (targetRect.getEndRow() + hideRowCount > control.cellSet
					.getRowCount()) {
				// 行数不够补充
				int addRowCount = targetRect.getEndRow() + hideRowCount
						- control.cellSet.getRowCount();
				cmds.add(getAppendRows(addRowCount));
			}
			int hideColCount = 0;
			for (int c = targetRect.getBeginCol(); c <= control.cellSet
					.getColCount(); c++) {
				if (!parser.isColVisible(c)) {
					hideColCount++;
				}
			}
			if (targetRect.getEndCol() + hideColCount > control.cellSet
					.getColCount()) {
				// 列数不够补充
				int addColCount = targetRect.getEndCol() + hideColCount
						- control.cellSet.getColCount();
				cmds.add(getAppendCols(addColCount));
			}
			Area area = control.getSelectedArea(0);
			if (area.getEndRow() == area.getBeginRow()
					&& area.getBeginCol() == area.getEndCol()) {
				// 只选择一个格子的情况，粘贴全部
				int endRow = targetRect.getEndRow();
				int rc = 0;
				for (int r = targetRect.getBeginRow(); r <= control.cellSet
						.getRowCount(); r++) {
					if (parser.isRowVisible(r)) {
						rc++;
						if (rc >= targetRect.getRowCount()) {
							endRow = r;
							break;
						}
					}
				}
				int endCol = targetRect.getEndCol();
				int cc = 0;
				for (int c = targetRect.getBeginCol(); c <= control.cellSet
						.getColCount(); c++) {
					if (parser.isColVisible(c)) {
						cc++;
						if (cc >= targetRect.getColCount()) {
							endCol = c;
							break;
						}
					}
				}
				area = new Area(targetRect.getBeginRow(),
						targetRect.getBeginCol(), endRow, endCol);
				targetRect = new CellRect(area);
			} else if (selectState == cs.selectState) {
				// 选择一行或者一列，而且源区域也是对应的选择行列的情况，也粘贴全部
				if ((selectState == GC.SELECT_STATE_ROW && selectedRows.size() == 1)
						|| (selectState == GC.SELECT_STATE_COL && selectedCols
								.size() == 1)) {
					area = new Area(targetRect.getBeginRow(),
							targetRect.getBeginCol(), targetRect.getEndRow(),
							targetRect.getEndCol());
					targetRect = new CellRect(area);
				}
			}

			// 剪切时不应该执行下面动作
			AtomicSpl ar = new AtomicSpl(control);
			ar.setType(AtomicSpl.PASTE_SELECTION);
			ar.setRect(targetRect);
			ar.setValue(cs);

			cmds.add(ar);

		}
		executeCmd(cmds);

		// 粘贴完后还能粘
		if (!isCut) {
			control.resetCellSelection(cs);
		} else {
			moveRect();
		}
		return true;
	}

	/**
	 * 执行剪切
	 */
	protected void moveRect() {
	}

	/**
	 * 单元格是否可以忽略
	 * 
	 * @param parser
	 * @param cp
	 * @return
	 */
	private boolean isCellIgnoreable(CellSetParser parser, CellLocation cp) {
		if (!parser.isRowVisible(cp.getRow())
				|| !parser.isColVisible(cp.getCol())) {
			return true;
		}
		INormalCell cell = control.getCellSet().getCell(cp.getRow(),
				cp.getCol());
		if (cell == null) {
			return true;
		}
		return false;
	}

	/**
	 * 从系统剪贴板粘贴文本
	 * 
	 * @param cmds
	 *            粘贴时需要同时执行的原子命令集
	 * @return
	 */
	private boolean pasteValue(Vector<IAtomicCmd> cmds) {
		CellRect targetRect = getSelectedRect();
		if (targetRect == null) {
			GM.messageDialog(GV.appFrame,
					mm.getMessage("dfxeditor.notselecttarget"));
			return false;
		}
		String data = GM.clipBoard();
		if (!StringUtils.isValidString(data)) {
			return false;
		}
		Matrix matrix = GMSpl.string2Matrix(data);

		targetRect.setColCount((int) matrix.getColSize());
		targetRect.setRowCount(matrix.getRowSize());
		CellSetParser parser = new CellSetParser(control.cellSet);
		int rc = 0;
		for (int r = targetRect.getBeginRow(); r <= control.cellSet
				.getRowCount(); r++) {
			if (parser.isRowVisible(r))
				rc++;
			if (rc >= matrix.getRowSize())
				break;
		}
		if (rc < matrix.getRowSize()) {
			int addRowCount = matrix.getRowSize() - rc;
			this.appendRows(addRowCount);
		}
		int cc = 0;
		for (int c = targetRect.getBeginCol(); c <= control.cellSet
				.getColCount(); c++) {
			if (parser.isColVisible(c))
				cc++;
			if (cc >= matrix.getColSize())
				break;
		}
		if (cc < matrix.getColSize()) {
			int addColCount = matrix.getColSize() - cc;
			this.appendCols(addColCount);
		}
		if (cmds == null) {
			cmds = new Vector<IAtomicCmd>();
		}
		AtomicSpl ar = new AtomicSpl(control);
		ar.setType(AtomicSpl.PASTE_STRINGSELECTION);
		ar.setRect(targetRect);
		ar.setValue(matrix);
		cmds.add(ar);
		executeCmd(cmds);
		return true;
	}

	/** 清除 */
	public static final byte CLEAR = 0;
	/** 清除表达式 */
	public static final byte CLEAR_EXP = 1;
	/** 清除格值 */
	public static final byte CLEAR_VAL = 2;

	/**
	 * 清除
	 * 
	 * @param clearType
	 *            清除方式
	 * @return
	 */
	public boolean clear(byte clearType) {
		if (isNothingSelected()) {
			return false;
		}
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int i = 0; i < selectedRects.size(); i++) {
			CellRect rect = (CellRect) selectedRects.get(i);
			Vector<IAtomicCmd> tmp = getClearRectCmds(rect, clearType);
			if (tmp != null)
				cmds.addAll(tmp);
		}
		this.executeCmd(cmds);
		control.getContentPanel().initEditor(ContentPanel.MODE_HIDE);
		return true;
	}

	/**
	 * 取清除区域的原子命令集
	 * 
	 * @param rect
	 * @param clearType
	 * @return
	 */
	public Vector<IAtomicCmd> getClearRectCmds(CellRect rect, byte clearType) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		switch (clearType) {
		case CLEAR:
			cmds.addAll(getClearCmds(rect, AtomicCell.CELL_EXP));
			cmds.addAll(getClearCmds(rect, AtomicCell.CELL_VALUE));
			cmds.addAll(getClearCmds(rect, AtomicCell.CELL_TIPS));
			break;
		case CLEAR_EXP:
			cmds.addAll(getClearCmds(rect, AtomicCell.CELL_EXP));
			break;
		case CLEAR_VAL:
			cmds.addAll(getClearCmds(rect, AtomicCell.CELL_VALUE));
			break;
		}
		return cmds;
	}

	/**
	 * 取清除的原子命令集
	 * 
	 * @param rect
	 *            清理的区域
	 * @param cmdType
	 *            清理的命令
	 * @return
	 */
	private Vector<IAtomicCmd> getClearCmds(CellRect rect, byte cmdType) {
		CellSet cs = control.cellSet;
		NormalCell nc;
		AtomicCell ac;
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
			for (int c = rect.getBeginCol(); c <= rect.getEndCol(); c++) {
				nc = (NormalCell) cs.getCell(r, c);
				ac = new AtomicCell(control, nc);
				ac.setProperty(cmdType);
				ac.setValue(null);
				cmds.add(ac);
			}
		}
		return cmds;
	}

	/**
	 * 删除行列
	 * 
	 * @param cmd
	 *            删除的命令
	 */
	private void deleteRowOrCol(short cmd) {
		Vector<IAtomicCmd> cmds = new Vector<IAtomicCmd>();
		for (int i = 0; i < selectedRects.size(); i++) {
			CellRect rect = (CellRect) selectedRects.get(i);
			switch (cmd) {
			case GCSpl.iDELETE_ROW:
				AtomicSpl aqr = new AtomicSpl(control);
				aqr.setType(AtomicSpl.REMOVE_ROW);
				aqr.setRect(rect);
				cmds.add(aqr);
				for (int r = rect.getBeginRow(); r <= rect.getEndRow(); r++) {
					control.removeRowBreakPoints(r);
				}
				break;
			case GCSpl.iDELETE_COL:
				AtomicSpl aqc = new AtomicSpl(control);
				aqc.setType(AtomicSpl.REMOVE_COL);
				aqc.setRect(rect);
				cmds.add(aqc);
				for (int c = rect.getBeginCol(); c <= rect.getEndCol(); c++) {
					control.removeColBreakPoints(c);
				}
				break;
			}
		}
		executeCmd(cmds);

		// 当选中区域为最后行列时，删除后清除当前选中区域
		CellRect rect = getSelectedRect();
		if (rect != null) {
			CellSet cs = control.getCellSet();
			if (rect.getEndRow() > cs.getRowCount()
					|| rect.getEndCol() > cs.getColCount()) {
				control.clearSelectedAreas();
			}
		}
	}

	/**
	 * 选择的区域
	 */
	private Vector<Object> selectedAreas;

	/**
	 * 设置选择的区域
	 * 
	 * @param selectedAreas
	 *            选择的区域
	 */
	public void setSelectedAreas(Vector<Object> selectedAreas) {
		this.selectedAreas = selectedAreas;
	}

	/**
	 * 重置选择的区域
	 */
	public void resetSelectedAreas() {
		control.setSelectedAreas(selectedAreas);
		if (selectedAreas != null && !selectedAreas.isEmpty()) {
			Area area;
			for (int i = 0; i < selectedAreas.size(); i++) {
				area = (Area) selectedAreas.get(i);
				if (area.getEndRow() > control.cellSet.getRowCount()) {
					selectedAreas.clear();
					int endRow = control.cellSet.getRowCount();
					if (selectState == GC.SELECT_STATE_ROW) {
						selectedAreas.add(new Area(endRow, 1, endRow,
								control.cellSet.getColCount()));
					} else if (selectState == GC.SELECT_STATE_CELL) {
						selectedAreas.add(new Area(endRow, area.getBeginCol(),
								endRow, area.getEndCol()));
					}
					break;
				} else if (area.getEndCol() > control.cellSet.getColCount()) {
					selectedAreas.clear();
					int endCol = control.cellSet.getColCount();
					if (selectState == GC.SELECT_STATE_COL) {
						selectedAreas.add(new Area(1, endCol, control.cellSet
								.getRowCount(), endCol));
					} else if (selectState == GC.SELECT_STATE_CELL) {
						selectedAreas.add(new Area(area.getBeginRow(), endCol,
								area.getEndRow(), endCol));
					}
					break;
				}
			}
			if (!selectedAreas.isEmpty()) {
				if (selectState == GC.SELECT_STATE_ROW) {
					selectedRows.clear();
					int bc, ec;
					for (int i = 0; i < selectedAreas.size(); i++) {
						area = (Area) selectedAreas.get(i);
						bc = area.getBeginCol();
						ec = area.getEndCol();
						if (bc == 1 && ec == control.cellSet.getColCount()) {
							for (int r = area.getBeginRow(); r <= area
									.getEndRow(); r++) {
								selectedRows.add(new Integer(r));
							}
						}
					}
				} else if (selectState == GC.SELECT_STATE_COL) {
					selectedCols.clear();
					int br, er;
					for (int i = 0; i < selectedAreas.size(); i++) {
						area = (Area) selectedAreas.get(i);
						br = area.getBeginRow();
						er = area.getEndRow();
						if (br == 1 && er == control.cellSet.getRowCount()) {
							for (int r = area.getBeginCol(); r <= area
									.getEndCol(); r++) {
								selectedCols.add(new Integer(r));
							}
						}
					}
				}
				selectedRects.clear();
				for (int i = 0; i < selectedAreas.size(); i++) {
					selectedRects
							.add(new CellRect((Area) selectedAreas.get(i)));
				}
			}
		}
		if (selectedAreas == null || selectedAreas.isEmpty()) {
			selectedRects.clear();
			selectedRows.clear();
			selectedCols.clear();
			control.m_activeCell = null;
			setSelectState(GC.SELECT_STATE_NONE);
		}
	}

	/**
	 * 重置编辑器
	 */
	public void resetEditor() {
		resetSelectedAreas();
		redrawRowHeader();
		resetActiveCell();
	}

	/**
	 * 重置当前格
	 */
	public void resetActiveCell() {
		NormalCell cell = getDisplayCell();
		if (cell != null) {
			CellSetParser parser = new CellSetParser(control.cellSet);
			int row = cell.getRow();
			int col = cell.getCol();
			int rc = control.cellSet.getRowCount();
			int cc = control.cellSet.getColCount();
			if (cell != null && row <= rc && col <= cc) {
				PgmNormalCell cellNew = control.cellSet.getPgmNormalCell(row,
						col);
				if (cellNew != cell) {
					control.setActiveCell(
							new CellLocation(cell.getRow(), cell.getCol()),
							false);
				}
				if (!parser.isRowVisible(row)
						|| ((ColCell) control.cellSet.getColCell(col))
								.getVisible() == ColCell.VISIBLE_ALWAYSNOT) {
					control.setActiveCell(null, false);
				}
			}
		}
	}

	/**
	 * 重画行表头
	 */
	public void redrawRowHeader() {
		// 引起行层次面板改变的动作，要刷新一下
		control.getRowHeader().updateUI();
	}

	/**
	 * 设置选择的状态
	 * 
	 * @param state
	 *            选择的状态。GC中常量
	 */
	private void setSelectState(byte state) {
		selectState = state;
		((SheetSpl) GVSpl.appSheet).selectState = state;
	}

	/**
	 * 选择区域
	 * 
	 * @param scrollActiveCellToVisible
	 *            是否滚动到选择区域显示
	 */
	public void selectAreas(boolean scrollActiveCellToVisible) {
		if (selectedAreas != null && !selectedAreas.isEmpty()) {
			Area area = (Area) selectedAreas.get(0);
			CellLocation pos = control.getActiveCell();
			CellSetParser parser = new CellSetParser(control.cellSet);
			int row = area.getBeginRow();
			int col = area.getBeginCol();
			if (pos == null || pos.getRow() != row || pos.getCol() != col) {
				if (!parser.isRowVisible(row) || !parser.isColVisible(col)) {
					control.m_activeCell = null;
					setSelectState(GC.SELECT_STATE_NONE);
				} else {
					control.setActiveCell(new CellLocation(row, col), false,
							scrollActiveCellToVisible);
					if (selectState == GC.SELECT_STATE_NONE) {
						if (selectedRows != null && !selectedRows.isEmpty()) {
							setSelectState(GC.SELECT_STATE_ROW);
						} else if (selectedCols != null
								&& !selectedCols.isEmpty()) {
							setSelectState(GC.SELECT_STATE_COL);
						} else {
							setSelectState(GC.SELECT_STATE_CELL);
						}
					}
					GVSpl.toolBarProperty.refresh(selectState, getProperty());
				}
			}
		}
	}

	/**
	 * 键盘ALT-C
	 */
	protected void altC() {

	}

	/**
	 * 键盘ALT-V
	 */
	protected void altV() {

	}

	/**
	 * 删除左边格子，当前格在首列时移动到上一行最后有内容格子的最后。CTRL-BACKSPACE事件
	 */
	public void ctrlBackSpace() {
		CellLocation activeCell = control.getActiveCell();
		if (activeCell == null) {
			return;
		}
		int curCol = activeCell.getCol();
		int curRow = activeCell.getRow();
		CellSet ics = control.getCellSet();

		CellRect srcRect, tarRect;

		if (curCol > 1) {
			int moveCols = ics.getColCount() - curCol + 1;
			srcRect = new CellRect(curRow, curCol, 1, moveCols);
			tarRect = new CellRect(curRow, curCol - 1, 1, moveCols);
			moveRect(srcRect, tarRect);
		} else if (curRow > 1) {
			int topUsedCols = getUsedCols(ics, curRow - 1);
			connectRowUpTo(curRow, topUsedCols + 1);
		}

	}

	/**
	 * 删除本格。只选中一个格子，而且后面为空时〔包含本格〕,将下一行接上来。CTRL-DELETE事件
	 */
	public void ctrlDelete() {
		Area a = null;
		CellRect rect = null;
		if (control.getSelectedAreas().size() > 0) {
			a = control.getSelectedArea(0);
			rect = new CellRect(a);
		}
		CellLocation activeCell = control.getActiveCell();
		if (activeCell == null) {
			return;
		}
		CellSet ics = control.getCellSet();
		int curCol = activeCell.getCol();
		int curRow = activeCell.getRow();
		int usedCols = getUsedCols(ics, curRow);
		CellRect srcRect, tarRect;

		if ((a.getBeginRow() == a.getEndRow() && a.getBeginCol() == a
				.getEndCol())
				&& usedCols <= curCol
				&& curRow < ics.getRowCount()) {
			connectRowUpTo(curRow + 1, curCol);
		} else {
			int moveCols = ics.getColCount() - a.getEndCol();
			srcRect = new CellRect(a.getBeginRow(), a.getEndCol() + 1,
					rect.getRowCount(), moveCols);
			tarRect = new CellRect(a.getBeginRow(), a.getBeginCol(),
					rect.getRowCount(), moveCols);
			moveRect(srcRect, tarRect);
		}
		control.contentView.reloadEditorText();
	}

	/**
	 * 移动区域
	 * 
	 * @param srcRect
	 *            源区域
	 * @param tarRect
	 *            目标区域
	 * @return
	 */
	private boolean moveRect(CellRect srcRect, CellRect tarRect) {
		return moveRect(srcRect, tarRect, true);
	}

	/**
	 * 移动区域
	 * 
	 * @param srcRect
	 *            源区域
	 * @param tarRect
	 *            目标区域
	 * @param scrollToTarget
	 *            目标区域未显示时，是否滚动到目标区域使其显示
	 * @return
	 */
	private boolean moveRect(CellRect srcRect, CellRect tarRect,
			boolean scrollToTarget) {
		Vector<IAtomicCmd> cmds = GMSpl.getMoveRectCmd(this, srcRect, tarRect);
		if (cmds == null) {
			return false;
		}
		executeCmd(cmds);
		if (scrollToTarget) {
			control.scrollToArea(control.setActiveCell(new CellLocation(tarRect
					.getBeginRow(), tarRect.getBeginCol())));
		}
		return true;
	}

	/**
	 * 将connnectRow连接到上一行的upCol位置
	 * 
	 * @param connectRow
	 *            int
	 * @param upCol
	 *            int
	 */
	private void connectRowUpTo(int connectRow, int upCol) {
		PgmCellSet cellSet = control.getCellSet();
		int usedCols = getUsedCols(cellSet, connectRow);
		if (usedCols == 0) {
			usedCols = 1;
		}
		CellRect srcRect = new CellRect(connectRow, (int) 1, 1, usedCols);
		CellRect tarRect = new CellRect(connectRow - 1, upCol, 1, usedCols);
		Vector<IAtomicCmd> cmds = GMSpl.getMoveRectCmd(this, srcRect, tarRect);
		if (cmds != null && !cmds.isEmpty()) {
			AtomicSpl cmd = new AtomicSpl(control);
			cmd.setType(AtomicSpl.REMOVE_ROW);
			CellRect rect = new CellRect(connectRow, (int) 1, 1,
					(int) cellSet.getColCount());
			cmd.setRect(rect);
			cmds.add(cmd);
			executeCmd(cmds);
			control.scrollToArea(control.setActiveCell(new CellLocation(tarRect
					.getBeginRow(), tarRect.getBeginCol())));
		}
	}

	/**
	 * 获取指定行中使用的列（非空）
	 * 
	 * @param row
	 *            行号
	 * @return
	 */
	private int getUsedCols(CellSet ics, int row) {
		int colCount = ics.getColCount();
		return colCount - getEmptyColumns(ics, row);
	}

	/**
	 * 获取指定行中空的列
	 * 
	 * @param row
	 *            行号
	 * @return
	 */
	private int getEmptyColumns(CellSet ics, int row) {
		int colCount = ics.getColCount();
		for (int c = colCount; c >= 1; c--) {
			INormalCell nc = ics.getCell(row, c);
			if (StringUtils.isValidString(nc.getExpString())) {
				return colCount - c;
			}
		}
		return colCount;
	}
}
