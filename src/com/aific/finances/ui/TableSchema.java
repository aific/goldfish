package com.aific.finances.ui;

import java.util.Vector;

import javax.swing.JTable;


/**
 * A table schema
 * 
 * @author Peter Macko
 * 
 * @param <T> the row type
 */
public class TableSchema<T> {

	private Vector<TableColumn<T>> columns;
	private JTable table;
	
	
	/**
	 * Create an instance of class TableSchema
	 * 
	 * @param table the table
	 */
	public TableSchema(JTable table) {
		this.table = table;
		columns = new Vector<TableColumn<T>>();
	}
	
	
	/**
	 * Add a column
	 * 
	 * @param column the column
	 */
	public void addColumn(TableColumn<T> column) {
		if (column.index >= 0) {
			throw new IllegalArgumentException("The column in already a part of a schema");
		}
		synchronized (columns) {
			column.index = columns.size();
			column.schema = this;
			columns.add(column);
		}
	}
	
	
	/**
	 * Get a column by index
	 * 
	 * @param index the column index
	 * @return the column
	 */
	public TableColumn<T> getColumn(int index) {
		return columns.get(index);
	}
	
	
	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	public int getColumnCount() {
		return columns.size();
	}
	
	
	/**
	 * Get the associated table
	 * 
	 * @return the JTable
	 */
	public JTable getTable() {
		return table;
	}
}
