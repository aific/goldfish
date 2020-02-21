package com.aific.finances.io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.aific.finances.Account;
import com.aific.finances.AccountType;
import com.aific.finances.Accounts;
import com.aific.finances.Transaction;
import com.aific.finances.util.SgmlElement;
import com.aific.finances.util.SgmlWithHeader;


/**
 * An OFX / QFX file.
 * 
 * @author Peter Macko
 */
public class OfxFile {
	
	private SgmlWithHeader ofx;
	
	private String institution;
	private AccountType accountType;
	private String accountNumber;
	private String currency;
	private SgmlElement transactions;

	
	/**
	 * Create an instance of class OfxFile
	 * 
	 * @param file the file
	 * @throws IOException the I/O exception
	 * @throws ParseException on parse error
	 */
	public OfxFile(File file) throws IOException, ParseException {
		ofx = SgmlWithHeader.loadFromFile(file);
		
		
		// Get the institution
		
		institution = ofx.getSgml().getText("SIGNONMSGSRSV1", "SONRS", "FI", "ORG");
		if (institution == null || institution.isEmpty()) {
			throw new ParseException("Cannot determine the financial institution", 0);
		}
		if (institution.equals("Sovereign Bank New")) {
			institution = "Santander Bank";
		}
		
		
		// Get the account type
		
		String accountTypeString = ofx.getSgml().getText("BANKMSGSRSV1", "STMTTRNRS", "STMTRS",
				"BANKACCTFROM", "ACCTTYPE");
		if ("CHECKING".equalsIgnoreCase(accountTypeString)) {
			accountType = AccountType.CHECKING_ACCOUNT;
		}
		else if ("SAVINGS".equalsIgnoreCase(accountTypeString)) {
			accountType = AccountType.SAVINGS_ACCOUNT;
		}
		else if (accountTypeString == null && ofx.getSgml().get("CREDITCARDMSGSRSV1") != null) {
			accountType = AccountType.CREDIT_CARD;
		}
		else {
			throw new ParseException("Unsupported account type", 0);
		}
		
		
		// Get the statement aggregate
		
		SgmlElement statement = null;
		
		switch (accountType) {
		case CHECKING_ACCOUNT:
		case SAVINGS_ACCOUNT:
			statement = ofx.getSgml().get("BANKMSGSRSV1", "STMTTRNRS", "STMTRS");
			break;
		case CREDIT_CARD:
			statement = ofx.getSgml().get("CREDITCARDMSGSRSV1", "CCSTMTTRNRS", "CCSTMTRS");
			break;
		default:
			throw new ParseException("Unsupported account type", 0);
		}
		if (statement == null) {
			throw new ParseException("Cannot find the statement aggregate", 0);
		}
		
		
		// Get the account number
		
		switch (accountType) {
		case CHECKING_ACCOUNT:
		case SAVINGS_ACCOUNT:
			accountNumber = statement.getText("BANKACCTFROM", "ACCTID");
			break;
		case CREDIT_CARD:
			accountNumber = statement.getText("CCACCTFROM", "ACCTID");
			break;
		default:
			throw new ParseException("Unsupported account type", 0);
		}
		if (accountNumber == null || accountNumber.isEmpty()) {
			throw new ParseException("Cannot determine the account number", 0);
		}
		
		
		// Get the currency
		
		currency = statement.getText("CURDEF");
		if (currency == null || currency.isEmpty()) {
			throw new ParseException("Cannot determine the currency", 0);
		}
		
		
		// Get the transactions
		
		transactions = statement.get("BANKTRANLIST");
		if (transactions == null) {
			throw new ParseException("Cannot read the transactions", 0);
		}
	}


	/**
	 * Try to match with an existing account
	 * 
	 * @param accounts the accounts
	 * @return the matched account, or null if none
	 */
	public Account matchAccount(Accounts accounts) {
		
		for (Account a : accounts.getAll()) {
			if (!a.getInstitution().equals(institution)) continue;
			if (!a.getType().equals(accountType)) continue;
			if (!a.getNumbers().contains(accountNumber)) continue;
			
			return a;
		}
		
		return null;
	}


	/**
	 * Create an account from the provided information
	 * 
	 * @param accounts the accounts
	 * @return the account
	 */
	public Account getAccount() {
		String id = UUID.randomUUID().toString();
		
		String shortNumber = accountNumber;
		if (shortNumber.length() > 4) {
			shortNumber = shortNumber.substring(shortNumber.length() - 4);
		}
		
		String accountName = institution + " " + shortNumber;
		return new Account(id, institution, Collections.singletonList(accountNumber),
				accountType, accountName, accountName, null);
	}
	
	
	/**
	 * Get the currency
	 * 
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	
	
	/**
	 * Get a list of transactions
	 * 
	 * @param account the account to use
	 * @return the list of transactions
	 * @throws ParseException on parse error
	 */
	public List<Transaction> loadTransactions(Account account) throws ParseException {
		
		List<Transaction> r = new ArrayList<Transaction>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		for (SgmlElement e : transactions.getChildren()) {
			if (!"STMTTRN".equals(e.getTag())) continue;
			
			String strDatePosted = e.getText("DTPOSTED");
			if (strDatePosted == null || strDatePosted.isEmpty()) {
				throw new ParseException("Cannot determine the date of a transaction", 0);
			}
			Date date = dateFormat.parse(strDatePosted.substring(0, 8));
			
			String name = e.getText("NAME");
			String memo = e.getText("MEMO");
			if (name == null || name.isEmpty()) {
				throw new ParseException("Cannot determine the transaction name", 0);
			}
			String description = name;
			if (memo != null && memo.startsWith(description)) description = memo;
			
			String strAmount = e.getText("TRNAMT");
			boolean positive = strAmount.startsWith("+") || Character.isDigit(strAmount.charAt(0));
			boolean negative = strAmount.startsWith("-");
			if (strAmount == null || strAmount.length() < 5 || (!positive && !negative)
					|| strAmount.charAt(strAmount.length() - 3) != '.') {
				throw new ParseException("Cannot determine the transaction amount", 0);
			}
			int cents = Integer.parseInt(strAmount.substring(0, strAmount.length() - 3)
					+ strAmount.substring(strAmount.length() - 2));
			
			String fitId = e.getText("FITID");
			if (name == null || name.isEmpty()) {
				throw new ParseException("Cannot determine the transaction ID", 0);
			}
			String id = account.getId() + ":" + fitId;
			
			String correctFidId = e.getText("CORRECTFITID");
			if (correctFidId != null && !fitId.equals(correctFidId)) {
				throw new ParseException("We currently don't support CORRECTFITID", 0);
			}
			
			Transaction t = new Transaction(account, id, date, description, "", cents);
			r.add(t);
		}
		
		return r;
	}
}
