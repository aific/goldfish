package com.aific.finances.plot;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import com.aific.finances.util.DoubleRange;
import com.aific.finances.util.Utils;


/**
 * The coordinates for the plot
 *
 * @param <X> the X axis data type
 */
public class XYCoordinates<X> {
	
	protected Chart<X> chart;
	private Graphics g;
	
	private List<ChartSeries> series;
	private List<X> xValues;
	private double dataMinY;
	private double dataMaxY;
	
	
	// Style
	
	private int minorTickMarkLength = 2;
	private int majorTickMarkLength = 6;
	
	private int topPlotMargin = 10;
	private int bottomPlotMargin = 10;
	private int leftPlotMargin = 10;
	private int rightPlotMargin = 20;
	
	private int verticalTickmarkMargin = 20;
	
	
	// Computed properties
	
	private TreeMap<X, Integer> xIndexes;
	
	private int width;
	private int height;
	private int xAxisOffset;
	private int yAxisOffset;
	
	private int plotWidth;
	private int plotHeight;
	private int columnWidth;
	
	private double rangeY;
	private int iMaxY;
	private int iMinY;
	private int unitY;
	private boolean twoSidedY;
	

	
	/**
	 * Initialize
	 * 
	 * @param g the graphics context
	 * @param chart the chart
	 * @param series the list of series
	 * @param xValues the list of X values
	 * @param data the chart data
	 */
	public XYCoordinates(Graphics g, Chart<X> chart,
			List<ChartSeries> series, List<X> xValues, double minY, double maxY)
	{
		this.chart = chart;
		this.g = g;
		
		this.series = series;
		this.xValues = xValues;
		this.dataMinY = minY;
		this.dataMaxY = maxY;
		
		FontMetrics fm = g.getFontMetrics();
		width = chart.getWidth();
		height = chart.getHeight();
		
		
		// Get the numerical indexes for the X axis

		if (xValues.isEmpty()) return;
		
		xIndexes = new TreeMap<>();
		for (X x : xValues) xIndexes.put(x, xIndexes.size());
		
		
		// Get the data range and adjust it to always display the axes
		
		if (minY > 0 && maxY > 0) minY = 0;
		if (minY < 0 && maxY < 0) maxY = 0;
		
		
		// Adjust the Y axis to finish with a nice number

		rangeY = maxY - minY;
		if (rangeY < 100) {
			rangeY = 100;
			maxY = minY + 100;
		}

		unitY = 1;
		double m = rangeY;
		while (m > 16) {
			m /= 10;
			unitY *= 10;
		}
		
		maxY = Math.ceil(maxY / unitY) * unitY;
		if (minY < 0) {
			minY = -Math.ceil(-minY / unitY) * unitY;
		}
		else {
			minY = Math.ceil(minY / unitY) * unitY;
		}
		
		iMaxY = (int) Math.round(Math.ceil(maxY / unitY));
		iMinY = (int) Math.round(Math.floor(minY / unitY));
		int iRange = iMaxY - iMinY;
		
		while (iRange > 10) {
			unitY *= 2;
			iMaxY = (int) Math.ceil(iMaxY / 2.0);
			iMinY = (int) Math.floor(iMinY / 2.0);
			iRange = iMaxY - iMinY;
		}
		
		while (iRange < 6) {
			unitY /= 2;
			iMaxY = (int) Math.ceil(iMaxY * 2.0);
			iMinY = (int) Math.floor(iMinY * 2.0);
			iRange = iMaxY - iMinY;
		}
		
		maxY = iMaxY *= unitY;
		minY = iMinY *= unitY;
		rangeY = maxY - minY;
		
		twoSidedY = iMinY < 0 && iMaxY > 0;
		
		
		// Calculate the plot height
		
		plotHeight = height - topPlotMargin - bottomPlotMargin
				- (twoSidedY ? 2 : 1) * verticalTickmarkMargin;

		if (height - topPlotMargin - bottomPlotMargin
				- (twoSidedY ? 2 : 1) * verticalTickmarkMargin
				- (int) (plotHeight / rangeY * maxY)
				< g.getFont().getSize() + fm.getDescent() + 4) {
			plotHeight -= g.getFont().getSize() + fm.getDescent() + 4;
		}
		
		
		// Calculate the position of the Y (vertical) axis 
		
		int yMaxLabelWidth = getMaximumYLabelWidth();
		yAxisOffset = leftPlotMargin + yMaxLabelWidth;
		
		
		// Calculate the plot width and the column width
		
		plotWidth = width - yAxisOffset - rightPlotMargin;
		columnWidth = plotWidth / xValues.size();
		
		
		// Calculate the position of the X (horizontal) axis
		
		/*xAxisOffset = topPlotMargin + (int) (plotHeight / rangeY * maxY)
				+ (iMaxY > 0 ? verticalTickmarkMargin : 0);*/
		xAxisOffset = translate(0, 0).y;
	}
	
	
	/**
	 * Get the chart
	 * 
	 * @return the chart
	 */
	public Chart<X> getChart()
	{
		return chart;
	}
	
	
	/**
	 * Get the ordered list of series values
	 * 
	 * @return the list of values
	 */
	public List<ChartSeries> getSeriesValues()
	{
		return series;
	}
	
	
	/**
	 * Get the ordered list of category (X) values
	 * 
	 * @return the list of category values
	 */
	public List<X> getCategoryValues()
	{
		return xValues;
	}
	
	
	/**
	 * Get the range of values
	 * 
	 * @return the data range
	 */
	public DoubleRange getValueRange()
	{
		return new DoubleRange(dataMinY, dataMaxY);
	}
	
	
	/**
	 * Translate a point into the coordinate system
	 * 
	 * @param x the X index
	 * @param y the Y value
	 * @return the point
	 */
	public Point translate(int x, double y)
	{
		int xp = yAxisOffset + columnWidth * x + columnWidth / 2;
		int yp = topPlotMargin + (iMaxY > 0 ? verticalTickmarkMargin : 0)
				+ (int) (plotHeight / rangeY * (iMaxY - y));
		return new Point(xp, yp);
	}
	
	
	/**
	 * Translate a point into the coordinate system
	 * 
	 * @param x the X value
	 * @param y the Y value
	 * @return the point
	 * @throws NoSuchElementException if not found
	 */
	public Point translate(X x, double y) throws NoSuchElementException
	{
		return translate(getIndexOf(x), y);
	}
	
	
	/**
	 * Translate a point into the coordinate system
	 * 
	 * @param x the X value
	 * @param y the Y value
	 * @return the point
	 * @throws NoSuchElementException if not found
	 */
	public Point translate(X x, Number y) throws NoSuchElementException
	{
		return translate(getIndexOf(x), y.doubleValue());
	}
	
	
	/**
	 * Get the index of the X value
	 * 
	 * @param x the X value
	 * @return the index value
	 * @throws NoSuchElementException if not found
	 */
	public int getIndexOf(X x) throws NoSuchElementException
	{
		Integer n = xIndexes.get(x);
		if (n == null) throw new NoSuchElementException();
		return n.intValue();
	}
	
	
	/**
	 * Get the width of a column for a single category
	 * 
	 * @return the width in pixels
	 */
	public int getCategoryAreaWidth()
	{
		return columnWidth;
	}
	
	
	/**
	 * Get the Y coordinate of the category axis X
	 * 
	 * @return the Y coordinate
	 */
	public int getCategoryAxisY()
	{
		return xAxisOffset;
	}
	
	
	/**
	 * Compute the maximum width of a Y label
	 * 
	 * @return the maximum width
	 */
	private int getMaximumYLabelWidth()
	{
		FontMetrics fm = g.getFontMetrics();
		
		int yMaxLabelWidth = 0;
		for (int v = iMinY; v <= iMaxY; v += unitY) {
			String s = Utils.AMOUNT_FORMAT_WHOLE.format(v);
			Rectangle2D r = fm.getStringBounds(s, g);
			yMaxLabelWidth = Math.max(yMaxLabelWidth, (int) Math.ceil(r.getWidth()));
		}
		
		String s = Utils.AMOUNT_FORMAT_WHOLE.format(-8_000_000);
		Rectangle2D r = fm.getStringBounds(s, g);
		yMaxLabelWidth = Math.max(yMaxLabelWidth, (int) Math.ceil(r.getWidth()));
		
		return yMaxLabelWidth;
	}
	
	
	/**
	 * Paint X axis values
	 */
	private void paintXAxisValues()
	{
		if (xValues.isEmpty()) return;
		
		int startX = yAxisOffset + columnWidth / 2;	// the first X coordinate
		int incX = columnWidth;						// the increment for the X coordinate
		int axisY = xAxisOffset;					// the Y coordinate of the axis
		
		FontMetrics fm = g.getFontMetrics();
		int fh = g.getFont().getSize();
		
		
		// Get the bounds
		
		Rectangle2D[] r = new Rectangle2D[xValues.size()];
		String[] s = new String[xValues.size()];
		
		{
			int x = startX;
			int i = 0;
			
			for (X value : xValues) {
				
				s[i] = "" + value;
				r[i] = fm.getStringBounds(s[i], g);
				r[i].setRect(x - (int)(r[i].getWidth() / 2),
						axisY + fh + 3, r[i].getWidth(), r[i].getHeight());
				
				x += incX;
				i++;
			}
		}
		
		
		// Make sure that there is no overlap with the last element
		
		int minSpace = 10;
		
		Rectangle2D last = r[r.length-1];
		for (int i = r.length-2; i >= 0; i--) {
			if (r[i] == null) continue;
			if (r[i].getMaxX() + minSpace > last.getX()) {
				r[i] = null;
			}
			else {
				break;
			}
		}
		
		
		// Make sure that there is no overlap in the rest of the sequence
		
		last = r[0];
		for (int i = 1; i < r.length-1; i++) {
			if (last == null) break;
			if (r[i] == null) continue;
			if (last.getMaxX() + minSpace > r[i].getX()) {
				r[i] = null;
			}
			else {
				last = r[i];
			}
		}
		
		
		// Paint selectively

		for (int i = 0; i < r.length; i++) {
			if (r[i] != null && s[i] != null) {
				g.drawString(s[i], (int) r[i].getX(), (int) r[i].getY());
				int x = startX + i * incX;
				int y = axisY - majorTickMarkLength / 2;
				g.drawLine(x, y, x, y + majorTickMarkLength);
			}
		}
	}
	
	
	/**
	 * Paint X axis tick marks
	 */
	private void paintXAxisMinorTickMarks()
	{
		int startX = yAxisOffset + columnWidth / 2;	// the first X coordinate
		int incX = columnWidth;						// the increment for the X coordinate
		int axisY = xAxisOffset;					// the Y coordinate of the axis
		
		for (int i = 0; i < xValues.size(); i++) {
			int x = startX + i * incX;
			int y = axisY - minorTickMarkLength / 2;
			g.drawLine(x, y, x, y + minorTickMarkLength);
		}
	}


	/**
	 * Paint the axes
	 */
	public void paintAxes()
	{
		// X Axis
		
		g.setColor(Color.BLACK);
		g.drawLine(yAxisOffset, xAxisOffset, width-1-rightPlotMargin, xAxisOffset);
		
		
		// Y Axis
		
		g.setColor(Color.BLACK);
		g.drawLine(yAxisOffset, topPlotMargin, yAxisOffset,
				iMinY < 0 ? height-1-bottomPlotMargin : xAxisOffset);
		
		
		// X axis labels and values
		
		FontMetrics fm = g.getFontMetrics();
		int fh = g.getFont().getSize();
		
		g.setColor(Color.BLACK);
		paintXAxisMinorTickMarks();
		paintXAxisValues();
		
		
		// Y axis labels and values
		
		for (int v = iMinY; v <= iMaxY; v += unitY) {
			
			int yp = topPlotMargin + (iMaxY > 0 ? verticalTickmarkMargin : 0)
					+ (int) (plotHeight / rangeY * (iMaxY - v));
			String s = Utils.AMOUNT_FORMAT_WHOLE.format(v);
			
			int cur_y = yp + (int)(fh / 2 - fm.getDescent()) + 1;
			Rectangle2D r = fm.getStringBounds(s, g);
			
			g.drawString(s, yAxisOffset - (int)(r.getWidth()) - 5, cur_y);
			g.drawLine(yAxisOffset - majorTickMarkLength/2, yp,
					yAxisOffset + majorTickMarkLength/2, yp);
		}
	}
}
