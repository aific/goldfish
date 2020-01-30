package com.aific.finances.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import com.aific.finances.Account;
import com.aific.finances.Document;
import com.aific.finances.Transaction;
import com.aific.finances.util.SgmlElement;


/**
 * An OFX / QFX reader.
 * 
 * @author Peter Macko
 */
public class OfxReader implements TransactionHistoryReader {


	/**
	 * Create an instance of class OfxReader
	 */
	public OfxReader() {
	}


	/**
	 * Read all transactions
	 * 
	 * @param document the document
	 * @param account the corresponding account
	 * @param file the input file
	 * @return the collection of all transactions in the file
	 * @throws IOException on I/O error
	 * @throws ParseException on parse error
	 */
	public Collection<Transaction> readTransactions(Document document, Account account, File file)
			throws IOException, ParseException {
		
		HashMap<String, String> header = new HashMap<String, String>();
		StringBuilder contentBuilder = new StringBuilder();
		
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			
	        String l;
	        boolean inContent = false;
	        while ((l = in.readLine()) != null) {
	        	
	        	if (!inContent) {
		        	if (l.isBlank()) {
		        		inContent = true;
		        	}
		        	else {
		        		int separator = l.indexOf(':');
		        		if (separator < 0) {
		        			throw new ParseException("A header line without ':'", 0 /* TODO */);
		        		}
		        		String k = l.substring(0, separator).trim();
		        		String v = l.substring(separator + 1).trim();
		        		header.put(k, v);
		        	}
	        	}
	        	else {
	        		contentBuilder.append(l).append("\n");
	        	}
	        }
		}
		
		SgmlElement content = SgmlElement.parse(contentBuilder.toString(), 0);
        
		System.out.println(header);
		System.out.println(content);
        
        return new ArrayList<Transaction>();
	}
}
