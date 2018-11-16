package hso.autonomy.tools.developer.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class ColorIcon implements Icon
{
	private final int width;

	private final int height;

	private final Color color;

	public ColorIcon(int width, int height, Color color)
	{
		this.width = width;
		this.height = height;
		this.color = color;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D) g.create();

		g2d.setColor(color);
		g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

		g2d.dispose();
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}
}
