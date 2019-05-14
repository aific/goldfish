package com.aific.finances;

import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * The collection of all accounts
 * 
 * @author Peter Macko
 */
public class Accounts {
	
	public static final String XML_ELEMENT = "accounts";
	
	HashMap<String, Account> accounts;
	
	
	/**
	 * Create an empty collection of all categories
	 */
	public Accounts() {
		this.accounts = new HashMap<String, Account>();
	}
	
	
	/**
	 * Get an Account for the given ID
	 * 
	 * @param id the account id
	 * @return the account
	 * @throws NoSuchElementException if not found
	 */
	public Account get(String id) {
		Account a = accounts.get(id);
		if (a == null) throw new NoSuchElementException();
		return a;
	}
	
	
	/**
	 * Get all accounts
	 * 
	 * @return all accounts
	 */
	public Collection<Account> getAll() {
		return accounts.values();
	}
	
	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		for (Account a : accounts.values()) {
			me.appendChild(a.toXMLElement(document));
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML. Note that this will clear and reload the shared
	 * global collection of the categories!
	 * 
	 * @param element the XML element
	 * @return the object
	 */
	public static Accounts fromXMLElement(Element element) {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		Accounts accounts = new Accounts();
		accounts.accounts.clear();
		
		NodeList accountsList = element.getElementsByTagName(Account.XML_ELEMENT);
		for (int i = 0; i < accountsList.getLength(); i++) {
			Node n = accountsList.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			Account.fromXMLElement((Element) n, accounts);
		}
		
		return accounts;
	}
}
