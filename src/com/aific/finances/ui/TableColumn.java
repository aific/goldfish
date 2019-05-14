package com.aific.finances.ui;

import javax.swing.table.TableColumnModel;

import com.aific.finances.util.Accessor;


/**
 * A column in a table
 * 
 * @author Peter Macko
 * 
 * @param <T> the row type
 */
public class TableColumn<T> {

	int index;
	TableSchema<T> schema;
	
	private String name;
	private Class<?> type;
	private boolean editable;
	private Accessor<T, ?> accessor;
	
	private boolean visible;
	private int previousWidth;
	private int previousMinWidth;
	private int previousMaxWidth;
	
	
	/**
	 * Create an instance of TableColumn
	 * 
	 * @param name the column name
	 * @param type the column type
	 * @param editable true if the column is editable
	 * @param accessor the accessor
	 */
	public TableColumn(String name, Class<?> type, boolean editable, Accessor<T, ?> accessor) {
		
		index = -1;
		schema = null;
		
		this.name = name;
		this.type = type;
		this.editable = editable;
		this.accessor = accessor;
		
		visible = true;
		previousWidth = 0;
		previousMinWidth = 0;
		previousMaxWidth = 0;
	}


	/**
	 * Return the column index
	 * 
	 * @return the index, or -1 if not part of a schema
	 */
	public int getIndex() {
		return index;
	}


	/**
	 * Return the column name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * Return the column data type
	 * 
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}


	/**
	 * Return whether the column is editable
	 * 
	 * @return true if it the column is editable
	 */
	public boolean isEditable() {
		return editable;
	}


	/**
	 * Return the accessor
	 * 
	 * @return the accessor
	 */
	public Accessor<T, ?> getAccessor() {
		return accessor;
	}
	
	
	/**
	 * Determine if the column is visible
	 * 
	 * @return true if it is visible
	 */
	public boolean isVisible() {
		return visible;
	}
	
	
	/**
	 * Show or hide the column
	 * 
	 * @param visible the new visibility setting
	 */
	public void setVisible(boolean visible) {
		
		if (this.visible == visible) return;
		this.visible = visible;
		
		TableColumnModel m = schema.getTable().getColumnModel();
		javax.swing.table.TableColumn c = m.getColumn(index);
		
		if (visible) {

			// Show
			
			c.setMaxWidth(previousMaxWidth);
			c.setMinWidth(previousMinWidth);
			c.setWidth(previousWidth);
		}
		else {
			
			// Hide
		
			previousWidth = c.getWidth();
			previousMinWidth = c.getMinWidth();
			previousMaxWidth = c.getMaxWidth();
			
			c.setMinWidth(0);
			c.setMaxWidth(0);
		}
	}
}
