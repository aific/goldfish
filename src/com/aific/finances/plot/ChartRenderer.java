package com.aific.finances.plot;

import java.awt.Graphics;
import java.util.List;

import com.aific.finances.util.DoubleRange;


/**
 * A chart renderer
 */
public interface ChartRenderer<X> {
	
	
	/**
	 * Get the data range for determining the bounds on the value axis
	 * 
	 * @param xValues the list of X values
	 * @param data the chart data
	 * @return the data range
	 */
	default public DoubleRange getValueRange(List<X> xValues, ChartData<X> data)
	{
		return data.getValueRange();
	}
	
	
	/**
	 * Paint the chart
	 * 
	 * @param g the graphics context
	 * @param coordinates the coordinates
	 * @param data the chart data
	 */
	public void paint(Graphics g, XYCoordinates<X> coordinates, ChartData<X> data);
}
