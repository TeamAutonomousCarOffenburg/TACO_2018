package hso.autonomy.tools.util.jogl.integration.inputController;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import hso.autonomy.tools.util.jogl.integration.GLWrapperJPanel;

public interface IGLInputController extends KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * Called to notify the input controller that it got activated and will
	 * receive input notifications from now on.
	 *
	 * @param glPanel - the glPanel the input controller listens to
	 */
	void inputActivated(GLWrapperJPanel glPanel);

	/**
	 * Called to notify the input controller that it got deactivated and will not
	 * receive any further input notifications (until it may get activated
	 * again).
	 */
	void inputDeactivated();
}
