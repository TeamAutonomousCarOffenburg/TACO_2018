package taco.agent.decision.behavior.impl;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.decision.behavior.IBehavior;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.decision.behavior.base.AudiCupBehavior;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.LightName;
import taco.agent.model.worldmodel.impl.Obstacle;
import taco.agent.model.worldmodel.street.*;
import taco.util.drive.AngleUtils;

import java.awt.*;
import java.util.List;

public class EmergencyCar extends AudiCupBehavior
{
	IPose3D startPosition;

	private DriveToPose driveToPose;

	private DriveWaypoints driveWaypoints;

	private IPose3D next;

	private IPose3D afterNext;

	private IPose3D goalPose;

	private boolean isInitperformed = false;

	Vector3D lastKnownEmergencyCarPosition;

	Segment lastKnownSegment;

	Direction lastValidCarDirection;

	private Stop stop;

	StreetMap map;

	float firstDetectionTime, resetTime;

	boolean reset;

	public EmergencyCar(IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(IBehaviorConstants.EMERGENCY_CAR, thoughtModel);
		driveToPose = (DriveToPose) behaviors.get(IBehaviorConstants.DRIVE_TO_POSE);
		driveWaypoints = (DriveWaypoints) behaviors.get(IBehaviorConstants.DRIVE_WAYPOINTS);
		stop = (Stop) behaviors.get(IBehaviorConstants.STOP);
	}

	@Override
	public void init()
	{
		super.init();
		next = null;
		afterNext = null;
		map = getWorldModel().getMap();
		reset = false;
	}

	@Override
	public void perform()
	{
		checkInit();

		// check if behaviour is performed longer than 20 seconds - if true - drivewaypoints
		if (getWorldModel().getGlobalTime() - firstDetectionTime < 15) {
			if (!reset) {
				resetTime = getWorldModel().getGlobalTime();

				getAgentModel().getLight(LightName.WARN).turnOn();

				Vector3D currentEmergencyCarPosition = lastKnownEmergencyCarPosition;
				List<Obstacle> recognizedObjects = getWorldModel().getRecognizedObjects();
				for (Obstacle object : recognizedObjects) {
					if (object.getType() == RecognizedObjectType.CAR) {
						currentEmergencyCarPosition = object.getPosition();
					}
				}

				// get the position/Angle/DeltaAngle/Direction of the this.car
				IPose3D currentCarPosition = getWorldModel().getThisCar().getPose();
				IPose3D lidarPosition = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getPose();
				// apply the current position to the lidar position (the current segment is otherwise the rear axle)
				currentCarPosition = currentCarPosition.applyTo(lidarPosition);

				if (lastKnownEmergencyCarPosition.distance(currentEmergencyCarPosition) > 2 ||
						startPosition.getPosition().distance(currentCarPosition.getPosition()) > 3) {
					isInitperformed = false;
					checkInit();
				}
				lastKnownEmergencyCarPosition = currentEmergencyCarPosition;

				Angle currentAngle = AngleUtils.getDirection(currentCarPosition.getHorizontalAngle());
				Direction currentCarDirection = Direction.getDirection(currentAngle);

				// calculate segments
				Segment currentSegment = map.getSegmentContaining(currentCarPosition.getPosition());
				Segment nextSegment = currentSegment;
				if (currentSegment != null) {
					lastKnownSegment = currentSegment;
					lastValidCarDirection = currentCarDirection;
					if (currentSegment.hasOutOption(currentCarDirection)) {
						nextSegment = currentSegment.getOutOption(currentCarDirection).getSegmentAfter();
					}
				} else {
					currentSegment = lastKnownSegment;
					nextSegment = currentSegment;
				}

				Angle deltaAngle = startPosition.getDeltaHorizontalAngle(new Pose3D(Vector3D.ZERO, currentAngle));
				if (nextSegment != null && (nextSegment.isStraight() || nextSegment.isCrossing())) {
					next = startPosition.applyTo(new Pose3D(0.7, -0.1, deltaAngle));
					afterNext = next.applyTo(new Pose3D(0.5, -0.2, deltaAngle));
				} else {
					next = startPosition.applyTo(new Pose3D(0.7, -0.05, deltaAngle));
					afterNext = next.applyTo(new Pose3D(0.5, -0.1, deltaAngle));
				}
				drawNextPoints(next, afterNext);
				goalPose = afterNext;

				// check if any part of the car is on a crossing
				Segment currentSegmentRearAxis = getWorldModel().getCurrentSegment().getSegment();
				if (currentSegmentRearAxis != null && currentSegment != null &&
						(currentSegment.isCrossing() || currentSegmentRearAxis.isCrossing())) {
					next = currentCarPosition.applyTo(new Pose3D(0.3, 0));
					afterNext = next.applyTo(new Pose3D(0.3, 0));
					driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);
					drawNextPoints(next, afterNext);
					goalPose = afterNext;
					driveToPose.perform();
					return;
				}
				// check if the goalpose is reached
				else {
					if (goalPose != null) {
						double distanceToTargetPose = currentCarPosition.getDistanceTo(goalPose);
						if (distanceToTargetPose < 0.4) {
							stop.perform();
							return;
						}
					}
				}

				// check if intersection is ahead
				if (nextSegment != null && nextSegment.isCrossing() && !currentSegment.isCrossing()) {
					stop.perform();
				} else {
					drawNextPoints(next, afterNext);
					driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);
					goalPose = afterNext;
					driveToPose.perform();
				}
			}
		} else {
			if (getWorldModel().getGlobalTime() - resetTime > 10) {
				reset = true;
				checkInit();
			}
			driveWaypoints.perform();
		}
	}

	private void drawNextPoints(IPose3D next, IPose3D afterNext)
	{
		getThoughtModel().getDrawings().draw("next", new Color(161, 40, 255, 128), next);
		getThoughtModel().getDrawings().draw("afterNext", new Color(255, 196, 0, 128), afterNext);
		getThoughtModel().log("next", next);
		getThoughtModel().log("afterNext", afterNext);
	}

	private void checkInit()
	{
		if (!isInitperformed) {
			// get the position and segment of this.car during the first detection
			startPosition = getWorldModel().getThisCar().getPose();
			IPose3D lidarPosition = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getPose();
			startPosition = startPosition.applyTo(lidarPosition);

			// get position of emergencyCar after first detection
			List<Obstacle> recognizedObjects = getWorldModel().getRecognizedObjects();
			for (Obstacle object : recognizedObjects) {
				if (object.getType() == RecognizedObjectType.CAR) {
					lastKnownEmergencyCarPosition = object.getPosition();
				}
			}

			lastKnownSegment = null;
			lastValidCarDirection = null;
			firstDetectionTime = getWorldModel().getGlobalTime();
		}
		isInitperformed = true;
		if (reset) {
			firstDetectionTime = getWorldModel().getGlobalTime();
		}
	}

	@Override
	public void onLeavingBehavior(IBehavior newBehavior)
	{
		super.onLeavingBehavior(newBehavior);
		getAgentModel().getLight(LightName.WARN).turnOff();
	}
}