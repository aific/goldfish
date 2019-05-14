package com.aific.finances.plot;

import java.util.Map;

import com.aific.finances.util.DoubleRange;


/**
 * Chart data
 */
public interface ChartData<X> extends Map<ChartSeries, Map<X, Double>>, ChartDataSource<X> {

	
	/**
	 * Get the maximum Y value
	 * 
	 * @return the maximum Y value or Double.NEGATIVE_INFINITY if none
	 */
	public double getMaxY();
	
	
	/**
	 * Get the minimum Y value
	 * 
	 * @return the minimum Y value or Double.POSITIVE_INFINITY if none
	 */
	public double getMinY();
	
	
	/**
	 * Get the range of values
	 * 
	 * @return the data range
	 */
	default public DoubleRange getValueRange()
	{
		return new DoubleRange(getMinY(), getMaxY());
	}

	
	/**
	 * Get chart data
	 */
	default public ChartData<X> getChartData() { return this; }	
}
