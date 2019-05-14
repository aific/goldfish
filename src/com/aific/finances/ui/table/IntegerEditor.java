package com.aific.finances.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import com.aific.finances.util.Utils;


/**
 * Cell editor for amounts
 */
@SuppressWarnings("serial")
public class IntegerEditor extends AbstractCellEditor implements TableCellEditor {
	
	private static final Pattern PATTERN = Pattern.compile("-?[0-9]+");
	private static final Pattern PATTERN2 = Pattern.compile(
			"-?[1-9][0-9]?[0-9]?(,[0-9][0-9][0-9])?(,[0-9][0-9][0-9])?(,[0-9][0-9][0-9])?(,[0-9][0-9][0-9])");
	
	private JTextField field;
	private JTable table;
	
	private int min;
	private int max;


	/**
	 * Create an instance of the cell editor
	 * 
	 * @param min the minimum allowed value
	 * @param max the maximum allowed value
	 */
	public IntegerEditor(int min, int max) {
		
		this.min = min;
		this.max = max;
		
		table = null;
		
		field = new JTextField();
		field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		field.setHorizontalAlignment(SwingConstants.RIGHT);
	}


	/**
	 * Update the attribute value
	 *
	 * @return the edited object
	 */
	public Object getCellEditorValue() {

		String s = field.getText();
		
		if (!PATTERN.matcher(s).matches() && !PATTERN2.matcher(s).matches()) {
			throw new NumberFormatException("Invalid amount");
		}
		
		s = s.replaceAll(",", "");
		
		
		int n = Integer.parseInt(s);
		
		if (n < min || n > max) {
			throw new NumberFormatException("Out of range");
		}
		
		return n;
	}
	
	
	/**
	 * Stop editing
	 * 
	 * @return true if it is actually ok to stop editing
	 */
	@Override
	public boolean stopCellEditing() {

		boolean result = false;

		try{
			result = super.stopCellEditing();
			field.setBackground(table.getBackground());
			return result;
		}
		catch (NumberFormatException e) {
			field.setBackground(Utils.getColorInBetween(Color.RED, table.getBackground(), 0.75f));
			return false;
		}
	}
	

	/**
	 * Initialize a cell editor
	 *
	 * @param table the table
	 * @param object the edited object
	 * @param isSelected whether the current row is selected
	 * @param row the row number
	 * @param column the column number
	 * @return the cell editor
	 */
	public Component getTableCellEditorComponent(JTable table,
												 Object object,
												 boolean isSelected,
												 int row,
												 int column) {
		Integer amount = (Integer) object;
		this.table = table;

		field.setForeground(table.getForeground());
		field.setBackground(table.getBackground());
		
		if (amount == null) {
			field.setText("");
		}
		else {
			
			String v = Utils.AMOUNT_FORMAT_WHOLE.format(amount.intValue());
			field.setText(v);
		}
		
		return field;
	}
}