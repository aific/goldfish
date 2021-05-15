package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.aific.finances.Category;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.Transaction;
import com.aific.finances.TransactionList;
import com.aific.finances.TransactionListListener;
import com.aific.finances.plot.BarChartRenderer;
import com.aific.finances.plot.BasicChartSeries;
import com.aific.finances.plot.Chart;
import com.aific.finances.plot.ChartRenderer;
import com.aific.finances.plot.ChartSeries;
import com.aific.finances.plot.CollectionChartDataSource;
import com.aific.finances.plot.LineChartRenderer;
import com.aific.finances.plot.StackedBarChartRenderer;
import com.aific.finances.util.Month;
import com.aific.finances.util.TimePeriod;
import com.aific.finances.util.Week;


/**
 * A panel with a plot
 */
@SuppressWarnings("serial")
public class PlotPanel extends JPanel {

	private Document document;
	private Handler handler;

	private CollectionChartDataSource<Transaction, TimePeriod> dataSource;
	private CollectionChartDataSource<Transaction, TimePeriod> netDataSource;
	private CollectionChartDataSource<Transaction, Category> dataSourceByCategory;

	private JPanel mainConfigurationPanel;
	private JComboBox<PlotConents> plotContentsCombo;
	private JComboBox<PlotTimeUnit> plotTimeUnitCombo;
	private JComboBox<PlotType> plotTypeCombo;
	private JSlider plotFromSlider;
	private JSlider plotToSlider;
	
	private JSplitPane splitPaneWithSeries;
	private JPanel chartCardPanel;
	private CardLayout chartCardLayout;
	private Chart<TimePeriod> chart;
	private Chart<Category> chartByCategory;
	private ChartSeriesTable seriesTable;
	
	private ChartSeries netSeries = new BasicChartSeries("Net", Color.BLUE);
	private ChartSeries expensesSeries = new BasicChartSeries("Expenses", Color.BLACK);
	private ChartSeries incomeSeries = new BasicChartSeries("Income", Color.GREEN.darker());
	private ChartSeries otherSeries = new BasicChartSeries("Other", Color.BLACK);
	private ChartSeries externalSeries = new BasicChartSeries("External Transfers", Color.CYAN);
	
	private ChartRenderer<TimePeriod> lineChartRenderer = new LineChartRenderer<>();
	private ChartRenderer<TimePeriod> barChartRenderer = new BarChartRenderer<>();
	private ChartRenderer<TimePeriod> stackedBarChartRenderer = new StackedBarChartRenderer<>();
	private ChartRenderer<Category> stackedBarChartByCategoryRenderer = new StackedBarChartRenderer<>();
	
	
	/**
	 * Create a new instance of {@link PlotPanel}
	 */
	public PlotPanel()
	{
		setLayout(new BorderLayout());
		handler = new Handler();
		
		document = MainFrame.getInstance().getDocument();

		
		// The main configuration panel 
		
		mainConfigurationPanel = new JPanel(new GridBagLayout());
		add(mainConfigurationPanel, BorderLayout.NORTH);
		
		GridBagConstraints c = new GridBagConstraints();
		int y = 0;
		
		JLabel plotContentsLabel = new JLabel("Plot contents: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = y;
		mainConfigurationPanel.add(plotContentsLabel, c);

		plotContentsCombo = new JComboBox<>(PlotConents.values());
		plotContentsCombo.addActionListener(handler);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		mainConfigurationPanel.add(plotContentsCombo, c);
		
		y++;
		
		JLabel plotTimeUnitLabel = new JLabel("Time unit: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = y;
		mainConfigurationPanel.add(plotTimeUnitLabel, c);

		plotTimeUnitCombo = new JComboBox<>(PlotTimeUnit.values());
		plotTimeUnitCombo.addActionListener(handler);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		mainConfigurationPanel.add(plotTimeUnitCombo, c);
		
		y++;
		
		JLabel plotTypeLabel = new JLabel("Plot type: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = y;
		mainConfigurationPanel.add(plotTypeLabel, c);

		plotTypeCombo = new JComboBox<>(PlotType.values());
		plotTypeCombo.addActionListener(handler);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		mainConfigurationPanel.add(plotTypeCombo, c);
		
		y++;
		
		JLabel plotFromLabel = new JLabel("From: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = y;
		mainConfigurationPanel.add(plotFromLabel, c);

		plotFromSlider = new JSlider();
		plotFromSlider.setMinimum(0);
		plotFromSlider.setMaximum(100000);
		plotFromSlider.setValue(0);
		plotFromSlider.addChangeListener(handler);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		mainConfigurationPanel.add(plotFromSlider, c);
		
		y++;
		
		JLabel plotToLabel = new JLabel("To: ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = y;
		mainConfigurationPanel.add(plotToLabel, c);

		plotToSlider = new JSlider();
		plotToSlider.setMinimum(0);
		plotToSlider.setMaximum(100000);
		plotToSlider.setValue(100000);
		plotToSlider.addChangeListener(handler);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = y;
		mainConfigurationPanel.add(plotToSlider, c);
		
		y++;
		
		
		// The main chart
		
		dataSource = new CollectionChartDataSource<>(document.getTransactions(), null, null, null);
		
		netDataSource = new CollectionChartDataSource<>(document.getTransactions(), null, null, null);
		netDataSource.setValueFunction(t -> t.getCents() / 100.0);
		netDataSource.setSeriesFunction(t -> netSeries);
		netDataSource.setPointVisibilityPredicate(t -> {
			Category category = t.getCategory();
			return category == null || category.getType() != CategoryType.BALANCED;
		});
		
		chart = new Chart<>();
		chart.addDataSource(dataSource);
		
		
		// The chart that displays values by categories
		
		dataSourceByCategory = new CollectionChartDataSource<>(document.getTransactions(), null, null, null);
		dataSourceByCategory.setCategoryFunction(t -> t.getCategory() == null ? Category.NULL_CATEGORY : t.getCategory());
		
		Comparator<ChartSeries> categoriesComparator = new Comparator<ChartSeries>() {
			@Override
			public int compare(ChartSeries a, ChartSeries b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		};
		
		ArrayList<Category> categoriesAxis = new ArrayList<>();
		categoriesAxis.add(Category.NULL_CATEGORY);
		categoriesAxis.addAll(document.getCategories().getCategories().stream()
				.filter(x -> x.getType() == CategoryType.INCOME)
				.sorted(categoriesComparator)
				.collect(Collectors.toList()));
		categoriesAxis.addAll(document.getCategories().getCategories().stream()
				.filter(x -> x.getType() == CategoryType.EXPENSE)
				.sorted(categoriesComparator)
				.collect(Collectors.toList()));
		categoriesAxis.addAll(document.getCategories().getCategories().stream()
				.filter(x -> x.getType() == CategoryType.EXTERNAL)
				.sorted(categoriesComparator)
				.collect(Collectors.toList()));
		
		chartByCategory = new Chart<>();
		chartByCategory.addDataSource(dataSourceByCategory);
		chartByCategory.setSortedCategoryValues(categoriesAxis);
		
		
		// The chart cards panel
		
		chartCardLayout = new CardLayout();
		chartCardPanel = new JPanel(chartCardLayout);
		chartCardPanel.add(chart, "chart");
		chartCardPanel.add(chartByCategory, "chartByCategory");
		
		
		// The chart series panel
		
		seriesTable = new ChartSeriesTable(document);
		seriesTable.addAdditionalSeries(incomeSeries);
		seriesTable.addAdditionalSeries(expensesSeries);
		seriesTable.addAdditionalSeries(netSeries);
		seriesTable.addAdditionalSeries(otherSeries);
		seriesTable.addAdditionalSeries(externalSeries);
		
		seriesTable.setLegendType(LegendType.LINE);
		seriesTable.getSelectionModel().addListSelectionListener(handler);
		
		JScrollPane seriesTableScroll = new JScrollPane(seriesTable);
		
		
		// The split view with the chart series panel
		
		splitPaneWithSeries = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				chartCardPanel, seriesTableScroll);
		splitPaneWithSeries.setResizeWeight(1);
		add(splitPaneWithSeries, BorderLayout.CENTER);
		
		
		// Set up the handlers
		
		document.getTransactions().addTransactionListListener(handler);
		
		
		// Configure the display

		configureSeriesTable();
		configureChart();

		
		// Resize components when shown
		
		addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			
			@Override
			public void ancestorAdded(AncestorEvent event) {
				splitPaneWithSeries.setDividerLocation(getWidth()
						- seriesTable.getWidth() - splitPaneWithSeries.getDividerSize() - 10);
			}
		});
	}
	
	
	/**
	 * Prepare to dispose the panel
	 */
	public void prepareDispose()
	{
		seriesTable.prepareDispose();
		
		document.getTransactions().removeTransactionListListener(handler);
	}
	
	
	/**
	 * Configure the chart
	 */
	protected void configureChart()
	{
		// Set the filters based on the series table
		
		Set<ChartSeries> series = new HashSet<>(seriesTable.getSelectedItems());
		dataSource.setSeriesVisibilityPredicate(s -> {
			if (series.isEmpty()) return true;
			return series.contains(s);
		});
		dataSourceByCategory.setSeriesVisibilityPredicate(s -> {
			if (series.isEmpty()) return true;
			return series.contains(s);
		});
		netDataSource.setSeriesVisibilityPredicate(s -> {
			if (series.isEmpty()) return true;
			return series.contains(s);
		});
		
		
		// Configure the chart type
		
		switch ((PlotType) plotTypeCombo.getSelectedItem()) {
		
		case LINE:
			chart.getLastLayer().setRenderer(lineChartRenderer);
			seriesTable.setLegendType(LegendType.LINE);
			break;
		
		case BAR:
		default:
			switch ((PlotConents) plotContentsCombo.getSelectedItem()) {
			case SUMMARY:
				chart.getLastLayer().setRenderer(barChartRenderer);
				break;
			default:
				chart.getLastLayer().setRenderer(stackedBarChartRenderer);
			}
			seriesTable.setLegendType(LegendType.BAR);
			break;
		}
		
		chartByCategory.getLastLayer().setRenderer(stackedBarChartByCategoryRenderer);

		
		// Select the chart
		
		switch ((PlotConents) plotContentsCombo.getSelectedItem()) {
		case CATEGORIES:
			chartCardLayout.show(chartCardPanel, "chartByCategory");
			break;
		default:
			chartCardLayout.show(chartCardPanel, "chat");
			break;
		}
		
		
		// Set the X axis

		TransactionList transactions = document.getTransactions();
		Function<Date, TimePeriod> dateToTimePeriod = null;
		switch ((PlotTimeUnit) plotTimeUnitCombo.getSelectedItem()) {
		case MONTH:
			dateToTimePeriod = d -> new Month(d);
			break;
		case WEEK:
			dateToTimePeriod = d -> new Week(d);
			break;
		default:
			break;
		}

		TimePeriod xMin;
		TimePeriod xMax;
		if (transactions.isEmpty() || dateToTimePeriod == null) {
			xMin = xMax = new Month(new Date());
			chart.setCategoryValues(null);
			dataSource.setCategoryFunction(t -> t.getMonth());
			netDataSource.setCategoryFunction(t -> t.getMonth());
		}
		else {
			xMin = dateToTimePeriod.apply(transactions.get(0).getDate());
			xMax = dateToTimePeriod.apply(transactions.get(0).getDate());
			for (Transaction t : transactions) {
				TimePeriod p = dateToTimePeriod.apply(t.getDate());
				if (p.compareTo(xMin) < 0) xMin = p; 
				if (p.compareTo(xMax) > 0) xMax = p; 
			}
			
			int length = xMin.countUntil(xMax);
			plotFromSlider.setMinimum(0);
			plotFromSlider.setMaximum(length-1);
			plotToSlider.setMinimum(0);
			plotToSlider.setMaximum(length-1);
			
			xMax = xMin.add(Math.min(plotToSlider.getValue(), length));
			xMin = xMin.add(Math.min(plotFromSlider.getValue(), length));
			
			List<TimePeriod> l = new ArrayList<>();
			for (TimePeriod p = xMin; p.compareTo(xMax) <= 0; p = p.getNext()) {
				l.add(p);
			}
			chart.setCategoryValues(l);
			final Function<Date, TimePeriod> fn = dateToTimePeriod;
			dataSource.setCategoryFunction(t -> fn.apply(t.getDate()));
			netDataSource.setCategoryFunction(t -> fn.apply(t.getDate()));
		}
		
		final TimePeriod minPeriod = xMin;
		final TimePeriod maxPeriod = xMax;
		int numPeriods = xMin.countUntil(xMax);
		
		
		// Set the data functions and the data sources
		
		chart.removeDataSource(netDataSource);
		chart.setSortedSeriesValues();
		chartByCategory.setSortedSeriesValues();
		
		Function<Transaction, Double> valueFunction;
		Function<Transaction, ? extends ChartSeries> seriesFunction;
		Predicate<Transaction> pointVisibilityFunction;

		switch ((PlotConents) plotContentsCombo.getSelectedItem()) {
		case EXPENSES:
			valueFunction = t -> {
				double v = t.getCents() / 100.0;
				Category c = t.getCategory();
				if (c == null) return -v;
				if (c.getType() == CategoryType.EXPENSE) v = -v;
				return v;
			};
			seriesFunction = t -> {
				return t.getCategory() == null ? otherSeries : t.getCategory();
			};
			pointVisibilityFunction = t -> {
				Category c = t.getCategory();
				if (c == null) return t.getCents() < 0;
				return c.getType() == CategoryType.EXPENSE;
			};
			break;
		case INCOME:
			valueFunction = t -> t.getCents() / 100.0;
			seriesFunction = t -> {
				return t.getCategory() == null ? otherSeries : t.getCategory();
			};
			pointVisibilityFunction = t -> {
				Category c = t.getCategory();
				if (c == null) return t.getCents() > 0;
				return c.getType() == CategoryType.INCOME;
			};
			break;
		case SUMMARY:
			valueFunction = t -> {
				double v = t.getCents() / 100.0;
				Category c = t.getCategory();
				if (c == null) return Math.abs(v);
				if (c.getType() == CategoryType.BALANCED) return 0.0;
				if (c.getType() == CategoryType.EXPENSE ) v = -v;
				return v;
			};
			seriesFunction = t -> {
				Category c = t.getCategory();
				if (c != null) {
					switch (c.getType()) {
					case EXPENSE : return expensesSeries;
					case INCOME  : return incomeSeries;
					case BALANCED: return null;
					case EXTERNAL: return externalSeries;
					}
				}
				return t.getCents() > 0 ? incomeSeries : expensesSeries;
			};
			pointVisibilityFunction = t -> {
				Category c = t.getCategory();
				return c == null || c.getType() != CategoryType.BALANCED;
			};
			chart.addDataSource(netDataSource);
			chart.setSortedSeriesValues(incomeSeries, expensesSeries, externalSeries, netSeries);
			break;
		case CATEGORIES:
			valueFunction = t -> {
				double v = t.getCents() / 100.0;
				Category c = t.getCategory();
				if (c == null) return Math.abs(v);
				if (c.getType() == CategoryType.BALANCED) return 0.0;
				if (c.getType() == CategoryType.EXPENSE ) v = -v;
				if (c.getType() == CategoryType.EXTERNAL) v = -v;
				return v;
			};
			seriesFunction = t -> {
				return t.getCategory() == null ? otherSeries : t.getCategory();
			};
			pointVisibilityFunction = t -> {
				Category c = t.getCategory();
				return c == null || c.getType() != CategoryType.BALANCED;
			};
			break;
		case SAVINGS:
			valueFunction = t -> -t.getCents() / 100.0;
			seriesFunction = t -> {
				return t.getCategory() == null ? externalSeries : t.getCategory();
			};
			pointVisibilityFunction = t -> {
				Category c = t.getCategory();
				if (c == null) return false;
				return c.getType() == CategoryType.EXTERNAL;
			};
			break;
		default:
			valueFunction = null;
			seriesFunction = null;
			pointVisibilityFunction = null;
			break;
		}
		
		pointVisibilityFunction = pointVisibilityFunction.and(t -> {
			return minPeriod.containsUntil(maxPeriod, t.getDate());
		});
		
		dataSource.setValueFunction(valueFunction);
		dataSource.setSeriesFunction(seriesFunction);
		dataSource.setPointVisibilityPredicate(pointVisibilityFunction);
		
		dataSourceByCategory.setValueFunction(valueFunction.andThen(v -> v / Math.max(1, numPeriods)));
		dataSourceByCategory.setSeriesFunction(seriesFunction);
		dataSourceByCategory.setPointVisibilityPredicate(pointVisibilityFunction);
	}
	
	
	/**
	 * Configure the series table
	 */
	protected void configureSeriesTable()
	{
		switch ((PlotConents) plotContentsCombo.getSelectedItem()) {
		case EXPENSES:
			seriesTable.setFilter(c -> {
				if (c instanceof Category) {
					return ((Category) c).getType() == CategoryType.EXPENSE;
				}
				return c == otherSeries;
			});
			break;
		case INCOME:
			seriesTable.setFilter(c -> {
				if (c instanceof Category) {
					return ((Category) c).getType() == CategoryType.INCOME;
				}
				return c == otherSeries;
			});
			break;
		case SUMMARY:
			// TODO Sort appropriately
			seriesTable.setFilter(c -> {
				return c == incomeSeries || c == expensesSeries
						|| c == netSeries || c == externalSeries;
			});
			break;
		case CATEGORIES:
			// TODO Sort appropriately
			seriesTable.setFilter(c -> {
				if (c instanceof Category) {
					switch (((Category) c).getType()) {
					case INCOME:
					case EXPENSE:
					case EXTERNAL:
						return true;
					default:
						return false;
					}
				}
				return c == otherSeries;
			});
			break;
		case SAVINGS:
			seriesTable.setFilter(c -> {
				if (c instanceof Category) {
					return ((Category) c).getType() == CategoryType.EXTERNAL;
				}
				return c == externalSeries;
			});
			break;
		default:
			break;
		}
	}

		
	/**
	 * Plot contents
	 */
	private enum PlotConents
	{
		SUMMARY     ("Summary over Time"),
		CATEGORIES  ("Summary by Category"),
		EXPENSES    ("Expenses"),
		INCOME      ("Income"),
		SAVINGS     ("Savings");
		
		
		private String description;
		
		
		/**
		 * Create an instance of the class
		 */
		PlotConents(String description)
		{
			this.description = description;
		}
		
		
		/**
		 * Get the string version
		 * 
		 * @return the string
		 */
		public String toString()
		{
			return description;
		}
	}

	
	/**
	 * Plot time units
	 */
	private enum PlotTimeUnit
	{
		MONTH  ("Month"),
		WEEK   ("Week");
		
		
		private String description;
		
		
		/**
		 * Create an instance of the class
		 */
		PlotTimeUnit(String description)
		{
			this.description = description;
		}
		
		
		/**
		 * Get the string version
		 * 
		 * @return the string
		 */
		public String toString()
		{
			return description;
		}
	}

	
	/**
	 * Plot type
	 */
	private enum PlotType
	{
		BAR      ("Bar"),
		LINE     ("Line");
		
		
		private String description;
		
		
		/**
		 * Create an instance of the classchartCardPanel
		 */
		PlotType(String description)
		{
			this.description = description;
		}
		
		
		/**
		 * Get the string version
		 * 
		 * @return the string
		 */
		public String toString()
		{
			return description;
		}
	}
	
	
	/**
	 * An event handler
	 */
	private class Handler implements ActionListener, ListSelectionListener,
		TransactionListListener, ChangeListener
	{

		/**
		 * Handle an action event
		 * 
		 * @param e the event
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			seriesTable.clearSelection();
			configureSeriesTable();
			
			configureChart();
			repaint();
		}

		
		/**
		 * Handle a selection change in the selection components
		 * 
		 * @param e the event
		 */
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			configureChart();
			repaint();
		}

		
		/**
		 * Transaction(s) added
		 * 
		 * @param list the transaction list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void transactionsAdded(TransactionList list, int from, int to)
		{
			configureChart();
			repaint();
		}
		

		/**
		 * Transaction(s) removed
		 * 
		 * @param list the transaction list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void transactionsRemoved(TransactionList list, int from, int to)
		{
			configureChart();
			repaint();
		}

		
		/**
		 * Transaction(s) data changed
		 * 
		 * @param list the transaction list that triggered this event
		 */
		public void transactionsDataChanged(TransactionList list)
		{
			configureChart();
			repaint();
		}


		/**
		 * State changed
		 * 
		 * @param e the event
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			configureChart();
			repaint();
		}
	}
}
