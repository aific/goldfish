package com.aific.finances.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.RowSorter;
import javax.swing.SortOrder;

import com.aific.finances.Categories;
import com.aific.finances.CategoriesListener;
import com.aific.finances.Document;
import com.aific.finances.plot.ChartSeries;
import com.aific.finances.util.Accessor;
import com.aific.finances.util.JBetterTable;


/**
 * A table of chart series
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class ChartSeriesTable extends JBetterTable<ChartSeries> {
	
	private TableColumn<ChartSeries> seriesColumn;
	private Handler handler;
	
	private List<ChartSeries> additionalSeries;
	private Document document;
	
	private SeriesNameRenderer seriesNameRenderer;
	
	
	/**
	 * Create an instance of class TransactionTable
	 * 
	 * @param document the document
	 */
	public ChartSeriesTable(Document document) {

		addColumn(seriesColumn = new TableColumn<ChartSeries>("Series", ChartSeries.class, false,
				new Accessor<ChartSeries, ChartSeries>() {
					@Override
					public ChartSeries get(ChartSeries object) {
						return object;
					}
				}));

		
		// Set the cell editors and renderers
		
		seriesNameRenderer = new SeriesNameRenderer(null);
		getColumnModel().getColumn(seriesColumn.getIndex()).setCellRenderer(seriesNameRenderer);
		

		// Set the column widths and other common properties
		
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));

		sorter.setSortKeys(sortKeys);
		sorter.setComparator(0, new Comparator<ChartSeries>() {

			@Override
			public int compare(ChartSeries a, ChartSeries b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		
		// Handlers
		
		handler = new Handler();

		
		// Initialize the data
		
		additionalSeries = new ArrayList<>();
		this.document = null;
		setDocument(document);
	}
	
	
	/**
	 * Add additional series
	 * 
	 * @param series the series to add
	 */
	public void addAdditionalSeries(ChartSeries series)
	{
		additionalSeries.add(series);
		getModifiableList().add(series);
		adjustColumns();
	}
	
	
	/**
	 * Prepare to dispose the panel
	 */
	public void prepareDispose()
	{
		this.document.getCategories().removeCategoriesListener(handler);
	}
	
	
	/**
	 * Set the document
	 * 
	 * @param document the new document
	 */
	public void setDocument(Document document)
	{
		if (this.document != null) {
			this.document.getCategories().removeCategoriesListener(handler);
		}
		
		this.document = document;
		
		getModifiableList().clear();
		
		if (this.document != null) {
			getModifiableList().addAll(document.getCategories());
			this.document.getCategories().addCategoriesListener(handler);
		}
		
		for (ChartSeries series : additionalSeries) {
			getModifiableList().add(series);
		}
		
		adjustColumns();
	}
	
	
	/**
	 * Set the type of the legend
	 * 
	 * @param type the type of the legend
	 */
	public void setLegendType(LegendType type)
	{
		seriesNameRenderer.setLegendType(type);
	}


	/**
	 * The handler
	 */
	private class Handler implements CategoriesListener {

		/**
		 * One or more categories added
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesAdded(Categories list, int from, int to)
		{
			data.clear();
			data.addAll(list);
			data.addAll(additionalSeries);
			
			model.fireTableDataChanged();
		}
		

		/**
		 * One or more categories removed
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesRemoved(Categories list, int from, int to)
		{
			data.clear();
			data.addAll(list);
			data.addAll(additionalSeries);
			
			model.fireTableDataChanged();
		}
		

		/**
		 * Category data changed
		 * 
		 * @param list the category list that triggered this event
		 */
		public void categoriesDataChanged(Categories list)
		{
			data.clear();
			data.addAll(list);
			data.addAll(additionalSeries);
			
			model.fireTableDataChanged();
		}
	}
}
