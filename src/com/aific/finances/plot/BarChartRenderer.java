package com.aific.finances.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import java.util.Map;


/**
 * A basic bar chart renderer
 */
public class BarChartRenderer<X> implements ChartRenderer<X> {
	
	private int margin = 2;
	private int spacing = 0;

	
	/**
	 * Paint the chart
	 * 
	 * @param g the graphics context
	 * @param coordinates the coordinates
	 * @param data the chart data
	 */
	public void paint(Graphics g, XYCoordinates<X> coordinates, ChartData<X> data)
	{
		List<X> xValues = coordinates.getCategoryValues();
		List<ChartSeries> series = coordinates.getSeriesValues();
		
		if (xValues.isEmpty() || series.isEmpty()) return;
		
		
		// Determine the display parameters
		
		int totalWidth = coordinates.getCategoryAreaWidth();
		int currentMargin = margin;
		int currentSpacing = spacing;
		int n = series.size();
		
		int width;	// of the bar
		while (true) {
			width = (totalWidth - 2 * currentMargin + currentSpacing) / n - currentSpacing;
			if (width <= 0) {
				if (currentSpacing > 0) {
					currentSpacing--;
				}
				else if (currentMargin > 1) {
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

				Point p = coordinates.translate(x, y);
				int left = p.x - totalWidth/2 + currentMargin + i * (width + currentSpacing);
				int top = p.y;
				int height = coordinates.getCategoryAxisY() - p.y;
				if (height < 0) {
					top += height;
					height = -height;
				}
				g.fillRect(left, top, width, height);
			}
		}
	}
}
