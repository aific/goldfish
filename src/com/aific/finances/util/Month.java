package com.aific.finances.util;

import java.util.Calendar;
import java.util.Date;


/**
 * A month
 */
public class Month extends TimePeriod {

	private int month;
	private int year;

	
	/**
	 * Create a new instance of {@link Month}
	 * 
	 * @param month the month
	 * @param year the year
	 */
	public Month(int month, int year)
	{
		this.month = month;
		this.year = year;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(year, month, c.getActualMinimum(Calendar.DAY_OF_MONTH), 0, 0, 0);
		first = c.getTime();
		
		c.set(year, month, c.getActualMaximum(Calendar.DAY_OF_MONTH),
				c.getActualMaximum(Calendar.HOUR_OF_DAY),
				c.getActualMaximum(Calendar.MINUTE),
				c.getActualMaximum(Calendar.SECOND));
		last = c.getTime();
	}
	
	
	/**
	 * Create a new instance of {@link Month}
	 * 
	 * @param date the date
	 */
	public Month(Date date)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		month = c.get(Calendar.MONTH);
		year  = c.get(Calendar.YEAR);
		
		c.clear();
		c.set(year, month, c.getActualMinimum(Calendar.DAY_OF_MONTH), 0, 0, 0);
		first = c.getTime();
		
		c.set(year, month, c.getActualMaximum(Calendar.DAY_OF_MONTH),
				c.getActualMaximum(Calendar.HOUR_OF_DAY),
				c.getActualMaximum(Calendar.MINUTE),
				c.getActualMaximum(Calendar.SECOND));
		last = c.getTime();
	}
	
	
	/**
	 * Get the next time period
	 * 
	 * @return the next time period
	 */
	@Override
	public Month getNext()
	{
		int m = month + 1;
		int y = year;
		if (m > Calendar.getInstance().getActualMaximum(Calendar.MONTH)) {
			m = Calendar.getInstance().getActualMinimum(Calendar.MONTH);
			y++;
		}
		return new Month(m, y);
	}


	/**
	 * Get the month
	 * 
	 * @return the month
	 */
	public int getMonth()
	{
		return month;
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
		return "" + (month + 1) + "/" + year;
	}
}
