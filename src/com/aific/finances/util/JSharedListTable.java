package com.aific.finances.util;


/**
 * A table in which the underlying list is shared
 * 
 * @author Peter Macko
 */
@SuppressWarnings("serial")
public class JSharedListTable<T> extends JBetterTable<T> {
	
	private Handler handler;
	
	
	/**
	 * Create an instance of the class
	 */
	public JSharedListTable()
	{
		data = new SharedArrayList<>();
		handler = new Handler();
		
	}
	
	
	/**
	 * Create an instance of the class
	 * 
	 * @param list the list
	 */
	public JSharedListTable(SharedList<T> list)
	{
		this();
		setSharedList(list);
	}
	
	
	/**
	 * Set the shared list
	 * 
	 * @param list the list
	 */
	public synchronized void setSharedList(SharedList<T> list)
	{
		if (isEditing()) getCellEditor().stopCellEditing();
		
		if (data != null && data instanceof SharedList) {
			((SharedList<?>) data).removeSharedListListener(handler);
		}
		
		if (data != null && data.size() > 0) {
			model.fireTableRowsDeleted(0, this.data.size() - 1);
		}
		
		data = list;
		
		if (data != null && data instanceof SharedList) {
			((SharedList<?>) data).addSharedListListener(handler);
		}

		if (data.size() > 0) {
			model.fireTableRowsInserted(0, data.size() - 1);
		}

		adjustColumns();		
		repaint();
	}


	/**
	 * The handler
	 */
	private class Handler implements SharedListListener {
		
		private ThreadLocal<Boolean> inEvent = new ThreadLocal<>();
		

		/**
		 * Element(s) added
		 * 
		 * @param list the shared list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void sharedListElementsAdded(SharedList<?> list, int from, int to) {
			
			if (list != data) return;
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			model.fireTableRowsInserted(from, to);
			model.fireTableDataChanged();
			repaint();
		}


		/**
		 * Element(s) removed
		 * 
		 * @param list the shared list that triggered this event
		 * @param from the from index
		 * @param to the to index (inclusive)
		 */
		public void sharedListElementsRemoved(SharedList<?> list, int from, int to) {
			
			if (list != data) return;
			
			if (isEditing()) getCellEditor().stopCellEditing();
			
			model.fireTableRowsDeleted(from, to);
			model.fireTableDataChanged();
			repaint();
		}
		

		/**
		 * Element(s) data changed
		 * 
		 * @param list the shared list that triggered this event
		 */
		public void sharedListDataChanged(SharedList<?> list) {
			
			if (list != data) return;
			if (inEvent.get() != null && inEvent.get().booleanValue()) return;
			
			try {
				inEvent.set(Boolean.TRUE);
				
				if (isEditing()) getCellEditor().stopCellEditing();
				
				model.fireTableDataChanged();
				repaint();
			}
			finally {
				inEvent.set(Boolean.FALSE);
			}
		}
	}
}
