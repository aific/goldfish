package com.aific.finances.plot;

import java.awt.Color;


/**
 * A series in the chart
 */
public interface ChartSeries {

	/**
	 * Get the name
	 * 
	 * @return the name
	 */
	public String getName();


	/**
	 * Get the color
	 * 
	 * @return the color
	 */
	public Color getColor();
}
