package com.aific.finances.util;


/**
 * A date unit
 */
public enum DateUnit {

	DAY    ("d", "day"  , "days"  , "daily"  , 1),
	WEEK   ("w", "week" , "weeks" , "weekly" , 7),
	MONTH  ("m", "month", "months", "monthly", 365/12.0),
	YEAR   ("y", "year" , "years" , "yearly" , 365);
	
	
	private String code;
	private String name;
	private String plural;
	private String adverb;
	private double days;
	
	
	/**
	 * Create a new instance of {@link DateUnit}
	 * 
	 * @param code the code
	 * @param name the name
	 * @param plural the plural
	 * @param adverb the adverb
	 * @param days the number of days
	 */
	private DateUnit(String code, String name, String plural, String adverb, double days) {
		this.code = code;
		this.name = name;
		this.plural = plural;
		this.adverb = adverb;
		this.days = days;
	}
	
	
	/**
	 * Get the code
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	
	/**
	 * Get the unit from the code
	 * 
	 * @param s the string code
	 * @return the unit
	 */
	public static DateUnit fromCode(String s) {
		for (DateUnit u : DateUnit.values()) {
			if (u.getCode().equals(s)) return u;
		}
		throw new IllegalArgumentException("No such DateUnit: " + s);
	}
	
	
	/**
	 * Get the human-readable name of the unit in the singular form
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Get the human-readable name of the unit in the plural form
	 * 
	 * @return the name
	 */
	public String getPlural() {
		return plural;
	}
	
	
	/**
	 * Format the name for the given value
	 * 
	 * @param value the value
	 * @return the human-readable name
	 */
	public String format(int value) {
		
		return "" + value + " " + (Math.abs(value) == 1 ? name : plural);
	}
	
	
	/**
	 * Format the name for the given value
	 * 
	 * @param value the value
	 * @param includeNo1 true to include "1" in the string if the value is 1
	 * @return the human-readable name
	 */
	public String format(int value, boolean includeNo1) {
		
		if (value == 1 && !includeNo1) return name;
		return "" + value + " " + (Math.abs(value) == 1 ? name : plural);
	}
	
	
	/**
	 * Format the name for the given value
	 * 
	 * @param value the value
	 * @param useAdverb true to use adverb if appropriate
	 * @param includeNo1 true to include "1" in the string if the value is 1
	 * @return the human-readable name
	 */
	public String getAdverbFor(int value, boolean useAdverb, boolean includeNo1) {
		
		if (value == 1 && useAdverb) return adverb;
		return "every" + (!includeNo1 && value == 1 ? "" : " " + value) + " " + (Math.abs(value) == 1 ? name : plural);
	}
	
	
	/**
	 * Format the name for the given value
	 * 
	 * @param value the value
	 * @return the human-readable name
	 */
	public String format(double value) {
		
		if (Math.abs(Math.abs(value) - 1) < 1e-10) {
			return "1 " + name;
		}
		else {
			return "" + value + " " + plural;
		}
	}
	
	
	/**
	 * Get the average number of days 
	 * 
	 * @return the number of days
	 */
	public double getDays() {
		return days;
	}
	
	
	/**
	 * Convert the value to the number of days
	 * 
	 * @param value the value
	 * @return the number of days
	 */
	public double toDays(double value) {
		return days * value;
	}
	
	
	/**
	 * Convert the value from the number of days
	 * 
	 * @param value the value
	 * @return the value in this unit
	 */
	public double fromDays(double value) {
		return value / days;
	}
}
