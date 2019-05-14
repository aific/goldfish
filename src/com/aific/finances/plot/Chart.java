package com.aific.finances.plot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import com.aific.finances.util.DoubleRange;


/**
 * A chart
 */
@SuppressWarnings("serial")
public class Chart<X> extends JComponent {
	
	private List<ChartLayer<X>> layers;
	private ChartLayer<X> lastLayer;
	private Comparator<? super ChartSeries> seriesComparator;
	private Comparator<? super X> categoryComparator;
	
	private List<X> categoryValues;
	private List<ChartSeries> series;

	
	/**
	 * Create a new instance of {@link Chart}
	 */
	public Chart()
	{
		layers = new ArrayList<>();
		lastLayer = new ChartLayer<>(this);
		layers.add(lastLayer);
		
		setSeriesComparator(null);		
		setCategoryComparator(null);		
		categoryValues = null;
		series = null;

		setMinimumSize  (new Dimension(320, 240));
		setPreferredSize(new Dimension(320, 240));
	}
	
	
	/**
	 * Set the comparator for the categories (X values)
	 * 
	 * @param comparator the comparator
	 */
	public void setSeriesComparator(Comparator<? super ChartSeries> comparator)
	{
		if (comparator != null) {
			this.seriesComparator = comparator;
		}
		else {
			this.seriesComparator = new Comparator<ChartSeries>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public int compare(ChartSeries a, ChartSeries b) {
					
					if (a == null && b == null) return 0;
					else if (a == null) return -1;
					else if (b == null) return 1;
					
					if (a instanceof Comparable && b instanceof Comparable) {
						try {
							return ((Comparable) a).compareTo(b);
						}
						catch (ClassCastException e) {
							return a.toString().compareTo(b.toString());
						}
					}
					else {
						return a.getName().compareTo(b.getName());
					}
				}
			};
		}
	}
	
	
	/**
	 * Set the comparator for the categories (X values)
	 * 
	 * @param comparator the comparator
	 */
	public void setCategoryComparator(Comparator<? super X> comparator)
	{
		if (comparator != null) {
			this.categoryComparator = comparator;
		}
		else {
			this.categoryComparator = new Comparator<X>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public int compare(X a, X b) {
					
					if (a == null && b == null) return 0;
					else if (a == null) return -1;
					else if (b == null) return 1;
					
					if (a instanceof Comparable && b instanceof Comparable) {
						try {
							return ((Comparable) a).compareTo(b);
						}
						catch (ClassCastException e) {
							return a.toString().compareTo(b.toString());
						}
					}
					else {
						return a.toString().compareTo(b.toString());
					}
				}
			};
		}
	}
	
	
	/**
	 * Get the last layer
	 * 
	 * @return the last layer
	 */
	public ChartLayer<X> getLastLayer()
	{
		return lastLayer;
	}
	
	
	/**
	 * Add a data source to the last layer
	 * 
	 * @param source the data source
	 */
	public void addDataSource(ChartDataSource<X> source)
	{
		// TODO Make this work with multiple layers
		lastLayer.addDataSource(source);
	}
	
	
	/**
	 * Remove a data source from the last layer
	 * 
	 * @param source the data source
	 * @return true if the collection of data sources contained this element
	 */
	public boolean removeDataSource(ChartDataSource<X> source)
	{
		// TODO Make this work with multiple layers
		return lastLayer.removeDataSource(source);
	}
	
	
	/**
	 * Explicitly set the series values
	 * 
	 * @param xValues the unsorted collection of the series values, or null to auto-generate
	 */
	public void setSeriesValues(Collection<? extends ChartSeries> values)
	{
		if (values == null) {
			series = null;
			return;
		}
		
		List<ChartSeries> l = new ArrayList<>();
		l.addAll(values);
		Collections.sort(l, seriesComparator);
		series = l;
	}
	
	
	/**
	 * Explicitly set the series values
	 * 
	 * @param xValues the sorted list of series values, or null to auto-generate
	 */
	public void setSortedSeriesValues(List<? extends ChartSeries> values)
	{
		if (values == null) {
			series = null;
			return;
		}
		
		List<ChartSeries> l = new ArrayList<>();
		l.addAll(values);
		series = l;
	}
	
	
	/**
	 * Explicitly set the series values
	 * 
	 * @param xValues the sorted array of series values, or null to auto-generate
	 */
	public void setSortedSeriesValues(ChartSeries... values)
	{
		if (values.length == 0) {
			series = null;
			return;
		}
		
		List<ChartSeries> l = new ArrayList<>();
		for (ChartSeries v : values) l.add(v);
		series = l;
	}
	
	
	/**
	 * Explicitly set the X values
	 * 
	 * @param xValues the unsorted collection of X values, or null to auto-generate
	 */
	public void setCategoryValues(Collection<X> values)
	{
		if (values == null) {
			categoryValues = null;
			return;
		}
		
		List<X> l = new ArrayList<>();
		l.addAll(values);
		Collections.sort(l, categoryComparator);
		categoryValues = l;
	}
	
	
	/**
	 * Explicitly set the X values
	 * 
	 * @param xValues the sorted list of X values, or null to auto-generate
	 */
	public void setSortedCategoryValues(List<X> values)
	{
		if (values == null) {
			categoryValues = null;
			return;
		}
		
		List<X> l = new ArrayList<>();
		l.addAll(values);
		categoryValues = l;
	}
	
	
	/**
	 * Paint the component
	 * 
	 * @param g the graphics context
	 */
	@Override
	public void paint(Graphics g)
	{
		// Get the data
	
		@SuppressWarnings("unchecked")
		ChartLayer<X>[] layers = this.layers.toArray(new ChartLayer[0]);
		@SuppressWarnings("unchecked")
		ChartData<X>[] data = new ChartData[layers.length];
		for (int i = 0; i < layers.length; i++) data[i] = layers[i].getData();

		
		// Get the category values
		
		List<X> x = categoryValues;
		if (x == null) {
			HashSet<X> set = new HashSet<>();
			for (ChartLayer<X> layer : layers) {
				set.addAll(layer.getCategoryValues());
			}
			x = new ArrayList<>();
			x.addAll(set);
			Collections.sort(x, categoryComparator);
		}
		
		
		// Get the list of series
		
		List<ChartSeries> series = this.series;
		if (series == null) {
			Set<ChartSeries> set = new HashSet<>();
			for (int i = 0; i < data.length; i++) {
				for (ChartSeries s : data[i].keySet()) {
					set.add(s);
				}
			}
			series = new ArrayList<>();
			series.addAll(set);
			Collections.sort(series, seriesComparator);
		}
		
		
		// Get the min and max Y value
		
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < data.length; i++) {
			DoubleRange r = layers[i].getRenderer().getValueRange(x, data[i]);
			minY = Math.min(minY, r.getMin());
			maxY = Math.max(maxY, r.getMax());
		}
		
		
		// Clear
		
		g.clearRect(0, 0, getWidth(), getHeight());
		
		
		// Draw
		
		if (!x.isEmpty()) {
			
			// Draw the layers

			XYCoordinates<X> coordinates = new XYCoordinates<>(g, this, series, x, minY, maxY);
			
			for (int i = 0; i < layers.length; i++) {
				layers[i].getRenderer().paint(g, coordinates, data[i]);
			}
			
			
			// Axes
			
			coordinates.paintAxes();
		}
	}
}
