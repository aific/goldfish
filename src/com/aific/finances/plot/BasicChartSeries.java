package com.aific.finances.plot;

import java.awt.Color;


/**
 * A series in the chart
 */
public class BasicChartSeries implements ChartSeries {
	
	private String name;
	private Color color;
	
	
	/**
	 * Create a basic chart series
	 * 
	 * @param name the name
	 * @param color the color
	 */
	public BasicChartSeries(String name, Color color)
	{
		this.name = name;
		this.color = color;
	}
	

	/**
	 * Get the name
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * Get the color
	 * 
	 * @return the color
	 */
	public Color getColor()
	{
		return color;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BasicChartSeries other = (BasicChartSeries) obj;
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}
