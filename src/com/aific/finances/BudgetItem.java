package com.aific.finances;

import java.text.ParseException;

import org.w3c.dom.Element;

import com.aific.finances.util.DateFrequencyUnit;
import com.aific.finances.util.DateUnit;


/**
 * An item in the budget
 */
public class BudgetItem {
	
	public static final String XML_ELEMENT = "budget-item";

	private Category category;
	private int cents;
	private int frequency;
	private DateFrequencyUnit frequencyUnit;
	private String description;
	private String note;
	
	
	/**
	 * Create a new budget item
	 */
	public BudgetItem()
	{
		category = null;
		cents = 0;
		frequency = 1;
		description = "";
		note = "";
		frequencyUnit = new DateFrequencyUnit(1, DateUnit.MONTH);
	}


	/**
	 * Get the category
	 * 
	 * @return the category
	 */
	public Category getCategory()
	{
		return category;
	}


	/**
	 * Set the category
	 * 
	 * @param category the category to set
	 */
	public void setCategory(Category category)
	{
		this.category = category;
	}


	/**
	 * Get the value in cents
	 * 
	 * @return the cents
	 */
	public int getCents()
	{
		return cents;
	}


	/**
	 * Set the value in cents
	 * 
	 * @param cents the cents to set
	 */
	public void setCents(int cents)
	{
		if (cents < 0) throw new IllegalArgumentException("The amount cannot be negative");
		this.cents = cents;
	}


	/**
	 * Get the description
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}


	/**
	 * Set the description
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}


	/**
	 * Get the note
	 * 
	 * @return the note
	 */
	public String getNote()
	{
		return note;
	}


	/**
	 * Set the note
	 * 
	 * @param note the note to set
	 */
	public void setNote(String note)
	{
		this.note = note;
	}


	/**
	 * Get the frequency
	 * 
	 * @return the frequency
	 */
	public int getFrequency()
	{
		return frequency;
	}


	/**
	 * Set the frequency
	 * 
	 * @param unit the frequency to set
	 */
	public void setFrequency(int frequency)
	{
		if (frequency < 0) throw new IllegalArgumentException("The frequency cannot be negative");
		this.frequency = frequency;
	}


	/**
	 * Get the frequency unit
	 * 
	 * @return the frequency unit
	 */
	public DateFrequencyUnit getFrequencyUnit()
	{
		return frequencyUnit;
	}


	/**
	 * Set the frequency unit
	 * 
	 * @param unit the frequency unit to set
	 */
	public void setFrequencyUnit(DateFrequencyUnit unit)
	{
		this.frequencyUnit = unit;
	}
	
	
	/**
	 * Get the amount per year
	 * 
	 * @return the currency amount per year (not cents!)
	 */
	public double getAmountPerYear()
	{
		return DateFrequencyUnit.YEARLY.convertFrom(
				cents * frequency, frequencyUnit) / 100.0;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + cents;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + frequency;
		result = prime * result + ((frequencyUnit == null) ? 0 : frequencyUnit.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
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
		BudgetItem other = (BudgetItem) obj;
		if (category == null) {
			if (other.category != null) {
				return false;
			}
		} else if (!category.equals(other.category)) {
			return false;
		}
		if (cents != other.cents) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (frequency != other.frequency) {
			return false;
		}
		if (frequencyUnit == null) {
			if (other.frequencyUnit != null) {
				return false;
			}
		} else if (!frequencyUnit.equals(other.frequencyUnit)) {
			return false;
		}
		if (note == null) {
			if (other.note != null) {
				return false;
			}
		} else if (!note.equals(other.note)) {
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BudgetItem [category=" + category + ", cents=" + cents
				+ ", frequency=" + frequency + ", frequencyUnit=" + frequencyUnit
				+ ", description=" + description + ", note=" + note + "]";
	}


	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		if (category != null) {
			Element xmlCategory = document.createElement("category");
			xmlCategory.appendChild(document.createTextNode(category.getId()));
			me.appendChild(xmlCategory);
		}

		Element xmlCents = document.createElement("cents");
		xmlCents.appendChild(document.createTextNode("" + cents));
		me.appendChild(xmlCents);
		
		Element xmlFrequency = document.createElement("frequency");
		xmlFrequency.appendChild(document.createTextNode("" + frequency));
		me.appendChild(xmlFrequency);
		
		Element xmlFrequencyUnit = document.createElement("frequency-unit");
		xmlFrequencyUnit.appendChild(document.createTextNode(frequencyUnit.getValueForXml()));
		me.appendChild(xmlFrequencyUnit);
		
		if (description != null) {
			Element xmlDesc = document.createElement("description");
			xmlDesc.appendChild(document.createTextNode(description));
			me.appendChild(xmlDesc);
		}
		
		if (note != null) {
			Element xmlDesc = document.createElement("note");
			xmlDesc.appendChild(document.createTextNode(note));
			me.appendChild(xmlDesc);
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param element the XML element
	 * @param document the parent document
	 * @return the object
	 * @throws ParseException on date parse error
	 */
	public static BudgetItem fromXMLElement(Element element, Document document) throws ParseException {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		String s_category = element.getElementsByTagName("category").getLength() > 0
				? element.getElementsByTagName("category").item(0).getTextContent() : null;
		String s_cents = element.getElementsByTagName("cents").item(0).getTextContent();
		String s_frequency = element.getElementsByTagName("frequency").item(0).getTextContent();
		String s_frequencyUnit = element.getElementsByTagName("frequency-unit").getLength() > 0
				? element.getElementsByTagName("frequency-unit").item(0).getTextContent() : null;
		String description = element.getElementsByTagName("description").getLength() > 0
				? element.getElementsByTagName("description").item(0).getTextContent() : null;
		String note = element.getElementsByTagName("note").getLength() > 0
				? element.getElementsByTagName("note").item(0).getTextContent() : null;
		
		int cents = Integer.parseInt(s_cents);
		int frequency = Integer.parseInt(s_frequency);
		
		Category category = s_category != null ? document.getCategories().get(s_category) : null;
		DateFrequencyUnit frequencyUnit = DateFrequencyUnit.parseXmlValue(s_frequencyUnit);

		BudgetItem b = new BudgetItem();
		b.category = category;
		b.cents = cents;
		b.frequency = frequency;
		b.frequencyUnit = frequencyUnit;
		b.description = description;
		b.note = note;
		
		return b;
	}
}
