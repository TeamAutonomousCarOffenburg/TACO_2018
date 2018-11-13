package taco.agent.model.thoughtmodel.impl;

import static taco.agent.model.agentmodel.IAudiCupMotor.DEFAULT_SPEED;
import static taco.agent.model.agentmodel.impl.enums.LidarPosition.LIDAR_FRONT;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.FRONT_CENTER;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.FRONT_CENTER_LEFT;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.FRONT_CENTER_RIGHT;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.REAR_CENTER;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.REAR_LEFT;
import static taco.agent.model.agentmodel.impl.enums.UltrasonicPosition.REAR_RIGHT;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Geometry;
import hso.autonomy.util.geometry.IPose3D;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.TachometerPosition;
import taco.agent.model.agentmodel.impl.enums.UltrasonicPosition;
import taco.agent.model.thoughtmodel.IAudiCupThoughtModel;

public class DriveWay
{
	private IAudiCupThoughtModel thoughtModel;

	private BaseDrive driveWay;

	private double targetSpeed;

	private Angle steeringAngle;

	public DriveWay(IAudiCupThoughtModel thoughtModel)
	{
		this.thoughtModel = thoughtModel;
		driveWay = new StraightDrive(thoughtModel.getWorldModel().getThisCar().getPose(), 0.5,
				thoughtModel.getAgentModel().getCarWidth(), thoughtModel.getAgentModel().getCarLength());
	}

	public void updateAfterPerform()
	{
		IAudiCupAgentModel agentModel = thoughtModel.getAgentModel();
		targetSpeed = agentModel.getMotor().getTargetSpeed();
		steeringAngle = agentModel.getSteering().getDesiredAngle();

		double carWidth = agentModel.getCarWidth() + 0.1;
		double carLength = agentModel.getCarLength();
		double middleRadius = 0;
		double innerRadius = 0;
		double outerRadius = 0;
		double minDistance = calculateThreshold(1.5, 0.5);
		Vector3D circleCenter = null;
		IPose3D carPose = thoughtModel.getWorldModel().getThisCar().getPose();
		if (Math.abs(steeringAngle.degrees()) > 1) {
			// we drive a curve
			middleRadius = thoughtModel.getDriveGeometry().calculateCurveRadius(steeringAngle);
			innerRadius = middleRadius - carWidth * 0.5;
			outerRadius = middleRadius + carWidth * 0.5 + steeringAngle.degrees() / 250;
			circleCenter = new Vector3D(0, middleRadius, 0);
			if (middleRadius < 0) {
				innerRadius = -middleRadius - carWidth * 0.5;
				outerRadius = -middleRadius + carWidth * 0.5 - steeringAngle.degrees() / 250;
			}
			driveWay = new CircleDrive(carPose, minDistance, carLength, circleCenter, innerRadius, outerRadius);
		} else {
			driveWay = new StraightDrive(carPose, minDistance, carLength, carWidth);
		}
		driveWay.drawDriveArea(thoughtModel.getDrawings());
	}

	public boolean isPositionInWay(Vector3D objectPosition, double distance)
	{
		return driveWay.isPositionInWay(objectPosition, distance);
	}

	public boolean isObstacleInPath()
	{
		return (targetSpeed > 0 && isObstacleAhead()) || (targetSpeed < 0 && isObstacleBehind());
	}

	public boolean isObstacleAhead()
	{
		double minDistance = calculateThreshold(1.5, 0.6);
		double distance = getObstacleAheadDistance(minDistance);
		return distance < minDistance;
	}

	public boolean isObstacleBehind()
	{
		double centerThreshold = calculateThreshold(1.5, 0.3);
		double outerThreshold = calculateThreshold(1.25, 0.15);
		IAudiCupAgentModel agentModel = thoughtModel.getAgentModel();

		return agentModel.getUltrasonic(REAR_LEFT).isCloserThan(outerThreshold) ||
				agentModel.getUltrasonic(REAR_CENTER).isCloserThan(centerThreshold) ||
				agentModel.getUltrasonic(REAR_RIGHT).isCloserThan(outerThreshold);
	}

	/**
	 * Returns the smallest distance of an obstacle in us that is in our current drive way
	 * @param distance the distance to the object
	 */
	public double getObstacleAheadDistance(double distance)
	{
		// UltrasonicPosition[] usNames = {FRONT_LEFT, FRONT_CENTER_LEFT, FRONT_CENTER, FRONT_CENTER_RIGHT,
		// FRONT_RIGHT};
		UltrasonicPosition[] usNames = {FRONT_CENTER_LEFT, FRONT_CENTER, FRONT_CENTER_RIGHT};
		LidarPosition[] lidarNames = {LIDAR_FRONT};

		for (UltrasonicPosition us : usNames) {
			double obstacleDistance =
					driveWay.getObstacleDistance(thoughtModel.getAgentModel().getUltrasonic(us), distance);
			if (obstacleDistance < distance && obstacleDistance != Double.POSITIVE_INFINITY) {
				distance = obstacleDistance;
			}
		}

		for (LidarPosition lidar : lidarNames) {
			for (int i = -20; i <= 20; i++) {
				double obstacleDistance =
						driveWay.getObstacleDistance(thoughtModel.getAgentModel().getLidar(lidar), i, distance);
				if (obstacleDistance < distance && obstacleDistance != Double.POSITIVE_INFINITY) {
					distance = obstacleDistance;
				}
			}
		}
		/*
		Vector3D objectPosition = thoughtModel.getAgentModel().getLidar(lidarNames[0]).getObjectPosition(-89);
		IPose3D carPos = thoughtModel.getWorldModel().getThisCar().getPose();

		thoughtModel.getDrawings().draw("objPosition", Color.RED, carPos.applyTo(objectPosition));
		*/
		/*thoughtModel.getDrawings().draw("objPosition", Color.BLACK,
				carPos.applyTo(thoughtModel.getAgentModel().getUltrasonic(FRONT_CENTER).getObjectPosition()));*/
		return distance;
	}

	private double calculateThreshold(double multiplier, double defaultSpeedThreshold)
	{
		double absoluteSpeed = Math.abs(thoughtModel.getAgentModel().getTachometer(TachometerPosition.LEFT).getSpeed());
		// old speed scaling was based on targetSpeed, this factor roughly gets us back into that range / unit
		absoluteSpeed *= 8.5;

		double speedFactor = (Geometry.getLinearFuzzyValue(DEFAULT_SPEED, 100, true, absoluteSpeed) * multiplier) + 1;
		return defaultSpeedThreshold * speedFactor;
	}
}
