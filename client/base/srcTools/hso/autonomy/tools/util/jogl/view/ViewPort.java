package hso.autonomy.tools.util.jogl.view;

import javax.media.opengl.GL2;

/**
 * Basic ViewPort abstraction.
 *
 * @author Stefan Glaser
 */
public class ViewPort
{
	private int x;

	private int y;

	private int width;

	private int height;

	public ViewPort()
	{
		x = 0;
		y = 0;
		width = 10;
		height = 10;
	}

	public void applyTo(GL2 gl)
	{
		gl.glViewport(x, y, width, height);
	}

	public void update(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width > 10 ? width : 10;
		this.height = height > 10 ? height : 10;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}
}
