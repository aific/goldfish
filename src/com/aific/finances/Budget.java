package com.aific.finances;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.util.SharedList;
import com.aific.finances.util.SharedListListener;


/**
 * A budget
 */
public class Budget {
	
	public static final String XML_ELEMENT = "budget";

	private BudgetItemList income;
	private BudgetItemList expenses;
	
	private Handler handler;
	private LinkedList<BudgetListener> listeners = null;

	
	/**
	 * Create an instance of {@link Budget}
	 */
	public Budget()
	{
		income = new BudgetItemList(BudgetItemList.Type.INCOME);
		expenses = new BudgetItemList(BudgetItemList.Type.EXPENSES);
		
		handler = new Handler();
		income.addSharedListListener(handler);
		expenses.addSharedListListener(handler);
	}
	
	
	/**
	 * Create an instance of {@link Budget}
	 * 
	 * @param element the XML element
	 * @param document the parent document
	 * @return the object
	 * @throws ParseException on parse error
	 */
	private Budget(Element element, Document document) throws ParseException
	{
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		
		// Income
		
		NodeList incomeList = element.getElementsByTagName(BudgetItemList.XML_ELEMENT_INCOME);
		if (incomeList.getLength() != 1) {
			throw new RuntimeException("The budget must have one <" + BudgetItemList.XML_ELEMENT_INCOME + "> element");
		}
		
		if (incomeList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
		income = BudgetItemList.fromXMLElement((Element) incomeList.item(0), document);

		if (income.getType() != BudgetItemList.Type.INCOME) throw new IllegalStateException();
		
		
		// Expenses
		
		NodeList expenseList = element.getElementsByTagName(BudgetItemList.XML_ELEMENT_EXPENSES);
		if (expenseList.getLength() != 1) {
			throw new RuntimeException("The budget must have one <" + BudgetItemList.XML_ELEMENT_EXPENSES + "> element");
		}
		
		if (expenseList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
		expenses = BudgetItemList.fromXMLElement((Element) expenseList.item(0), document);
		
		if (expenses.getType() != BudgetItemList.Type.EXPENSES) throw new IllegalStateException();

		
		// Finalize
		
		handler = new Handler();
		income.addSharedListListener(handler);
		expenses.addSharedListListener(handler);
	}


	/**
	 * Get the income items
	 * 
	 * @return the income
	 */
	public BudgetItemList getIncome()
	{
		return income;
	}


	/**
	 * Get the expense items
	 * 
	 * @return the expenses
	 */
	public BudgetItemList getExpenses()
	{
		return expenses;
	}
	

	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void addBudgetListener(BudgetListener listener)
	{
		if (listeners == null) listeners = new LinkedList<>();
		listeners.add(listener);
	}

	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeBudgetListener(BudgetListener listener)
	{
		if (listeners != null) {
			Iterator<BudgetListener> i = listeners.iterator();
			while (i.hasNext()) {
				BudgetListener l = i.next();
				if (l == null || l == listener) i.remove();
			}
		}
	}
	
	
	/**
	 * Fire the listener
	 */
	protected synchronized void fireBudgetUpdated()
	{
		if (listeners != null) {
			for (BudgetListener l : listeners) {
				if (l != null) l.budgetUpdated(this);
			}
		}
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);

		me.appendChild(income.toXMLElement(document));
		me.appendChild(expenses.toXMLElement(document));
		
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
	public static Budget fromXMLElement(Element element,
			Document document) throws ParseException {

		return new Budget(element, document);
	}
	
	
	/**
	 * An event handler
	 */
	private class Handler implements SharedListListener {

		/**
		 * Element(s) added
		 * 
		 * @param list the shared list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void sharedListElementsAdded(SharedList<?> list, int from, int to)
		{
			fireBudgetUpdated();
		}

		/**
		 * Element(s) removed
		 * 
		 * @param list the shared list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void sharedListElementsRemoved(SharedList<?> list, int from, int to)
		{
			fireBudgetUpdated();
		}

		/**
		 * Element(s) data changed
		 * 
		 * @param list the shared list that triggered this event
		 */
		public void sharedListDataChanged(SharedList<?> list)
		{
			fireBudgetUpdated();
		}
	}
}
