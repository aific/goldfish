package com.aific.finances;


/**
 * A categories list listener
 * 
 * @author Peter Macko
 */
public interface CategoriesListener {

	/**
	 * One or more categories added
	 * 
	 * @param list the category list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void categoriesAdded(Categories list, int from, int to);

	/**
	 * One or more categories removed
	 * 
	 * @param list the category list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void categoriesRemoved(Categories list, int from, int to);

	/**
	 * Category data changed
	 * 
	 * @param list the category list that triggered this event
	 */
	public void categoriesDataChanged(Categories list);
}
