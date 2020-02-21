package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.aific.finances.Category;
import com.aific.finances.CategoryDetector;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.util.Utils;


/**
 * The categories window
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class CategoriesFrame extends JFrame {
	
	private static CategoriesFrame instance = null;
	private static final String TITLE = "Categories and Rules";
	
	@SuppressWarnings("unused")
	private Document document;
	private boolean modified;
	
	private CategoryEditorTable categoriesTable;
	private JScrollPane categoriesPane;
	
	private JPanel detectorsPanel;
	private JButton newDetectorButton;
	private DetectorsTable detectorsTable;
	
	private Handler handler;

	
	/**
	 * Create a new instance of {@link CategoriesFrame}
	 * 
	 * @param document the document
	 */
	public CategoriesFrame(Document document) {
		super(TITLE);
		
		this.document = document;
		
		instance = this;
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		handler = new Handler();
		addWindowListener(handler);

		setLayout(new BorderLayout());
		
		if (Utils.IS_MACOS) new MainMenu(this);
		
		
		// Categories
		
		categoriesTable = new CategoryEditorTable(document);
		categoriesTable.getSelectionModel().addListSelectionListener(handler);
		categoriesPane = new JScrollPane(categoriesTable);
		
		
		// Detectors

		detectorsPanel = new JPanel(new BorderLayout());
		detectorsTable = new DetectorsTable(document.getCategories(), MainFrame.getInstance());

		JToolBar tools = new JToolBar();
		tools.setFloatable(false);
		
		newDetectorButton = new JButton("New"); 
		newDetectorButton.addActionListener(handler);
		tools.add(newDetectorButton);
		
		detectorsPanel.add(tools, BorderLayout.NORTH);
		detectorsPanel.add(new JScrollPane(detectorsTable), BorderLayout.CENTER);
		
		
		// Select the first category
		
		categoriesTable.setRowSelectionInterval(0, 0);
		
		
		// The split pane
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				categoriesPane, detectorsPanel);
		
		int w = 0;
		for (int i = 0; i < categoriesTable.getColumnModel().getColumnCount(); i++) {
			w += categoriesTable.getColumnModel().getColumn(i).getWidth() + 2;
		}
		splitPane.setDividerLocation(w);
		
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		
		// Finish
		
		pack();
		Utils.centerWindow(this);
		setVisible(true);
	}

	
	/**
	 * Get the global instance of the main window
	 * 
	 * @return the instance of the main window
	 */
	public static CategoriesFrame getInstance() {
		return instance;
	}
	
	
	/**
	 * Set the window title
	 */
	protected void setTitle() {
		setTitle(TITLE + (modified ? "*" : ""));
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
			
			// TODO This is not doing the right thing! 
			
			int r = JOptionPane.showConfirmDialog(this,
					"Your categories have been modified. Save changes?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			switch (r) {
			case JOptionPane.YES_OPTION:
				// TODO
				break;
			case JOptionPane.CANCEL_OPTION:
				return false;
			}

		}
		
		return true;
	}
	
	
	/**
	 * Close the window
	 */
	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	
	/**
	 * The event handler
	 */
	 private class Handler extends WindowAdapter implements ListSelectionListener, ActionListener {

		/**
		 * The event for window closing
		 * 
		 * @param e the window event
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			
			// Probably not necessary: if (checkModified()) { ... }
			
			categoriesTable.prepareDispose();
			detectorsTable.prepareDispose();
			
			instance = null;
			MainFrame.getInstance().categoriesFrame = null;
			dispose();
		}

		
		/**
		 * The event for selection change
		 * 
		 * @param e the event
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			if (e.getSource() == categoriesTable.getSelectionModel() && categoriesTable.getSelectedRow() >= 0) {
				Category c = categoriesTable.getCategory(categoriesTable.getSelectedRow());
				detectorsTable.filterByCategory(c);
			}
		}


		/**
		 * The action event
		 * 
		 * @param e the event
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getSource() == newDetectorButton) {
				Category c = categoriesTable.getCategory(categoriesTable.getSelectedRow());
				
				CategoryDetector matchingDetector = null;
				if (c.getType() == CategoryType.BALANCED) {
					matchingDetector = new CategoryDetector(UUID.randomUUID().toString(),
							c, "", "", "", 0, 0, "", null);
					c.add(matchingDetector);
				}
					
				CategoryDetector d = new CategoryDetector(UUID.randomUUID().toString(),
						c, "", "", "", 0, 0, matchingDetector == null ? null : "", matchingDetector);
				c.add(d);
				
				d.setDescription("");	// Update the matching detector
				MainFrame.getInstance().setModified();
			}
		}
	 }
}
