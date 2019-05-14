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
public class AmountRenderer implements TableCellRenderer {
	
	private JLabel label;
	

	/**
	 * Create an instance of the cell renderer
	 */
	public AmountRenderer() {
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
			
			int cents = amount.intValue();
			int absCents = Math.abs(cents);
			
			String strWhole = Utils.AMOUNT_FORMAT_WHOLE.format(absCents / 100);
			String strCents = Integer.toString(absCents % 100);
			if (strCents.length() == 1) strCents = "0" + strCents;
			
			// Always show the numbers as positive
			label.setText(strWhole + "." + strCents + " ");
		}

		return label;
	}
}