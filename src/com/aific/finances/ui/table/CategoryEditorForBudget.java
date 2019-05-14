package com.aific.finances.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.TableCellEditor;

import com.aific.finances.Categories;
import com.aific.finances.Category;
import com.aific.finances.CategoryType;
import com.aific.finances.ui.LegendIcon;
import com.aific.finances.ui.LegendType;


/**
 * Cell editor
 */
@SuppressWarnings("serial")
public class CategoryEditorForBudget extends AbstractCellEditor implements TableCellEditor {

	private Categories categories;
	private CategoryType type;
	
	private JComboBox<Category>[] combos;
	private int comboIndex;
	
	private TreeSet<Category> sortingSet;


	/**
	 * Create an instance of the cell editor
	 * 
	 * @param type the category type
	 * @param categories the available categories
	 */
	@SuppressWarnings("unchecked")
	public CategoryEditorForBudget(JTable table, CategoryType type, Categories categories) {
		
		this.categories = categories;
		this.type = type;
		
		comboIndex = 0;
		combos = new JComboBox[2];
		
		for (int i = 0; i < combos.length; i++) {
			
			combos[i] = new JComboBox<>();
			combos[i].setBorder(null);
			combos[i].setRenderer(new CategoryComboBoxRenderer());
			
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
		}
		
		sortingSet = new TreeSet<Category>(new Comparator<Category>() {

			@Override
			public int compare(Category o1, Category o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}
			
		});
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
		JComboBox<Category> combo = combos[comboIndex];
		
		combo.removeAllItems();
		
		sortingSet.clear();
		for (Category c : categories.getCategories()) {
			if (c.getType() == type) sortingSet.add(c);
		}
		
		for (Category d : sortingSet) combo.addItem(d);
		combo.setSelectedItem(object);
		
		return combo;
	}
	
	
	/**
	 * Category combo box renderer
	 */
	private class CategoryComboBoxRenderer implements ListCellRenderer<Category> {
		
		private BasicComboBoxRenderer basicRenderer;
		
		private JPanel panel;
		private LegendIcon legendIcon;
		private JLabel label;

		
		/**
		 * Create an instance of the renderer
		 */
		public CategoryComboBoxRenderer()
		{
			basicRenderer = new BasicComboBoxRenderer();
			
			legendIcon = new LegendIcon(LegendType.BAR);
			legendIcon.setOpaque(false);
			
			label = new JLabel("");
			label.setOpaque(false);
			
			panel = new JPanel(new BorderLayout(1, 0));
			panel.add(legendIcon, BorderLayout.WEST);
			panel.add(label, BorderLayout.CENTER);
		}
		
	
		/**
		 * Initialize a cell editor
		 *
		 * @param list the list
		 * @param object the edited object
		 * @param index the index
		 * @param isSelected whether the current row is selected
		 * @param cellHasFocus whether the current row has focus
		 * @return the cell editor
	     */
		@Override
	    public Component getListCellRendererComponent(
	                                       JList<? extends Category> list,
	                                       Category object,
	                                       int index,
	                                       boolean isSelected,
	                                       boolean cellHasFocus)
		{
			Component c = basicRenderer.getListCellRendererComponent(list, object, index, isSelected, cellHasFocus);

			if (object == null) {
				return c;
			}
			else {
				/*if (cellHasFocus || isSelected) {
					c.setBackground(Utils.getColorInBetween(category.getColor(),
							list.getSelectionBackground(), 0.5f));
				}
				else {
					c.setBackground(category.getColor());
				}
				return c;*/
				
				panel.setForeground(list.getForeground());
				panel.setBackground(list.getBackground());
				
				legendIcon.color = object == null ? list.getForeground() : object.getColor();
				
				label.setText(object == null ? "" : object.toString());
				label.setForeground(cellHasFocus || isSelected ? list.getSelectionForeground() : list.getForeground());
				panel.setBackground(cellHasFocus || isSelected ? list.getSelectionBackground() : list.getBackground());
				
				return panel;
			}
	    }
	}
}