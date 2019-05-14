package com.aific.finances;


/**
 * A budget listener
 */
public interface BudgetListener {

	/**
	 * Budget was updated
	 * 
	 * @param budget the budget
	 */
	public void budgetUpdated(Budget budget);
}
