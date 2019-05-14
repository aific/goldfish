package com.aific.finances.util;


/**
 * The frequency
 */
public final class DateFrequencyUnit implements Comparable<DateFrequencyUnit> {
	
	public static final DateFrequencyUnit YEARLY = new DateFrequencyUnit(1, DateUnit.YEAR);

	private int count;
	private DateUnit unit;
	
	
	/**
	 * Create a new instance of {@link DateFrequencyUnit}
	 * 
	 * @param count the count
	 * @param unit the unit
	 */
	public DateFrequencyUnit(int count, DateUnit unit)
	{
		this.count = count;
		this.unit = unit;
	}


	/**
	 * Get the frequency number
	 * 
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}


	/**
	 * Get the frequency unit
	 * 
	 * @return the unit
	 */
	public DateUnit getUnit()
	{
		return unit;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DateFrequencyUnit other = (DateFrequencyUnit) obj;
		if (count != other.count) {
			return false;
		}
		if (unit != other.unit) {
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return unit.getAdverbFor(count, false, false);
	}
	
	
	/**
	 * To an XML-friendly string value
	 * 
	 * @return the value for XML output
	 */
	public String getValueForXml() {
		return "" + count + unit.getCode();
	}
	
	
	/**
	 * Get the number of days
	 * 
	 * @return the number of days
	 */
	public double getDays()
	{
		return unit.toDays(count);
	}
	
	
	/**
	 * Convert the value to this unit
	 * 
	 * @param value
	 * @param unit the unit of the value
	 */
	public double convertFrom(double value, DateFrequencyUnit unit)
	{
		return value * getDays() / unit.getDays();
	}
	
	
	/**
	 * Parse from an XML-friendly string value
	 * 
	 * @param s the string value
	 * @return the value
	 */
	public static DateFrequencyUnit parseXmlValue(String s) {
		
		String n = null;
		String u = null;
		
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) {
				n = s.substring(0, i);
				u = s.substring(i);
				break;
			}
		}
		
		if (n == null || u == null) {
			throw new IllegalArgumentException();
		}
		
		while (n.startsWith(" ")) n = n.substring(1);
		while (u.startsWith(" ")) u = u.substring(1);
		
		if (n.isEmpty() || u.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		int count = Integer.parseInt(n);
		DateUnit unit = DateUnit.fromCode(u);
		
		return new DateFrequencyUnit(count, unit);
	}


	/**
	 * Compare to another frequency unit
	 * 
	 * @param other the other object
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(DateFrequencyUnit other) {
		
		double a = unit.toDays(count);
		double b = other.unit.toDays(other.count);
		
		if (Math.abs(a - b) < 1e-7) return 0;
		return a < b ? -1 : 1;
	}
}
