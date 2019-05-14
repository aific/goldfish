package com.aific.finances.io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

import com.aific.finances.Account;
import com.aific.finances.Document;
import com.aific.finances.Transaction;


/**
 * A transaction history reader
 * 
 * @author Peter Macko
 */
public interface TransactionHistoryReader {

	/**
	 * Read all transactions
	 * 
	 * @param document the document
	 * @param account the corresponding account
	 * @param file the input file
	 * @return the collection of all transactions in the file
	 * @throws IOException on I/O error
	 * @throws ParseException on parse error
	 */
	public Collection<Transaction> readTransactions(Document document, Account account, File file)
			throws IOException, ParseException;
}
