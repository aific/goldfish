package com.aific.finances.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * An element in a SGML file.
 * 
 * @author Peter Macko
 */
public class SgmlElement {
	
	private String tag;
	private String text;
	private ArrayList<SgmlElement> children;
	private Map<String, SgmlElement> map;
	

	/**
	 * Create an empty element
	 * 
	 * @param tag the element tag
	 */
	public SgmlElement(String tag) {
		this.tag = tag;
		this.text = "";
		this.children = new ArrayList<SgmlElement>();
		this.map = new HashMap<String, SgmlElement>();
	}


	/**
	 * Get the tag
	 * 
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}


	/**
	 * Get the text
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}


	/**
	 * Get the children
	 * 
	 * @return the children
	 */
	public ArrayList<SgmlElement> getChildren() {
		return children;
	}
	
	
	/**
	 * Follow a path and get the element.
	 * 
	 * @param path the tags
	 * @return the child element (first if there are more than one) or null if not
	 */
	public SgmlElement get(String... path) {
		
		SgmlElement e = this;
		for (String t : path) {
			e = e.map.get(t);
			if (e == null) return null;
		}
		
		return e;
	}
	
	
	/**
	 * Follow a path and get the text associated with the last element.
	 * 
	 * @param path the tags
	 * @return the text, or null if none
	 */
	public String getText(String... path) {
		
		SgmlElement e = get(path);
		return e == null ? null : e.text;
	}
	
	
	/**
	 * Parser helper: Extract the tag.
	 * 
	 * @param str the content to parse
	 * @param p the start position of the tag
	 * @param closing if this is a closing tag
	 * @return the tag name
	 * @throws ParseException on parse error
	 */
	private static String extractTag(String str, int p, boolean closing)
		throws ParseException {
		
		int e = str.indexOf('>', p);
		if (e < 0) {
			throw new ParseException("No '>' for the tag", p);
		}
		
		if (closing && (p >= str.length() || str.charAt(p + 1) != '/')) {
			throw new ParseException("Expected '/'", p + 1);
		}
		
		String tag = str.substring(p + (closing ? 2 : 1), e).trim();
		
		if (tag.startsWith("/")) {
			throw new ParseException("Unexpected '/' within a tag", p + 1);
		}
		
		int x = tag.indexOf('<');
		if (x >= 0) {
			throw new ParseException("Unexpected '<' within a tag", p + 1 + x);
		}
		
		x = tag.indexOf(' ');
		if (x >= 0) {
			throw new ParseException("Unexpected ' ' within a tag", p + 1 + x);
		}
		
		return tag;
	}
	
	
	/**
	 * Parse
	 * 
	 * @param str the content to parse
	 * @param start the start position
	 * @return the parsed element
	 * @throws ParseException on parse error
	 */
	public static SgmlElement parse(String str, int start)
		throws ParseException {
		
		if (str == null) {
			throw new NullPointerException("str is null");
		}
		
		if (start < 0 || start >= str.length()) {
			throw new IllegalArgumentException("Invalid start");
		}
		
		int p = start;
		int length = str.length();
		
		SgmlElement root = null;
		LinkedList<SgmlElement> stack = new LinkedList<SgmlElement>();
		
		
		while (true) {
			
			
			// Skip whitespace to reach the next piece of content
			
			while (p < length && Character.isWhitespace(str.charAt(p))) p++;
			
			if (p >= length) {
				if (root == null) {
					throw new ParseException("Did not find the opening tag", p);
				}
				if (!stack.isEmpty()) {
					throw new ParseException("Did not find a closing tag", p);
				}
				return root;
			}

			
			// Depending on whether this is a tag

			if (str.charAt(p) == '<') {
				
				if (p + 1 == length) {
					throw new ParseException("Unexpected end of input", p + 1);
				}
				
				if (str.charAt(p + 1) != '/') {
					
					// Close all text tags
					
					while (!stack.isEmpty()) {
						if (stack.peek().getText().isEmpty()) {
							break;
						}
						else {
							stack.pop();
						}
					}
					
					
					// If this is an opening tag, add it to the stack
					
					String tag = extractTag(str, p, false /* closing */);
					SgmlElement element = new SgmlElement(tag);
					if (root == null) {
						root = element;
					}
					else {
						if (stack.isEmpty()) {
							throw new ParseException("Unexpected second root tag", p);
						}
					}
					if (!stack.isEmpty()) {
						stack.peek().children.add(element);
						stack.peek().map.putIfAbsent(element.getTag(), element);
					}
					stack.push(element);
					
					p = str.indexOf('>', p) + 1;
				}
				else {
					
					// If this is a closing tag, then figure out what to close
					
					String tag = extractTag(str, p, true /* closing */);
					boolean found = false;
					
					while (!stack.isEmpty()) {
						if (stack.pop().getTag().equals(tag)) {
							found = true;
							break;
						}
					}
					if (!found) {
						throw new ParseException("Cannot find the corresponding opening tag", p);
					}
					
					p = str.indexOf('>', p) + 1;
				}

			}
			else {
				
				if (root == null) {
					throw new ParseException("Expected '<'", p);
				}
				
				
				// Start going through the context
				
				StringBuilder b = new StringBuilder();
				boolean ignoring = false;
				for ( ; p < length; p++) {
					char c = str.charAt(p);
					if (c == '<') break;
					
					if (c == '>') {
						throw new ParseException("Unexpected '>'", p);
					}
					
					if (c == '\n' || c == '\r') {
						ignoring = true;
					}
					
					if (Character.isWhitespace(c)) {
						if (!ignoring) b.append(c); 
					}
					else {
						if (ignoring) {
							ignoring = false;
							b.append(' ');
						}
						b.append(c);
					}
				}
				
				String text = b.toString();
				if (!text.isEmpty()) {
					SgmlElement inner = stack.peek();
					if (!inner.text.isEmpty()) {
						// Should this be an error?
					}
					inner.text += text;
					inner.text = inner.text.trim();
				}
			}
		}
	}
	
	
	@Override
	public String toString() {
		return "SgmlElement [tag=" + tag + ", text=" + text + ", children=" + children + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SgmlElement))
			return false;
		SgmlElement other = (SgmlElement) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
