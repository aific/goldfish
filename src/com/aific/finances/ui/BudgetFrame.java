package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.aific.finances.util.Utils;


/**
 * A frame for setting up a budget
 */
@SuppressWarnings("serial")
public class BudgetFrame extends JFrame
{
	private Handler handler;
	private BudgetPanel budgetPanel;
	
	
	/**
	 * Create an instance of {@link BudgetFrame}
	 */
	public BudgetFrame()
	{
		super("Budget");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		
		handler = new Handler();
		addWindowListener(handler);
		
		budgetPanel = new BudgetPanel();
		getContentPane().add(budgetPanel, BorderLayout.CENTER);
		
		if (Utils.IS_MACOS) new MainMenu(this);
		
		setMinimumSize(new Dimension(640, 320));
		
		pack();
		setSize(900, 480);
		Utils.centerWindow(this);
		setVisible(true);
	}
	
	
	/**
	 * Close the window
	 */
	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	
	/**
	 * An event handler
	 */
	private class Handler extends WindowAdapter
	{

		/**
		 * The event for window closing
		 * 
		 * @param e the window event
		 */
		@Override
		public void windowClosing(WindowEvent e)
		{
			if (budgetPanel != null) budgetPanel.prepareDispose();
			
			MainFrame.getInstance().budgetFrame = null;
			dispose();
		}
	}
}
