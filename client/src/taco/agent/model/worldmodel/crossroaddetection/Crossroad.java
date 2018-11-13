package taco.agent.model.worldmodel.crossroaddetection;

import hso.autonomy.util.geometry.Area2D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import taco.agent.communication.perception.RecognizedObject;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.model.agentmodel.ICameraSensor;

public class Crossroad
{
	private static final double DISTANCE_STOP_TO_CROSSROAD = 0.1;

	private static final double DISTANCE_BOX_TO_CROSSROAD = 0.3;

	private static final double CROSSROAD_WIDTH = 1;

	private static final float TTL = 1f;

	private double typeConfidence;

	private double distanceConfidence;

	private double euclideanDistance;

	private Vector2D distance;

	private Area2D.Int box;

	private RecognizedObjectType type;

	private float detectionTime;

	private ICameraSensor camera;

	public Crossroad(RecognizedObject object, float time, ICameraSensor baslerCamera)
	{
		this.box = object.getArea();
		this.type = object.getType();
		this.detectionTime = time;
		this.typeConfidence = object.getConfidence();
		this.camera = baslerCamera;
		init();
	}

	private void init()
	{
		if (box == null || camera == null)
			return;

		// distance estimation gets worse with crossroads further to the borders
		// reduce distanceConfidence the further away it is from camera's focalX
		int imgMidX = box.getMinX() + box.getWidth() / 2;
		double focalPointX = camera.getFocalPoint().getX();
		distanceConfidence = Math.abs(imgMidX - focalPointX) / focalPointX;

		// TODO: add stoplines to distance calculation -> Find rotation of crossroad and improve offset
		// but...no aheadLineY available at all in LaneMiddle
		// check for stopline in Lane Middle Sensor
		//		Vector2D possibleStopLine = null;
		//		if (laneMiddleSensor.getCurrent().getAheadLineY() >= 0){
		//			possibleStopLine = new Vector2D();
		//		}

		// calculate distance to midX, bottomY and add offset to Y mid
		Vector2D midCrossroad = new Vector2D(imgMidX, box.getMaxY());
		Vector3D midCrossroadTrans = camera.pixelToCarHomography(midCrossroad);
		distance = new Vector2D(
				midCrossroadTrans.getX() + CROSSROAD_WIDTH / 2 + DISTANCE_BOX_TO_CROSSROAD, midCrossroadTrans.getY());
		euclideanDistance = Math.sqrt(Math.pow(distance.getX(), 2) + Math.pow(distance.getY(), 2));
	}

	public boolean isValid(float time)
	{
		return (time - detectionTime) < TTL;
	}

	/**
	 *
	 * @return euclidean distance to middle of crossroad
	 */
	public double getEuclideanDistance()
	{
		return euclideanDistance;
	}

	/**
	 *
	 * @return distance to middle of crossroad
	 */
	public Vector2D getDistanceVector()
	{
		return distance;
	}

	/**
	 *
	 * @return bounding box of recognized crossroad in image coordinates
	 */
	public Area2D.Int getBox()
	{
		return this.box;
	}

	/**
	 *
	 * @return recognized object type
	 */
	public RecognizedObjectType getType()
	{
		return type;
	}

	/**
	 *
	 * @return confidence for crossroad classification
	 */
	public double getTypeConfidence()
	{
		return typeConfidence;
	}

	/**
	 *
	 * @return confidence for distance estimation to middle of crossroad
	 */
	public double getDistanceConfidence()
	{
		return distanceConfidence;
	}

	/**
	 *
	 * @param requiredTypeConfidence wanted confidence of classification
	 * @param requiredDistanceConfidence wanted confidence of distance estimation
	 * @return true if confidence requirements are met
	 */
	boolean isReliable(double requiredTypeConfidence, double requiredDistanceConfidence)
	{
		return requiredDistanceConfidence <= distanceConfidence && requiredTypeConfidence <= typeConfidence;
	}

	/**
	 * compares boxes of crossroads via type and overlap
	 * @param compCrossroad crossroad to compare this to
	 * @param minOverlappedFactor boxes have to be overlapped by at least this factor for similarity
	 * @return true if crossroad type is same and boxes are overlapped by at least minOverlappedFactor
	 */
	public boolean isSimilarTo(Crossroad compCrossroad, float minOverlappedFactor)
	{
		Area2D.Int a1 = box;
		Area2D.Int a2 = compCrossroad.getBox();
		int x_overlap = Math.max(0, Math.min(a1.getMaxX(), a2.getMaxX()) - Math.max(a1.getMinX(), a2.getMinX()));
		int y_overlap = Math.max(0, Math.min(a1.getMaxY(), a2.getMaxY()) - Math.max(a1.getMinY(), a2.getMinY()));
		int overlapArea = x_overlap * y_overlap;
		int area1 = a1.getWidth() * a1.getHeight();
		int area2 = a2.getWidth() * a2.getHeight();
		int biggerArea = (area1 > area2) ? area1 : area2;
		return type == compCrossroad.getType() && (overlapArea / (float) biggerArea) > minOverlappedFactor;
	}
}
