package com.aific.finances.plot;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;


/**
 * Chart data
 */
public class BasicChartData<X> extends AbstractMap<ChartSeries, Map<X, Double>> implements ChartData<X> {

	private Map<ChartSeries, Map<X, Double>> data;
	
	
	/**
	 * Create an instance of {@link BasicChartData}
	 */
	public BasicChartData()
	{
		data = new HashMap<ChartSeries, Map<X, Double>>();
	}
	
	
	/**
	 * Create an instance of {@link BasicChartData} by combining one or more data sets
	 */
	@SuppressWarnings("unchecked")
	public static <X> ChartData<X> combine(Collection<ChartData<X>> data)
	{
		BiFunction<Double, Double, Double> valueCombinator = (a, b) -> a.doubleValue() + b.doubleValue();
		BasicChartData<X> r = new BasicChartData<>();
		
		if (data == null) return r;
		
		for (ChartData<? extends X> d : data) {
			for (Map.Entry<ChartSeries, ?> e : d.entrySet()) {
				
				Map<X, Double> m = r.data.get(e.getKey());
				if (m == null) r.data.put(e.getKey(), m = new HashMap<>());
				
				for (Map.Entry<? extends X, Double> p : ((Map<? extends X, Double>) e.getValue()).entrySet()) {
					Double v = m.get(p.getKey());
					if (v == null) {
						v = p.getValue();
					}
					else {
						v = valueCombinator.apply(v, p.getValue());
					}
					m.put(p.getKey(), v);
				}
			}
		}
		
		return r;
	}
	
	
	/**
	 * Get the data
	 * 
	 * @return the map of chart series to X/Y values
	 */
	public Map<ChartSeries, Map<X, Double>> getData()
	{
		return data;
	}
	
	
	/**
	 * Clear
	 */
	@Override
	public synchronized void clear()
	{
		data.clear();
	}
	
	
	/**
	 * Add data for a particular series. Replace existing data if it already exists.
	 * 
	 * @param series the chart series
	 * @param data the data
	 * @return the previous value, or null if none
	 */
	@Override
	public Map<X, Double> put(ChartSeries series, Map<X, Double> data)
	{
		return this.data.put(series, data);
	}
	
	
	/**
	 * Add data for a multiple series at the time. Replace existing data if it already exists.
	 * 
	 * @param series the chart series
	 * @param data the data
	 */
	@Override
	public void putAll(Map<? extends ChartSeries, ? extends Map<X, Double>> data)
	{
		this.data.putAll(data);
	}
	
	
	/**
	 * Get data for a particular series.
	 * 
	 * @param series the chart series
	 * @return the data, or null if not there
	 */
	@Override
	public Map<X, Double> get(Object series)
	{
		return this.data.get(series);
	}


	/**
	 * Get all entries
	 * 
	 * @return all entries
	 */
	@Override
	public Set<Map.Entry<ChartSeries, Map<X, Double>>> entrySet()
	{
		return data.entrySet();
	}


	/**
	 * Get all keys
	 * 
	 * @return all keys
	 */
	@Override
	public Set<ChartSeries> keySet()
	{
		return data.keySet();
	}
	
	
	/**
	 * Get the maximum Y value
	 * 
	 * @return the maximum Y value or Double.NEGATIVE_INFINITY if none
	 */
	public double getMaxY()
	{
		double v = Double.NEGATIVE_INFINITY;
		for (Map<X, Double> m : data.values()) {
			for (Double y : m.values()) {
				v = Math.max(v, y.doubleValue());
			}
		}
		return v;
	}
	
	
	/**
	 * Get the minimum Y value
	 * 
	 * @return the minimum Y value or Double.POSITIVE_INFINITY if none
	 */
	public double getMinY()
	{
		double v = Double.POSITIVE_INFINITY;
		for (Map<X, Double> m : data.values()) {
			for (Double y : m.values()) {
				v = Math.min(v, y.doubleValue());
			}
		}
		return v;
	}
}
