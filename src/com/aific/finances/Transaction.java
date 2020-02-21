package com.aific.finances;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.aific.finances.util.Month;


/**
 * A transaction
 * 
 * @author Peter Macko
 */
public class Transaction {
	
	public static final String XML_ELEMENT = "transaction";
	private static final DateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private Account account;
	private String id;

	private Date date;
	private String description;
	private String address;
	private int cents;
	
	private CategoryDetector categoryDetector;
	private String note;
	
	private Set<CategoryDetector> candidateDetectors;
	
	private Transaction matchingTransaction;
	
	
	/**
	 * Create an object of type {@link Transaction}
	 * 
	 * @param document the document
	 * @param account the account
	 * @param id the transaction ID, unique within the account
	 * @param date the transaction date
	 * @param type the transaction type
	 * @param description the description 
	 * @param address the address or payee type information
	 * @param cents the signed amount as cents, negative for debits, positive for credits
	 */
	public Transaction(Account account, String id, Date date,
			String description, String address, int cents) {
		
		this.account = account;
		this.id = id;
		this.date = date;
		this.description = description;
		this.address = address;
		this.cents = cents;
		
		this.categoryDetector = Categories.NULL_DETECTOR;
		this.note = "";
		this.candidateDetectors = Collections.emptySet();
		
		this.matchingTransaction = null;
	}


	/**
	 * Get the account
	 * 
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}


	/**
	 * Get the transaction ID
	 * 
	 * @return the ID, unique within the account
	 */
	public String getId() {
		return id;
	}

	
	/**
	 * Get the transaction date
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	
	/**
	 * Get the transaction month
	 * 
	 * @return the date
	 */
	public Month getMonth() {
		return new Month(date);
	}


	/**
	 * Get the transaction description
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * Get the address or transaction payee type
	 *  
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * Get the signed amount as cents, negative for debits, positive for credits
	 * 
	 * @return the cents, negative for debits, positive for credits
	 */
	public int getCents() {
		return cents;
	}


	/**
	 * Get the category
	 * 
	 * @return the category
	 */
	public Category getCategory() {
		return categoryDetector.getCategory();
	}


	/**
	 * Get the detector that detected the category
	 * 
	 * @return the detector, or null for manual entry
	 */
	public CategoryDetector getCategoryDetector() {
		return categoryDetector;
	}


	/**
	 * Get the user-supplied note
	 * 
	 * @return the note
	 */
	public String getNote() {
		return note;
	}


	/**
	 * Set the detector that detected the category
	 * 
	 * @param detector the detector
	 */
	public void setCategoryDetector(CategoryDetector detector) {
		this.categoryDetector = detector;
	}


	/**
	 * Set the user-supplied note
	 * 
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}


	/**
	 * Set the candidate detectors
	 * 
	 * @param candidateDetectors the new set of candidate detectors
	 */
	public void setCandidateDetectors(Set<CategoryDetector> candidateDetectors) {
		this.candidateDetectors = candidateDetectors;
	}


	/**
	 * Return the matching transaction
	 * 
	 * @return the matching transaction
	 */
	public Transaction getMatchingTransaction() {
		return matchingTransaction;
	}


	/**
	 * Set the matching transaction
	 * 
	 * @param matchingTransaction the new matching transaction
	 */
	public void setMatchingTransaction(Transaction matchingTransaction) {
		this.matchingTransaction = matchingTransaction;
	}


	/**
	 * Return the set of candidate detectors
	 * 
	 * @return the candidate detectors
	 */
	public Set<CategoryDetector> getCandidateDetectors() {
		return candidateDetectors;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
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
		Transaction other = (Transaction) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
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
		return "Transaction [id=" + id + ", date=" + date + ", description="
				+ description + ", cents=" + cents + "]";
	}


	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);
		
		Attr attr = document.createAttribute("id");
		attr.setValue(id);
		me.setAttributeNode(attr);
		
		if (account != null) {
			attr = document.createAttribute("account");
			attr.setValue(account.getId());
			me.setAttributeNode(attr);
		}
			
		Element xmlDate = document.createElement("date");
		xmlDate.appendChild(document.createTextNode(XML_DATE_FORMAT.format(date)));
		me.appendChild(xmlDate);
		
		if (description != null) {
			Element xmlDesc = document.createElement("description");
			xmlDesc.appendChild(document.createTextNode(description));
			me.appendChild(xmlDesc);
		}
		
		if (address != null) {
			Element xmlAddress = document.createElement("address");
			xmlAddress.appendChild(document.createTextNode(address));
			me.appendChild(xmlAddress);
		}
		
		Element xmlCents = document.createElement("cents");
		xmlCents.appendChild(document.createTextNode("" + cents));
		me.appendChild(xmlCents);
		
		if (note != null) {
			Element xmlNote = document.createElement("note");
			xmlNote.appendChild(document.createTextNode(note));
			me.appendChild(xmlNote);
		}
		
		if (categoryDetector != null) {
			Element xmlDetector = document.createElement("category_detector");
			xmlDetector.appendChild(document.createTextNode(categoryDetector.getId()));
			me.appendChild(xmlDetector);
		}
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param element the XML element
	 * @param document the parent document
	 * @param transactions the parent list of transactions
	 * @return the object
	 * @throws ParseException on date parse error
	 */
	public static Transaction fromXMLElement(Element element,
			Document document, TransactionList transactions) throws ParseException {
		
		if (transactions == null) transactions = document.getTransactions();
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		String id = element.getAttribute("id");
		String s_account = element.hasAttribute("account") ? element.getAttribute("account") : null;
		String s_date = element.getElementsByTagName("date").item(0).getTextContent();
		String description = element.getElementsByTagName("description").getLength() > 0
				? element.getElementsByTagName("description").item(0).getTextContent() : null;
		String address = element.getElementsByTagName("address").getLength() > 0
				? element.getElementsByTagName("address").item(0).getTextContent() : null;
		String s_cents = element.getElementsByTagName("cents").item(0).getTextContent();
		String note = element.getElementsByTagName("note").getLength() > 0
				? element.getElementsByTagName("note").item(0).getTextContent() : null;
		String s_cd = element.getElementsByTagName("category_detector").getLength() > 0
				? element.getElementsByTagName("category_detector").item(0).getTextContent() : null;
		
		int cents = Integer.parseInt(s_cents);
		Account account = s_account != null ? document.getAccounts().get(s_account) : null;
		Date date = XML_DATE_FORMAT.parse(s_date);
		CategoryDetector cd = s_cd != null ? document.getCategories().detectors.get(s_cd) : null;
		
		Transaction transaction = new Transaction(account, id, date, description, address, cents);
		document.getCategories().detectCategories(transaction, transactions);
		if (cd != null) transaction.setCategoryDetector(cd);
		if (note != null) transaction.setNote(note);
		
		return transaction;
	}
}
