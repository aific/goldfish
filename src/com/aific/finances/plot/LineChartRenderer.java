package com.aific.finances.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * A basic line chart renderer
 */
public class LineChartRenderer<X> implements ChartRenderer<X> {

	
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
		if (xValues.isEmpty()) return;
		
		
		// Series
		
		for (Entry<ChartSeries, Map<X, Double>> dataSeries : data.entrySet()) {
			
			if (dataSeries.getKey() == null)
				g.setColor(Color.BLACK);
			else
				g.setColor(dataSeries.getKey().getColor());
			
			Point lastPoint = null; 
			
			for (X x : xValues) {
				Double y = dataSeries.getValue().get(x);
				if (y == null) y = new Double(0);
				
				Point p = coordinates.translate(x, y);
				
				g.fillOval(p.x-2, p.y-2, 5, 5);
				
				if (lastPoint != null) {
					g.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
				}
				
				lastPoint = p;
			}
		}
	}
}
