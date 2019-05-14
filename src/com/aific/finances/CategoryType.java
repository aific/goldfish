package com.aific.finances;


/**
 * Category type
 * 
 * @author Peter Macko
 */
public enum CategoryType {

	INCOME {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Income";
		}
	},
	
	EXPENSE {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Expense";
		}
	},
	
	BALANCED {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "Balanced";
		}
	},
	
	EXTERNAL {
		
		/**
		 * Get the account type string
		 * 
		 * @returns a human-readable string
		 */
		@Override
		public String toString() {
			return "External";
		}
	}
}
