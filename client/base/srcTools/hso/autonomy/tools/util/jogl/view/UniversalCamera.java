package hso.autonomy.tools.util.jogl.view;

import java.awt.Point;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.tools.util.jogl.integration.GLWrapperJPanel;

/**
 * Universal 2D and 3D camera implementation. This class abstracts a general
 * camera with a position and orientation (just horizontal and vertical
 * rotation) in the space, a field of view and a near and far clipping plane. It
 * provides two camera modes, a 2D and 3D mode.
 *
 * @author Stefan Glaser
 */
public class UniversalCamera
{
	public enum CameraMode { Camera2D, Camera3D }

	protected CameraMode mode;

	protected float fovY;

	protected float near;

	protected float far;

	protected Vector3D position;

	protected Vector3D defaultPosition;

	protected Angle horizontalAngle;

	protected Angle verticalAngle;

	protected Rotation orientation;

	public UniversalCamera(CameraMode mode)
	{
		this(mode, 45f, 0.1f, 100f);
	}

	public UniversalCamera(CameraMode mode, float fovy)
	{
		this(mode, fovy, 0.1f, 100f);
	}

	public UniversalCamera(CameraMode mode, float fovY, float near, float far)
	{
		this(mode, fovY, near, far, Vector3D.PLUS_K, Angle.ZERO, Angle.ZERO);
	}

	public UniversalCamera(CameraMode mode, float fovY, float near, float far, Vector3D position, Angle horizontalAngle,
			Angle verticalAngle)
	{
		this.mode = CameraMode.Camera3D;
		setMode(mode);

		this.fovY = fovY;
		this.near = near;
		this.far = far;

		this.position = Vector3D.PLUS_K;
		this.horizontalAngle = Angle.ZERO;
		this.verticalAngle = Angle.ZERO;

		setPosition(position);
		setHorizontalAngle(horizontalAngle);
		setVerticalAngle(verticalAngle);

		defaultPosition = position;
	}

	public void applyTo(GL2 gl, GLU glu, GLWrapperJPanel glPanel)
	{
		float viewportRatio = (float) glPanel.getViewPort().getWidth() / (float) glPanel.getViewPort().getHeight();

		// Change to projection matrix and apply camera perspective
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		if (mode == CameraMode.Camera3D) {
			glu.gluPerspective(fovY, viewportRatio, near, far);
			gl.glEnable(GL2.GL_DEPTH_TEST);
		} else {
			// In the 2D case, we interpret the eye x-, y-position as camera
			// position and the eye z-position as distance to the scene with a 90
			// degrees field of view in the horizontal plane
			float x = (float) position.getX();
			float y = (float) position.getY();
			float z = (float) position.getZ() / 2;
			float left = x - z * viewportRatio;
			float right = x + z * viewportRatio;
			float bottom = y - z;
			float top = y + z;

			gl.glOrthof(left, right, bottom, top, -1, 1);
			gl.glDisable(GL2.GL_DEPTH_TEST);
		}

		// Change back to model view matrix and apply camera pose
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		if (mode == CameraMode.Camera3D) {
			Vector3D forward = orientation.applyTo(Vector3D.PLUS_J);
			Vector3D up = orientation.applyTo(Vector3D.PLUS_K);
			float eyeX = (float) position.getX();
			float eyeY = (float) position.getY();
			float eyeZ = (float) position.getZ();
			float centerX = (float) (position.getX() + forward.getX());
			float centerY = (float) (position.getY() + forward.getY());
			float centerZ = (float) (position.getZ() + forward.getZ());
			float upX = (float) up.getX();
			float upY = (float) up.getY();
			float upZ = (float) up.getZ();

			glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
		}
	}

	public void setMode(CameraMode mode)
	{
		if (this.mode != mode) {
			this.mode = mode;

			// Change camera parameters to match current camera mode
			if (mode == CameraMode.Camera2D) {
				horizontalAngle = Angle.ZERO;
				verticalAngle = Angle.deg(-90);
				updateOrientation();
				setPosition(position);
			}
		}
	}

	public CameraMode getMode()
	{
		return mode;
	}

	public void setFovY(float fovY)
	{
		this.fovY = fovY;
	}

	public float getFovY()
	{
		return fovY;
	}

	public void setNear(float near)
	{
		this.near = near;
	}

	public float getNear()
	{
		return near;
	}

	public void setFar(float far)
	{
		this.far = far;
	}

	public float getFar()
	{
		return far;
	}

	public Vector3D getPosition()
	{
		return position;
	}

	public void setDefaultPosition(Vector3D position)
	{
		this.defaultPosition = position;
	}

	public void setPosition(Vector3D position)
	{
		if (position == null) {
			return;
		}

		if (mode == CameraMode.Camera2D && position.getZ() < 0.001) {
			this.position = new Vector3D(position.getX(), position.getY(), 0.001);
			return;
		}

		this.position = position;
	}

	public Angle getHorizontalAngle()
	{
		return horizontalAngle;
	}

	public void setHorizontalAngle(Angle angle)
	{
		if (mode == CameraMode.Camera2D) {
			return;
		}

		this.horizontalAngle = angle;

		updateOrientation();
	}

	public Angle getVerticalAngle()
	{
		return verticalAngle;
	}

	public void setVerticalAngle(Angle angle)
	{
		if (mode == CameraMode.Camera2D) {
			return;
		}

		this.verticalAngle = angle;

		if (verticalAngle.degrees() < -90) {
			verticalAngle = Angle.deg(-90);
		} else if (verticalAngle.degrees() > 90) {
			verticalAngle = Angle.deg(90);
		}

		updateOrientation();
	}

	private void updateOrientation()
	{
		orientation = new Rotation(Vector3D.PLUS_K, horizontalAngle.radians())
							  .applyTo(new Rotation(Vector3D.PLUS_I, verticalAngle.radians()));
	}

	public Vector3D getRay(int x, int y, ViewPort viewPort)
	{
		if (mode == CameraMode.Camera2D) {
			int halfViewportWidth = viewPort.getWidth() / 2;
			int halfViewportHeight = viewPort.getHeight() / 2;

			double meterPerPixel = position.getZ() / (2 * halfViewportHeight);

			return new Vector3D(												//
					position.getX() + (x - halfViewportWidth) * meterPerPixel,  //
					position.getY() + (halfViewportHeight - y) * meterPerPixel, //
					0);
		} else {
			int halfViewportWidth = viewPort.getWidth() / 2;
			int halfViewportHeight = viewPort.getHeight() / 2;

			double radPerPixel = Math.toRadians(fovY) / viewPort.getHeight();
			double horizontalRayAngle = (halfViewportWidth - x) * radPerPixel;
			double verticalRayAngle = (halfViewportHeight - y) * radPerPixel;

			Rotation rot = orientation.applyTo(new Rotation(Vector3D.PLUS_K, horizontalRayAngle)
													   .applyTo(new Rotation(Vector3D.PLUS_I, verticalRayAngle)));

			Vector3D ray = rot.applyTo(Vector3D.PLUS_J);
			double factor = -position.getZ() / ray.getZ();
			if (factor < 0 || factor > far) {
				factor = far;
			}

			return position.add(ray.scalarMultiply(factor));
		}
	}

	public Point getPixel(Vector3D ray, ViewPort viewPort)
	{
		if (mode == CameraMode.Camera2D) {
			int halfViewportWidth = viewPort.getWidth() / 2;
			int halfViewportHeight = viewPort.getHeight() / 2;

			double pixelPerMeter = (2 * halfViewportHeight) / position.getZ();

			Point ret = new Point();
			ret.x = halfViewportWidth + (int) ((ray.getX() - position.getX()) * pixelPerMeter);
			ret.y = halfViewportHeight - (int) ((ray.getY() - position.getY()) * pixelPerMeter);

			return ret;
		} else {
			// TODO: Calculate pixel based on ray
			return new Point();
		}
	}

	public void zoom(float xPos, float yPos, float zoomFactor)
	{
		// zoom is only available in 2D mode
		if (mode != CameraMode.Camera2D) {
			return;
		}

		float factor;
		float positionFactor;

		if (zoomFactor == 0) {
			// Keep current zoom level
			return;
		} else if (zoomFactor < 0) {
			// Zoom out (--> factor > 1)
			factor = 1 - zoomFactor;
			positionFactor = zoomFactor;
		} else {
			// Zoom in (--> factor < 1)
			// Limit zoom to 1mm distance
			if (position.getZ() <= 0.001) {
				return;
			}
			factor = 1 / (1 + zoomFactor);
			positionFactor = 1 - factor;
		}

		Vector3D movementVector = new Vector3D(			 //
				position.getX() + xPos * positionFactor, //
				position.getY() + yPos * positionFactor, //
				position.getZ() * factor);
		setPosition(movementVector);
	}

	public void applyDefaultPose()
	{
		// Change camera parameters to match current camera mode
		horizontalAngle = Angle.ZERO;
		if (mode == CameraMode.Camera2D) {
			verticalAngle = Angle.deg(-90);
		} else {
			verticalAngle = Angle.ZERO;
		}

		updateOrientation();
		setPosition(defaultPosition);
	}

	public void moveCameraLocal(float x, float y, float z)
	{
		setPosition(position.add(orientation.applyTo(new Vector3D(x, y, z))));
	}

	public void move(double x, double y)
	{
		setPosition(position.add(new Vector3D(x, y, 0)));
	}

	public void move(float x, float y, float z)
	{
		setPosition(position.add(new Vector3D(x, y, z)));
	}

	public void move(Vector3D movementVector)
	{
		setPosition(position.add(movementVector));
	}

	public void rotateHorizontal(double angle)
	{
		if (mode == CameraMode.Camera2D) {
			return;
		}

		setHorizontalAngle(horizontalAngle.add(angle));
	}

	public void rotateVertical(double angle)
	{
		if (mode == CameraMode.Camera2D) {
			return;
		}

		setVerticalAngle(verticalAngle.add(-angle));
	}
}
