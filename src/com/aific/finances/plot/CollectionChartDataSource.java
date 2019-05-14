package com.aific.finances.plot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aific.finances.util.SharedList;
import com.aific.finances.util.SharedListListener;


/**
 * Chart data source from a collection
 */
public class CollectionChartDataSource<T, X> implements ChartDataSource<X>, WithChartLayer<X> {
	
	private Collection<T> data;
	private ChartLayer<X> layer;
	
	private Function<T, Double               > valueFunction;
	private Function<T, ? extends X          > categoryFunction;
	private Function<T, ? extends ChartSeries> seriesFunction;
	
	private Predicate<T                      > pointVisibilityPredicate;
	private Predicate<ChartSeries            > seriesVisibilityPredicate;
	private BiFunction<Double, Double, Double> valueCombinator;
	
	private AtomicReference<ChartData<X>> chartData;	// cache of the generated data
	
	
	/**
	 * Create the data source from a collection
	 * 
	 * @param data the data source
	 * @param valueFunction the function to get the value of an element (the Y value)
	 * @param categoryFunction the function to get the category of an element (the X value)
	 * @param seriesFunction the function to extract the series from an element
	 */
	public CollectionChartDataSource(Collection<T> data,
			Function<T, Double> valueFunction,
			Function<T, ? extends X> categoryFunction,
			Function<T, ? extends ChartSeries> seriesFunction)
	{
		this.data = data;
		this.layer = null;
		
		this.valueFunction = valueFunction;
		this.categoryFunction = categoryFunction;
		this.seriesFunction = seriesFunction;
		
		this.pointVisibilityPredicate = (a) -> true;
		this.seriesVisibilityPredicate = (a) -> true;
		this.valueCombinator = (a, b) -> a.doubleValue() + b.doubleValue();
		
		this.chartData = new AtomicReference<ChartData<X>>(null);
		
		if (data instanceof SharedList<?>) {
			((SharedList<?>) data).addSharedListListener(new SharedListListener() {
				
				@Override
				public void sharedListElementsRemoved(SharedList<?> list, int from, int to) {
					invalidateCache();
				}
				
				@Override
				public void sharedListElementsAdded(SharedList<?> list, int from, int to) {
					invalidateCache();
				}
				
				@Override
				public void sharedListDataChanged(SharedList<?> list) {
					invalidateCache();
				}
			});
		}
	}

	
	/**
	 * Get the associated instance of {@link ChartLayer}
	 * 
	 * @return the instance of {@link ChartLayer}, or null if none
	 */
	public ChartLayer<X> getChartLayer()
	{
		return layer;
	}

	
	/**
	 * Set the instance of {@link ChartLayer}
	 * 
	 * @param layer the instance of {@link ChartLayer}, or null if none
	 */
	public void setChartLayer(ChartLayer<X> layer)
	{
		this.layer = layer;
	}

	
	/**
	 * Invalidate the cache of the generated chart data
	 */
	public void invalidateCache()
	{
		ChartData<X> previous = chartData.getAndSet(null);
		if (previous != null) {
			ChartLayer<X> l = getChartLayer();
			if (l !=  null) {
				Chart<X> chart = l.getChart();
				if (chart != null) chart.invalidate();
			}
		}
	}
	
	
	/**
	 * Set the value accessor
	 * 
	 * @param accessor the Y value accessor
	 */
	public void setValueFunction(Function<T, Double> accessor)
	{
		valueFunction = accessor;
		invalidateCache();
	}
	
	
	/**
	 * Set the category accessor
	 * 
	 * @param accessor the X value accessor
	 */
	public void setCategoryFunction(Function<T, ? extends X> accessor)
	{
		categoryFunction = accessor;
		invalidateCache();
	}
	
	
	/**
	 * Set the category accessor
	 * 
	 * @param accessor the series value accessor
	 */
	public void setSeriesFunction(Function<T, ? extends ChartSeries> accessor)
	{
		seriesFunction = accessor;
		invalidateCache();
	}
	
	
	/**
	 * Set the visibility predicate
	 * 
	 * @param predicate the visibility predicate
	 */
	public void setPointVisibilityPredicate(Predicate<T> predicate)
	{
		pointVisibilityPredicate = predicate;
		invalidateCache();
	}
	
	
	/**
	 * Set the visibility predicate
	 * 
	 * @param accessor the visibility value predicate
	 */
	public void setSeriesVisibilityPredicate(Predicate<ChartSeries> predicate)
	{
		seriesVisibilityPredicate = predicate;
		invalidateCache();
	}
	
	
	/**
	 * Set the value combinator
	 * 
	 * @param combinator the combinator
	 */
	public void setValueCombinator(BiFunction<Double, Double, Double> combinator)
	{
		valueCombinator = combinator;
		invalidateCache();
	}

	
	/**
	 * Build the chart data
	 * 
	 * @return the chart data
	 */
	private ChartData<X> generateChartData()
	{
		ChartData<X> chartData = new BasicChartData<>();
		
		if (categoryFunction == null || valueFunction == null || seriesFunction == null
				|| pointVisibilityPredicate == null || valueCombinator == null) return chartData;
		
		for (T d : data) {
			if (!pointVisibilityPredicate.test(d)) continue;
			
			X x = categoryFunction.apply(d);
			Double y = valueFunction.apply(d);
			ChartSeries series = seriesFunction.apply(d);
			
			if (!seriesVisibilityPredicate.test(series)) continue;
			
			Map<X, Double> m = chartData.get(series);
			if (m == null) {
				m = new HashMap<>();
				chartData.put(series, m);
			}
			
			Double prev = m.get(x);
			if (prev == null) {
				m.put(x, y);
			}
			else {
				m.put(x, valueCombinator.apply(prev, y));
			}
		}
		
		return chartData;
	}

	
	/**
	 * Get chart data
	 */
	public ChartData<X> getChartData()
	{
		ChartData<X> r = chartData.get();
		if (r != null) return r;
		
		r = generateChartData();
		chartData.set(r);
		return r;
	}
}
