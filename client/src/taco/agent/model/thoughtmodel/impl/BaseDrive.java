package taco.agent.model.thoughtmodel.impl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.logging.DrawingMap;
import taco.agent.model.agentmodel.ILidar;
import taco.agent.model.agentmodel.IUltrasonic;

import java.util.Arrays;

public abstract class BaseDrive
{
	/** the distance in which obstacles are not save calculated from the front of the car */
	protected double distance;

	/** the length (in m) of the car */
	protected double carLength;

	/** global pose of the car */
	protected IPose3D carPose;

	public BaseDrive(IPose3D carPose, double distance, double carLength)
	{
		this.distance = distance;
		this.carPose = carPose;
		this.carLength = carLength;
	}

	protected abstract void drawDriveArea(DrawingMap drawings);

	/**
	 * @param objectPosition the position in car coordinates
	 * @param distance the distance in m from front of car
	 * @return true if the object is in our drive way
	 */
	public abstract boolean isPositionInWay(Vector3D objectPosition, double distance);

	/**
	 * @param us the ultrasonic sensor for which to check
	 * @param distance the distance in m from front of car
	 * @return the distance of the obstacle if in way, Double.POSITIVE_INFINITY if not in way
	 */
	public double getObstacleDistance(IUltrasonic us, double distance)
	{
		if (us == null)
			return Double.POSITIVE_INFINITY;

		// check position
		Vector3D objectPosition = us.getObjectPosition();
		if (!isPositionInWay(objectPosition, distance)) {
			return Double.POSITIVE_INFINITY;
		}

		// check distance
		return us.getDistance();
	}

	public double getObstacleDistance(ILidar lidar, int angle, double distance)
	{
		if (lidar == null)
			return Double.POSITIVE_INFINITY;

		// check position
		Vector3D objectPosition = lidar.getObjectPosition(angle);
		if (!isPositionInWay(objectPosition, distance)) {
			return Double.POSITIVE_INFINITY;
		}

		// Calculate median
		double[] arr = new double[5];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = lidar.getDistance(angle + i);
		}
		Arrays.sort(arr);
		return (arr[arr.length / 2] + arr[(arr.length - 1) / 2]) / 2;

		// return lidar.getDistance(angle);
	}

	protected boolean checkDistance(Vector3D objectPosition, double distance)
	{
		return objectPosition.distance(new Vector3D(carLength, 0, 0)) < distance;
	}
}
