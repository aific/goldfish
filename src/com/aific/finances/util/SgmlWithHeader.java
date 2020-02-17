package com.aific.finances.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * A SGML document with a key/value header (e.g. an OCX file).
 * 
 * @author Peter Macko
 */
public class SgmlWithHeader {
	
	private Map<String, String> header;
	private SgmlElement sgml;


	/**
	 * Create an empty document.
	 */
	private SgmlWithHeader() {
	}
	
	
	/**
	 * Get the header.
	 * 
	 * @return the header
	 */
	public Map<String, String> getHeader() {
		return header;
	}
	
	
	/**
	 * Get the SGML.
	 * 
	 * @return the SGML contents
	 */
	public SgmlElement getSgml() {
		return sgml;
	}
	
	
	/**
	 * Load and parse a file.
	 * 
	 * @param file the file
	 * @return the document
	 * @throws IOException on I/O error
	 * @throws ParseException on parse error
	 */
	public static SgmlWithHeader loadFromFile(File file)
		throws IOException, ParseException {
		
		StringBuilder contentBuilder = new StringBuilder();
		HashMap<String, String> header = new HashMap<String, String>();
		
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
		        			throw new ParseException("A header line without ':'", 0);
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
		
		SgmlWithHeader document = new SgmlWithHeader();
		document.header = Collections.unmodifiableMap(header);
		document.sgml = SgmlElement.parse(contentBuilder.toString(), 0);
		
		return document;
	}
}
