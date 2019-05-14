package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.aific.finances.Document;
import com.aific.finances.Main;
import com.aific.finances.Transaction;
import com.aific.finances.util.JBetterTextField;
import com.aific.finances.util.Month;
import com.aific.finances.util.Utils;


/**
 * The main window
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements Runnable {
	
	private static MainFrame instance = null;
	private static final String TITLE = Main.PROGRAM_NAME;
	
	private boolean modified;
	
	private TransactionTable transactionsTable;
	private JScrollPane transactionsPane;
	
	private MonthList monthList;
	private JScrollPane monthsPane;
	
	private JToolBar toolbar;
	private JBetterTextField searchField;
	
	private JLabel statusLabel;
	
	CategoriesFrame categoriesFrame;
	PlotFrame plotFrame;
	BudgetFrame budgetFrame;
	
	private Document document;
	private Handler handler;

	
	/**
	 * Create a new instance of {@link MainFrame}
	 */
	public MainFrame() {
		super(TITLE);
		
		instance = this;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		
		handler = new Handler();
		addWindowListener(handler);
		
		new MainMenu(this);
		
		
		// The document
		
		document = new Document();
		
		
		// Transactions
		
		transactionsTable = new TransactionTable(document);
		transactionsTable.getSelectionModel().addListSelectionListener(handler);
		transactionsPane = new JScrollPane(transactionsTable);
		
		
		// Months
		
		monthList = new MonthList(document);
		monthList.addListSelectionListener(handler);
		
		monthsPane = new JScrollPane(monthList,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,  
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		monthsPane.getHorizontalScrollBar().setUnitIncrement(10);
		monthList.setJScrollPane(monthsPane);
		
		Dimension monthListMinSize = monthList.getMinimumSize();
		monthListMinSize.height += monthsPane.getHorizontalScrollBar()
				.getMinimumSize().height;
		monthsPane.setMinimumSize(monthListMinSize);
		
		JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				monthsPane, transactionsPane);
		topSplitPane.setDividerLocation(120);
		topSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
				new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				monthList.setCellHeight(((Number) pce.getNewValue()).intValue()
						- monthsPane.getHorizontalScrollBar().getMinimumSize().height);
			}
		});
		getContentPane().add(topSplitPane, BorderLayout.CENTER);
		
		
		// The tool bar
		
		toolbar = new JToolBar();
		
		searchField = new JBetterTextField();
		searchField.setPlaceholder(" Search");
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				configureTransactionsFilter();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				configureTransactionsFilter();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				configureTransactionsFilter();
			}
		});
		toolbar.add(searchField);
		
		getContentPane().add(toolbar, BorderLayout.PAGE_START);
		
		
		// Status
		
		statusLabel = new JLabel("");
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
		
		
		// Apply styles
		
		monthList.setBackground(new Color(230, 242, 255));
		monthsPane.setBackground(monthList.getBackground());
		
		monthList.setBorder(null);
		monthsPane.setBorder(null);
		
		transactionsPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// http://stackoverflow.com/questions/8934169/how-to-change-the-color-or-background-color-of-jsplitpane-divider
		topSplitPane.setUI(new BasicSplitPaneUI() {
		    public BasicSplitPaneDivider createDefaultDivider() {
		        return new BasicSplitPaneDivider(this) {
		            @Override
		            public void paint(Graphics g) {
		            	g.setColor(new Color(230, 242, 255));
		            	g.fillRect(0, 0, getSize().width, getSize().height);
		            	super.paint(g);
		            	g.setColor(new Color(26, 133, 255));
		            	g.drawLine(0, getSize().height / 2, getSize().width - 0, getSize().height / 2);
		            }
		        };
		    }
		});
		topSplitPane.setDividerSize(5);
		topSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// Another idea to try:
		//   https://www.formdev.com/blog/swing-tip-jsplitpane-with-zero-size-divider/

		
		// Finish
		
		configureTransactionsFilter();
		
		pack();
		setSize(1000, 650);
		setTitle();
		Utils.centerWindow(this);
	}


	/**
	 * Run the program
	 */
	@Override
	public void run() {
		setVisible(true);
	}
	
	
	/**
	 * Get the global instance of the main window
	 * 
	 * @return the instance of the main window
	 */
	public static MainFrame getInstance() {
		return instance;
	}
	
	
	/**
	 * Get the document
	 * 
	 * @return the instance of {@link Document}
	 */
	public Document getDocument() {
		return document;
	}
	
	
	/**
	 * Get the transactions table
	 * 
	 * @return the instance of {@link TransactionTable}
	 */
	public TransactionTable getTransactionTable() {
		return transactionsTable;
	}
	
	
	/**
	 * Set the window title
	 */
	protected void setTitle() {
		if (document.getFile() == null) {
			setTitle("Untitled" + (modified ? "*" : "") + " - " + TITLE);
		}
		else {
			setTitle(document.getFile().getName() + (modified ? "*" : "") + " - " + TITLE);
		}
	}

	
	/**
	 * Clear and start new document
	 */
	public void clear() {
		
		document = new Document();
		
		transactionsTable.setDocument(document);
		monthList.setDocument(document);
		
		if (categoriesFrame != null) categoriesFrame.close();
		if (plotFrame != null) plotFrame.close();
		if (budgetFrame != null) budgetFrame.close();
		
		setModified(false);
		setTitle();
	}

	
	/**
	 * Open a document
	 * 
	 * @param file the file
	 */
	public void open(File file) {

		try {
			document = Document.fromFile(file);
			
			transactionsTable.setDocument(document);
			monthList.setDocument(document);
			
			if (categoriesFrame != null) categoriesFrame.close();
			if (plotFrame != null) plotFrame.close();
			if (budgetFrame != null) budgetFrame.close();

			setModified(false);
			setTitle();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Save the document
	 * 
	 * @param file the file
	 */
	public void save(File file) {

		try {
			document.toFile(file);
			setModified(false);
			setTitle();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Save the document - the current document must be set
	 */
	public void save() {
		if (document.getFile() == null) throw new IllegalStateException("The document must be set");
		save(document.getFile());
	}
	
	
	/**
	 * Export the document to CSV
	 * 
	 * @param file the file
	 */
	public void exportToCsv(File file) {

		try {
			document.exportToCsv(file, transactionsTable.getFilter());
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Determine if the document is modified
	 * 
	 * @return true if modified
	 */
	public boolean isModified() {
		return modified;
	}


	/**
	 * Set the modified state
	 * 
	 * @param modified the new state of modified
	 */
	public void setModified(boolean modified) {
		
		if (this.modified != modified) {
			this.modified = modified;
			setTitle();
		}
	}


	/**
	 * Set the modified state
	 */
	public void setModified() {
		setModified(true);
	}
	
	
	/**
	 * Check with the user if it is okay to proceed if the document has been modified
	 * 
	 * @return true to proceed
	 */
	public boolean checkModified() {
		
		if (modified) {
			int r = JOptionPane.showConfirmDialog(this, "Your document has been modified. Save changes?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			switch (r) {
			case JOptionPane.YES_OPTION:
				if (document.getFile() == null) {
					File f = FileChoosers.chooseDocumentFile(this, "Save document", false);
					if (f == null) return false;
					if (Utils.getExtension(f) == null) f = new File(f.getAbsolutePath() + "." + Document.FILE_EXTENSION);
					if (Utils.checkOverwrite(this, f)) {
						save(f);
					}
				}
				else {
					save();
				}
				break;
			case JOptionPane.CANCEL_OPTION:
				return false;
			}

		}
		
		return true;
	}
	
	
	/**
	 * Configure the filter for transactions
	 */
	private void configureTransactionsFilter()
	{
		// Get the list of months
		
		ArrayList<Month> months = new ArrayList<>();
		boolean allMonths = false;
		
		for (Object obj : monthList.getSelectedValuesList()) {
			MonthList.MonthElement m = (MonthList.MonthElement) obj;
			if (m instanceof MonthList.SummaryMonthElement) {
				allMonths = true;
			}
			else {
				months.add(new Month(m.getMonth(), m.getYear()));
			}
		}
		
		if (allMonths) months.clear();
		
		
		// Parse the search field
		
		searchField.setForeground(getForeground());
		
		List<String> searchTerms = new ArrayList<>();
		String searchString = searchField.getText();
		StringBuilder searchTerm = new StringBuilder();
		boolean inQuotes = false;
		
		for (int i = 0; i < searchString.length(); i++) {
			char c = searchString.charAt(i);
			
			if (c == '"') {
				inQuotes = !inQuotes;
				searchTerm.append(c);
			}
			else if (!inQuotes && Character.isWhitespace(c)) {
				if (searchTerm.length() > 0) {
					searchTerms.add(searchTerm.toString());
					searchTerm.setLength(0);
				}
			}
			else {
				searchTerm.append(c);
			}
		}
		if (searchTerm.length() > 0) {
			searchTerms.add(searchTerm.toString());
			searchTerm.setLength(0);
		}
		
		if (inQuotes) {
			// This is probably OK...
		}

		
		// Set the filter
		
		transactionsTable.setFilter(t -> {

			// Check the list of months
			
			boolean ok = false;
			if (!months.isEmpty()) {
				for (Month m : months) {
					if (m.contains(t.getDate())) {
						ok = true;
						break;
					}
				}
				if (!ok) return false;
			}
			
			
			// Check the search bar filters
			
			// TODO The rest of parsing and validation should also happen outside
			
			for (String term : searchTerms) {
				
				boolean termPhrase = term.startsWith("\"");
				if (term.startsWith("\"")) term = term.substring(1);
				if (term.endsWith("\"")) term = term.substring(0, term.length()-1);
				if (term.isEmpty()) continue;
				
				String termLower = term.toLowerCase();
				
				
				// Value search
				
				if (!termPhrase && (term.startsWith("=") || term.startsWith("<=")
						|| term.startsWith(">=") || term.startsWith("<")
						|| term.startsWith(">"))) {
					
					String operator = term.substring(0, 1);
					if (term.length() > 1 && term.charAt(1) == '=') {
						operator += "=";
					}
					if (term.length() <= operator.length()) {
						searchField.setForeground(Color.RED);
						return false;
					}
					
					int amount;
					try {
						// TODO Allow commas in the parsing & maybe also make it locale-aware?
						amount = (int) Math.round(Double.parseDouble(
								term.substring(operator.length())) * 100);
					}
					catch (NumberFormatException e) {
						searchField.setForeground(Color.RED);
						return false;
					}
					
					if (operator.equals("=")) {
						if (Math.abs(t.getCents()) != amount) return false;
						continue;
					}
					else if (operator.equals(">")) {
						if (Math.abs(t.getCents()) <= amount) return false;
						continue;
					}
					else if (operator.equals("<")) {
						if (Math.abs(t.getCents()) >= amount) return false;
						continue;
					}
					else if (operator.equals(">=")) {
						if (Math.abs(t.getCents()) < amount) return false;
						continue;
					}
					else if (operator.equals("<=")) {
						if (Math.abs(t.getCents()) > amount) return false;
						continue;
					}
				}
				
				
				// String search
				
				ok = false;
				if (t.getCategory() != null) {
					if (t.getCategory().getName().toLowerCase().contains(termLower)) {
						ok = true;
					}
				}
				if (t.getCategoryDetector() != null) {
					if (t.getCategoryDetector().toString().toLowerCase().contains(termLower)) {
						ok = true;
					}
				}
				if (t.getDescription() != null) {
					if (t.getDescription().toLowerCase().contains(termLower)) {
						ok = true;
					}
				}
				if (!ok) return false;
			}
			
			
			// We passed all checks
			
			return true;
		});
	}


	/**
	 * The event handler
	 */
	private class Handler extends WindowAdapter implements ListSelectionListener {

		/**
		 * The event for window closing
		 * 
		 * @param e the window event
		 */
		@Override
		public void windowClosing(WindowEvent e) {

			if (checkModified()) {
				instance = null;
				dispose();
				System.exit(0);
			}
		}

		
		/**
		 * Handle a selection change in the selection components
		 * 
		 * @param e the event
		 */
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			if (e.getSource() == monthList) {
				configureTransactionsFilter();
				repaint();
			}
			
			if (e.getSource() == transactionsTable.getSelectionModel()) {
				
				if (transactionsTable.getSelectedRowCount() <= 1) {
					statusLabel.setText("");
				}
				else {
					int total = 0;
					
					for (Transaction t : transactionsTable.getSelectedItems()) {
						total += t.getCents();
					}
					
					statusLabel.setText("<html><body><p>&nbsp;"
							+ "Sum = <font color=\""
							+ (total >= 0 ? "green" : "red") + "\">"
							+ Utils.AMOUNT_FORMAT_WITH_SIGN.format(total / 100.0)
							+ "</font></p></body></html>");
				}				
			}
		}
	}
}
