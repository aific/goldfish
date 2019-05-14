package com.aific.finances.util;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A shared array list
 */
public class SharedArrayList<E> extends AbstractSharedList<E> {

	private ArrayList<E> data;
	
	
	/**
	 * Create a new array list
	 */
	public SharedArrayList()
	{
		data = new ArrayList<>();
	}


	/**
	 * Add an element if it is not already there
	 *
	 * @param element the element to add
	 * @return true if the element was actually added
	 */
	@Override
	public synchronized boolean add(E element) {
		
		int n = data.size();
		
		if (data.add(element)) {
			fireSharedListElementsAdded(n, n);
			return true;
		}
		else {
			return false;
		}
	}


	/**
	 * Add a collection of elements
	 *
	 * @param elements the elements to add
	 * @return true if any elements were added
	 */
	@Override
	public synchronized boolean addAll(Collection<? extends E> elements) {
		
		int n = data.size();
		
		if (data.addAll(elements)) {
			fireSharedListElementsAdded(n, data.size()-1);
			return true;
		}
		else {
			return false;
		}
	}

	
	/**
	 * Get an element
	 * 
	 * @param index the index
	 * @return the element
	 */
	@Override
	public E get(int index)
	{
		return data.get(index);
	}

	
	/**
	 * Get the size of the list
	 * 
	 * @return the size
	 */
	@Override
	public int size()
	{
		return data.size();
	}
}
