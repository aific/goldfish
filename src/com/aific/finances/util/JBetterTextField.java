package com.aific.finances.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextField;
import javax.swing.text.Document;


/**
 * A better version of JTextField
 * 
 * @author Peter Macko
 */
public class JBetterTextField extends JTextField {

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 4047803876818931301L;
	
	
	/**
	 * The placeholder text
	 */
	private String placeholder;

	
	/**
	 * Create an empty text field
	 */
	public JBetterTextField()
	{
	}

	
	/**
	 * Create a new text field
	 * 
	 * @param doc the document
	 * @param text the initial text
	 * @param columns the preferred number of columns
	 */
	public JBetterTextField(
			final Document doc,
			final String text,
			final int columns)
	{
		super(doc, text, columns);
	}

	
	/**
	 * Create a new text field
	 * 
	 * @param columns the preferred number of columns
	 */
	public JBetterTextField(final int columns)
	{
		super(columns);
	}

	
	/**
	 * Create a new text field
	 * 
	 * @param text the initial text
	 */
	public JBetterTextField(final String text)
	{
		super(text);
	}

	
	/**
	 * Create a new text field
	 * 
	 * @param text the initial text
	 * @param columns the preferred number of columns
	 */
	public JBetterTextField(final String text, final int columns)
	{
		super(text, columns);
	}

	
	/**
	 * Get the placeholder text
	 * 
	 * @return the placeholder text
	 */
	public String getPlaceholder()
	{
		return placeholder;
	}

	
	/**
	 * Set the placeholder text
	 * 
	 * @param placeholder the placeholder text
	 */
	public void setPlaceholder(final String placeholder)
	{
		this.placeholder = placeholder;
	}

	
	/**
	 * Paint the component
	 * 
	 * @param g the graphics context
	 */
	@Override
	protected void paintComponent(final Graphics g)
	{
		// https://stackoverflow.com/questions/16213836/java-swing-jtextfield-set-placeholder
		
		super.paintComponent(g);

		if (placeholder.length() == 0 || getText().length() > 0) {
			return;
		}

		final Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(getDisabledTextColor());
		g2D.drawString(placeholder, getInsets().left, g.getFontMetrics()
				.getMaxAscent() + getInsets().top);
	}
}
