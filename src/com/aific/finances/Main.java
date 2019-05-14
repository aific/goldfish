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
	 * The main function
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) throws Exception {
		
		// Set-up platform-specific properties
		
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", PROGRAM_NAME);
			System.setProperty("com.apple.macos.smallTabs", "true");
			System.setProperty("com.apple.mrj.application.live-resize", "true");
			
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
