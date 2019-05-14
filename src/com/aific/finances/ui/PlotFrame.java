package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.aific.finances.util.Utils;


/**
 * A frame with a plot
 */
@SuppressWarnings("serial")
public class PlotFrame extends JFrame
{
	private Handler handler;
	private PlotPanel plotPanel;
	
	
	/**
	 * Create an instance of {@link PlotFrame}
	 */
	public PlotFrame()
	{
		super("Analysis");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		
		handler = new Handler();
		addWindowListener(handler);
		
		plotPanel = new PlotPanel();
		getContentPane().add(plotPanel, BorderLayout.CENTER);
		
		if (Utils.IS_MACOS) new MainMenu(this);
		
		setMinimumSize(new Dimension(480, 320));

		pack();
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
			if (plotPanel != null) plotPanel.prepareDispose();
			
			MainFrame.getInstance().plotFrame = null;
			dispose();
		}
	}
}
