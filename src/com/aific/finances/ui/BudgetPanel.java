package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.aific.finances.Budget;
import com.aific.finances.BudgetListener;
import com.aific.finances.CategoryType;
import com.aific.finances.Document;
import com.aific.finances.util.DateUnit;
import com.aific.finances.util.Utils;


/**
 * A panel for setting up a budget
 */
@SuppressWarnings("serial")
public class BudgetPanel extends JPanel
{
	private Document document;
	private Budget budget;
	private Handler handler;
	
	private JPanel incomePanel;
	private BudgetItemsEditor incomeEditor;
	
	private JPanel expensesPanel;
	private BudgetItemsEditor expensesEditor;
	
	private JTabbedPane tabbedPane;
	private JLabel headerLabel;
	

	/**
	 * Create a new instance of the panel
	 */
	public BudgetPanel()
	{
		setLayout(new BorderLayout());
		handler = new Handler();
		
		document = MainFrame.getInstance().getDocument();
		budget = document.getBudgets().getOrCreateCurrentBudget();
		budget.addBudgetListener(handler);
		
		
		// Header
		
		headerLabel = new JLabel();
		updateSummary();
		add(headerLabel, BorderLayout.NORTH);
		
		
		// Income
		
		incomePanel = new JPanel(new BorderLayout());
		
		incomeEditor = new BudgetItemsEditor(document, CategoryType.INCOME, budget.getIncome());
		incomePanel.add(incomeEditor, BorderLayout.CENTER);
		
		
		// Expenses
		
		expensesPanel = new JPanel(new BorderLayout());
		
		expensesEditor = new BudgetItemsEditor(document, CategoryType.EXPENSE, budget.getExpenses());
		expensesPanel.add(expensesEditor, BorderLayout.CENTER);
		
		
		// The main tabbed pane
		
		tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("Income", incomePanel);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_I);
		
		tabbedPane.addTab("Expenses", expensesPanel);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_E);
		
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	
	/**
	 * Prepare to dispose the panel
	 */
	public void prepareDispose()
	{
		budget.removeBudgetListener(handler);
	}
	
	
	/**
	 * Update the summary
	 */
	public void updateSummary()
	{
		DateUnit unit = DateUnit.MONTH;
		double income = budget.getIncome().getSumPerYear() / (365.0 / unit.getDays());
		double expenses = budget.getExpenses().getSumPerYear() / (365.0 / unit.getDays());
		double net = income - expenses;
		
		headerLabel.setText("<html><body><table>"
				+ "<tr><td align=\"right\">Income:</td><td>"
				+ Utils.AMOUNT_FORMAT.format(income) + "</td></tr>"
				+ "<tr><td align=\"right\">Expenses:</td><td>"
				+ Utils.AMOUNT_FORMAT.format(expenses) + "</td></tr>"
				+ "<tr><td align=\"right\">Net:</td><td color=\""
				+ (net >= 0 ? "green" : "red") + "\">"
				+ Utils.AMOUNT_FORMAT_WITH_SIGN.format(net) + "</td></tr>"
				+ "</table></body></html>");
	}
	
	
	/**
	 * An event handler
	 */
	private class Handler implements BudgetListener {
		
		/**
		 * Budget was updated
		 * 
		 * @param budget the budget
		 */
		public void budgetUpdated(Budget budget)
		{
			MainFrame.getInstance().setModified();
			updateSummary();
		}
	}
}
