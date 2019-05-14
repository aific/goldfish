package com.aific.finances.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.Category;
import com.aific.finances.CategoryDetector;
import com.aific.finances.ui.LegendIcon;
import com.aific.finances.ui.LegendType;


/**
 * Cell renderer
 */
public class CategoryRenderer implements TableCellRenderer {
	
	private JPanel panel;
	private LegendIcon legendIcon;
	private JLabel label;

	private JPanel nullPanel;


	/**
	 * Create an instance of the cell renderer
	 */
	public CategoryRenderer() {
		
		legendIcon = new LegendIcon(LegendType.BAR);
		legendIcon.setOpaque(false);
		
		label = new JLabel("");
		label.setOpaque(false);
		
		panel = new JPanel(new BorderLayout(1, 0));
		panel.add(legendIcon, BorderLayout.WEST);
		panel.add(label, BorderLayout.CENTER);
		
		nullPanel = new JPanel(new BorderLayout(1, 0));
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
		CategoryDetector edited = null;
		Category category = null;

		if (object == null) {
			// Nothing to do
		}
		else if (object instanceof CategoryDetector) {
			edited = (CategoryDetector) object;
			category = edited == null ? null : edited.getCategory();
		}
		else if (object instanceof Category) {
			category = (Category) object;
		}
		
		/*if (category == null) {
			label.setText("");
			label.setBackground(table.getBackground());
		}
		else {
			label.setText(" " + edited.toString() + " ");
			label.setBackground(category.getColor());
		}
		
		return label;*/
		
		if (category == null) {
			
			nullPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return nullPanel;
		}
		else {
			panel.setForeground(table.getForeground());
			panel.setBackground(table.getBackground());
			
			legendIcon.color = category == null ? table.getForeground() : category.getColor();
			
			label.setText(edited == null
					? (category == null ? "(null)" : category.getName())
					: edited.toString());
			label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
			panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			
			return panel;
		}
	}
}