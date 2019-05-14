package com.aific.finances;

import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.util.SharedArrayList;


/**
 * A list of budget items
 */
public class BudgetItemList extends SharedArrayList<BudgetItem> {
	
	public static final String XML_ELEMENT_INCOME = "income";
	public static final String XML_ELEMENT_EXPENSES = "expenses";
	
	public static enum Type {
		INCOME,
		EXPENSES
	};
	
	private Type type;
	
	private double sumPerYear;
	
	
	/**
	 * Create an instance of {@link BudgetItemList}
	 * 
	 * @param type the type
	 */
	public BudgetItemList(Type type)
	{
		this.type = type;
		
		updateSummary();
	}
	
	
	/**
	 * Get the type of the list
	 * 
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}
	
	
	/**
	 * Update the summary statistics
	 */
	protected void updateSummary()
	{
		double sumPerYear = 0;
		for (BudgetItem b : this) {
			sumPerYear += b.getAmountPerYear();
		}
		
		this.sumPerYear = sumPerYear;
	}
	
	
	/**
	 * Get the sum per year
	 * 
	 * @return the sum per year
	 */
	public double getSumPerYear()
	{
		return sumPerYear;
	}
	
	
	/**
	 * Callback for when something in the shared list has changed
	 */
	@Override
	protected void sharedListChanged()
	{
		super.sharedListChanged();
		updateSummary();
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		
		String name;
		switch (type) {
		case EXPENSES:
			name = XML_ELEMENT_EXPENSES;
			break;
		case INCOME:
			name = XML_ELEMENT_INCOME;
			break;
		default:
			throw new IllegalStateException("Illegal budget list type");
		}
		
		Element me = document.createElement(name);
		
		for (BudgetItem item : this) {
			me.appendChild(item.toXMLElement(document));
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
	public static BudgetItemList fromXMLElement(Element element,
			Document document) throws ParseException {
		
		BudgetItemList l;
		
		if (element.getNodeName().equals(XML_ELEMENT_EXPENSES)) {
			l = new BudgetItemList(Type.EXPENSES);
		}
		else if (element.getNodeName().equals(XML_ELEMENT_INCOME)) {
			l = new BudgetItemList(Type.INCOME);
		}
		else {
			throw new IllegalArgumentException();
		}
		
		NodeList tlist = element.getElementsByTagName(BudgetItem.XML_ELEMENT);
		for (int i = 0; i < tlist.getLength(); i++) {
			Node n = tlist.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			l.add(BudgetItem.fromXMLElement((Element) n, document));
		}
		
		return l;
	}
}
