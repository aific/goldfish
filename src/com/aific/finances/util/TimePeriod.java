package com.aific.finances.util;

import java.util.Date;


/**
 * A time period
 */
public abstract class TimePeriod implements Comparable<TimePeriod> {

	protected Date first;
	protected Date last;	// inclusive
	
	
	/**
	 * Create a new instance of the class. The overriding instances must
	 * initialize the instance variables
	 */
	protected TimePeriod()
	{
	}
	
	
	/**
	 * Get the next time period
	 * 
	 * @return the next time period
	 */
	public abstract TimePeriod getNext();
	
	
	/**
	 * Add
	 * 
	 * @param n the number of periods to add
	 * @return the resulting time period
	 */
	public TimePeriod add(int n) {
		if (n < 0) throw new IllegalArgumentException();
		TimePeriod p = this;
		for (int i = 0; i < n; i++) {
			p = p.getNext();
		}
		return p;
	}
	
	
	/**
	 * Determine if the given date falls within this date range
	 * 
	 * @param date the date
	 * @return true if it falls into this date range
	 */
	public boolean contains(Date date)
	{
		return date.compareTo(first) >= 0
				&& date.compareTo(last) <= 0;
	}
	
	
	/**
	 * Determine if the given date falls within this date range until the end
	 * of the other range
	 * 
	 * @param end the final time period (inclusive)
	 * @param date the date
	 * @return true if it falls into this date range
	 */
	public boolean containsUntil(TimePeriod end, Date date)
	{
		return date.compareTo(first) >= 0
				&& date.compareTo(end.last) <= 0;
	}
	
	
	/**
	 * Count how many time periods are in between
	 * 
	 * @param end the final time period (inclusive)
	 * @return the count
	 */
	public int countUntil(TimePeriod end)
	{
		int count = 0;
		for (TimePeriod p = this; p.compareTo(end) <= 0; p = p.getNext()) {
			count++;
		}
		return count;
	}
	
	
	/**
	 * Compare to another instance of {@link TimePeriod}
	 * 
	 * @param other the other time period
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(TimePeriod other)
	{
		int r = first.compareTo(other.first);
		if (r != 0) return r;
		
		r = last.compareTo(other.last);
		return -r;	// Longer durations go first
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((last == null) ? 0 : last.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimePeriod other = (TimePeriod) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (last == null) {
			if (other.last != null) {
				return false;
			}
		} else if (!last.equals(other.last)) {
			return false;
		}
		return true;
	}
}
