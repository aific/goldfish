package com.aific.finances.plot;


/**
 * A data source for a chart
 */
public interface ChartDataSource<X> {

	
	/**
	 * Get the chart data
	 */
	public ChartData<X> getChartData();
}
