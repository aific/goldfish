package com.aific.finances.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aific.finances.util.DoubleRange;
import com.aific.finances.util.Utils;


/**
 * A stacked bar chart renderer
 */
public class StackedBarChartRenderer<X> implements ChartRenderer<X> {
	
	private int margin = 2;
	
	
	/**
	 * Get the data range for determining the bounds on the value axis
	 * 
	 * @param xValues the list of X values
	 * @param data the chart data
	 * @return the data range
	 */
	@Override
	public DoubleRange getValueRange(List<X> xValues, ChartData<X> data)
	{
		double totalMin = 0;
		double totalMax = 0;
		
		for (X x : xValues) {
			
			double min = 0;
			double max = 0;
			for (Entry<ChartSeries, Map<X, Double>> dataSeries : data.entrySet()) {
				for (Entry<X, Double> e : dataSeries.getValue().entrySet()) {
					if (e.getKey().equals(x)) {
						double v = e.getValue();
						if (v < 0) min += v;
						if (v > 0) max += v;
					}
				}
			}
			
			if (min < totalMin) totalMin = min;
			if (max > totalMax) totalMax = max;
		}
		
		return new DoubleRange(totalMin, totalMax);
	}

	
	/**
	 * Paint the chart
	 * 
	 * @param g the graphics context
	 * @param coordinates the coordinates
	 * @param data the chart data
	 */
	@Override
	public void paint(Graphics g, XYCoordinates<X> coordinates, ChartData<X> data)
	{
		List<X> xValues = coordinates.getCategoryValues();
		List<ChartSeries> series = coordinates.getSeriesValues();
		
		if (xValues.isEmpty() || series.isEmpty()) return;
		
		
		// Determine the display parameters
		
		int totalWidth = coordinates.getCategoryAreaWidth();
		int currentMargin = margin;
		
		int width;	// of the bar
		while (true) {
			width = totalWidth - 2 * currentMargin;
			if (width <= 0) {
				if (currentMargin > 1) {
					currentMargin--;
				}
				else {
					// Cannot satisfy the constraints
					return;
				}
			}
			else {
				break;
			}
		}
		
		for (X x : xValues) {
			
			double lower = 0;
			double upper = 0;
			int count = 0;
			ChartSeries firstSeries = null;
			
			for (int i = 0; i < series.size(); i++) {
				ChartSeries s = series.get(i);
				Map<X, Double> d = data.get(s);
				if (d == null || d.isEmpty()) continue;
				
				Double y = d.get(x);
				if (y == null) y = new Double(0);
				
				if (s == null) {
					g.setColor(Color.BLACK);
				}
				else {
					g.setColor(s.getColor());
				}

				Point p1, p2;
				if (y.doubleValue() < 0) {
					p1 = coordinates.translate(x, lower);
					p2 = coordinates.translate(x, lower + y);
					lower += y;
				}
				else if (y.doubleValue() > 0) {
					p1 = coordinates.translate(x, upper + y);
					p2 = coordinates.translate(x, upper);
					upper += y;
				}
				else {
					continue;
				}
				
				count++;
				if (firstSeries == null) firstSeries = s;

				int left = p1.x - totalWidth/2 + currentMargin;
				int top = p1.y;
				int height = p2.y - p1.y;
				if (height == 0) continue;
				
				if (height < 0) {
					top += height;
					height = -height;
				}
				
				g.fillRect(left, top, width, height);
			}
			
			
			// The value label(s)
			
			if (width >= 8) {
				
				Font f = g.getFont();
				try {
					g.setFont(f.deriveFont((float) Math.min(width-2, f.getSize())));
					
					if (count == 1) {
						g.setColor(Utils.getColorInBetween(firstSeries.getColor(), Color.BLACK, 0.50f));
					}
					else {
						g.setColor(Color.BLACK);
					}
					
					// TODO ensure that there is a good-enough margin to fit the label
					
					if (upper > 0) {
						Point p = coordinates.translate(x, upper);
						Utils.drawVerticalString(g, " " + Utils.AMOUNT_FORMAT.format(upper), p.x, p.y);
					}
					if (lower < 0) {
						Point p = coordinates.translate(x, lower);
						String s = Utils.AMOUNT_FORMAT.format(lower) + " ";
						Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
						Utils.drawVerticalString(g, s, p.x, p.y + (int) r.getWidth());
					}
				}
				finally {
					g.setFont(f);
				}
			}
		}
	}
}
