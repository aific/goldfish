package com.aific.finances.plot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A layer in the chart
 */
public class ChartLayer<X> {
	
	private Chart<X> chart;
	
	private List<ChartDataSource<X>> dataSources;
	private ChartRenderer<X> renderer;

	
	/**
	 * Create a new instance of {@link ChartLayer}
	 * 
	 * @param chart the chart
	 */
	public ChartLayer(Chart<X> chart)
	{
		this.chart = chart;
		
		dataSources = new ArrayList<>();
		renderer = new BarChartRenderer<>(); 
	}
	
	
	/**
	 * Get the associated chart
	 * 
	 * @return the chart
	 */
	public Chart<X> getChart()
	{
		return chart;
	}
	
	
	/**
	 * Add a data source
	 * 
	 * @param source the data source
	 */
	@SuppressWarnings("unchecked")
	public void addDataSource(ChartDataSource<X> source)
	{
		if (source instanceof WithChartLayer) {
			if (((WithChartLayer<X>) source).getChartLayer() != null
					&& ((WithChartLayer<X>) source).getChartLayer() != this) {
				throw new IllegalStateException("The data source is already added to a different chart layer");
				
			}
			((WithChartLayer<X>) source).setChartLayer(this);
		}
		dataSources.add(source);
	}
	
	
	/**
	 * Remove a data source
	 * 
	 * @param source the data source
	 * @return true if the collection of data sources contained this element
	 */
	@SuppressWarnings("unchecked")
	public boolean removeDataSource(ChartDataSource<X> source)
	{
		if (dataSources.remove(source)) {
			if (source instanceof WithChartLayer) {
				((WithChartLayer<X>) source).setChartLayer(null);
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Get the list of data sources
	 * 
	 * @return the list of data sources
	 */
	public List<ChartDataSource<X>> getDataSources()
	{
		return dataSources;
	}
	
	
	/**
	 * Collect and return the data from the sources
	 * 
	 * @return the chart data
	 */
	public ChartData<X> getData()
	{
		List<ChartData<X>> data = new ArrayList<>();
		for (ChartDataSource<X> source : dataSources) {
			data.add(source.getChartData());
		}
		
		return BasicChartData.combine(data);
	}
	
	
	/**
	 * Get the set of category values
	 * 
	 * @return the set of category values
	 */
	public Set<X> getCategoryValues()
	{
		HashSet<X> set = new HashSet<>();
		
		for (Map<X, Double> l : getData().values()) {
			for (X v : l.keySet()) {
				set.add(v);
			}
		}
		
		return set;
	}
	
	
	/**
	 * Set the renderer
	 * 
	 * @param renderer the renderer
	 */
	public void setRenderer(ChartRenderer<X> renderer)
	{
		this.renderer = renderer;
	}
	
	
	/**
	 * Get the renderer for the layer
	 * 
	 * @return the renderer
	 */
	public ChartRenderer<X> getRenderer()
	{
		return renderer;
	}
}
