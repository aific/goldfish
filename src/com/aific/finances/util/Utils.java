package com.aific.finances.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * Miscellaneous utilities
 * 
 * @author Peter Macko
 */
public class Utils {

	public static final boolean IS_MACOS = System.getProperty("os.name").startsWith("Mac");
	public static final boolean IS_LINUX = System.getProperty("os.name").startsWith("Linux");
	
	public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.00");
	public static final DecimalFormat NUMBER_FORMAT_WITH_SIGN = new DecimalFormat("+0.00;-0.00");
	public static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,###,###,###,###,##0.00");
	public static final DecimalFormat AMOUNT_FORMAT_WHOLE = new DecimalFormat("#,###,###,###,###,##0");
	public static final DecimalFormat AMOUNT_FORMAT_WITH_SIGN
		= new DecimalFormat("+#,###,###,###,###,##0.00;-#,###,###,###,###,##0.00");
	
	public static boolean useAlternatingTableRowColors = true;
	public static final Color TABLE_ALTERNATE_ROWS_COLOR
		= getColorInBetween((new JTable()).getBackground(), Color.GRAY, 0.1);

	
	/**
	 * Compute a color in between the two given colors
	 * 
	 * @param color1 the first color
	 * @param color2 the second color
	 * @param weight where in between (0 = color1, 1 = color2)
	 * @return the color in between color1 and color2
	 */
	public static Color getColorInBetween(Color color1, Color color2, double weight) {
		
		int r1 = color1.getRed();
		int g1 = color1.getGreen();
		int b1 = color1.getBlue();
		
		int r2 = color2.getRed();
		int g2 = color2.getGreen();
		int b2 = color2.getBlue();
		
		int r3 = (int) Math.round(r1 + weight * (r2 - r1));
		int g3 = (int) Math.round(g1 + weight * (g2 - g1));
		int b3 = (int) Math.round(b1 + weight * (b2 - b1));
		
		return new Color(r3, g3, b3);
	}


	/**
	 * Center the window
	 * 
	 * @param frame the window frame to center
	 */
	public static void centerWindow(Window frame) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getWidth()) / 2, (d.height - frame.getHeight()) / 2);
	}

	
	/**
	 * Overwrite confirmation
	 * 
	 * @param parent the parent component
	 * @param file the file to ask about
	 * @return true if the user wishes to overwrite the file
	 */
	public static boolean shouldOverwrite(Component parent, File file) {
		return JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite " + file.getName() + "?", "Warning",
											 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}
	
	
	/**
	 * Check whether the file exists, and if so, ask the user for a confirmation
	 * 
	 * @param parent the parent component
	 * @param file the file to check
	 * @return true if the file does not exist, or if the user wishes to overwrite it if it does
	 */
	public static boolean checkOverwrite(Component parent, File file) {
		if (!file.exists()) return true;
		return shouldOverwrite(parent, file);
	}


	/**
	 * Get the extension of a file.
	 * The code is based on http://java.sun.com/docs/books/tutorial/uiswing/components/examples/Utils.java
	 *
	 * @param f the file
	 * @return the file extension
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if ((i > 0) && (i < s.length() - 1)) {
			ext = s.substring(i + 1);
		}
		return ext;
	}


	/**
	 * Get the extension of a file.
	 * The code is based on http://java.sun.com/docs/books/tutorial/uiswing/components/examples/Utils.java
	 *
	 * @param f the file name
	 * @return the file extension
	 */
	public static String getExtension(String f) {
		String ext = null;
		String s = f;
		int i = s.lastIndexOf('.');
		
		if ((i > 0) && (i < s.length() - 1)) {
			ext = s.substring(i + 1);
		}
		return ext;
	}
	
	
	/**
	 * Draw a centered string
	 * 
	 * @param g the graphics context
	 * @param s the string
	 * @param x the middle X coordinate
	 * @param y the middle Y coordinate
	 */
	public static void drawCenteredString(Graphics g, String s, int x, int y) {
		
		FontMetrics fm = g.getFontMetrics();
		int fh = g.getFont().getSize();
		
		int cur_y = y + (int)(fh / 2 - fm.getDescent()) + 1; 
		Rectangle2D r = fm.getStringBounds(s, g);
		g.drawString(s, x - (int)(r.getWidth() / 2), cur_y);
	}
	
	
	/**
	 * Draw a vertical string
	 * 
	 * @param g the graphics context
	 * @param s the string
	 * @param x the middle X coordinate
	 * @param y the middle Y coordinate
	 */
	public static void drawVerticalString(Graphics g, String s, int x, int y) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		FontMetrics fm = g.getFontMetrics();
		int fh = g.getFont().getSize();
		int dh = (int)(fh / 2 - fm.getDescent()) + 1; 
		
		AffineTransform oldTransform = g2.getTransform();
		g2.rotate(-Math.PI/2, x + dh, y);
		g2.drawString(s, x + dh, y);
		g2.setTransform(oldTransform);
	}
	
	
	/**
	 * Escape and quote a CSV string
	 * 
	 * @param str the string
	 * @return the escaped and quoted string
	 */
	public static String escapeCsv(String str)
	{
		if (str.isEmpty()) return "";
		return "\"" + str.replace("\"", "\"\"") + "\"";
	}
}
