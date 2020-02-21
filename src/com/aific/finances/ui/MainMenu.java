package com.aific.finances.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.aific.finances.Account;
import com.aific.finances.Accounts;
import com.aific.finances.Document;
import com.aific.finances.Transaction;
import com.aific.finances.io.OfxFile;
import com.aific.finances.util.Utils;


/**
 * The main menu
 * 
 * @author Peter Macko
 */
public class MainMenu implements ActionListener {

	private JFrame frame;
	
	private JMenuBar mainMenu;
	
	private JMenu fileMenu;
	private JMenuItem fileNewMenuItem;
	private JMenuItem fileOpenMenuItem;
	private JMenuItem fileSaveMenuItem;
	private JMenuItem fileSaveAsMenuItem;
	private JMenuItem fileImportMenuItem;
	private JMenuItem fileExportCsvMenuItem;
	private JMenuItem fileQuitMenuItem;
	
	private JMenu toolsMenu;
	private JMenuItem toolsAnalysisMenuItem;
	private JMenuItem toolsBudgetMenuItem;
	private JMenuItem toolsCategoriesMenuItem;

	
	/**
	 * Create a new instance of {@link MainMenu}
	 * 
	 * @param frame the JFrame
	 */
	public MainMenu(JFrame frame) {
		
		this.frame = frame;
		mainMenu = new JMenuBar();
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		mainMenu.add(fileMenu);

		fileNewMenuItem = new JMenuItem("New", KeyEvent.VK_N);
		fileNewMenuItem.addActionListener(this);
		fileMenu.add(fileNewMenuItem);

		fileOpenMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		fileOpenMenuItem.addActionListener(this);
		fileOpenMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_O));
		fileMenu.add(fileOpenMenuItem);

		fileMenu.addSeparator();

		fileSaveMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
		fileSaveMenuItem.addActionListener(this);
		fileSaveMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_S));
		fileMenu.add(fileSaveMenuItem);

		fileSaveAsMenuItem = new JMenuItem("Save as...", KeyEvent.VK_A);
		fileSaveAsMenuItem.addActionListener(this);
		fileMenu.add(fileSaveAsMenuItem);

		fileMenu.addSeparator();

		fileImportMenuItem = new JMenuItem("Import Transactions...", KeyEvent.VK_I);
		fileImportMenuItem.addActionListener(this);
		fileImportMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_I));
		fileMenu.add(fileImportMenuItem);

		fileExportCsvMenuItem = new JMenuItem("Export to CSV...", KeyEvent.VK_E);
		fileExportCsvMenuItem.addActionListener(this);
		fileExportCsvMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_E));
		fileMenu.add(fileExportCsvMenuItem);

		if (!Utils.IS_MACOS) {
			
			fileMenu.addSeparator();
	
			fileQuitMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
			fileQuitMenuItem.addActionListener(this);
			fileQuitMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_Q));
			fileMenu.add(fileQuitMenuItem);
		}
		
		toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		mainMenu.add(toolsMenu);

		toolsAnalysisMenuItem = new JMenuItem("Analysis...", KeyEvent.VK_A);
		toolsAnalysisMenuItem.addActionListener(this);
		toolsAnalysisMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK));
		toolsMenu.add(toolsAnalysisMenuItem);

		toolsBudgetMenuItem = new JMenuItem("Budget...", KeyEvent.VK_B);
		toolsBudgetMenuItem.addActionListener(this);
		toolsBudgetMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_B, InputEvent.SHIFT_DOWN_MASK));
		toolsMenu.add(toolsBudgetMenuItem);
		
		toolsMenu.addSeparator();

		toolsCategoriesMenuItem = new JMenuItem("Categories and Rules...", KeyEvent.VK_C);
		toolsCategoriesMenuItem.addActionListener(this);
		toolsCategoriesMenuItem.setAccelerator(getKeyStrokeForMenu(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK));
		toolsMenu.add(toolsCategoriesMenuItem);

		this.frame.setJMenuBar(mainMenu);
	}
	
	
	/**
	 * Return a key stroke accelerator for a menu item
	 *
	 * @param keyCode the virtual key
	 * @return the accelerator
	 */
	@SuppressWarnings("deprecation")
	private static KeyStroke getKeyStrokeForMenu(int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask());
	}
	
	
	/**
	 * Return a key stroke accelerator for a menu item
	 *
	 * @param keyCode the virtual key
	 * @param modifier the modifiers
	 * @return the accelerator
	 */
	@SuppressWarnings("deprecation")
	private static KeyStroke getKeyStrokeForMenu(int keyCode, int modifiers) {
		return KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask() | modifiers);
	}


	/**
	 * The action handler
	 * 
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			
			if (e.getSource() == fileNewMenuItem) {
				if (MainFrame.getInstance().checkModified()) {
					MainFrame.getInstance().clear();
				}
			}
			
			if (e.getSource() == fileOpenMenuItem) {
				File f = FileChoosers.chooseDocumentFile(frame, "Open document", true);
				if (f == null) return;
				if (MainFrame.getInstance().checkModified()) {
					MainFrame.getInstance().open(f);
					MainFrame.getInstance().getDocument().getCategories()
						.detectCategoriesForUncategorized(
							MainFrame.getInstance().getTransactionTable().getTransactions());
				}
			}
			
			
			if (e.getSource() == fileSaveMenuItem) {
				
				if (MainFrame.getInstance().getDocument().getFile() == null) {
					File f = FileChoosers.chooseDocumentFile(frame, "Save document", false);
					if (f == null) return;
					
					if (Utils.getExtension(f) == null) f = new File(f.getAbsolutePath() + "." + Document.FILE_EXTENSION);
					
					if (Utils.checkOverwrite(frame, f)) {
						MainFrame.getInstance().save(f);
					}
				}
				else {
					MainFrame.getInstance().save();
				}
			}
			
			
			if (e.getSource() == fileSaveAsMenuItem) {
				
				File f = FileChoosers.chooseDocumentFile(frame, "Save document as", false);
				if (f == null) return;
				
				if (Utils.getExtension(f) == null) f = new File(f.getAbsolutePath() + "." + Document.FILE_EXTENSION);
					
				if (Utils.checkOverwrite(frame, f)) {
					MainFrame.getInstance().save(f);
				}
			}
				
			
			if (e.getSource() == fileImportMenuItem) {
				
				File f = FileChoosers.chooseTransactionsImportFile(frame, "Import Transactions", true);
				if (f == null) return;
				
				Document d = MainFrame.getInstance().getDocument();
				Accounts accounts = MainFrame.getInstance().getDocument().getAccounts();
				Collection<Transaction> transactions = null;
				
				String extension = Utils.getExtension(f).toLowerCase();
				switch (extension) {
				case "ofx":
				case "qfx": {
					OfxFile ofx = new OfxFile(f);
					if (!ofx.getCurrency().equals("USD")) {
						throw new Exception("The only currency we currently support is USD");
					}
					Account a = ofx.matchAccount(accounts);
					if (a == null) {
						a = ofx.getAccount();
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(),
								"Account \"" + a.getName() + "\" is not yet in the document. Add it?",
								"Import Transactions",
								 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
								!= JOptionPane.YES_OPTION) return;
						accounts.add(a);
					}
					transactions = ofx.loadTransactions(a);
					break;
				}
				case "csv": {
					Account a = ChooseAccountDialog.chooseAccount(frame, accounts);
					if (a == null) return;
					
					transactions = a.getReader().readTransactions(d, a, f);
					break;
				}
				default:
					throw new Exception("Unsupported file type");
				}
				
				if (transactions == null) {
					throw new Exception("Internal error");
				}
				
				MainFrame.getInstance().getDocument().getCategories().detectCategoriesAll(transactions,
						MainFrame.getInstance().getTransactionTable().getTransactions());
				
				MainFrame.getInstance().getTransactionTable().setVisible(false);
				MainFrame.getInstance().getTransactionTable().getTransactions().addAll(transactions);
				MainFrame.getInstance().getTransactionTable().adjustColumns();
				MainFrame.getInstance().getTransactionTable().setVisible(true);
				MainFrame.getInstance().setModified();
			}
			
			
			if (e.getSource() == fileExportCsvMenuItem) {
				
				File f = FileChoosers.chooseExportCsvFile(frame, "Export to CSV");
				if (f == null) return;
				
				if (Utils.getExtension(f) == null) f = new File(f.getAbsolutePath() + ".csv");
					
				if (Utils.checkOverwrite(frame, f)) {
					MainFrame.getInstance().exportToCsv(f);
				}
			}
			
			
			if (e.getSource() == fileQuitMenuItem) {
				MainFrame m = MainFrame.getInstance();
				if (m == null) System.exit(0);
				
				CategoriesFrame c = CategoriesFrame.getInstance();
				if (c != null) {
					c.setVisible(false);
					c.dispose();
				}
				
				if (m.checkModified()) {
					m.setVisible(false);
					m.dispose();
					System.exit(0);
				}
				else return;
			}
			
			
			if (e.getSource() == toolsAnalysisMenuItem) {
				MainFrame.getInstance().plotFrame = new PlotFrame();
			}
			
			
			if (e.getSource() == toolsBudgetMenuItem) {
				MainFrame.getInstance().budgetFrame = new BudgetFrame();
			}
			
			
			if (e.getSource() == toolsCategoriesMenuItem) {
				CategoriesFrame c = CategoriesFrame.getInstance();
				if (c == null) {
					MainFrame.getInstance().categoriesFrame
						= new CategoriesFrame(MainFrame.getInstance().getDocument());
				}
				else {
					c.toFront();
				}
			}
		}
		catch (Exception e1) {
			String m = e1.getMessage();
			if (m == null || "".equals(m.trim())) {
				m = e1.getClass().getCanonicalName();
			}
			
			JOptionPane.showMessageDialog(frame, e1.getMessage(),
					"Failed", JOptionPane.ERROR_MESSAGE);

			e1.printStackTrace(System.err);
		}
	}
}
