package com.aific.finances.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.Categories;
import com.aific.finances.CategoriesListener;
import com.aific.finances.Category;
import com.aific.finances.CategoryDetector;
import com.aific.finances.CategoryListener;
import com.aific.finances.CategoryType;
import com.aific.finances.util.Accessor;
import com.aific.finances.util.JBetterTable;
import com.aific.finances.util.Utils;


/**
 * A table of categories
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class DetectorsTable extends JBetterTable<CategoryDetector> {
	
	private Handler handler;
	private TableColumn<CategoryDetector> matchingPatternColumn;
	
	private Categories categories;
	private MainFrame documentFrame;


	/**
	 * Create an instance of class DetectorsTable
	 * 
	 * @param categories the categories
	 * @param documentFrame the document frame
	 */
	public DetectorsTable(Categories categories, MainFrame documentFrame) {
		
		this.categories = categories;
		this.documentFrame = documentFrame;

		handler = new Handler();
		
		addColumn(new TableColumn<CategoryDetector>("Pattern", String.class, true,
				new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return object.getPattern();
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						
						try {
							object.setPattern(value);
							DetectorsTable.this.documentFrame.setModified();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Invalid regular expression: " + e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						
						try {
							MainFrame.getInstance().getDocument().getTransactions().categoryDetectorUpdated(object);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Internal Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}));

		addColumn(new TableColumn<CategoryDetector>("Min", String.class, true,
				new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return Utils.NUMBER_FORMAT.format(object.getCentsMin() / 100.0);
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						
						try {
							if (!value.matches("-?\\d+\\.?\\d?\\d?")) throw new Exception();
							object.setCentsRange((int) (Double.parseDouble(value) * 100), object.getCentsMax());
							DetectorsTable.this.documentFrame.setModified();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"The number must be of the form #.##",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						
						try {
							MainFrame.getInstance().getDocument().getTransactions().categoryDetectorUpdated(object);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Internal Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}));
		
		addColumn(new TableColumn<CategoryDetector>("Max", String.class, true,
				new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return Utils.NUMBER_FORMAT.format(object.getCentsMax() / 100.0);
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						
						try {
							if (!value.matches("-?\\d+\\.?\\d?\\d?")) throw new Exception();
							object.setCentsRange(object.getCentsMin(), (int) (Double.parseDouble(value) * 100));
							DetectorsTable.this.documentFrame.setModified();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"The number must be of the form #.##",
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						
						try {
							MainFrame.getInstance().getDocument().getTransactions().categoryDetectorUpdated(object);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Internal Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}));

		addColumn(new TableColumn<CategoryDetector>("Vendor", String.class, true,
				new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return object.getVendor();
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						object.setVendor(value);
						MainFrame.getInstance().getTransactionTable().repaint();
						DetectorsTable.this.documentFrame.setModified();
					}
				}));
		
		addColumn(new TableColumn<CategoryDetector>("Description", String.class, true,
				new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return object.getDescription();
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						object.setDescription(value);
						MainFrame.getInstance().getTransactionTable().repaint();
						DetectorsTable.this.documentFrame.setModified();
					}
				}));

		addColumn(matchingPatternColumn = new TableColumn<CategoryDetector>("Matching Pattern",
				String.class, true, new Accessor<CategoryDetector, String>() {
		
					@Override
					public String get(CategoryDetector object) {
						return object.getMatchingPattern();
					}
		
					@Override
					public void set(CategoryDetector object, String value) {
						
						try {
							object.setMatchingPattern(value);
							DetectorsTable.this.documentFrame.setModified();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Invalid regular expression: " + e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						
						try {
							MainFrame.getInstance().getDocument().getTransactions().categoryDetectorUpdated(object);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(DetectorsTable.this.getParent(),
									"Internal Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}));

		
		// Define the contents
		
		for (Category c : categories.getCategories()) {
			
			for (CategoryDetector d : c.getDetectors()) {
				if (d.isDerived()) continue;
				getModifiableList().add(d);
			}
			
			c.addCategoryListener(handler);
		}
		
		
		// Set the cell editors and renderers

		// TODO
		

		// Set the column widths and other common properties
		
		/*List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		getRowSorter().setSortKeys(sortKeys); */
		
		
		// Handler
		
		// TODO
		//categories.addCategoriesListener(handler);
		
		adjustColumns();
	}
	
	
	/**
	 * Get the categories
	 * 
	 * @return the categories
	 */
	public Categories getCategories() {
		return categories;
	}
	
	
	/**
	 * Set the category filter
	 * 
	 * @param category the category
	 */
	public void filterByCategory(Category category) {
		
		sorter.setRowFilter(new CategoryRowFilter(category));
		
		matchingPatternColumn.setVisible(category == null
				|| category.getType() == CategoryType.BALANCED);
	}
	
	
	/**
	 * Get the collection of category detectors
	 * 
	 * @return the collection of category detectors
	 */
	public Collection<CategoryDetector> getCategoryDetectors() {
		return getData();
	}
	
	
	/**
	 * Prepare to dispose
	 */
	public void prepareDispose()
	{
		for (Category c : categories.getCategories()) {
			c.removeCategoryListener(handler);
		}
	}
	
	
	/**
	 * Prepare the cell renderer
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		
		if (c.getBackground() != getSelectionBackground()) {
			CategoryDetector d = model.getRow(convertRowIndexToModel(row));
			if (d.isSameAsBuiltin()) {
				c.setForeground(Color.GRAY);
			}
			else {
				c.setForeground(getForeground());
			}
		}
		
		return c;
	}

	
	/**
	 * The handler
	 */
	private class Handler implements CategoriesListener, CategoryListener {

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

		
		/**
		 * A detector was added
		 * 
		 * @param category the category
		 * @param detector the detector that was added
		 */
		@Override
		public void categoryDetectorAdded(Category category, CategoryDetector detector) {
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			int s;
			synchronized (this) {
				s = getModifiableList().size();
				getModifiableList().add(detector);
			}
			
			int v = convertRowIndexToView(s);
			setRowSelectionInterval(v, v);
		}
	}
	
	
	/**
	 * The row filter based on the category
	 */
	private class CategoryRowFilter extends RowFilter<TableModel, Integer> {
		
		private Category category;
		
		
		/**
		 * Create an instance of CategoryRowFilter
		 * 
		 * @param category the category
		 */
		public CategoryRowFilter(Category category) {
			this.category = category;
		}
		

		/**
		 * Determine whether to include the given entry
		 * 
		 * @param entry the entry
		 * @return true to include
		 */
		@Override
		public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			if (category == null) return true;
			return entry.getModel().getRow(entry.getIdentifier()).getCategory().equals(category);
		}
	}
}
