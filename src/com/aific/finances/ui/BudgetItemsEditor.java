package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.aific.finances.BudgetItem;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.util.SharedList;


/**
 * An editor of budget items
 */
@SuppressWarnings("serial")
public class BudgetItemsEditor extends JPanel {
	
	private SharedList<BudgetItem> list;
	
	private JButton newButton;
	private BudgetItemsTable table;

	private Handler handler;
	

	/**
	 * Create a new instance of the panel
	 * 
	 * @param document the document
	 * @param type the type of the items
	 * @param list the list of budget items
	 */
	public BudgetItemsEditor(Document document, CategoryType type, SharedList<BudgetItem> list)
	{
		this.list = list;
		
		setLayout(new BorderLayout());
		handler = new Handler();
		
		
		// Tools

		JToolBar tools = new JToolBar();
		tools.setFloatable(false);
		
		newButton = new JButton("New"); 
		newButton.addActionListener(handler);
		tools.add(newButton);
		
		
		// Table
		
		table = new BudgetItemsTable(document, type, list);

		
		
		// Finish
		
		add(tools, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	
	/**
	 * Prepare to dispose the panel
	 */
	public void prepareDispose()
	{
	}
	
	
	/**
	 * An event handler
	 */
	private class Handler implements ActionListener {


		/**
		 * The action event
		 * 
		 * @param e the event
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getSource() == newButton) {

				list.add(new BudgetItem());
			}
		}
	}
}
