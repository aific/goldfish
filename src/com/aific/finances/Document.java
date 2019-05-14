package com.aific.finances;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aific.finances.util.Utils;


/**
 * A document 
 * 
 * @author Peter Macko
 */
public class Document {
	
	public static final String FILE_EXTENSION = "gff";
	public static final String XML_ELEMENT = "document";

	private File file;
	
	private Accounts accounts;
	private Categories categories;
	private TransactionList transactions;
	private Budgets budgets;
	
	
	/**
	 * Create a blank document
	 */
	public Document() {
		
		file = null;
		
		accounts = new Accounts();
		categories = Categories.fromBuiltin();
		transactions = new TransactionList();
		budgets = new Budgets();
	}
	
	
	/**
	 * Create a blank document from an XML element
	 * 
	 * @param element the XML element
	 * @throws ParseException 
	 */
	private Document(Element element) throws ParseException {
		
		file = null;
		
		if (!element.getNodeName().equals(XML_ELEMENT)) {
			throw new IllegalArgumentException();
		}
		
		
		// Accounts
		
		NodeList accountsList = element.getElementsByTagName(Accounts.XML_ELEMENT);
		if (accountsList.getLength() != 1) {
			throw new RuntimeException("The document must have one <" + Accounts.XML_ELEMENT + "> element");
		}
		
		if (accountsList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
		accounts = Accounts.fromXMLElement((Element) accountsList.item(0));
		
		
		// Categories and rules
		
		categories = Categories.fromBuiltin();
		
		NodeList categoriesList = element.getElementsByTagName(Categories.XML_ELEMENT);
		if (categoriesList.getLength() > 1) {
			throw new RuntimeException("The document must have one <" + Categories.XML_ELEMENT + "> element");
		}
		else if (categoriesList.getLength() == 1) {
			if (categoriesList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			categories.updateFromXMLElement((Element) categoriesList.item(0));
		}
		
		
		// Transactions
		
		NodeList transactionsList = element.getElementsByTagName(TransactionList.XML_ELEMENT);
		if (transactionsList.getLength() != 1) {
			throw new RuntimeException("The document must have one <" + TransactionList.XML_ELEMENT + "> element");
		}
		
		if (transactionsList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
		transactions = TransactionList.fromXMLElement((Element) transactionsList.item(0), this);
		
		
		// Budgets
		
		NodeList budgetsList = element.getElementsByTagName(Budgets.XML_ELEMENT);
		if (budgetsList.getLength() > 1) {
			throw new RuntimeException("The document must have no more than one <" + Budgets.XML_ELEMENT + "> element");
		}
		else if (budgetsList.getLength() == 1) {
			if (budgetsList.item(0).getNodeType() != Node.ELEMENT_NODE) throw new IllegalArgumentException();
			budgets = Budgets.fromXMLElement((Element) budgetsList.item(0), this);
		}
		else {
			budgets = new Budgets();
		}
	}
	
	
	/**
	 * Get the file
	 * 
	 * @return the file, or null if not set
	 */
	public File getFile() {
		return file;
	}
	
	
	/**
	 * Get the accounts
	 * 
	 * @return accounts
	 */
	public Accounts getAccounts() {
		return accounts;
	}
	
	
	/**
	 * Get the categories
	 * 
	 * @return categories
	 */
	public Categories getCategories() {
		return categories;
	}
	
	
	/**
	 * Get the transaction list
	 * 
	 * @return the transactions
	 */
	public TransactionList getTransactions() {
		return transactions;
	}
	
	
	/**
	 * Get the collection of budgets
	 * 
	 * @return the budgets
	 */
	public Budgets getBudgets() {
		return budgets;
	}

	
	/**
	 * Write the object to an XML
	 * 
	 * @param document the XML document
	 * @return the element (not yet added to the document)
	 */
	public Element toXMLElement(org.w3c.dom.Document document) {
		Element me = document.createElement(XML_ELEMENT);

		me.appendChild(accounts.toXMLElement(document));
		me.appendChild(transactions.toXMLElement(document));
		me.appendChild(categories.updatesToXMLElement(document));
		me.appendChild(budgets.toXMLElement(document));
		
		return me;
	}
	
	
	/**
	 * Read the object from an XML
	 * 
	 * @param element the XML element
	 * @return the object
	 * @throws ParseException 
	 */
	public static Document fromXMLElement(Element element) throws ParseException {
		return new Document(element);
	}

	
	/**
	 * Load a document from file
	 * 
	 * @param file the file
	 */
	public static Document fromFile(File file) {

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
		
			Document d = fromXMLElement(doc.getDocumentElement());
			
			d.file = file;
			return d;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Save the document to a file (and update the file field of the document)
	 * 
	 * @param file the file
	 */
	public void toFile(File file) {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			org.w3c.dom.Document d = docBuilder.newDocument();
			
			d.appendChild(toXMLElement(d));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(d);
			
			FileOutputStream out = new FileOutputStream(file);
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
			out.close();
			
			this.file = file;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Export the document to CSV
	 * 
	 * @param file the file
	 * @param predicate the predicate to filter the exported transactions, or null if none
	 */
	public void exportToCsv(File file, Predicate<Transaction> predicate) throws IOException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			
			out.write("Date,");
			out.write("Category,");
			out.write("\"Category - Details\",");
			out.write("Amount,");
			out.write("Description,");
			out.write("Account,");
			out.write("Note");
			out.newLine();
			
			ArrayList<Transaction> l = new ArrayList<>(transactions);
			Collections.sort(l, (a, b) -> {
				return b.getDate().compareTo(a.getDate());
			});
			
			for (Transaction t : l) {
				
				if (predicate != null) {
					if (!predicate.test(t)) continue;
				}
				
				out.write(dateFormat.format(t.getDate()));
				out.write(",");
				
				CategoryDetector detector = t.getCategoryDetector();
				if (detector != null && detector.getCategory() != null) {
					out.write(Utils.escapeCsv(detector.getCategory().getName()));
				}
				out.write(",");
				
				if (detector != null && ((detector.getVendor() != null && !detector.getVendor().isEmpty())
						|| (detector.getDescription() != null && !detector.getDescription().isEmpty()))) {
					String s = detector.getVendor() != null && !detector.getVendor().isEmpty() ? detector.getVendor() : "";
					if (detector.getDescription() != null && !detector.getDescription().isEmpty()) {
						if (!s.isEmpty()) s += " - ";
						s += detector.getDescription();
					}
					out.write(Utils.escapeCsv(s));
				}
				out.write(",");
				
				out.write(Utils.NUMBER_FORMAT.format(t.getCents() / 100));
				out.write(",");
				
				out.write(Utils.escapeCsv(t.getDescription()));
				out.write(",");
				
				out.write(Utils.escapeCsv(t.getAccount().getShortName()));
				out.write(",");
				
				out.write(Utils.escapeCsv(t.getNote()));
				out.write(",");
				
				out.newLine();
			}
		}
	}
}
