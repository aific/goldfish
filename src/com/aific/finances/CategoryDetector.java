package com.aific.finances;

import java.util.Collection;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An automatic detector for transaction categories 
 * 
 * @author Peter Macko
 */
public class CategoryDetector implements Comparable<CategoryDetector> {
	
	public static final String XML_ELEMENT = "detector";
	public static final int MAX_MATCHING_DAYS_DELTA = 5;
	
	private String id;
	private Category category;
	private String vendor;
	private String description;
	
	private String pattern;
	private Pattern compiledPattern;
	private int centsMin;
	private int centsMax;
	
	private String matchingPattern;
	private Pattern compiledMatchingPattern;
	private CategoryDetector matchingDetector;
	
	
	/**
	 * Create an object of type {@link CategoryDetector} and add it to the set
	 * of detectors corresponding to the given {@link Category}
	 *
	 * @param id the unique ID
	 * @param categories the collection of categories
	 * @param category the category (must be contained in the categories collection)
	 * @param vendor the vendor
	 * @param description the description of what was detected (optional)
	 * @param pattern the regular expression pattern
	 * @param min the min value of signed cents (use 0 for both min and max to disable)
	 * @param max the max value of signed cents
	 * @param matchingPattern the regular expression pattern for matching opposite transactions
	 * @param matchingDetector the matching category detector
	 */
	public CategoryDetector(String id, Categories categories, Category category, String vendor, String description,
			String pattern, int min, int max, String matchingPattern, CategoryDetector matchingDetector) {
		
		this.id = id;
		this.category = category;
		this.vendor = vendor;
		this.description = description;
		
		this.pattern = pattern;
		this.compiledPattern = Pattern.compile(pattern);
		this.centsMin = min;
		this.centsMax = max;
		
		this.matchingPattern = matchingPattern;
		this.compiledMatchingPattern = matchingPattern == null ? null : Pattern.compile(matchingPattern);
		this.matchingDetector = matchingDetector;
		if (this.matchingDetector != null) this.matchingDetector.matchingDetector = this; 
		
		if (category != null) category.add(this);
		categories.detectors.put(id, this);
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
	 * Get the category
	 * 
	 * @return the category
	 */
	public Category getCategory() {
		return category;
	}


	/**
	 * Set the vendor
	 * 
	 * @param vendor the vendor to set
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
		
		if (matchingDetector != null) {
			matchingDetector.vendor = vendor;
		}
	}


	/**
	 * Get the vendor
	 * 
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}


	/**
	 * Get the description of what was detected (optional)
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * Set the description of what was detected (optional)
	 * 
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;

		if (matchingDetector != null) {
			String d = description;
			if ("".equals(d)) d = "Match"; else d = "Match - " + d;
			matchingDetector.description = d;
		}
	}


	/**
	 * Get the regular expression pattern
	 * 
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}


	/**
	 * Set the regular expression pattern
	 * 
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.compiledPattern = Pattern.compile(pattern);
		this.pattern = pattern;
		
		if (matchingDetector != null) {
			matchingDetector.matchingPattern = this.pattern;
			matchingDetector.compiledMatchingPattern = this.compiledPattern;
		}
	}


	/**
	 * Get the minimum amount in signed cents. If the min and the max are
	 * both 0, the check is disabled.
	 * 
	 * @return the minimum amount of cents
	 */
	public int getCentsMin() {
		return centsMin;
	}


	/**
	 * Get the maximum amount in signed cents. If the min and the max are
	 * both 0, the check is disabled.
	 * 
	 * @return the maximum amount of cents
	 */
	public int getCentsMax() {
		return centsMax;
	}


	/**
	 * Set the range of cents to match
	 * 
	 * @param min the min value of signed cents (use 0 for both min and max to disable)
	 * @param max the max value of signed cents
	 */
	public void setCentsRange(int min, int max) {
		
		if (max > min) {
			int t = max;
			max = min;
			min = t;
		}
		
		this.centsMin = min;
		this.centsMax = max;
		
		if (matchingDetector != null) {
			matchingDetector.centsMin = -max;
			matchingDetector.centsMax = -min;
		}
	}


	/**
	 * Get the regular expression pattern for matching transactions
	 * 
	 * @return the pattern
	 */
	public String getMatchingPattern() {
		return matchingPattern;
	}


	/**
	 * Set the regular expression pattern for matching transactions
	 * 
	 * @param pattern the pattern to set
	 */
	public void setMatchingPattern(String pattern) {
		
		if ((matchingPattern != null) != (pattern != null)) {
			throw new IllegalArgumentException();
		}
		
		this.matchingPattern = pattern;
		this.compiledMatchingPattern = matchingPattern == null ? null : Pattern.compile(matchingPattern);

		if (matchingDetector != null) {
			matchingDetector.pattern = this.matchingPattern;
			matchingDetector.compiledPattern = this.compiledMatchingPattern;
		}
	}
	
	
	/**
	 * Get the corresponding matching detector
	 * 
	 * @return the matching detector, or null if none
	 */
	public CategoryDetector getMatchingDetector() {
		return matchingDetector;
	}
	
	
	/**
	 * For detectors with matching patterns, return true if this is the corresponding
	 * matching detector
	 * 
	 * @return true if this is the matching detector, false if it is the main detector
	 */
	public boolean isDerived() {
		if (matchingDetector == null) return false;
		return id.compareTo(matchingDetector.id) > 0;
	}

	
	/**
	 * Does this detector accept the given transaction? 
	 * 
	 * @param transaction the transaction
	 * @param existingTransactions the list of existing transactions
	 * @return true if it accepts it
	 */
	public boolean accepts(Transaction transaction, TransactionList existingTransactions) {
		
		if (centsMin != 0 && centsMax != 0) {
			int min = Math.min(centsMin, centsMax);
			int max = Math.max(centsMin, centsMax);
			if (transaction.getCents() < min || transaction.getCents() > max) {
				return false;
			}
		}

		if (!compiledPattern.matcher(transaction.getDescription()).matches()) return false;
		
		if (compiledMatchingPattern != null && matchingDetector != null) {
			Collection<Transaction> c = existingTransactions.getByCents(-transaction.getCents());
			if (c == null) return false;
			
			for (Transaction t : c) {
				if (Math.abs(transaction.getDate().getTime() - t.getDate().getTime())
						<= MAX_MATCHING_DAYS_DELTA * 24l * 3600l * 1000l) {
					if (compiledMatchingPattern.matcher(t.getDescription()).matches()) {
						t.getCandidateDetectors().add(matchingDetector);
						t.setCategoryDetector(matchingDetector);
						t.setMatchingTransaction(transaction);
						transaction.setMatchingTransaction(t);
						return true;
					}
				}
			}
			
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Determine if this category detector is the same as the one of the built-in detectors
	 * 
	 * @return true if it is the same as a built-in detector
	 */
	public boolean isSameAsBuiltin() {
		return equals(Categories.builtinCategories().getDetector(id));
	}
	
	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		Attr attr = document.createAttribute("id");
		attr.setValue(id);
		me.setAttributeNode(attr);
		
		Element xmlVendor = document.createElement("vendor");
		xmlVendor.appendChild(document.createTextNode(vendor));
		me.appendChild(xmlVendor);
		
		Element xmlDescription = document.createElement("description");
		xmlDescription.appendChild(document.createTextNode(description));
		me.appendChild(xmlDescription);
		
		Element xmlPattern = document.createElement("pattern");
		xmlPattern.appendChild(document.createTextNode(pattern));
		me.appendChild(xmlPattern);
		
		Element xmlCentsMin = document.createElement("cents_min");
		xmlCentsMin.appendChild(document.createTextNode("" + centsMin));
		me.appendChild(xmlCentsMin);
		
		Element xmlCentsMax = document.createElement("cents_max");
		xmlCentsMax.appendChild(document.createTextNode("" + centsMax));
		me.appendChild(xmlCentsMax);
		
		if (matchingPattern != null) {
			Element xmlMatches = document.createElement("matches");
			xmlMatches.appendChild(document.createTextNode(matchingPattern));
			me.appendChild(xmlMatches);
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param category the parent category
	 * @param element the XML element
	 * @return the object
	 */
	public static CategoryDetector fromXMLElement(Category category, Element element) {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		String id = element.getAttribute("id");
		String pattern = element.getElementsByTagName("pattern").item(0).getTextContent();
		
		String vendor = "";
		String description = "";
		
		if (element.getElementsByTagName("vendor").getLength() > 0)
			vendor = element.getElementsByTagName("vendor").item(0).getTextContent();
		if (element.getElementsByTagName("description").getLength() > 0)
			description = element.getElementsByTagName("description").item(0).getTextContent();
		
		int centsMin = 0;
		int centsMax = 0;
		
		if (element.getElementsByTagName("cents_min").getLength() > 0)
			centsMin = Integer.parseInt(element.getElementsByTagName("cents_min").item(0).getTextContent());
		if (element.getElementsByTagName("cents_max").getLength() > 0)
			centsMax = Integer.parseInt(element.getElementsByTagName("cents_max").item(0).getTextContent());
		
		String matches = null;
		if (element.getElementsByTagName("matches").getLength() > 0) {
			matches = element.getElementsByTagName("matches").item(0).getTextContent();
			if (category.getType() != CategoryType.BALANCED) {
				throw new RuntimeException("Invalid category detector: \"matches\" supported only "
						+ "for balanced categories");
			}
		}
		else {
			if (category.getType() == CategoryType.BALANCED) {
				throw new RuntimeException("Invalid category detector: \"matches\" must be present "
						+ "in balanced categories");
			}
		}
		
		
		// Check if the detector already exists, and if so, modify it
		
		CategoryDetector d = category.getDetector(id);
		
		if (d == null) {
			
			CategoryDetector matchingDetector = null;
			if (matches != null) {
				String s = description;
				if ("".equals(s)) s = "Match"; else s = "Match - " + s;
				matchingDetector = new CategoryDetector(id + "::m", category.getContainer(),
						category, vendor, s, matches, -centsMax, -centsMin, pattern, null);
			}

			d = new CategoryDetector(id, category.getContainer(), category, vendor,
					description, pattern, centsMin, centsMax, matches, matchingDetector);
			
			if (matchingDetector != null) matchingDetector.matchingDetector = d;
		}
		else {
		
			d.setVendor(vendor);
			d.setDescription(description);
			d.setCentsRange(centsMin, centsMax);
			d.setPattern(pattern);
			if (matches != null) d.setMatchingPattern(matches);

		}
		
		return d;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (category == null) return "----------";
		
		String s = category.getName();
		if (description != null && !"".equals(description)) {
			if (vendor != null && !"".equals(vendor)) {
				s += " (" + description + " - " + vendor + ")";
			}
			else {
				s += " (" + description + ")";
			}
		}
		else {
			if (vendor != null && !"".equals(vendor)) {
				s += " (" + vendor + ")";
			}
		}
		
		return s;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + centsMax;
		result = prime * result + centsMin;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((matchingPattern == null) ? 0 : matchingPattern.hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result + ((vendor == null) ? 0 : vendor.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CategoryDetector other = (CategoryDetector) obj;
		if (category == null) {
			if (other.category != null) {
				return false;
			}
		} else if (!category.equals(other.category)) {
			return false;
		}
		if (centsMax != other.centsMax) {
			return false;
		}
		if (centsMin != other.centsMin) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (matchingPattern == null) {
			if (other.matchingPattern != null) {
				return false;
			}
		} else if (!matchingPattern.equals(other.matchingPattern)) {
			return false;
		}
		if (pattern == null) {
			if (other.pattern != null) {
				return false;
			}
		} else if (!pattern.equals(other.pattern)) {
			return false;
		}
		if (vendor == null) {
			if (other.vendor != null) {
				return false;
			}
		} else if (!vendor.equals(other.vendor)) {
			return false;
		}
		return true;
	}


	/**
	 * Compare to another {@link CategoryDetector}
	 * 
	 * @param other the other object
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(CategoryDetector other) {
		
		if (category != other.category) {
			if (category == null) return -1;
			if (other.category == null) return 1;
			return category.compareTo(other.category);
		}

		return toString().compareTo(other.toString());
	}
}
