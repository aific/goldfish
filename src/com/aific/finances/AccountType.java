package com.aific.finances;


/**
 * Account type
 * 
 * @author Peter Macko
 */
public enum AccountType {

	CHECKING_ACCOUNT {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Checking Account";
		}
	},
	
	SAVINGS_ACCOUNT {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Savings Account";
		}
	},
	
	CREDIT_CARD {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Credit Card";
		}
	}
}
