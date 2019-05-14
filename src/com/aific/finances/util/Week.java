package com.aific.finances.util;

import java.util.Calendar;
import java.util.Date;


/**
 * A week
 */
public class Week extends TimePeriod {

	private int week;
	private int year;

	
	/**
	 * Create a new instance of {@link Week}
	 * 
	 * @param week the month
	 * @param year the year
	 */
	public Week(int week, int year)
	{
		this.week = week;
		this.year = year;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.WEEK_OF_YEAR, week);
		first = c.getTime();

		c.add(Calendar.WEEK_OF_YEAR, 1);
		c.add(Calendar.MILLISECOND, -1);
		last = c.getTime();
	}
	
	
	/**
	 * Create a new instance of {@link Week}
	 * 
	 * @param date the date
	 */
	public Week(Date date)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		week = c.get(Calendar.WEEK_OF_YEAR);
		year = c.get(Calendar.YEAR);
		
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.WEEK_OF_YEAR, week);
		first = c.getTime();

		c.add(Calendar.WEEK_OF_YEAR, 1);
		c.add(Calendar.MILLISECOND, -1);
		last = c.getTime();
	}
	
	
	/**
	 * Get the next time period
	 * 
	 * @return the next time period
	 */
	@Override
	public Week getNext()
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.WEEK_OF_YEAR, week);
		c.add(Calendar.WEEK_OF_YEAR, 1);
		return new Week(c.getTime());
	}


	/**
	 * Get the week of the year
	 * 
	 * @return the week of the year
	 */
	public int getWeekOfYear()
	{
		return week;
	}


	/**
	 * Get the year
	 * 
	 * @return the year
	 */
	public int getYear()
	{
		return year;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Use the first day of week instead?
		return "" + (week + 1) + "/" + year;
	}
}
