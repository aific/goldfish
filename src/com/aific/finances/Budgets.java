package com.aific.finances;

import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.util.SharedArrayList;


/**
 * A collection of budgets
 */
public class Budgets {
	
	public static final String XML_ELEMENT = "budgets";

	private SharedArrayList<Budget> budgets;
	
	
	/**
	 * Create an instance of the class
	 */
	public Budgets()
	{
		budgets = new SharedArrayList<>();
	}
	
	
	/**
	 * Get the current budget, or create one if none
	 * 
	 * @return the budget
	 */
	public synchronized Budget getOrCreateCurrentBudget()
	{
		if (budgets.isEmpty()) {
			budgets.add(new Budget());
		}
		
		return budgets.get(0);
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);

		for (Budget b : budgets) {
			me.appendChild(b.toXMLElement(document));
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
	public static Budgets fromXMLElement(Element element,
			Document document) throws ParseException {

		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		Budgets l = new Budgets();
		
		NodeList tlist = element.getElementsByTagName(Budget.XML_ELEMENT);
		for (int i = 0; i < tlist.getLength(); i++) {
			Node n = tlist.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			l.budgets.add(Budget.fromXMLElement((Element) n, document));
		}
		
		return l;
	}
}
