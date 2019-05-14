package com.aific.finances.util;


/**
 * A property accessor
 * 
 * @author Peter Macko
 * 
 * @param <T> the object type
 * @param <V> the value type
 */
public interface Accessor<T, V> {

	/**
	 * Get the property value
	 * 
	 * @param object the object
	 * @return the value
	 */
	public V get(T object);

	/**
	 * Set the property value
	 * 
	 * @param object the object
	 * @param value the new value
	 */
	default public void set(T object, V value) { throw new UnsupportedOperationException(); }
}
