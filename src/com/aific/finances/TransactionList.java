package com.aific.finances;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.util.AbstractSharedList;


/**
 * A list of transactions
 * 
 * @author Peter Macko
 */
public class TransactionList extends AbstractSharedList<Transaction> {

	public static final String XML_ELEMENT = "transactions";

	private ArrayList<Transaction> transactions;
	private HashMap<Transaction, Integer> transactionsMap;
	private HashMap<Integer, Collection<Transaction>> transactionsAmountMap;
	
	private List<TransactionListListener> listeners;
	
	
	/**
	 * Create an empty {@link TransactionList}
	 */
	public TransactionList() {

		transactions = new ArrayList<Transaction>();
		transactionsMap = new HashMap<Transaction, Integer>();
		transactionsAmountMap = new HashMap<Integer, Collection<Transaction>>();
		listeners = null;
	}
	
	
	/**
	 * Return the number of elements in the list
	 * 
	 * @return the number of elements
	 */
	@Override
	public synchronized int size() {
		return transactions.size();
	}
	
	
	/**
	 * Determine if the list contains the given element
	 * 
	 * @param transaction the transaction
	 */
	@Override
	public synchronized boolean contains(Object transaction) {
		return transactionsMap.containsKey(transaction);
	}


	/**
	 * Add a transaction if it is not already there
	 *
	 * @param transaction the transaction to add
	 * @return true if the transaction was actually added
	 */
	@Override
	public synchronized boolean add(Transaction transaction) {
		
		if (transactionsMap.containsKey(transaction)) return false;
		
		transactions.add(transaction);
		transactionsMap.put(transaction, transactions.size() - 1);
		
		Collection<Transaction> c = transactionsAmountMap.get(transaction.getCents());
		if (c == null) {
			c = new ArrayList<Transaction>();
			transactionsAmountMap.put(transaction.getCents(), c);
		}
		c.add(transaction);
		
		fireTransactionsAdded(transactions.size() - 1, transactions.size() - 1);
		
		return true;
	}


	/**
	 * Add a collection of transactions, but only those that are not already
	 * in the table
	 *
	 * @param newTransactions the transactions to add
	 * @return true if any elements were added
	 */
	@Override
	public synchronized boolean addAll(Collection<? extends Transaction> newTransactions) {
		
		int numAdded = 0;
		for (Transaction t : newTransactions) {
			if (add(t)) numAdded++;
		}

		return numAdded > 0;
	}
	
	
	/**
	 * Clear
	 */
	@Override
	public synchronized void clear() {
		
		int n = transactions.size();
		
		transactions.clear();
		transactionsMap.clear();
		
		if (n > 0) fireTransactionsRemoved(0, n-1);
	}

	
	/**
	 * Get a transaction by its index
	 * 
	 * @param index the index
	 * @return the corresponding transaction
	 */
	@Override
	public synchronized Transaction get(int index) {
		return transactions.get(index);
	}

	
	/**
	 * Get the list of transactions
	 * 
	 * @return the list of transactions
	 */
	public synchronized List<Transaction> getList() {
		return transactions;
	}

	
	/**
	 * Get the collection of transactions with the given amount
	 * 
	 * @param cents the cents
	 * @return the collection of transactions, or null if not found
	 */
	public synchronized Collection<Transaction> getByCents(int cents) {
		return transactionsAmountMap.get(cents);
	}
	
	
	/**
	 * Get an iterator for the collection
	 * 
	 * @return the iterator
	 */
	public Iterator<Transaction> iterator()
	{
		return new TransactionIterator();
	}
	
	
	/**
	 * The category detector was updated
	 * 
	 * @param detector the detector
	 */
	public void categoryDetectorUpdated(CategoryDetector detector) {
		
		if (detector.getCategory() == null)
			throw new InternalError("Cannot update the null category detector");
		
		boolean updated = false;
		Categories c = detector.getCategory().getContainer();
		
		if (detector.getMatchingDetector() != null) {
			for (Transaction t : transactions) {
				if (t.getCategoryDetector() == detector.getMatchingDetector()) {
					if (!detector.accepts(t, this)) {
						t.setCategoryDetector(Categories.NULL_DETECTOR);
						updated = true;
					}
				}
			}
		}
		
		for (Transaction t : transactions) {
			
			if (t.getCategoryDetector() == detector) {
				if (!detector.accepts(t, this)) {
					t.setCategoryDetector(Categories.NULL_DETECTOR);
					c.detectCategories(t, this);
					updated = true;
				}
			}
			else if (t.getCategory() == null) {
				c.detectCategories(t, this);
				if (t.getCategory() != null) updated = true;
			}
		}
		
		if (updated) fireTransactionsDataChanged();
	}
	
	
	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void addTransactionListListener(TransactionListListener listener) {
		
		if (listeners == null) listeners = new LinkedList<TransactionListListener>();
		listeners.add(listener);
	}
	
	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeTransactionListListener(TransactionListListener listener) {
		
		if (listeners != null) listeners.remove(listener);
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireTransactionsAdded(int from, int to) {
		
		if (listeners != null) {
			for (TransactionListListener l : listeners)
				l.transactionsAdded(this, from, to);
		}
		fireSharedListElementsAdded(from, to);
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireTransactionsRemoved(int from, int to) {
		
		if (listeners != null) {
			for (TransactionListListener l : listeners)
				l.transactionsRemoved(this, from, to);
		}
		fireSharedListElementsRemoved(from, to);
	}
	
	
	/**
	 * Fire a listener
	 */
	public synchronized void fireTransactionsDataChanged() {
		
		if (listeners != null) {
			for (TransactionListListener l : listeners)
				l.transactionsDataChanged(this);
		}
		fireSharedListDataChanged();
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		for (Transaction t : transactions) {
			me.appendChild(t.toXMLElement(document));
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML. Note that this will clear and reload the shared
	 * global collection of the categories!
	 * 
	 * @param element the XML element
	 * @param document the parent document
	 * @return the object
	 * @throws ParseException on parse error
	 */
	public static TransactionList fromXMLElement(Element element,
			Document document) throws ParseException {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		TransactionList l = new TransactionList();
		
		NodeList tlist = element.getElementsByTagName(Transaction.XML_ELEMENT);
		for (int i = 0; i < tlist.getLength(); i++) {
			Node n = tlist.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			l.add(Transaction.fromXMLElement((Element) n, document, l));
		}
		
		return l;
	}
	
	
	/**
	 * The iterator
	 */
	private class TransactionIterator implements Iterator<Transaction> {
		
		private int index = 0;
		

		/**
		 * Determine if the iterator has another element
		 * 
		 * @return true if there is another element
		 */
		@Override
		public boolean hasNext()
		{
			return index < transactions.size();
		}

		
		/**
		 * Get the next transaction
		 * 
		 * @return the next transaction
		 */
		@Override
		public Transaction next()
		{
			return transactions.get(index++);
		}
	}
}
