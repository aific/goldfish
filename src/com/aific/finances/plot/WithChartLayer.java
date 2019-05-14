package com.aific.finances.plot;


/**
 * An interface for objects that store a reference to a {@link ChartLayer}
 */
public interface WithChartLayer<X> {

	/**
	 * Get the associated instance of {@link ChartLayer}
	 * 
	 * @return the instance of {@link ChartLayer}, or null if none
	 */
	public ChartLayer<X> getChartLayer();

	/**
	 * Set the instance of {@link ChartLayer}
	 * 
	 * @param layer the instance of {@link ChartLayer}, or null if none
	 */
	public void setChartLayer(ChartLayer<X> layer);
}
