package com.aific.finances.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.aific.finances.ui.TableColumn;
import com.aific.finances.ui.TableSchema;


/**
 * A table with some better features
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class JBetterTable<T> extends JTable {
	
	private static final boolean CUSTOM_DO_LAYOUT = false;
	
	protected TableModel model;
	protected TableRowSorter<TableModel> sorter;
	protected List<T> data;
	
	private TableSchema<T> schema;
	private TableColumnAdjuster tca;
	private MouseHandler mouseHandler;
	private ScrollPaneMouseHandler scrollPaneMouseHandler;


	/**
	 * Create an instance of {@link JBetterTable}
	 */
	public JBetterTable()
	{
		data = new ArrayList<>();	// Stub for the data
		
		
		// Set the initial table schema

		schema = new TableSchema<>(this);
		sorter = null;
		model = new TableModel();
		setModel(model);

		sorter = new TableRowSorter<TableModel>(model);
		setRowSorter(sorter);

		
		// Set the column widths

		setAutoResizeMode(AUTO_RESIZE_OFF);
		tca = new TableColumnAdjuster(this);
		getTableHeader().setReorderingAllowed(true);
		
		
		// Set some useful properties
		
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		
		// Override some UI defaults
		
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		
		
		// Set callbacks
		
		mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		
		
		// Set the callback for the containing scroll pane
		
		scrollPaneMouseHandler = new ScrollPaneMouseHandler();
		addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			
			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				// http://stackoverflow.com/questions/22040701/access-the-jscrollpane-in-which-the-jtable-is-contained
				Container parent = getParent();
				if (parent == null || !(parent instanceof JViewport)) return;
				Container enclosing = parent.getParent();
				if (enclosing == null || !(enclosing instanceof JScrollPane)) return;
				enclosing.addMouseListener(scrollPaneMouseHandler);
			}
		});
	}
	
	
	/**
	 * Get the data
	 * 
	 * @return the (unmodifiable) collection of data
	 */
	public List<T> getData()
	{
		return Collections.unmodifiableList(data);
	}
	
	
	/**
	 * Get the modifiable list
	 * 
	 * @return get the modifiable list
	 */
	public List<T> getModifiableList()
	{
		return model.getModifiableList();
	}
	
	
	/**
	 * Adjust the table columns
	 */
	public void adjustColumns()
	{
		tca.adjustColumns();
	}
	
	
	/**
	 * Add a column to the schema
	 * 
	 * @param column the table column
	 * @return the column (such as for call chaining)
	 */
	protected TableColumn<T> addColumn(TableColumn<T> column)
	{
		schema.addColumn(column);

		model = new TableModel();
		setModel(model);
		
		return column;
	}

	
	/**
	 * Set the data model for this table to newModel and register with it for
	 * listener notifications from the new data model.
	 * 
	 * @param model the new model
	 */
	public void setModel(TableModel model)
	{
		super.setModel(model);

		
		// Set the row sorter
		
		TableRowSorter<TableModel> prev = sorter;
		
		sorter = new TableRowSorter<>(model);
		setRowSorter(sorter);
		
		if (prev != null) {
			sorter.setRowFilter(prev.getRowFilter());
			sorter.setSortKeys(prev.getSortKeys());
			sorter.setMaxSortKeys(prev.getMaxSortKeys());
			
			for (int i = 0; i < model.getColumnCount(); i++) {
				sorter.setComparator(i, sorter.getComparator(i));
			}
		}
	}

	
	/**
	 * Set the data model for this table to newModel and register with it for
	 * listener notifications from the new data model.
	 * 
	 * @param model the new model
	 */
	@Override
	public void setModel(javax.swing.table.TableModel model)
	{
		if (this.model == null) {
			super.setModel(model);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	
	/**
	 * Set a filter
	 * 
	 * @param filter the filter
	 */
	public void setFilter(Predicate<T> filter)
	{
		sorter.setRowFilter(new PredicateRowFilter(filter));
	}

	
	/**
	 * Get the filter
	 * 
	 * @return the filter, or null if either not set or not a predicate-based filter
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Predicate<T> getFilter()
	{
		RowFilter<? super JBetterTable<T>.TableModel, ? super Integer> filter = sorter.getRowFilter();
		if (filter instanceof JBetterTable.PredicateRowFilter) {
			return (Predicate<T>) ((JBetterTable.PredicateRowFilter) filter).getPredicate();
		}
		return null;
	}
	
	
	/**
	 * Get the collection of selected items
	 * 
	 * @return the collection of selection items
	 */
	public Collection<T> getSelectedItems()
	{
		int[] r = getSelectedRows();
		
		Collection<T> items = new ArrayList<>(r.length);
		for (int i : r) {
			items.add(data.get(convertRowIndexToModel(i)));
		}
		
		return items;
	}

	
	/**
	 * Determine whether the width of the viewport determines the width of the table
	 * 
	 * @return true if it determines
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		// http://stackoverflow.com/questions/15234691/enabling-auto-resize-of-jtable-only-if-it-fit-viewport
		
		return CUSTOM_DO_LAYOUT
				? getPreferredSize().width < getParent().getWidth()
						: super.getScrollableTracksViewportWidth();
	}

	
	/**
	 * Do the layout
	 */
	@Override
	public void doLayout()
	{
		// http://stackoverflow.com/questions/15234691/enabling-auto-resize-of-jtable-only-if-it-fit-viewport
		
		if (CUSTOM_DO_LAYOUT) {
			super.doLayout();
			return;
		}
		
		javax.swing.table.TableColumn resizingColumn = null;

		if (tableHeader != null)
			resizingColumn = tableHeader.getResizingColumn();

		//  Viewport size changed. May need to increase columns widths

		if (resizingColumn == null)
		{
			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			super.doLayout();
		}

		//  Specific column resized. Reset preferred widths

		else
		{
			TableColumnModel tcm = getColumnModel();

			for (int i = 0; i < tcm.getColumnCount(); i++)
			{
				javax.swing.table.TableColumn tc = tcm.getColumn(i);
				tc.setPreferredWidth( tc.getWidth() );
			}

			// Columns don't fill the viewport, invoke default layout

			if (tcm.getTotalColumnWidth() < getParent().getWidth())
				setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			super.doLayout();
		}

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
	
	
	/**
	 * Prepare the cell renderer
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		boolean selected = isRowSelected(row);
		
		
		// A workaround for a weird Apple Look and Feel bug; we don't like system color proxies
		
		boolean colorProxy = c.getBackground().getClass().getSimpleName().equals("SystemColorProxy");


		// Clear the borders for some components; a workaround for another Apple LAF bug
		
		if (c instanceof DefaultTableCellRenderer.UIResource) {
			((DefaultTableCellRenderer.UIResource) c).setBorder(null);
		}

		
		// Set the background color
		
		if (c.getBackground().equals(getBackground())
				|| (Utils.useAlternatingTableRowColors
						&& c.getBackground().equals(Utils.TABLE_ALTERNATE_ROWS_COLOR))
				|| c.getBackground().equals(getSelectionBackground())
				|| colorProxy) {
			
			if (selected) {
				c.setBackground(getSelectionBackground());
			}
			else if (Utils.useAlternatingTableRowColors && (row & 1) == 1) {
				c.setBackground(Utils.TABLE_ALTERNATE_ROWS_COLOR);
			}
			else {
				c.setBackground(getBackground());
			}
		}
		else if (selected) {
			c.setBackground(Utils.getColorInBetween(c.getBackground(),
					getSelectionBackground(), 0.5f));
		}
		
		
		// Set the foreground color
		
		if (c.getForeground().equals(getForeground())
				|| c.getForeground().equals(getSelectionForeground())
				|| colorProxy) {
			
			if (selected) {
				c.setForeground(getSelectionForeground());
			}
			else {
				c.setForeground(getForeground());
			}
		}
		
		return c;
	}
	
	
	/**
	 * The table model
	 */
	protected class TableModel extends AbstractTableModel {
		
		private TableList list;
		
		
		/**
		 * Create an instance of this class
		 */
		public TableModel()
		{
			list = new TableList();
		}
		
		
		/**
		 * Get the modifiable table list
		 * 
		 * @return get the modifiable list
		 */
		public List<T> getModifiableList()
		{
			return list;
		}
		

		/**
		 * Return the number of columns
		 *
		 * @return the number of columns
		 */
		public int getColumnCount()
		{
			return schema.getColumnCount();
		}

		/**
		 * Return the number of rows
		 *
		 * @return the number of rows
		 */
		public int getRowCount()
		{
			return data.size();
		}

		/**
		 * Return the column name
		 *
		 * @param col the column id
		 * @return the column name
		 */
		public String getColumnName(int col)
		{
			return schema.getColumn(col).getName();
		}
		
		/**
		 * Return the row
		 * 
		 * @param row the row
		 * @return the transaction
		 */
		public T getRow(int row)
		{
			return data.get(row);
		}

		/**
		 * Return the value at the given row and column
		 *
		 * @param row the row id
		 * @param col the column id
		 * @return the object
		 */
		public Object getValueAt(int row, int col)
		{
			return schema.getColumn(col).getAccessor().get(getRow(row));
		}

		/**
		 * Return the class of the given column
		 *
		 * @param col the column id
		 * @return the column class
		 */
		public Class<?> getColumnClass(int col)
		{
			return schema.getColumn(col).getType();
		}

		/**
		 * Determine whether the given cell is editable
		 *
		 * @param row the row id
		 * @param col the column id
		 * @return true if it is editable
		 */
		public boolean isCellEditable(int row, int col)
		{
			return schema.getColumn(col).isEditable();
		}

		
		/**
		 * Change the value in the given cell
		 *
		 * @param value the new value
		 * @param row the row id
		 * @param col the column id
		 */
		@SuppressWarnings("unchecked")
		public void setValueAt(Object value, int row, int col)
		{
			((Accessor<T, Object>) schema.getColumn(col)
					.getAccessor()).set(getRow(row), value);
			fireTableCellUpdated(row, col);
		}
		
		
		/**
		 * A modifiable list that automatically fires the right table events
		 */
		protected class TableList extends AbstractList<T> {
			
			/**
			 * Append an element
			 * 
			 * @param e the element
			 * @return true
			 */
			@Override
			public boolean add(T e)
			{
				boolean r;
				int p;
				
				synchronized (data) {
					p = data.size();
					r = data.add(e);
				}
			
				if (r) fireTableRowsInserted(p, p);
				return r;
			}
			
			
			/**
			 * Insert an element
			 * 
			 * @param index the index
			 * @param e the element
			 */
			@Override
			public void add(int index, T e)
			{
				data.add(index, e);
				fireTableRowsInserted(index, index);
			}
			
			
			/**
			 * Insert multiple elements
			 * 
			 * @param c the collection of elements to insert
			 * @return true if this list changed as a result of this call
			 */
			@Override
			public boolean addAll(Collection<? extends T> c)
			{
				int index = data.size();
				boolean r = data.addAll(index, c);
				if (r && !c.isEmpty()) fireTableRowsInserted(index, index + c.size() - 1);
				return r;
			}
			
			
			/**
			 * Insert multiple elements
			 * 
			 * @param index the index
			 * @param c the collection of elements to insert
			 * @return true if this list changed as a result of this call
			 */
			@Override
			public boolean addAll(int index, Collection<? extends T> c)
			{
				boolean r = data.addAll(index, c);
				if (r && !c.isEmpty()) fireTableRowsInserted(index, index + c.size() - 1);
				return r;
			}
			

			/**
			 * Get an element
			 * 
			 * @param index the index
			 * @return the value
			 */
			@Override
			public T get(int index)
			{
				return data.get(index);
			}
			
			
			/**
			 * Remove an element
			 * 
			 * @param index the index
			 * @return the removed element
			 */
			@Override
			public T remove(int index)
			{
				T r = data.remove(index);
				fireTableRowsDeleted(index, index);
				return r;
			}
			
			
			/**
			 * Replace an element
			 * 
			 * @param index the index
			 * @param e the new element
			 * @return the removed element
			 */
			@Override
			public T set(int index, T e)
			{
				T r = data.set(index, e);
				fireTableRowsUpdated(index, index);
				return r;
			}

			
			/**
			 * Get the size of the list
			 * 
			 * @return the size of the list
			 */
			@Override
			public int size()
			{
				return data.size();
			}
		}
	}
	
	
	/**
	 * The row filter
	 */
	private class PredicateRowFilter extends RowFilter<TableModel, Integer> {
		
		private Predicate<T> filter;
		
		
		/**
		 * Create an instance of {@link PredicateRowFilter}
		 * 
		 * @param filter the filter
		 */
		public PredicateRowFilter(Predicate<T> filter)
		{
			this.filter = filter;
		}
		
		
		/**
		 * Get the predicate
		 * 
		 * @return the predicate
		 */
		public Predicate<T> getPredicate()
		{
			return filter;
		}
		

		/**
		 * Determine whether to include the given entry
		 * 
		 * @param entry the entry
		 * @return true to include
		 */
		@Override
		public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			if (filter == null) return true;
			return filter.test(entry.getModel().getRow(entry.getIdentifier()));
		}
	}
	
	
	/**
	 * The mouse adapter
	 */
	private class MouseHandler extends MouseAdapter
	{
		/**
		 * The mouse was clicked
		 * 
		 * @param e the event
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{
			// Clear the selection if the mouse was clicked outside of the rows,
			// but will this ever be called, or will we always get events in the
			// enclosing JScrollPane?
			
			int row = rowAtPoint(e.getPoint());
			if (row < 0) clearSelection();
		}
	}
	
	
	/**
	 * The mouse adapter for the scroll pane container
	 */
	private class ScrollPaneMouseHandler extends MouseAdapter
	{
		/**
		 * The mouse was clicked
		 * 
		 * @param e the event
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{
			// Clear the selection if the mouse was clicked outside of the rows
			
			clearSelection();
		}
	}
}
