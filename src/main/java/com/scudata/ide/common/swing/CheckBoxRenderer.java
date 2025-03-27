package com.scudata.ide.common.swing;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.scudata.ide.common.GM;
import com.scudata.ide.common.GV;

/**
 * JTable单元格复选框渲染器
 *
 */
public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造函数
	 */
	public CheckBoxRenderer() {
		setHorizontalAlignment(JLabel.CENTER);
	}

	/**
	 * 返回编辑控件
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		if (value == null || !(value instanceof Boolean)) {
			value = new Boolean(false);
		}
		try {
			setSelected(((Boolean) value).booleanValue());
		} catch (Exception e) {
			GM.showException(GV.appFrame, e);
		}
		return this;
	}
}
