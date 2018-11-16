/*******************************************************************************
 * Copyright 2008, 2012 Hochschule Offenburg
 * Klaus Dorer, Mathias Ehret, Stefan Glaser, Thomas Huber, Fabian Korak,
 * Simon Raffeiner, Srinivasa Ragavan, Thomas Rinklin,
 * Joachim Schilling, Ingo Schindler, Rajit Shahi, Bjoern Weiler
 *
 * This file is part of magmaOffenburg.
 *
 * magmaOffenburg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * magmaOffenburg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with magmaOffenburg. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package hso.autonomy.tools.developer.util.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class TitleLabel extends JLabel
{
	public TitleLabel(String title, Icon icon)
	{
		this(title, icon, 16);
	}

	private TitleLabel(String title, Icon icon, int fontSize)
	{
		super(title, icon, SwingConstants.LEFT);
		setFont(new Font(Font.DIALOG, Font.BOLD, fontSize));
		setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
		setIconTextGap(6);
	}

	Color factorizeColor(Color c, double factor)
	{
		int r = Math.min(Math.max((int) (c.getRed() * factor), 0), 255);
		int g = Math.min(Math.max((int) (c.getGreen() * factor), 0), 255);
		int b = Math.min(Math.max((int) (c.getBlue() * factor), 0), 255);
		return new Color(r, g, b);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		double strength = 0.15;
		int height = this.getHeight();
		int width = this.getWidth();
		Color bgColor = getBackground();
		Color startColor = factorizeColor(bgColor, 1.0 + strength);
		Color endColor = factorizeColor(bgColor, 1.0 - strength);

		g2d.setPaint(new GradientPaint(0, 0, startColor, 0, height, endColor));
		g2d.fillRect(0, 0, width, height);

		g2d.setPaint(endColor.darker());
		g2d.drawLine(0, height - 1, width, height - 1);

		super.paintComponent(g);
	}
}
