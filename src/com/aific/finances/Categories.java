package com.aific.finances;

import java.io.InputStream;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;


/**
 * The collection of all categories
 * 
 * @author Peter Macko
 */
public class Categories extends AbstractList<Category> {
	
	public static final String XML_ELEMENT = "categories";

	private static Categories builtinCategories = fromBuiltin();
	
	ArrayList<Category> categories;
	HashMap<String, CategoryDetector> detectors;
	private CategoryDetector nullDetector;
	
	private List<CategoriesListener> listeners;

	
	/**
	 * Create an empty collection of all categories
	 */
	private Categories() {
		
		this.categories = new ArrayList<Category>();
		this.detectors = new HashMap<String, CategoryDetector>();
		this.listeners = null;
		
		this.nullDetector = new CategoryDetector("", this, null, null, null, ".*", 0, 0, null, null);
	}
	
	
	/**
	 * Get the global instance of the built-in categories
	 * 
	 * @return the instance of the built-in categories
	 */
	static Categories builtinCategories() {
		return builtinCategories;
	}
	
	
	/**
	 * Get all categories
	 * 
	 * @return the set of all categories
	 */
	public List<Category> getCategories() {
		return Collections.unmodifiableList(categories);
	}
	
	
	/**
	 * Get the number of categories
	 * 
	 * @return the number of categories
	 */
	public int size() {
		return categories.size();
	}
	
	
	/**
	 * Get a category by its index in the list
	 * 
	 * @param index the index
	 * @return the category
	 */
	public Category get(int index) {
		return categories.get(index);
	}
	
	
	/**
	 * Get a category by its ID
	 * 
	 * @param id the ID
	 * @return the category, or null if not found
	 */
	public Category get(String id) {
		
		for (Category c : categories) {
			if (c.getId().equals(id)) return c;
		}
		
		return null;
	}
	
	
	/**
	 * Get the null detector for the null category - to be used for transactions
	 * with no category
	 * 
	 * @return the null detector
	 */
	public CategoryDetector getNullDetector() {
		return nullDetector;
	}
	
	
	/**
	 * Get a detector by its ID
	 * 
	 * @param id the ID
	 * @return the detector, or null if not found
	 */
	public CategoryDetector getDetector(String id) {
		return detectors.get(id);
	}
	
	
	/**
	 * Attempt to detect possible categories for the given transaction
	 * 
	 * @param transaction the transaction
	 * @param existingTransactions the list of existing transactions
	 * @return true if at least one category was found
	 */
	public boolean detectCategories(Transaction transaction,
			TransactionList existingTransactions) {
		
		HashSet<CategoryDetector> matches = new HashSet<CategoryDetector>();
		CategoryDetector firstMatch = null;
		
		for (Category c : categories) {
			for (CategoryDetector d : c.getDetectors()) {
				if (d.accepts(transaction, existingTransactions)) {
					matches.add(d);
					if (firstMatch == null) {
						firstMatch = d;
					}
				}
			}
		}
		
		transaction.setCandidateDetectors(matches);
		
		if (firstMatch != null
				&& transaction.getCategoryDetector() == getNullDetector()) {
			transaction.setCategoryDetector(firstMatch);
		}
		
		return !matches.isEmpty();
	}
	
	
	/**
	 * Attempt to detect possible categories for each transaction in a collection
	 * 
	 * @param transactions the transactions
	 * @param existingTransactions the list of existing transactions
	 */
	public void detectCategoriesAll(Collection<Transaction> transactions,
			TransactionList existingTransactions) {
		for (Transaction t : transactions) detectCategories(t, existingTransactions);
	}
	
	
	/**
	 * Attempt to detect possible categories for each uncategorized transaction in a list
	 * 
	 * @param transactionList the transaction list
	 */
	public void detectCategoriesForUncategorized(TransactionList transactionList) {
		for (Transaction t : transactionList.getList())
			if (t.getCategory() == null) detectCategories(t, transactionList);
	}
	
	
	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void addCategoriesListener(CategoriesListener listener) {
		
		if (listeners == null) listeners = new LinkedList<CategoriesListener>();
		listeners.add(listener);
	}
	
	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeCategoriesListener(CategoriesListener listener) {
		
		if (listeners != null) listeners.remove(listener);
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireCategoriesAdded(int from, int to) {
		
		if (listeners != null) {
			for (CategoriesListener l : listeners)
				l.categoriesAdded(this, from, to);
		}
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireCategoriesRemoved(int from, int to) {
		
		if (listeners != null) {
			for (CategoriesListener l : listeners)
				l.categoriesRemoved(this, from, to);
		}
	}
	
	
	/**
	 * Fire a listener
	 */
	public synchronized void fireCategoriesDataChanged() {
		
		if (listeners != null) {
			for (CategoriesListener l : listeners)
				l.categoriesDataChanged(this);
		}
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		for (Category c : categories) {
			me.appendChild(c.toXMLElement(document));
		}
		
		return me;
	}

	
	/**
	 * Write the updates to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element updatesToXMLElement(Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		for (Category c : categories) {
			if (!c.isSameAsBuiltin()) {
				me.appendChild(c.updatesToXMLElement(document));
			}
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML and update the contents of the current object
	 * 
	 * @param element the XML element
	 * @return the object
	 */
	public void updateFromXMLElement(Element element) {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		NodeList categoriesList = element.getElementsByTagName(Category.XML_ELEMENT);
		for (int i = 0; i < categoriesList.getLength(); i++) {
			Node n = categoriesList.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			Category.fromXMLElement((Element) n, this);
		}
	}
	
	
	/**
	 * Read the object from an XML. Note that this will clear and reload the shared
	 * global collection of the categories!
	 * 
	 * @param element the XML element
	 * @return the object
	 */
	public static Categories fromXMLElement(Element element) {
		
		Categories categories = new Categories();
		categories.categories.clear();
		categories.updateFromXMLElement(element);
		
		return categories;
	}
	
	
	/**
	 * Load the built-in categories from the bundled XML file
	 * 
	 *  @return the object
	 */
	public static Categories fromBuiltin() {

		URL categoriesURL = Main.class.getResource("/resources/categories.xml");

		try {
			InputStream categoriesURLStream = categoriesURL.openStream();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document doc = dBuilder.parse(categoriesURLStream);
			doc.getDocumentElement().normalize();
			
			Categories c = fromXMLElement(doc.getDocumentElement());
			
			categoriesURLStream.close();
			
			return c;
		}
		catch (SAXParseException e) {
			System.err.println("Error at line " + e.getLineNumber() + ": " + e.toString());
			throw new RuntimeException("Cannot load the built-in categories", e);
		}
		catch (Exception e) {
			throw new RuntimeException("Cannot load the built-in categories", e);
		}		
	}
}
