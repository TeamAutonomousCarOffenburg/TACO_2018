package hso.autonomy.tools.util.jogl.integration.inputController.impl;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import hso.autonomy.tools.util.jogl.view.UniversalCamera;
import hso.autonomy.tools.util.jogl.integration.GLWrapperJPanel;
import hso.autonomy.tools.util.jogl.integration.inputController.GLInputControllerAdapter;

/**
 * Default camera input controller implementation.
 *
 * @author Stefan Glaser
 */
public class GLCameraInputController extends GLInputControllerAdapter
{
	private Point lastPoint;

	protected GLWrapperJPanel glPanel;

	protected boolean enabled;

	public GLCameraInputController()
	{
		enabled = true;
	}

	@Override
	public void inputActivated(GLWrapperJPanel glPanel)
	{
		this.glPanel = glPanel;
		lastPoint = null;

		InputMap inputMap = this.glPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = this.glPanel.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("F1"), KeyPressedAction.F1_ACTION);
		inputMap.put(KeyStroke.getKeyStroke("F2"), KeyPressedAction.F2_ACTION);
		inputMap.put(KeyStroke.getKeyStroke("F3"), KeyPressedAction.F3_ACTION);

		actionMap.put(KeyPressedAction.F1_ACTION, new KeyPressedAction(KeyPressedAction.F1_ACTION));
		actionMap.put(KeyPressedAction.F2_ACTION, new KeyPressedAction(KeyPressedAction.F2_ACTION));
		actionMap.put(KeyPressedAction.F3_ACTION, new KeyPressedAction(KeyPressedAction.F3_ACTION));
	}

	@Override
	public void inputDeactivated()
	{
		this.glPanel = null;
	}

	public void enable()
	{
		enabled = true;
	}

	public void disable()
	{
		enabled = false;
		lastPoint = null;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (!enabled) {
			return;
		}

		UniversalCamera camera = glPanel.getCamera();
		float factor = 0.1f;

		if (e.isControlDown()) {
			factor *= 10;
		}

		if (camera.getMode() == UniversalCamera.CameraMode.Camera2D) {
			glPanel.zoomCameraFrame(e.getX(), e.getY(), -e.getWheelRotation() * factor);
		} else {
			camera.moveCameraLocal(0, -e.getWheelRotation() * factor, 0);
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (enabled) {
			if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
				lastPoint = new Point(e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (!enabled || lastPoint == null) {
			return;
		}

		UniversalCamera camera = glPanel.getCamera();

		if (camera.getMode() == UniversalCamera.CameraMode.Camera2D) {
			if (SwingUtilities.isRightMouseButton(e)) {
				glPanel.moveCameraFrame(lastPoint.x - e.getX(), lastPoint.y - e.getY());
			}
		} else {
			float factor = 1f;

			if (e.isControlDown()) {
				factor = 2;
			}

			if (SwingUtilities.isLeftMouseButton(e)) {
				double xDiffRatio = ((double) lastPoint.x - e.getX()) / glPanel.getHeight();
				double yDiffRatio = ((double) e.getY() - lastPoint.y) / glPanel.getHeight();

				camera.rotateHorizontal(xDiffRatio * 2 * Math.toRadians(camera.getFovY()));
				camera.rotateVertical(yDiffRatio * 2 * Math.toRadians(camera.getFovY()));
			}

			if (SwingUtilities.isRightMouseButton(e)) {
				int xDiff = lastPoint.x - e.getX();
				int yDiff = e.getY() - lastPoint.y;

				camera.moveCameraLocal(factor * xDiff / 200f, 0, factor * yDiff / 200f);
			}
		}

		lastPoint = new Point(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// if (enabled) {
		// if (SwingUtilities.isLeftMouseButton(e)
		// || SwingUtilities.isRightMouseButton(e)) {
		// lastPoint = null;
		// }
		// }
	}

	private class KeyPressedAction extends AbstractAction
	{
		public static final int F1_ACTION = KeyEvent.VK_F1;
		public static final int F2_ACTION = KeyEvent.VK_F2;
		public static final int F3_ACTION = KeyEvent.VK_F3;
		private int keyValue;

		public KeyPressedAction(int keyAction)
		{
			keyValue = keyAction;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			switch (this.keyValue) {
			case F1_ACTION:
				UniversalCamera camera = glPanel.getCamera();
				if (camera.getMode() == UniversalCamera.CameraMode.Camera2D) {
					camera.setMode(UniversalCamera.CameraMode.Camera3D);
				} else {
					camera.setMode(UniversalCamera.CameraMode.Camera2D);
				}
				glPanel.repaint();
				break;
			case F2_ACTION:
				glPanel.getCamera().applyDefaultPose();
				glPanel.repaint();
				break;
			case F3_ACTION:
				enabled = !enabled;
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_F3) {
			enabled = !enabled;
			return;
		}

		if (enabled) {
			if (e.getKeyCode() == KeyEvent.VK_F1) {
				UniversalCamera camera = glPanel.getCamera();
				if (camera.getMode() == UniversalCamera.CameraMode.Camera2D) {
					camera.setMode(UniversalCamera.CameraMode.Camera3D);
				} else {
					camera.setMode(UniversalCamera.CameraMode.Camera2D);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_F2) {
				glPanel.getCamera().applyDefaultPose();
			}
		}
	}
}
