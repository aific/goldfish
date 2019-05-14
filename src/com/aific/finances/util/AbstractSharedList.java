package com.aific.finances.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * A list that can be shared among multiple components
 */
public abstract class AbstractSharedList<E> extends AbstractList<E> implements SharedList<E> {
	
	private LinkedList<SharedListListener> listeners = null;
	
	
	/**
	 * Create a new instance of the list
	 */
	public AbstractSharedList()
	{
		// Nothing to do
	}
	

	/**
	 * Add a listener
	 * 
	 * @param listener the listener
	 */
	@Override
	public synchronized void addSharedListListener(SharedListListener listener)
	{
		if (listeners == null) listeners = new LinkedList<>();
		listeners.add(listener);
	}

	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener
	 */
	@Override
	public synchronized void removeSharedListListener(SharedListListener listener)
	{
		if (listeners != null) {
			Iterator<SharedListListener> i = listeners.iterator();
			while (i.hasNext()) {
				SharedListListener l = i.next();
				if (l == null || l == listener) i.remove();
			}
		}
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireSharedListElementsAdded(int from, int to)
	{
		sharedListChanged();
		if (listeners != null) {
			for (SharedListListener r : listeners) {
				SharedListListener l = r;
				if (l != null) l.sharedListElementsAdded(this, from, to);
			}
		}
	}
	
	
	/**
	 * Fire a listener
	 * 
	 * @param from the from index
	 * @param to the to index (inclusive)
	 */
	protected synchronized void fireSharedListElementsRemoved(int from, int to)
	{
		sharedListChanged();
		if (listeners != null) {
			for (SharedListListener r : listeners) {
				SharedListListener l = r;
				if (l != null) l.sharedListElementsRemoved(this, from, to);
			}
		}
	}
	
	
	/**
	 * Fire a listener
	 */
	protected synchronized void fireSharedListDataChanged()
	{
		sharedListChanged();
		if (listeners != null) {
			for (SharedListListener r : listeners) {
				SharedListListener l = r;
				if (l != null) l.sharedListDataChanged(this);
			}
		}
	}
	
	
	/**
	 * Fire the change event for data
	 */
	@Override
	public void fireDataChanged()
	{
		fireSharedListDataChanged();
	}

	
	/**
	 * Callback for when something in the shared list has changed
	 */
	protected void sharedListChanged()
	{
		// Nothing to do
	}
}
