package taco.agent.model.agentmodel.impl;

import hso.autonomy.util.geometry.Polygon;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.agent.communication.perception.IPerception;
import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.VectorUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import taco.agent.model.agentmeta.impl.CameraConfiguration;
import taco.agent.model.agentmodel.ICameraSensor;
import taco.agent.model.worldmodel.lanedetection.LaneMiddle;

public class CameraSensor extends AudiCupSensor implements ICameraSensor
{
	private float focalPointX;

	private float focalPointY;

	private float focalHorizontal;

	private float focalVertical;

	// TODO: move to audicupconfig->cameraConfig
	private static double[][] HOMOGRAPHY_MATRIX_FAR =
			new double[][] {{1.184270722718884, -0.101937738460910, -678.865306464623359},
					{-0.029241173627513, -1.707900161085749, 21.416498135870860},
					{-0.000011070624513, -0.005343552433654, 1.000000000000000}};

	private static double[][] HOMOGRAPHY_MATRIX_NEAR =
			new double[][] {{1.167158838893850, -0.006364007901108, -688.340967024627616},
					{-0.030166688107650, -1.645338407658838, -1.192952587855534},
					{-0.000029334781990, -0.005340181990620, 1.000000000000000}};

	// this represents the area in which the vision can detect obstacles
	private final Polygon visibleArea;

	private RealMatrix homographyMatrixNear;

	private RealMatrix homographyMatrixFar;

	private Vector2D focalPoint;

	public CameraSensor(CameraConfiguration config)
	{
		super(config.getName(), config.getPose());
		focalPointX = config.getFocalPointX();
		focalPointY = config.getFocalPointY();
		focalHorizontal = config.getFocalLengthX();
		focalVertical = config.getFocalLengthY();
		focalPoint = new Vector2D(focalPointX, focalPointY);
		homographyMatrixNear = MatrixUtils.createRealMatrix(HOMOGRAPHY_MATRIX_NEAR);
		homographyMatrixFar = MatrixUtils.createRealMatrix(HOMOGRAPHY_MATRIX_FAR);

		Vector2D bottomLeft = new Vector2D(0.5, 1);
		Vector2D bottomRight = new Vector2D(0.5, -0.6);
		Vector2D topRight = new Vector2D(4.5, -1);
		Vector2D topLeft = new Vector2D(4.5, 1.4);
		visibleArea = new Polygon(bottomLeft, bottomRight, topRight, topLeft);
	}

	@Override
	public void updateFromPerception(IPerception perception)
	{
		// for now we do not get camera perceptions in client
	}

	@Override
	public Vector3D pixelToCar(Vector2D position)
	{
		if (position.getY() <= focalPointY) {
			// this is a point above the horizon
			return null;
		}

		// Calculate pixel coordinates relative to the center of the image
		double mx = position.getX() - focalPointX;
		double my = position.getY() - focalPointY;

		// Calculate direction vector from relative pixel coordinates and focal-length
		Vector3D dir = new Vector3D(1, -mx / focalHorizontal, -my / focalVertical);

		// for now we assume the camera is not rotated

		// Scale direction vector such that it touches the ground
		Vector3D cameraPosition = dir.scalarMultiply(pose.getZ() / Math.abs(dir.getZ()));

		return pose.applyTo(cameraPosition);
	}

	@Override
	public Vector2D carToPixel(Vector2D position)
	{
		return carToPixel(VectorUtils.to3D(position));
	}

	@Override
	public Vector2D carToPixel(Vector3D position)
	{
		Vector3D cameraPosition = pose.applyInverseTo(position);
		if (cameraPosition.getX() == 0) {
			return null;
		}

		double scale = 1.0 / cameraPosition.getX();

		double mx = cameraPosition.getY() * scale * focalHorizontal;
		double my = cameraPosition.getZ() * scale * focalVertical;

		return new Vector2D(-mx + focalPointX, -my + focalPointY);
	}

	@Override
	public Area2D.Float pixelToCar(Area2D.Int area)
	{
		Vector3D bottomLeft = pixelToCar(area.getBottomLeft());
		Vector3D bottomRight = pixelToCar(area.getBottomRight());
		if (bottomLeft == null || bottomRight == null) {
			return null;
		}

		double height = LaneMiddle.pixelToCamera(area.getHeight());
		return new Area2D.Float(bottomLeft.getX() - height, bottomRight.getX(), bottomLeft.getY(), bottomRight.getY());
	}

	public Vector3D pixelToCarHomography(Vector2D imagePoint)
	{
		RealVector p = MatrixUtils.createRealVector(new double[] {imagePoint.getX(), imagePoint.getY(), 1});
		Vector3D pTrans = perspectiveTransform(homographyMatrixNear, p);
		if (pTrans.getX() > 1.5 || Math.abs(pTrans.getY()) > 0.7) {
			pTrans = perspectiveTransform(homographyMatrixFar, p);
		}
		return pTrans;
	}

	private Vector3D perspectiveTransform(RealMatrix m, RealVector p)
	{
		RealVector transformed = m.operate(p);
		// output in millimeters, hence divide by 1000
		double scalar = transformed.getEntry(2);
		double worldY = transformed.getEntry(0) / scalar / 1000;
		double worldX = transformed.getEntry(1) / scalar / 1000;
		return new Vector3D(worldX, worldY, 0);
	}

	@Override
	public Vector2D getFocalPoint()
	{
		return focalPoint;
	}

	@Override
	public Polygon getVisibleArea()
	{
		return visibleArea;
	}
}
