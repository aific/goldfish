package com.aific.finances.ui;

import com.aific.finances.BudgetItem;
import com.aific.finances.Category;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.ui.table.AmountEditor;
import com.aific.finances.ui.table.AmountRenderer;
import com.aific.finances.ui.table.CategoryEditorForBudget;
import com.aific.finances.ui.table.CategoryRenderer;
import com.aific.finances.ui.table.DateFrequencyEditor;
import com.aific.finances.ui.table.IntegerEditor;
import com.aific.finances.ui.table.IntegerRenderer;
import com.aific.finances.util.Accessor;
import com.aific.finances.util.DateFrequencyUnit;
import com.aific.finances.util.JSharedListTable;
import com.aific.finances.util.SharedList;


/**
 * A list of budget items
 */
@SuppressWarnings("serial")
public class BudgetItemsTable extends JSharedListTable<BudgetItem> {

	private CategoryType type;
	
	
	/**
	 * Create a new instance of the component
	 * 
	 * @param document the document
	 * @param type the type of the list
	 * @param list the list of budget items
	 */
	public BudgetItemsTable(Document document, CategoryType type, SharedList<BudgetItem> list)
	{
		this.type = type;
		
		TableColumn<BudgetItem> categoryColumn
			= addColumn(new TableColumn<BudgetItem>("Category", Category.class, true,
				new Accessor<BudgetItem, Category>() {
			
					@Override
					public Category get(BudgetItem object) {
						return object.getCategory();
					}

					@Override
					public void set(BudgetItem object, Category value) {
						object.setCategory(value);
						list.fireDataChanged();
					}
				}));
		TableColumn<BudgetItem> amountColumn
			= addColumn(new TableColumn<BudgetItem>("Amount", Integer.class, true,
				new Accessor<BudgetItem, Integer>() {
			
					@Override
					public Integer get(BudgetItem object) {
						return object.getCents();
					}

					@Override
					public void set(BudgetItem object, Integer value) {
						object.setCents(value.intValue());
						list.fireDataChanged();
					}
				}));
		TableColumn<BudgetItem> frequencyColumn
			= addColumn(new TableColumn<BudgetItem>("Count", Integer.class, true,
				new Accessor<BudgetItem, Integer>() {
			
					@Override
					public Integer get(BudgetItem object) {
						return object.getFrequency();
					}
	
					@Override
					public void set(BudgetItem object, Integer value) {
						object.setFrequency(value.intValue());
						list.fireDataChanged();
					}
				}));
		TableColumn<BudgetItem> unitColumn
			= addColumn(new TableColumn<BudgetItem>("Unit", DateFrequencyUnit.class, true,
				new Accessor<BudgetItem, DateFrequencyUnit>() {
			
					@Override
					public DateFrequencyUnit get(BudgetItem object) {
						return object.getFrequencyUnit();
					}
	
					@Override
					public void set(BudgetItem object, DateFrequencyUnit value) {
						object.setFrequencyUnit(value);
						list.fireDataChanged();
					}
				}));
		TableColumn<BudgetItem> descriptionColumn
			= addColumn(new TableColumn<BudgetItem>("Description", String.class, true,
				new Accessor<BudgetItem, String>() {
			
					@Override
					public String get(BudgetItem object) {
						return object.getDescription();
					}

					@Override
					public void set(BudgetItem object, String value) {
						object.setDescription(value);
						list.fireDataChanged();
					}
				}));
		TableColumn<BudgetItem> noteColumn
			= addColumn(new TableColumn<BudgetItem>("Note", String.class, true,
				new Accessor<BudgetItem, String>() {
			
					@Override
					public String get(BudgetItem object) {
						return object.getNote();
					}

					@Override
					public void set(BudgetItem object, String value) {
						object.setNote(value);
						list.fireDataChanged();
					}
				}));
		
		
		// Customize the editors
		
		setDefaultRenderer(Category.class, new CategoryRenderer());
		setDefaultEditor  (Category.class, new CategoryEditorForBudget(this, type, document.getCategories()));
		setDefaultEditor  (DateFrequencyUnit.class, new DateFrequencyEditor(this));
				
		getColumnModel().getColumn(frequencyColumn.getIndex()).setCellRenderer(new IntegerRenderer());
		getColumnModel().getColumn(frequencyColumn.getIndex()).setCellEditor  (new IntegerEditor(0, 10_000));
		getColumnModel().getColumn(amountColumn   .getIndex()).setCellRenderer(new AmountRenderer());
		getColumnModel().getColumn(amountColumn   .getIndex()).setCellEditor  (new AmountEditor());
		
		
		// Customize the column and the form widths
		
		getColumnModel().getColumn(categoryColumn   .getIndex()).setPreferredWidth(160);
		getColumnModel().getColumn(amountColumn     .getIndex()).setPreferredWidth(100);
		getColumnModel().getColumn(frequencyColumn  .getIndex()).setPreferredWidth(50);
		getColumnModel().getColumn(unitColumn       .getIndex()).setPreferredWidth(100);
		getColumnModel().getColumn(descriptionColumn.getIndex()).setPreferredWidth(240);
		getColumnModel().getColumn(noteColumn       .getIndex()).setPreferredWidth(160);
		
		
		
		// Set the shared list

		setSharedList(list);
		adjustColumns();
	}


	/**
	 * Get the type of the list
	 * 
	 * @return the type
	 */
	public CategoryType getType()
	{
		return type;
	}
}
