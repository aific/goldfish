package com.aific.finances.ui.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.util.Utils;


/**
 * Cell renderer for amounts
 */
public class IntegerRenderer implements TableCellRenderer {
	
	private JLabel label;
	

	/**
	 * Create an instance of the cell renderer
	 */
	public IntegerRenderer() {
		label = new JLabel("", SwingConstants.RIGHT);
		label.setOpaque(true);
	}
	
	
	/**
	 * Initialize a cell renderer
	 *
	 * @param table the table
	 * @param object the edited object
	 * @param isSelected whether the current row is selected
	 * @param hasFocus whether the cell has focus
	 * @param row the row number
	 * @param column the column number
	 * @return the cell renderer
	 */
	public Component getTableCellRendererComponent(JTable table, Object object,
												   boolean isSelected, boolean hasFocus,
												   int row, int column) {
		Integer amount = (Integer) object;

		if (isSelected) {
			label.setForeground(table.getSelectionForeground());
			label.setBackground(table.getSelectionBackground());
		}
		else {
			label.setForeground(table.getForeground());
			label.setBackground(table.getBackground());
		}

		if (amount == null) {
			label.setText("");
		}
		else {
			
			String v = Utils.AMOUNT_FORMAT_WHOLE.format(amount.intValue());
			label.setText(v + " ");
		}

		return label;
	}
}