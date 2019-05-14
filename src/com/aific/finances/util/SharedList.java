package com.aific.finances.util;

import java.util.List;


/**
 * A list that can be shared among multiple components
 */
public interface SharedList<E> extends List<E> {

	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	public void addSharedListListener(SharedListListener listener);

	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	public void removeSharedListListener(SharedListListener listener);
	
	/**
	 * Fire the change event for data
	 */
	public void fireDataChanged();
}
