package com.aific.finances.util;


/**
 * A numerical range
 */
public class DoubleRange {

	private double min;
	private double max;
	
	
	/**
	 * Create a new instance of {@link DoubleRange}
	 * @param min
	 * @param max
	 */
	public DoubleRange(double min, double max)
	{
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
	}


	/**
	 * Return the minimum
	 * 
	 * @return the minimum
	 */
	public double getMin()
	{
		return min;
	}


	/**
	 * Return the maximum 
	 * 
	 * @return the maximum
	 */
	public double getMax() 
	{
		return max;
	}
	
	
	/**
	 * Create a new range that is a superset of this range and the provided range
	 * 
	 * @param other the other range
	 */
	public DoubleRange superset(DoubleRange other)
	{
		return new DoubleRange(Math.min(min, other.min), Math.max(max, other.max));
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DoubleRange other = (DoubleRange) obj;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)) {
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DoubleRange [min=" + min + ", max=" + max + "]";
	}
}
