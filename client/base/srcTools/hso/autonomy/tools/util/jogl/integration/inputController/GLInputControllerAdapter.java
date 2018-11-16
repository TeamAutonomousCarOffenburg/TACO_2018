package hso.autonomy.tools.util.jogl.integration.inputController;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import hso.autonomy.tools.util.jogl.integration.GLWrapperJPanel;

/**
 * An abstract adapter class for receiving input events. The methods in this
 * class are empty. This class exists as convenience for creating listener
 * objects.
 *
 * @author Stefan Glaser
 */
public abstract class GLInputControllerAdapter implements IGLInputController
{
	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
	}

	@Override
	public void inputActivated(GLWrapperJPanel glPanel)
	{
	}

	@Override
	public void inputDeactivated()
	{
	}
}
