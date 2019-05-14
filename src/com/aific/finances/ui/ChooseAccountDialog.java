package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.aific.finances.Account;
import com.aific.finances.Accounts;
import com.aific.finances.util.Utils;


/**
 * A dialog to choose an account
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class ChooseAccountDialog extends JDialog {

	private ArrayList<Account> accounts;
	
	private JScrollPane accountsListScroll;
	private JList<Account> accountsList;
	
	private Account account = null;
	
	
	/**
	 * Create an instance of ChooseAccountDialog
	 * 
	 * @param parent the parent frame
	 * @param accounts the collection of accounts
	 */
	public ChooseAccountDialog(JFrame parent, Accounts accounts) {
		super(parent, "Choose an Account");
		
		setModal(true);
		setLayout(new BorderLayout());
		
		
		// Create the list of accounts
		
		this.accounts = new ArrayList<Account>();
		this.accounts.addAll(accounts.getAll());
		
		Collections.sort(this.accounts, new Comparator<Account>() {
			@Override
			public int compare(Account a, Account b) {
				int r = a.getName().compareTo(b.getName());
				if (r != 0) return r;
				return a.getId().compareTo(b.getId());
			}
		});

		Account[] a = new Account[0];
		accountsList = new JList<Account>(this.accounts.toArray(a));
		accountsList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					int index = accountsList.locationToIndex(event.getPoint());
					if (index >= 0 && index < ChooseAccountDialog.this.accounts.size()) {
						account = ChooseAccountDialog.this.accounts.get(index);
						setVisible(false);
						dispose();
					}
				}
			}
		});

		accountsListScroll = new JScrollPane(accountsList);
		getContentPane().add(accountsListScroll, BorderLayout.CENTER);
		
		
		// Finish
		
		pack();
		Utils.centerWindow(this);
	}
	
	
	/**
	 * Choose an account
	 * 
	 * @param parent the parent frame
	 * @param accounts the collection of accounts
	 * @return the account, or null if canceled
	 */
	public static Account chooseAccount(JFrame parent, Accounts accounts) {
		
		ChooseAccountDialog d = new ChooseAccountDialog(parent, accounts);
		d.setVisible(true);
		
		return d.account;
	}
}
