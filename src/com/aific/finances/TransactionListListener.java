package com.aific.finances;


/**
 * A transaction list listener
 * 
 * @author Peter Macko
 */
public interface TransactionListListener {

	/**
	 * Transaction(s) added
	 * 
	 * @param list the transaction list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void transactionsAdded(TransactionList list, int from, int to);

	/**
	 * Transaction(s) removed
	 * 
	 * @param list the transaction list that triggered this event
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	public void transactionsRemoved(TransactionList list, int from, int to);

	/**
	 * Transaction(s) data changed
	 * 
	 * @param list the transaction list that triggered this event
	 */
	public void transactionsDataChanged(TransactionList list);
}
