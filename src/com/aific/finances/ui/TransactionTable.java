package com.aific.finances.ui;

import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.Categories;
import com.aific.finances.CategoriesListener;
import com.aific.finances.CategoryDetector;
import com.aific.finances.Document;
import com.aific.finances.Transaction;
import com.aific.finances.TransactionList;
import com.aific.finances.ui.table.CategoryDetectorEditor;
import com.aific.finances.ui.table.CategoryRenderer;
import com.aific.finances.util.Accessor;
import com.aific.finances.util.JSharedListTable;
import com.aific.finances.util.Utils;


/**
 * A table of transactions
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class TransactionTable extends JSharedListTable<Transaction> {
	
	private TableColumn<Transaction> amountColumn;
	private TableColumn<Transaction> descriptionColumn;
	
	private Handler handler;
	
	Document document;
	private TransactionList transactions;


	/**
	 * Create an instance of class TransactionTable
	 * 
	 * @param document the document
	 */
	public TransactionTable(Document document) {
		
		addColumn(new TableColumn<Transaction>("Date", Date.class, false,
				new Accessor<Transaction, Date>() {
			
					@Override
					public Date get(Transaction object) {
						return object.getDate();
					}

					@Override
					public void set(Transaction object, Date value) {
						throw new UnsupportedOperationException();
					}
				}));

		addColumn(new TableColumn<Transaction>("Category", CategoryDetector.class, true,
				new Accessor<Transaction, CategoryDetector>() {
		
					@Override
					public CategoryDetector get(Transaction object) {
						return object.getCategoryDetector();
					}
		
					@Override
					public void set(Transaction object, CategoryDetector value) {
						if (value != object.getCategoryDetector()) {
							object.setCategoryDetector(value);
							TransactionTable.this
								.transactions.fireTransactionsDataChanged();
							MainFrame.getInstance().setModified();
						}
					}
				}));

		addColumn(amountColumn = new TableColumn<Transaction>("Amount", Integer.class, false,
				new Accessor<Transaction, Integer>() {
		
					@Override
					public Integer get(Transaction object) {
						return object.getCents();
					}
		
					@Override
					public void set(Transaction object, Integer value) {
						throw new UnsupportedOperationException();
					}
				}));
	
		addColumn(descriptionColumn = new TableColumn<Transaction>("Description", String.class, true,
				new Accessor<Transaction, String>() {
		
					@Override
					public String get(Transaction object) {
						return object.getDescription();
					}
		
					@Override
					public void set(Transaction object, String value) {
						if (value != get(object) && !value.equals(get(object)))
							throw new UnsupportedOperationException();
					}
				}));
		
		addColumn(new TableColumn<Transaction>("Account", String.class, false,
				new Accessor<Transaction, String>() {
		
					@Override
					public String get(Transaction object) {
						if (object.getAccount() == null) return "N/A";
						return object.getAccount().getShortName();
					}
		
					@Override
					public void set(Transaction object, String value) {
						throw new UnsupportedOperationException();
					}
				}));

		addColumn(new TableColumn<Transaction>("Note", String.class, true,
				new Accessor<Transaction, String>() {
		
					@Override
					public String get(Transaction object) {
						return object.getNote() == null ? "" : object.getNote();
					}
		
					@Override
					public void set(Transaction object, String value) {
						if ("".equals(value)) value = null;
						if (value == object.getNote()) return;
						if (value != null && value.equals(object.getNote())) return;
						object.setNote(value);
						MainFrame.getInstance().setModified();
					}
				}));
		
		setSharedList(this.transactions = new TransactionList());		// Temporary

		
		// Set the cell editors and renderers

		setDefaultRenderer(Date.class, new DateRenderer());
		setDefaultRenderer(String.class, new StringRenderer());
		setDefaultRenderer(CategoryDetector.class, new CategoryRenderer());
		setDefaultEditor  (CategoryDetector.class, new CategoryDetectorEditor(this, document.getCategories()));
		
		getColumnModel().getColumn(amountColumn.getIndex()).setCellRenderer(new AmountRenderer());
		getColumnModel().getColumn(descriptionColumn.getIndex()).setCellEditor(new ReadOnlyTextEditor());
		

		// Set the column widths and other common properties
		
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
		getRowSorter().setSortKeys(sortKeys); 
		
		getColumnModel().getColumn(0).setPreferredWidth(64);
		getColumnModel().getColumn(descriptionColumn.getIndex()).setPreferredWidth(320);
		
		
		// Handlers
		
		handler = new Handler();
		
		
		// Define the contents
		
		setDocument(document);
	}


	/**
	 * Get the list of transactions
	 * 
	 * @return the list of transactions
	 */
	public TransactionList getTransactions() {
		return transactions;
	}


	/**
	 * Set the document
	 * 
	 * @param document the document
	 */
	public void setDocument(Document document) {
		
		if (isEditing()) getCellEditor().stopCellEditing();

		if (this.document != null) {
			this.document.getCategories().removeCategoriesListener(handler);
		}
		
		this.document = document;
		this.document.getCategories().addCategoriesListener(handler);
		
		setSharedList(this.transactions = document.getTransactions());
		setDefaultEditor(CategoryDetector.class, new CategoryDetectorEditor(this, document.getCategories()));
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
			model.fireTableDataChanged();
		}
		

		/**
		 * Category data changed
		 * 
		 * @param list the category list that triggered this event
		 */
		public void categoriesDataChanged(Categories list) {
			model.fireTableDataChanged();
		}
	}


	/**
	 * Cell renderer
	 */
	private class DateRenderer implements TableCellRenderer {
		
		private SimpleDateFormat dateFormat;
		private JLabel label;
		

		/**
		 * Create an instance of the cell renderer
		 */
		public DateRenderer() {
			dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			label = new JLabel("");
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
			Date date = (Date) object;
			Transaction t = model.getRow(table.convertRowIndexToModel(row));
			
			if (isSelected) {
				label.setForeground(table.getSelectionForeground());
				label.setBackground(table.getSelectionBackground());
			}
			else {
				label.setForeground(table.getForeground());
				label.setBackground(table.getBackground());
			}
			
			if (t.getMatchingTransaction() != null) {
				label.setForeground(Color.GRAY);
			}
			
			if (date == null) {
				label.setText("");
			}
			else {
				label.setText(dateFormat.format(date));
			}
			
			return label;
		}
	}


	/**
	 * Cell renderer
	 */
	private class StringRenderer implements TableCellRenderer {
		
		private JLabel label;
		

		/**
		 * Create an instance of the cell renderer
		 */
		public StringRenderer() {
			label = new JLabel("");
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
			String str = (String) object;
			Transaction t = model.getRow(table.convertRowIndexToModel(row));
			
			if (isSelected) {
				label.setForeground(table.getSelectionForeground());
				label.setBackground(table.getSelectionBackground());
			}
			else {
				label.setForeground(table.getForeground());
				label.setBackground(table.getBackground());
			}
			
			if (t.getMatchingTransaction() != null) {
				label.setForeground(Color.GRAY);
			}
			
			if (str == null) {
				label.setText("");
			}
			else {
				label.setText(str);
			}
			
			return label;
		}
	}


	/**
	 * Cell renderer
	 */
	private class AmountRenderer implements TableCellRenderer {
		
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
			Transaction t = model.getRow(table.convertRowIndexToModel(row));

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
				
				String strWhole = Integer.toString(absCents / 100);
				String strCents = Integer.toString(absCents % 100);
				if (strCents.length() == 1) strCents = "0" + strCents;
				
				label.setText((cents < 0 ? "-" : "+") + strWhole + "." + strCents + " ");

				Color c = (cents < 0) ? Color.RED : Color.GREEN.darker().darker();
				if (isSelected) {
					label.setForeground(Utils.getColorInBetween(c,
							table.getSelectionForeground(), 0.5f));
				}
				else {
					label.setForeground(c);
				}
			}
			
			if (t.getMatchingTransaction() != null) {
				label.setForeground(Color.GRAY);
			}

			return label;
		}
	}


	/**
	 * Cell editor
	 */
	private class ReadOnlyTextEditor extends AbstractCellEditor implements TableCellEditor {

		private JTextField field;


		/**
		 * Create an instance of the cell editor
		 */
		public ReadOnlyTextEditor() {
			field = new JTextField();
			field.setEditable(false);
			field.setBorder(null);
		}


		/**
		 * Update the attribute value
		 *
		 * @return the edited object
		 */
		public Object getCellEditorValue() {
			return field.getText();
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
			field.setText(object == null ? "" : object.toString());

			field.setForeground(table.getSelectionForeground());
			field.setBackground(table.getSelectionBackground());
			
			return field;
		}
	}
}
