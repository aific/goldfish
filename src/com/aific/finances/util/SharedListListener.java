package com.aific.finances.util;


/**
 * A shared list listener
 * 
 * @author Peter Macko
 */
public interface SharedListListener {

	/**
	 * Element(s) added
	 * 
	 * @param list the shared list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void sharedListElementsAdded(SharedList<?> list, int from, int to);

	/**
	 * Element(s) removed
	 * 
	 * @param list the shared list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void sharedListElementsRemoved(SharedList<?> list, int from, int to);

	/**
	 * Element(s) data changed
	 * 
	 * @param list the shared list that triggered this event
	 */
	public void sharedListDataChanged(SharedList<?> list);
}
