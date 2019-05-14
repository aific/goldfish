package com.aific.finances.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.aific.finances.Categories;
import com.aific.finances.CategoriesListener;
import com.aific.finances.Category;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.Transaction;
import com.aific.finances.TransactionList;
import com.aific.finances.TransactionListListener;
import com.aific.finances.util.Utils;


/**
 * The list of months
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class MonthList extends JList {

	private DefaultListModel<MonthElement> model;
	private HashSet<MonthElement> elementSet;
	
	private Handler handler;
	private MouseListener mouseHandler;
	private ScrollPaneMouseHandler scrollPaneMouseHandler;

	private Document document;
	private TransactionList transactions;
	private CellRenderer renderer;
	
	private List<Category> categories;
	private HashMap<Category, Integer> maxAmountByCategory;
	private int maxPositiveAbsAmountInCategory;
	private int maxNegativeAbsAmountInCategory;
	
	private JScrollPane scroll;


	/**
	 * Create an instance of class MonthList
	 * 
	 * @param document the document
	 */
	public MonthList(Document document) {
		super(new DefaultListModel<MonthElement>()); 
		
		this.model = (DefaultListModel<MonthElement>) getModel();
		this.model.addElement(new SummaryMonthElement());
		
		elementSet = new HashSet<MonthElement>();
		scroll = null;
		
		
		// Get the categories to display (default to all)
		
		this.categories = new ArrayList<Category>();
		this.categories.addAll(document.getCategories().getCategories());
		this.categories.add(null /* uncategorized */);
		Collections.sort(categories, new Category.ByName());
		
		this.maxAmountByCategory = new HashMap<Category, Integer>();
		this.maxPositiveAbsAmountInCategory = 0;
		this.maxNegativeAbsAmountInCategory = 0;
		
		
		// Set the renderer
		
		renderer = new CellRenderer();
		setCellRenderer(renderer);
		
		
		// Set the layout
		
		setLayoutOrientation(HORIZONTAL_WRAP);
		setVisibleRowCount(1);
		
		Dimension size = getMinimumSize();
		size.height = 40;
		setMinimumSize(size);
		
		
		// Handlers
		
		handler = new Handler();
		addComponentListener(handler);
		
		mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);

		
		// Set the contents
		
		setDocument(document);
		
		
		// Set the callback for the containing scroll pane
		
		scrollPaneMouseHandler = new ScrollPaneMouseHandler();
		addAncestorListener(new AncestorListener() {
			
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
			
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			
			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				// http://stackoverflow.com/questions/22040701/access-the-jscrollpane-in-which-the-jtable-is-contained
				Container parent = getParent();
				if (parent == null || !(parent instanceof JViewport)) return;
				Container enclosing = parent.getParent();
				if (enclosing == null || !(enclosing instanceof JScrollPane)) return;
				enclosing.addMouseListener(scrollPaneMouseHandler);
			}
		});
	}
	
	
	/**
	 * Get the document
	 * 
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}
	
	
	/**
	 * Get the list of transactions
	 * 
	 * @return the list of transactions
	 */
	public TransactionList getTransactions() {
		return transactions;
	}


	/**
	 * Set the document
	 * 
	 * @param document the document
	 */
	public void setDocument(Document document) {
		
		if (this.document != null) {
			if (transactions != null) {
				transactions.removeTransactionListListener(handler);
			}
			this.document.getCategories().removeCategoriesListener(handler);
		}

		this.document = document;
		this.document.getCategories().addCategoriesListener(handler);
		
		categories.clear();
		categories.addAll(document.getCategories().getCategories());
		categories.add(null /* uncategorized */);
		Collections.sort(categories, new Category.ByName());

		transactions = document.getTransactions();
		transactions.addTransactionListListener(handler);

		model.clear();
		elementSet.clear();
		model.addElement(new SummaryMonthElement());
		handler.transactionsDataChanged(transactions);

		if (transactions.size() > 0) {
			handler.transactionsAdded(transactions, 0, transactions.size() - 1);
		}
		
		if (scroll != null) {
			// TODO We would like to scroll to the end to show the latest data, but the following does not work,
			// since it appears here too early before the scroll bar had a chance to adjust
			//scroll.getHorizontalScrollBar().setValue(scroll.getHorizontalScrollBar().getMaximum());
		}
	}
	
	
	/**
	 * Set the scroll pane
	 */
	public void setJScrollPane(JScrollPane scroll) {
		this.scroll = scroll;
	}

	
	/**
	 * Set the cell height
	 * 
	 * @param height the new cell height
	 */
	public void setCellHeight(int height) {

		int w = Math.max(height, renderer.getCell().getMinimumSize().width);
		int h = Math.max(height, renderer.getCell().getMinimumSize().height);
		
		int oldHeight = getFixedCellHeight();
		if (oldHeight < 0) oldHeight = getSize().height;
		double r = (double) oldHeight / (double) h;
		
		double scrollPos = 0;
		JScrollBar scrollBar = null;
		double fixedPosition = 0.5;
		
		if (scroll != null) {
			scrollBar = scroll.getHorizontalScrollBar();
			
			int selected = getSelectedIndex();
			if (selected >= 0) {
				int prevWidth = renderer.getCell().getPreferredSize().width;
				Point p = indexToLocation(selected);
				if (p.x <= scrollBar.getValue()) {
					fixedPosition = 0;
				}
				else if (p.x + prevWidth >= scrollBar.getValue() + scrollBar.getVisibleAmount()) {
					fixedPosition = 1;
				}
				else {
					int middleX = p.x + prevWidth / 2 - scrollBar.getValue();
					fixedPosition = middleX / (double) scrollBar.getVisibleAmount();
				}
			}
			else {
				if (scrollBar.getValue() == 0) {
					//fixedPosition = 0;
				}
			}
			
			if (scrollBar.getMaximum() > 0) {
				scrollPos = (scrollBar.getValue() + fixedPosition * scrollBar.getVisibleAmount())
						/ (r * scrollBar.getMaximum());
			}
		}
		
		renderer.getCell().setPreferredSize(new Dimension(w, h));
		setFixedCellHeight(h);
		
		if (scrollBar != null) {
			scrollBar.setValue((int) Math.round(scrollPos * scrollBar.getMaximum())
					- (int) Math.round(fixedPosition * scrollBar.getVisibleAmount()));
		}
		
		repaint();
	}
	
	
	/**
	 * Does this track the viewport height?
	 * 
	 *  @return true if yes
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}

	
	/**
	 * The handler
	 */
	private class Handler implements TransactionListListener, CategoriesListener, ComponentListener {

		/**
		 * Transactions added
		 * 
		 * @param list the transaction list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void transactionsAdded(TransactionList list, int from, int to) {
			
			for (int i = from; i <= to; i++) {
				Transaction t = list.get(i);
				
				
				// Find the corresponding month
				
				MonthElement x = null;
				for (MonthElement e : elementSet) {
					if (e.accepts(t)) {
						x = e;
						break;
					}
				}
				
				
				// Create and add the month if it does not exist, making sure that there
				// is a continuous series of months with no gaps
				
				if (x == null) {
					Calendar c = Calendar.getInstance();
					c.setTime(t.getDate());
					x = new MonthElement(c.get(Calendar.MONTH), c.get(Calendar.YEAR));
					elementSet.add(x);
					
					if (model.size() <= 1) {
						model.insertElementAt(x, 0);
					}
					else {
						MonthElement first = model.get(0);
						MonthElement last = model.get(model.size()-2);
						if (x.compareTo(first) < 0) {
							int m = first.getMonth();
							int y = first.getYear();
							while (true) {
								if (--m < 0) {
									m = 11;
									y--;
								}
								if (x.getMonth() == m && x.getYear() == y) break;
								MonthElement z = new MonthElement(m, y);
								elementSet.add(z);
								model.insertElementAt(z, 0);
							}
							model.insertElementAt(x, 0);
						}
						else if (x.compareTo(last) > 0) {
							int m = last.getMonth();
							int y = last.getYear();
							while (true) {
								if (++m > 11) {
									m = 0;
									y++;
								}
								if (x.getMonth() == m && x.getYear() == y) break;
								MonthElement z = new MonthElement(m, y);
								elementSet.add(z);
								model.insertElementAt(z, model.size()-1);
							}
							model.insertElementAt(x, model.size()-1);
						}
					}
				}
				
				
				// Add the transaction
				
				synchronized (MonthList.this) {
					x.add(t);
				}
			}
		}


		/**
		 * Transactions removed
		 * 
		 * @param list the transaction list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void transactionsRemoved(TransactionList list, int from, int to) {
			transactionsDataChanged(list);
		}
		

		/**
		 * Transaction(s) data changed
		 * 
		 * @param list the transaction list that triggered this event
		 */
		public void transactionsDataChanged(TransactionList list) {
			
			maxAmountByCategory.clear();
			maxPositiveAbsAmountInCategory = 0;
			maxNegativeAbsAmountInCategory = 0;

			for (int i = 0; i < model.getSize(); i++) {
				model.get(i).recomputeStats();
			}
			
			repaint();
		}
		

		/**
		 * One or more categories added
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesAdded(Categories list, int from, int to) {
			MonthList.this.repaint();
		}
		

		/**
		 * One or more categories removed
		 * 
		 * @param list the category list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void categoriesRemoved(Categories list, int from, int to) {
			MonthList.this.repaint();
		}
		

		/**
		 * Category data changed
		 * 
		 * @param list the category list that triggered this event
		 */
		public void categoriesDataChanged(Categories list) {
			MonthList.this.repaint();
		}


		/**
		 * Component resized
		 * 
		 * @param e the event
		 */
		@Override
		public void componentResized(ComponentEvent e) {
		}


		/**
		 * Component moved
		 * 
		 * @param e the event
		 */
		@Override
		public void componentMoved(ComponentEvent e) {
		}


		/**
		 * Component shown
		 * 
		 * @param e the event
		 */
		@Override
		public void componentShown(ComponentEvent e) {
		}


		/**
		 * Component hidden
		 * 
		 * @param e the event
		 */
		@Override
		public void componentHidden(ComponentEvent e) {
		}
	}
	
	
	/**
	 * The element
	 */
	public class MonthElement implements Comparable<MonthElement> {
		
		private int month;
		private int year;
		
		private Date first;
		private Date last;
		
		private List<Transaction> transactions;
		private HashSet<Transaction> transactionSet;
		private HashMap<Category, Integer> amountByCategory;
		
		
		/**
		 * Create an instance of Element
		 * 
		 * @param month the month
		 * @param year the year 
		 */
		public MonthElement(int month, int year) {
			
			this.month = month;
			this.year = year;
			this.transactions = new ArrayList<Transaction>();
			this.transactionSet = new HashSet<Transaction>();
			
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(year, month, c.getActualMinimum(Calendar.DAY_OF_MONTH), 0, 0, 0);
			first = c.getTime();
			
			c.set(year, month, c.getActualMaximum(Calendar.DAY_OF_MONTH),
					c.getActualMaximum(Calendar.HOUR_OF_DAY),
					c.getActualMaximum(Calendar.MINUTE),
					c.getActualMaximum(Calendar.SECOND));
			last = c.getTime();
			
			amountByCategory = new HashMap<Category, Integer>();
		}
		
		
		/**
		 * Determine if this transaction falls within this date range
		 * 
		 * @param t the transaction
		 * @return true if it falls into this date range
		 */
		public boolean accepts(Transaction t) {
			return t.getDate().compareTo(first) >= 0
					&& t.getDate().compareTo(last) <= 0;
		}
		
		
		/**
		 * Update the statistics given the transaction
		 * 
		 * @param t the transaction
		 */
		private void updateStats(Transaction t) {
			
			Integer amount = amountByCategory.get(t.getCategory());
			if (amount == null) amount = 0;
			amount += t.getCents();
			amountByCategory.put(t.getCategory(), amount);
			
			Integer maxAmount = maxAmountByCategory.get(t.getCategory());
			if (maxAmount == null || Math.abs(maxAmount) < Math.abs(amount)) {
				maxAmount = amount;
				maxAmountByCategory.put(t.getCategory(), maxAmount);
				
				if (maxAmount > 0) {
					if (maxPositiveAbsAmountInCategory < Math.abs(maxAmount)) {
						maxPositiveAbsAmountInCategory = Math.abs(maxAmount);
					}
				}
				else {
					if (maxNegativeAbsAmountInCategory < Math.abs(maxAmount)) {
						maxNegativeAbsAmountInCategory = Math.abs(maxAmount);
					}
				}
			}	
		}
		
		
		/**
		 * Add a transaction
		 * 
		 * @param t the transaction
		 */
		public void add(Transaction t) {
			if (!accepts(t)) {
				throw new IllegalArgumentException("Transaction with date "
						+ t.getDate() + " does not belong to "
						+ this.getClass().getSimpleName() + " " + this);
			}
			if (transactionSet.add(t)) {
				transactions.add(t);
				updateStats(t);
			}
		}
		
		
		/**
		 * Recompute the statistics
		 */
		public void recomputeStats() {
			
			amountByCategory.clear();
			for (Transaction t : transactions) updateStats(t);
		}
		
		
		/**
		 * Get a total amount by category
		 * 
		 * @param category the category
		 * @return the total (possibly negative) amount in cents, or 0 if not found 
		 */
		public int getAmount(Category category) {
			Integer amount = amountByCategory.get(category);
			return amount == null ? 0 : amount.intValue();
		}
		
		
		/**
		 * Get the string representation of this element
		 * 
		 * @return the string
		 */
		@Override
		public String toString() {
			return "" + (month + 1) + "/" + year;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + month;
			result = prime * result + year;
			return result;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MonthElement other = (MonthElement) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (month != other.month)
				return false;
			if (year != other.year)
				return false;
			return true;
		}

		
		/* (non-Javadoc)
		 * Get the outer type
		 */
		private MonthList getOuterType() {
			return MonthList.this;
		}


		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(MonthElement e) {
			
			if (this instanceof SummaryMonthElement) {
				if (e instanceof SummaryMonthElement) {
					return 0;
				}
				else {
					return 1;
				}
			}
			else if (e instanceof SummaryMonthElement) {
				return -1;
			}
				
			if (e.year != year) return year - e.year;
			return month - e.month;
		}


		/**
		 * Get the month
		 * 
		 * @return the month
		 */
		public int getMonth() {
			return month;
		}


		/**
		 * Get the year
		 * 
		 * @return the year
		 */
		public int getYear() {
			return year;
		}
	}
	
	
	/**
	 * The summary element
	 */
	public class SummaryMonthElement extends MonthElement {

		/**
		 * Create an instance of Element
		 */
		public SummaryMonthElement() {
			super(-1, -1);
		}
		
		
		/**
		 * Get a total amount by category
		 * 
		 * @param category the category
		 * @return the total (possibly negative) amount in cents, or 0 if not found 
		 */
		public int getAmount(Category category) {
			
			if (elementSet.isEmpty()) return 0;
			
			int sum = 0;
			for (MonthElement e : elementSet) {
				sum += e.getAmount(category);
			}
			
			return sum / elementSet.size();
		}
		
		
		/**
		 * Get the string representation of this element
		 * 
		 * @return the string
		 */
		@Override
		public String toString() {
			return "Average";
		}
	}

	
	/**
	 * The cell component
	 */
	private class Cell extends JPanel {
		
		private static final int BASE = 2;
		
		private MonthElement contents;
		

		/**
		 * Create an instance of the cell component
		 */
		public Cell() {
			this.contents = null;
			setOpaque(true);
			setMinimumSize(new Dimension(60, 40));
			setPreferredSize(new Dimension(100, 100));
		}

		
		/**
		 * Set the contents
		 * 
		 * @param contents the new contents
		 */
		public void setContents(MonthElement contents) {
			this.contents = contents;
		}
		
		
		/**
		 * Paint
		 * 
		 * @param g the graphics context
		 */
		@Override
		public void paint(Graphics g) {			
			
			// Get the panel size and font
			
			Font f = g.getFont();
			int width = getWidth();
			int height = getHeight();

			
			// Clear the panel

			g.setColor(getBackground());
			
			g.fillRect(0, 0, width, height);
			if (contents == null) return;

			
			// Draw the label
			
			String title = contents.toString();
			int x = width / 2;
			int y = height - g.getFont().getSize() / 2 - 5;

			g.setColor(getForeground());

			Utils.drawCenteredString(g, title, x, y);
			
			
			// Initialize the bar plot
			
			int numCategories = 0;
			for (Category c : categories) {
				if (!maxAmountByCategory.containsKey(c)) continue;
				numCategories++;
			}
			
			
			// Draw the bars
			
			int bars_margin_h = 5;
			int w = (int) ((width - 2 * bars_margin_h) / (3 + 2 * 0.5 + numCategories));
			
			boolean drawLabels = w - 4 >= 4;
			int label_margin_t = 0;
			int label_margin_b = 0;
			
			if (drawLabels) {
				float fh = Math.min(f.getSize2D(), w - 4.0f);
				g.setFont(f.deriveFont(fh));
				FontMetrics fm = g.getFontMetrics();
				
				double longest_label_amount = 0;
				double longest_label_category = 0;
				
				for (int i = -1; i < categories.size(); i++) {
					Category c = i < 0 ? null : categories.get(i);
					if (c != null && !maxAmountByCategory.containsKey(c)) continue;
					
					int amount = contents.getAmount(c);
					String s = Utils.AMOUNT_FORMAT.format(amount/100.0);
					if (amount > 0) s = "+" + s;
					
					Rectangle2D r = fm.getStringBounds(s, g);
					longest_label_amount = Math.max(longest_label_amount, r.getWidth());
					
					r = fm.getStringBounds(c == null ? "(savings)" : c.getName(), g);
					longest_label_category = Math.max(longest_label_category, r.getWidth());
				}
				
				label_margin_t = (int) Math.ceil(longest_label_amount);
				label_margin_b = (int) Math.ceil(longest_label_category);
				
				/*if (label_margin_t + label_margin_b + 10 + f.getSize() > 3 * height / 4) {
					drawLabels = false;
					label_margin_t = 0;
					label_margin_b = 0;
					g.setFont(f);
				}*/
			}
			
			int bars_margin_t = 5 + label_margin_t;
			int bars_margin_b = 5 + label_margin_b;
			
			//x = bars_margin_h;
			x = (int) (width - w * (2 * 0.5 + numCategories + 3)) / 2;
			
			y = y - f.getSize() / 2 - bars_margin_b;
			int m = y - bars_margin_t;
			
			
			// Income

			int p = x;
			
			for (Category c : categories) {
				if (!maxAmountByCategory.containsKey(c)) continue;
				if (c != null && c.getType() == CategoryType.INCOME) {
					paintBar(g, p, y, w, m, c, drawLabels);
					p += w;
				}
			}
			
			
			// Expenses
			
			p += (int) (0.5 * w);
			
			for (Category c : categories) {
				if (!maxAmountByCategory.containsKey(c)) continue;
				if (c != null && c.getType() == CategoryType.EXPENSE) {
					paintBar(g, p, y, w, m, c, drawLabels);
					p += w;
				}
			}

			
			// Other 
			
			p += (int) (0.5 * w);
			
			paintBar(g, p, y, w, m, null, drawLabels);
			p += w;
			
			
			// External transfers
			
			int external = 0;
			for (Category c : categories) {
				int amount = contents.getAmount(c);
				if (c != null && c.getType() == CategoryType.EXTERNAL) {
					external += amount;
				}
			}
			
			paintBar(g, p, y, w, m, drawLabels ? "(savings)" : null,
					-external, external <= 0 ? Color.GREEN.darker().darker() : Color.RED.darker());
			p += w;

			
			// Net
			
			int net = 0;
			for (Category c : categories) {
				int amount = contents.getAmount(c);
				if (c == null
						|| (c.getType() != CategoryType.BALANCED
							&& c.getType() != CategoryType.EXTERNAL)) {
					net += amount;
				}
			}
			
			paintBar(g, p, y, w, m, drawLabels ? "(net)" : null,
					net, net >= 0 ? Color.GREEN.darker() : Color.RED);
			p += w;

			
			// Finish
			
			if (drawLabels) {
				g.setFont(f);
			}
		}
		
		
		/**
		 * Paint a bar for the given category
		 * 
		 * @param g the graphics context
		 * @param x the X coordinate
		 * @param y the bottom Y coordinate
		 * @param w the bar width
		 * @param m the bar area height
		 * @param label the label
		 * @param amount the amount
		 * @param color the color
		 */
		private void paintBar(Graphics g, int x, int y, int w, int m,
				String label, int amount, Color color) {

			int max = amount > 0
					? maxPositiveAbsAmountInCategory
					: maxNegativeAbsAmountInCategory;
			
			g.setColor(color);
			
			int h = (int) Math.round((Math.abs(amount) / (double) max) * (m - BASE));				
			g.fillRect(x, y - BASE - h, w, h + BASE);
			
			
			// Label

			if (label != null) {
				
				g.setColor(color.darker());
				
				String s = Utils.AMOUNT_FORMAT.format(amount/100.0);
				if (amount > 0) s = "+" + s;
				Utils.drawVerticalString(g, " " + s, x + w/2, y - BASE - h);
				
				Rectangle2D r = g.getFontMetrics().getStringBounds(label + " ", g);
				Utils.drawVerticalString(g, label + " ", x + w/2, y + (int) r.getWidth());
			}
		}
		
		
		/**
		 * Paint a bar for the given category
		 * 
		 * @param g the graphics context
		 * @param x the X coordinate
		 * @param y the bottom Y coordinate
		 * @param w the bar width
		 * @param m the bar area height
		 * @param c the category
		 * @param drawLabels true to draw the labels
		 */
		private void paintBar(Graphics g, int x, int y, int w, int m, Category c,
				boolean drawLabels) {
			
			int amount = contents.getAmount(c);
			
			Color color = c == null ? Color.BLACK : c.getColor();
			color = color.darker();
			
			String label = null;
			if (drawLabels) label = c == null ? "(other)" : c.getName(); 

			paintBar(g, x, y, w, m, label, amount, color);
		}
	}


	/**
	 * Cell renderer
	 */
	private class CellRenderer implements ListCellRenderer {
		
		private Cell cell;
		

		/**
		 * Create an instance of the cell renderer
		 */
		public CellRenderer() {
			cell = new Cell();
		}
		
		
		/**
		 * Get the cell
		 * 
		 * @return the cell
		 */
		public Cell getCell() {
			return cell;
		}


		/**
		 * Initialize a cell renderer
		 *
		 * @param list the list
		 * @param object the object
		 * @param index the index
		 * @param isSelected whether the current row is selected
		 * @param cellHasFocus whether the cell has focus
		 * @return the cell renderer
		 */
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			MonthElement e = (MonthElement) value;

			if (isSelected) {
				cell.setForeground(list.getForeground());
				cell.setBackground(Utils.getColorInBetween(list.getBackground(),
						list.getSelectionBackground(), 0.25));
			}
			else {
				cell.setForeground(list.getForeground());
				cell.setBackground(list.getBackground());
			}
			
			cell.setContents(e);
			
			return cell;
		}
	}
	
	
	/**
	 * The mouse adapter
	 */
	private class MouseHandler extends MouseAdapter
	{
		/**
		 * The mouse was clicked
		 * 
		 * @param e the event
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{
			// Clear the selection if the mouse was clicked outside of the elements
			
			int index = locationToIndex(e.getPoint());
			if (index < 0) {
				clearSelection();
				return;
			}
			
			Rectangle r = getCellBounds(index, index);
			if (r == null || !r.contains(e.getPoint())){
				clearSelection();
			}
		}
		
		
		/**
		 * The mouse was pressed
		 * 
		 * @param e the event
		 */
		@Override
		public void mousePressed(MouseEvent e)
		{
			mouseClicked(e);
		}
		
		
		/**
		 * The mouse was released
		 * 
		 * @param e the event
		 */
		@Override
		public void mouseReleased(MouseEvent e)
		{
			mouseClicked(e);
		}
	}

	
	/**
	 * The mouse adapter for the scroll pane container
	 */
	private class ScrollPaneMouseHandler extends MouseAdapter
	{
		/**
		 * The mouse was clicked
		 * 
		 * @param e the event
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{
			// Clear the selection if the mouse was clicked outside of the rows

			clearSelection();
		}
	}
}
