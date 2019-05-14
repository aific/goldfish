package com.aific.finances.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.aific.finances.plot.ChartSeries;


/**
 * Cell renderer
 */
public class SeriesNameRenderer implements TableCellRenderer {
	
	private JPanel panel;
	private LegendIcon legendIcon;
	private JLabel label;
	

	/**
	 * Create an instance of the cell renderer
	 * 
	 * @param type the legend type
	 */
	public SeriesNameRenderer(LegendType type) {
		
		legendIcon = new LegendIcon(type);
		legendIcon.setOpaque(false);
		
		label = new JLabel("");
		label.setOpaque(false);
		
		panel = new JPanel(new BorderLayout(1, 0));
		panel.add(legendIcon, BorderLayout.WEST);
		panel.add(label, BorderLayout.CENTER);
	}
	
	
	/**
	 * Set the type of the legend
	 * 
	 * @param type the type of the legend
	 */
	public void setLegendType(LegendType type)
	{
		legendIcon.setLegendType(type);
	}

	
	/**
	 * Initialize a cell renderer
	 *
	 * @param table the table
	 * @param object the edited object
	 * @param isSelected whether the current row is selected
	 * @param hasFocus whether the cell has focus
	 * @param row the row number
	 * @param column the column number
	 * @return the cell renderer
	 */
	public Component getTableCellRendererComponent(JTable table, Object object,
												   boolean isSelected, boolean hasFocus,
												   int row, int column) {
		ChartSeries series = (ChartSeries) object;
		
		panel.setForeground(table.getForeground());
		panel.setBackground(table.getBackground());
		
		legendIcon.color = series == null ? table.getForeground() : series.getColor();
		
		label.setText(series == null ? "(null)" : series.getName());
		label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		
		return panel;
	}
}
