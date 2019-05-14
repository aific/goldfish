package com.aific.finances.ui.table;

import java.awt.Component;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

import com.aific.finances.util.DateFrequencyUnit;
import com.aific.finances.util.DateUnit;


/**
 * Cell editor
 */
@SuppressWarnings("serial")
public class DateFrequencyEditor extends AbstractCellEditor implements TableCellEditor {

	private JComboBox<DateFrequencyUnit>[] combos;
	private int comboIndex;
	
	private TreeSet<DateFrequencyUnit> values;


	/**
	 * Create an instance of the cell editor
	 * 
	 * @param type the category type
	 * @param categories the available categories
	 */
	@SuppressWarnings("unchecked")
	public DateFrequencyEditor(JTable table) {
		
		comboIndex = 0;
		combos = new JComboBox[2];
		
		values = new TreeSet<>();
		values.add(new DateFrequencyUnit(1, DateUnit.DAY));
		values.add(new DateFrequencyUnit(1, DateUnit.WEEK));
		values.add(new DateFrequencyUnit(2, DateUnit.WEEK));
		values.add(new DateFrequencyUnit(1, DateUnit.MONTH));
		values.add(new DateFrequencyUnit(2, DateUnit.MONTH));
		values.add(new DateFrequencyUnit(1, DateUnit.YEAR));

		for (int i = 0; i < combos.length; i++) {
			
			combos[i] = new JComboBox<>();
			combos[i].setBorder(null);
			
			combos[i].putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); 
			combos[i].addPopupMenuListener(new PopupMenuListener() {
				
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				}
				
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
					// Stop cell editing after selecting an option in the combo box
					TableCellEditor editor = table.getCellEditor();
					if (editor != null) editor.stopCellEditing();
				}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent arg0) {
				}
			});
			
			combos[i].removeAllItems();
			for (DateFrequencyUnit v : values) combos[i].addItem(v);
		}
	}


	/**
	 * Update the attribute value
	 *
	 * @return the edited object
	 */
	public Object getCellEditorValue() {
		return combos[comboIndex].getSelectedItem();
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
		
		comboIndex = (comboIndex + 1) % combos.length;
		JComboBox<DateFrequencyUnit> combo = combos[comboIndex];
		
		combo.setSelectedItem(object);
		
		return combo;
	}
}