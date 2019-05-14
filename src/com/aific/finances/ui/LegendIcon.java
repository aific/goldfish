package com.aific.finances.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;


/**
 * The legend icon
 */
@SuppressWarnings("serial")
public class LegendIcon extends JPanel {
	
	private LegendType legendType;

	public int horizontalMargin;
	public int verticalMargin;
	public Color color;
	
	
	/**
	 * Create an instance of this class
	 * 
	 * @param type the legend type
	 */
	public LegendIcon(LegendType type) {
		
		this.legendType = type;
		
		horizontalMargin = 2;
		verticalMargin = 2;
		
		Dimension s = getPreferredSize();
		s.width = 24;
		
		setMinimumSize(s);
		setPreferredSize(s);
		setMaximumSize(s);
		
		color = getForeground();
	}
	
	
	/**
	 * Set the type of the legend
	 * 
	 * @param type the type of the legend
	 */
	public void setLegendType(LegendType type)
	{
		this.legendType = type;
	}

	
	/**
	 * Paint
	 * 
	 * @param g the graphics context
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		int w = getWidth();
		int h = getHeight();
		
		g.setColor(color);
		
		if (legendType != null) {
			switch (legendType) {
			case LINE:
				g.drawLine(horizontalMargin, h/2, w-horizontalMargin-1, h/2);
				break;
			case BAR:
				g.fillRect(horizontalMargin, verticalMargin,
						w-horizontalMargin-verticalMargin-1, h-2*verticalMargin);
				break;
			}
		}
	}
}
