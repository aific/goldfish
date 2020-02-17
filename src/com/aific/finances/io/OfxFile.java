package com.aific.finances.io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.UUID;

import com.aific.finances.Account;
import com.aific.finances.AccountType;
import com.aific.finances.Accounts;
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
		
		
		// Get the account number
		
		switch (accountType) {
		case CHECKING_ACCOUNT:
		case SAVINGS_ACCOUNT:
			accountNumber = ofx.getSgml().getText("BANKMSGSRSV1", "STMTTRNRS", "STMTRS",
					"BANKACCTFROM", "ACCTID");
			break;
		case CREDIT_CARD:
			accountNumber = ofx.getSgml().getText("CREDITCARDMSGSRSV1", "CCSTMTTRNRS", "CCSTMTRS",
					"CCACCTFROM", "ACCTID");
			break;
		default:
			throw new ParseException("Unsupported account type", 0);
		}
		if (accountNumber == null || accountNumber.isEmpty()) {
			throw new ParseException("Cannot determine the account number", 0);
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
}
