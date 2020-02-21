package com.aific.finances.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import au.com.bytecode.opencsv.CSVReader;

import com.aific.finances.Account;
import com.aific.finances.Document;
import com.aific.finances.Transaction;


/**
 * A transaction history reader for the Sovereign / Santander Bank
 * 
 * @author Peter Macko
 */
public class SantanderHistoryReader implements TransactionHistoryReader {


	/**
	 * Create an instance of class SatanderHistoryReader
	 */
	public SantanderHistoryReader() {
	}


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
			throws IOException, ParseException {
		
		CSVReader reader = new CSVReader(new FileReader(file));
		
		try {
			
			// Find the beginning of the header and save it
			
			String[] nextLine;
			int lineNo = 0;
			
			int indexDate = -1;
			int indexDescription = -1;
			int indexAmount = -1;
			int indexSerialNum = -1;
			
			boolean foundHeader = false;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				
				for (String s : nextLine) {
					if (s.equals("Amount")) {
						foundHeader = true;
						
						for (int i = 0; i < nextLine.length; i++) {
							if (nextLine[i].equals("Date")) indexDate = i;
							if (nextLine[i].equals("Description")) indexDescription = i;
							if (nextLine[i].equals("Amount")) indexAmount = i;
							if (nextLine[i].equals("Serial Num")) indexSerialNum = i;
						}
						
						if (indexDate < 0)
							throw new ParseException("Did not find column \"Date\"", lineNo);
						if (indexDescription < 0)
							throw new ParseException("Did not find column \"Description\"", lineNo);
						if (indexAmount < 0)
							throw new ParseException("Did not find column \"Amount\"", lineNo);
						if (indexSerialNum < 0)
							throw new ParseException("Did not find column \"Serial Num\"", lineNo);

						break;
					}
				}
				
				if (foundHeader) break;
			}
			
			if (!foundHeader) {
				throw new ParseException("Did not find the transaction history header", lineNo);
			}
			
			
			// Now parse the transactions
			
			ArrayList<Transaction> r = new ArrayList<Transaction>();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				
				Date date = dateFormat.parse(nextLine[indexDate]);
				String description = nextLine[indexDescription];
				String serialNum = nextLine[indexSerialNum];
				
				String strAmount = nextLine[indexAmount];
				int dotPosition = strAmount.length() - 3;
				if (dotPosition < 1 || strAmount.charAt(dotPosition) != '.') {
					throw new ParseException("The amount does not have a valid cents part", lineNo);
				}
				
				char sign = strAmount.charAt(0);
				boolean haveSign = true;
				if (Character.isDigit(sign)) {
					sign = '+';
					haveSign = false;
				}
				if (sign != '+' && sign != '-') {
					throw new ParseException("The amount does not have a valid sign", lineNo);
				}
				
				int amountWhole = Integer.parseInt(strAmount.substring(
						haveSign ? 1 : 0, dotPosition)); 
				int amountCents = Integer.parseInt(strAmount.substring(dotPosition + 1));
				int cents = (amountWhole * 100 + amountCents) * (haveSign && sign == '-' ? -1 : 1);
				
				Transaction t = new Transaction(account, serialNum, date, description, null, cents);
				r.add(t);
			}
		
			return r;
		}
		finally {
			reader.close();
		}
	}
}
