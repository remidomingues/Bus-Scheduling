/**
 * File : GradientProgressBar.java
 *
 * Created on May 17, 2012, 2:15:41 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package presentation;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JProgressBar;

/**
 * Progress bar painted with a gradient
 */
public class GradientProgressBar extends JProgressBar
{
	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** Gradient end color */
	private Color gradientEnd = Color.WHITE;

	/**
	 * Constructor
	 * @param gradientEnd Gradient end color
	 */
	public GradientProgressBar(Color gradientEnd)
	{
		this.gradientEnd = gradientEnd;
	}

	/**
	 * Update the gradientEnd value
	 * @param gradientEnd The gradientEnd value
	 */
	public void setGradientEnd(Color gradientEnd)
	{
		this.gradientEnd = gradientEnd;
	}

	/**
	 * @param g 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		if (!isOpaque())
		{
			super.paintComponent(g);
			return;
		}
		Color color1 = getBackground().darker();
		Color color2 = gradientEnd;
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		GradientPaint gp = new GradientPaint(0, 0, color1, width, 0, color2);

		g2d.setPaint(gp);
		g2d.fillRect(0, 0, width, height);

		setOpaque(false);
		super.paintComponent(g);
		setOpaque(true);
	}
}
