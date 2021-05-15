package com.aific.finances;

import javax.swing.UIManager;

import com.aific.finances.ui.MainFrame;
import com.aific.finances.util.Utils;


/**
 * The main class
 * 
 * @author Peter Macko
 */
public class Main {
	
	public static final String PROGRAM_NAME = "Goldfish Finances";
	
	
	/**
	 * Set a property with silent failover
	 * 
	 * @param key the key
	 * @param value the value
	 */
	private static void setSystemProperty(String key, String value) {
		try {
			System.setProperty(key, value);
		}
		catch (Exception e) {
			// do a silent fail-over
		}
	}

	
	/**
	 * The main function
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) throws Exception {
		
		// Set-up platform-specific properties
		
		setSystemProperty("apple.laf.useScreenMenuBar", "true");
		setSystemProperty("com.apple.mrj.application.growbox.intrudes", "false");
		setSystemProperty("com.apple.mrj.application.apple.menu.about.name", PROGRAM_NAME);
		setSystemProperty("com.apple.macos.smallTabs", "true");
		setSystemProperty("com.apple.mrj.application.live-resize", "true");
		
		try {
			if (!Utils.IS_LINUX) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}
		catch (Exception e) {
			// do a silent fail-over
		}

		
		// Create the main window
		
		MainFrame frame = new MainFrame();
		
		
		// Apple-specific stuff
		
		if (Utils.IS_MACOS) {
			osxadapter.OSXAdapter.setQuitHandler(frame, MainFrame.class.getMethod("checkModified"));
		}
		
		
		// Run
		
		frame.run();
	}
}
