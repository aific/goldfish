package com.aific.finances;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.plot.ChartSeries;


/**
 * A category for a transaction
 * 
 * @author Peter Macko
 */
public class Category implements ChartSeries, Comparable<Category> {
	
	public static final String XML_ELEMENT = "category";

	private Categories categories;
	
	private String id;
	private String name;
	private CategoryType type;
	private Color color;
	
	private HashMap<String, CategoryDetector> detectors;
	private CategoryDetector nullDetector;
	
	private List<CategoryListener> listeners;

	
	/**
	 * Create an object of type Category
	 * 
	 * @param categories the parent collection of categories
	 * @param id the unique ID
	 * @param name the name
	 * @param type the category type
	 * @param color the color
	 */
	public Category(Categories categories, String id, String name, CategoryType type, Color color) {
		
		this.categories = categories;
		
		this.id = id;
		this.name = name;
		this.type = type;
		this.color = color;
		
		this.listeners = null;
		this.detectors = new HashMap<String, CategoryDetector>();
		this.nullDetector = new CategoryDetector(id, this, null, null, ".*", 0, 0, null, null);
		
		add(nullDetector);
		
		synchronized (categories) {
			int c = categories.categories.size();
			categories.categories.add(this);
			categories.fireCategoriesAdded(c, c);
		}
	}
	
	
	/**
	 * Get the parent collection of categories that contains this category
	 * 
	 * @return the parent collection of categories
	 */
	public Categories getContainer() {
		return categories;
	}
	
	
	/**
	 * Add a detector
	 * 
	 * @param detector the detector
	 */
	public void add(CategoryDetector detector) {
		categories.detectors.put(detector.getId(), detector);
		if (detector != nullDetector) {
			detectors.put(detector.getId(), detector);
			fireCategoryDetectorAdded(detector);
		}
	}
	
	
	/**
	 * Get the unique ID
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * Get the name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * Set the name
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Get the category type
	 * 
	 * @return the type
	 */
	public CategoryType getType() {
		return type;
	}


	/**
	 * Set the category type
	 * 
	 * @param type the type to set
	 */
	public void setType(CategoryType type) {
		this.type = type;
	}


	/**
	 * Get the category color
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}


	/**
	 * Set the category color
	 * 
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	
	/**
	 * Get the null detector used for manual entry
	 * 
	 * @return the null detector
	 */
	public CategoryDetector getNullDetector() {
		return nullDetector;
	}
	
	
	/**
	 * Get all detectors
	 * 
	 * @return the set of all detectors
	 */
	public Collection<CategoryDetector> getDetectors() {
		return Collections.unmodifiableCollection(detectors.values());
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
	 * Find a set of all detectors that match the given transaction
	 * 
	 * @param transaction the transaction
	 * @param existingTransactions the list of existing transactions
	 * @return the set of all matching detectors
	 */
	public Set<CategoryDetector> findMatchingDetectors(Transaction transaction,
			TransactionList existingTransactions) {
		HashSet<CategoryDetector> result = new HashSet<CategoryDetector>();
		for (CategoryDetector d : detectors.values()) {
			if (d.accepts(transaction, existingTransactions)) result.add(d);
		}
		return result;
	}
	
	
	/**
	 * Determine if this category is the same as the built-in category
	 * 
	 * @return true if it is the same as a built-in category
	 */
	public boolean isSameAsBuiltin() {
		return equalsCompletely(Categories.builtinCategories().get(id));
	}
	
	
	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void addCategoryListener(CategoryListener listener) {
		
		if (listeners == null) listeners = new LinkedList<CategoryListener>();
		listeners.add(listener);
	}
	
	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	public synchronized void removeCategoryListener(CategoryListener listener) {
		
		if (listeners != null) listeners.remove(listener);
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param detector the detector that was added
	 */
	protected synchronized void fireCategoryDetectorAdded(CategoryDetector detector) {
		
		if (listeners != null) {
			for (CategoryListener l : listeners)
				l.categoryDetectorAdded(this, detector);
		}
	}

	
	/**
	 * Create the base XML element without the detectors
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	private Element createXMLElement(Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		Attr attr = document.createAttribute("id");
		attr.setValue(id);
		me.setAttributeNode(attr);
		
		Element xmlName = document.createElement("name");
		xmlName.appendChild(document.createTextNode(name));
		me.appendChild(xmlName);
		
		Element xmlColorR = document.createElement("color_red");
		xmlColorR.appendChild(document.createTextNode("" + color.getRed()));
		me.appendChild(xmlColorR);
		
		Element xmlColorG = document.createElement("color_green");
		xmlColorG.appendChild(document.createTextNode("" + color.getGreen()));
		me.appendChild(xmlColorG);
		
		Element xmlColorB = document.createElement("color_blue");
		xmlColorB.appendChild(document.createTextNode("" + color.getBlue()));
		me.appendChild(xmlColorB);
		
		Element xmlType = document.createElement("type");
		xmlType.appendChild(document.createTextNode(type.name()));
		me.appendChild(xmlType);
		
		return me;
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(Document document) {
		Element me = createXMLElement(document);

		Element xmlDetectors = document.createElement("detectors");
		me.appendChild(xmlDetectors);
		for (CategoryDetector d : detectors.values()) {
			if (d.isDerived()) continue;
			xmlDetectors.appendChild(d.toXMLElement(document));
		}
		
		return me;
	}

	
	/**
	 * Write updates to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element updatesToXMLElement(Document document) {
		Element me = createXMLElement(document);

		Element xmlDetectors = document.createElement("detectors");
		me.appendChild(xmlDetectors);
		for (CategoryDetector d : detectors.values()) {
			if (d.isDerived() || d.isSameAsBuiltin()) continue;
			xmlDetectors.appendChild(d.toXMLElement(document));
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param element the XML element
	 * @param categories the parent collection of categories
	 * @return the object
	 */
	public static Category fromXMLElement(Element element, Categories categories) {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		String id = element.getAttribute("id");
		String name = element.getElementsByTagName("name").item(0).getTextContent();
		String cr = element.getElementsByTagName("color_red").item(0).getTextContent();
		String cg = element.getElementsByTagName("color_green").item(0).getTextContent();
		String cb = element.getElementsByTagName("color_blue").item(0).getTextContent();
		String sType = element.getElementsByTagName("type").item(0).getTextContent();
		
		int r = Integer.parseInt(cr);
		int g = Integer.parseInt(cg);
		int b = Integer.parseInt(cb);
		Color color = new Color(r, g, b);
		
		CategoryType type = Enum.valueOf(CategoryType.class, sType.toUpperCase());
		
		
		// Check if the category already exists, and if so, update it

		Category category = categories.get(id);
		
		if (category == null) {
			category = new Category(categories, id, name, type, color);
		}
		else {
			
			if (category.type != type) {
				throw new RuntimeException("The category cannot change its type");
			}
			
			category.setName(name);
			category.setColor(color);
		}
		
		Node detectorsNode = element.getElementsByTagName("detectors").item(0);
		if (detectorsNode.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
		Element detectorsElement = (Element) detectorsNode;
		
		NodeList detectorsList = detectorsElement.getElementsByTagName(CategoryDetector.XML_ELEMENT);
		for (int i = 0; i < detectorsList.getLength(); i++) {
			Node n = detectorsList.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			
			CategoryDetector.fromXMLElement(category, (Element) n);
		}
		
		return category;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	/**
	 * Determine whether the object is completely equal
	 * 
	 * @param obj the other object
	 * @return true if it is equal
	 */
	public boolean equalsCompletely(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (detectors == null) {
			if (other.detectors != null)
				return false;
		} else if (!detectors.equals(other.detectors))
			return false;
		return true;
	}


	/**
	 * Compare to another {@link Category}
	 * 
	 * @param other the other object
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(Category other) {
		if (other == null) return 1;
		int r = name.compareTo(other.name);
		if (r != 0) return r;
		return id.compareTo(other.id);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}


	/**
	 * Comparator - compare by name
	 */
	public static class ByName implements Comparator<Category> {

		/**
		 * Compare two categories
		 * 
		 * @param a the first category
		 * @param b the second category
		 * @return the result of the comparison
		 */
		@Override
		public int compare(Category a, Category b) {
			if (a == b) return 0;
			if (a == null) return 1;
			if (b == null) return -1;
			return a.getName().compareTo(b.getName());
		}
	}
}
