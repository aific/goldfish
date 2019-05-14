package com.aific.finances.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.Categories;
import com.aific.finances.CategoriesListener;
import com.aific.finances.Category;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.util.Accessor;
import com.aific.finances.util.JBetterTable;
import com.aific.finances.util.Utils;


/**
 * A table of categories
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class CategoryEditorTable extends JBetterTable<Category> {
		
	private Handler handler;
	
	private Document document;
	private Categories categories;


	/**
	 * Create an instance of class CategoriesTable
	 * 
	 * @param document the document
	 */
	public CategoryEditorTable(Document document) {
			
		addColumn(new TableColumn<Category>("Name", String.class, true,
				new Accessor<Category, String>() {
		
					@Override
					public String get(Category object) {
						return object.getName();
					}
		
					@Override
					public void set(Category object, String value) {
						if (!value.equals(object.getName())) {
							for (Category c : categories.getCategories()) {
								if (c != object && c.getName().equalsIgnoreCase(value)) {
									JOptionPane.showMessageDialog(CategoryEditorTable.this.getParent(),
											"This name is already used by another category",
											"Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
							object.setName(value);
							CategoryEditorTable.this
								.categories.fireCategoriesDataChanged();
							CategoriesFrame.getInstance().setModified();
						}
					}
				}));
		
		addColumn(new TableColumn<Category>("Type", CategoryType.class, false,
				new Accessor<Category, CategoryType>() {
		
					@Override
					public CategoryType get(Category object) {
						return object.getType();
					}
		
					@Override
					public void set(Category object, CategoryType value) {
						throw new UnsupportedOperationException();
						/*if (!value.equals(object.getType())) {
							object.setType(value);
							CategoriesTable.this
								.categories.fireCategoriesDataChanged();
							CategoriesFrame.getInstance().setModified();
						}*/
					}
				}));
		
		addColumn(new TableColumn<Category>("Color", Color.class, true,
				new Accessor<Category, Color>() {
		
					@Override
					public Color get(Category object) {
						return object.getColor();
					}
		
					@Override
					public void set(Category object, Color value) {
						if (!value.equals(object.getColor())) {
							object.setColor(value);
							CategoryEditorTable.this
								.categories.fireCategoriesDataChanged();
							CategoriesFrame.getInstance().setModified();
						}
					}
				}));
		
		
		// Define the contents
		
		this.document = document;
		data = this.categories = document.getCategories();

		
		// Set the cell editors and renderers

		setDefaultRenderer(Color.class, new ColorRenderer());
		setDefaultEditor  (Color.class, new ColorEditor());
		setDefaultEditor  (CategoryType.class, new TypeEditor());
		

		// Set the column widths and other common properties
		
		setAutoCreateRowSorter(true);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		getRowSorter().setSortKeys(sortKeys); 
		
		
		// Handlers
		
		handler = new Handler();
		categories.addCategoriesListener(handler);
		
		adjustColumns();
	}
		
	
	/**
	 * Get the list of categories
	 * 
	 * @return the list of categories
	 */
	public Categories getCategories() {
		return categories;
	}
	
	
	/**
	 * Get the document
	 * 
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}
	
	
	/**
	 * Get the category by row
	 * 
	 * @param row the row
	 * @return the category
	 */
	public Category getCategory(int row) {
		return categories.get(convertRowIndexToModel(row));
	}
	
	
	/**
	 * Prepare to dispose
	 */
	public void prepareDispose()
	{
		categories.removeCategoriesListener(handler);
	}

	
	/**
	 * The handler
	 */
	private class Handler implements CategoriesListener {

		/**
		 * One or more categories added
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesAdded(Categories list, int from, int to) {
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			model.fireTableRowsInserted(from, to);
			model.fireTableDataChanged();
		}


		/**
		 * One or more categories removed
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesRemoved(Categories list, int from, int to) {
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			model.fireTableRowsDeleted(from, to);
			model.fireTableDataChanged();
		}
		

		/**
		 * Category data changed
		 * 
		 * @param list the category list that triggered this event
		 */
		public void categoriesDataChanged(Categories list) {
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			model.fireTableDataChanged();
		}
	}
	

	/**
	 * Cell renderer
	 */
	private class ColorRenderer implements TableCellRenderer {
		
		private JLabel label;
		

		/**
		 * Create an instance of the cell renderer
		 */
		public ColorRenderer() {
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
			Color color = (Color) object;

			if (isSelected) {
				label.setBackground(Utils.getColorInBetween(color,
						table.getSelectionBackground(), 0.75f));
			}
			else {
				label.setBackground(color);
			}

			return label;
		}
	}
	
	
	/**
	 * Cell editor
	 */
	private class TypeEditor extends AbstractCellEditor implements TableCellEditor {

		private JComboBox<CategoryType>[] combos;
		private int comboIndex;


		/**
		 * Create an instance of the cell editor
		 */
		@SuppressWarnings("unchecked")
		public TypeEditor() {
			
			comboIndex = 0;
			combos = new JComboBox[2];
			
			for (int i = 0; i < combos.length; i++) {
				
				combos[i] = new JComboBox<CategoryType>();
				combos[i].setBorder(null);
				
				combos[i].putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); 
				combos[i].addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
						TableCellEditor editor = CategoryEditorTable.this.getCellEditor();
						if (editor != null) editor.stopCellEditing();
					}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent arg0) {
					}
				});
				
				combos[i].addItem(CategoryType.INCOME);
				combos[i].addItem(CategoryType.EXPENSE);
				combos[i].addItem(CategoryType.BALANCED);
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
			JComboBox<CategoryType> combo = combos[comboIndex];
			
			combo.setSelectedItem((CategoryType) object);
			
			return combo;
		}
	}
	
	
	/**
	 * Cell editor
	 */
	private class ColorEditor extends AbstractCellEditor
	implements TableCellEditor, ActionListener {
		
		// From: http://docs.oracle.com/javase/tutorial/uiswing/components/table.html

		private static final String EDIT = "edit";

		private Color currentColor;
		private JButton button;
		private JColorChooser colorChooser;
		private JDialog dialog;

		
		/**
		 * Create an instance of the cell editor
		 */
		public ColorEditor() {
			
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			//Set up the dialog that the button brings up.
			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(button,
					"Pick a Color",
					true,  //modal
					colorChooser,
					this,  //OK button handler
					null); //no CANCEL button handler
		}

		
		/**
		 * The action handler
		 * 
		 * @param e the action event
		 */
	    public void actionPerformed(ActionEvent e) {
	        if (EDIT.equals(e.getActionCommand())) {
	            //The user has clicked the cell, so
	            //bring up the dialog.
	            button.setBackground(currentColor);
	            colorChooser.setColor(currentColor);
	            dialog.setVisible(true);

	            fireEditingStopped(); //Make the renderer reappear.

	        } else { //User pressed dialog's "OK" button.
	            currentColor = colorChooser.getColor();
	        }
	    }


		/**
		 * Update the attribute value
		 *
		 * @return the edited object
		 */
		public Object getCellEditorValue() {
			return currentColor;
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
			
	        currentColor = (Color) object;
	        return button;
		}
	}
}
