package com.aific.finances;


/**
 * A category listener
 * 
 * @author Peter Macko
 */
public interface CategoryListener {

	/**
	 * A detector was added
	 * 
	 * @param category the category
	 * @param detector the detector that was added
	 */
	public void categoryDetectorAdded(Category category, CategoryDetector detector);
}
