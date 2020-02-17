package com.aific.finances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aific.finances.io.TransactionHistoryReader;


/**
 * An account
 * 
 * @author Peter Macko
 */
public class Account {
	
	public static final String XML_ELEMENT = "account";

	private String id;

	private String institution;
	private List<String> numbers;
	private AccountType type;
	private String name;
	private String shortName;
	private TransactionHistoryReader reader;
	
	
	/**
	 * Create an instance of Account
	 * 
	 * @param accounts the parent collection of accounts
	 * @param id the unique ID
	 * @param institution the institution
	 * @param numbers the account numbers (there can be more than one if the number changed)
	 * @param type the account type
	 * @param name the account name
	 * @param shortName the short account name
	 * @param reader the transaction history reader
	 */
	public Account(String id, String institution,
			List<String> numbers, AccountType type, String name, String shortName,
			TransactionHistoryReader reader) {
		
		this.id = id;
		this.institution = institution;
		this.numbers = numbers;
		this.type = type;
		this.name = name;
		this.shortName = shortName;
		this.reader = reader;
	}
	
	
	/**
	 * Get the account ID
	 * 
	 * @return the unique account ID
	 */
	public String getId() {
		return id;
	}


	/**
	 * Get the institution
	 * 
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}


	/**
	 * Set the institution
	 * 
	 * @param institution the institution to set
	 */
	public void setInstitution(String institution) {
		this.institution = institution;
	}


	/**
	 * Get the collection of all account numbers
	 * 
	 * @return the account numbers
	 */
	public List<String> getNumbers() {
		return Collections.unmodifiableList(numbers);
	}


	/**
	 * Get the accont type
	 * 
	 * @return the type
	 */
	public AccountType getType() {
		return type;
	}


	/**
	 * Set the account type
	 * 
	 * @param type the type to set
	 */
	public void setType(AccountType type) {
		this.type = type;
	}


	/**
	 * Get the account name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * Set the account name
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Get the short account name
	 * 
	 * @return the short name
	 */
	public String getShortName() {
		return shortName;
	}


	/**
	 * Set the short account name
	 * 
	 * @param shortName the short name to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}


	/**
	 * Get the transaction history reader
	 * 
	 * @return the transaction history reader
	 */
	public TransactionHistoryReader getReader() {
		return reader;
	}


	/**
	 * Set the transaction history reader
	 * 
	 * @param reader the transaction history reader
	 */
	public void setReader(TransactionHistoryReader reader) {
		this.reader = reader;
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
		Account other = (Account) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
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
		
		Element xmlInstitution = document.createElement("institution");
		xmlInstitution.appendChild(document.createTextNode(institution));
		me.appendChild(xmlInstitution);
		
		for (String n : numbers) {
			Element xmlNumber = document.createElement("number");
			xmlNumber.appendChild(document.createTextNode(n));
			me.appendChild(xmlNumber);
		}
		
		Element xmlType = document.createElement("type");
		xmlType.appendChild(document.createTextNode(type.name()));
		me.appendChild(xmlType);
		
		Element xmlName = document.createElement("name");
		xmlName.appendChild(document.createTextNode(name));
		me.appendChild(xmlName);
		
		Element xmlShortName = document.createElement("short_name");
		xmlShortName.appendChild(document.createTextNode(shortName));
		me.appendChild(xmlShortName);
		
		Element xmlReader = document.createElement("reader");
		xmlReader.appendChild(document.createTextNode(reader.getClass().getCanonicalName()));
		me.appendChild(xmlReader);
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param accounts the parent collection of accounts
	 * @param element the XML element
	 * @return the object
	 */
	public static Account fromXMLElement(Element element) {
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		String id = element.getAttribute("id");
		String institution = element.getElementsByTagName("institution").item(0).getTextContent();
		String sType = element.getElementsByTagName("type").item(0).getTextContent();
		String name = element.getElementsByTagName("name").item(0).getTextContent();
		String shortName = element.getElementsByTagName("short_name").item(0).getTextContent();
		String sReader = element.getElementsByTagName("reader").item(0).getTextContent();
		
		NodeList numberElements = element.getElementsByTagName("number");
		ArrayList<String> numbers = new ArrayList<String>();
		for (int i = 0; i < numberElements.getLength(); i++) {
			numbers.add(numberElements.item(i).getTextContent());
		}
		
		AccountType type = Enum.valueOf(AccountType.class, sType);
		
		TransactionHistoryReader reader;
		try {
			reader = (TransactionHistoryReader) Class.forName(sReader)
					.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return new Account(id, institution, numbers, type, name, shortName, reader);
	}
}
