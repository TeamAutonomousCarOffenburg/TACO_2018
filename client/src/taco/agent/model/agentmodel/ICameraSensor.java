package taco.agent.model.agentmodel;

import hso.autonomy.util.geometry.Polygon;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Area2D;

/**
 * Representation of a camera.
 */
public interface ICameraSensor extends IAudiCupSensor {
	/**
	 * Uses raytracing. Alternative: pixelToCarHomograhy()
	 * @param position pixel position
	 * @return pixel position in car coordinate system
	 */
	Vector3D pixelToCar(Vector2D position);

	Vector2D carToPixel(Vector2D position);

	Vector2D carToPixel(Vector3D position);

	Area2D.Float pixelToCar(Area2D.Int area);

	/**
	 * Same as pixelToCar(), but uses homography instead of raytracing. Way more accurate in 1m to the sides and up to
	 * 3m ahead
	 * @param position pixel position
	 * @return pixel position in car coordinate system
	 */
	Vector3D pixelToCarHomography(Vector2D position);

	Vector2D getFocalPoint();

	Polygon getVisibleArea();
}