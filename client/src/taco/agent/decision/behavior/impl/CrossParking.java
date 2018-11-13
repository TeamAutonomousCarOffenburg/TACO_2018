package taco.agent.decision.behavior.impl;

import java.awt.Color;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.decision.behavior.IBehavior;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.decision.behavior.IFollowLane;
import taco.agent.decision.behavior.base.AudiCupComplexBehavior;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.impl.Lidar;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.LightName;
import taco.agent.model.agentmodel.impl.enums.UltrasonicPosition;
import taco.agent.model.worldmodel.ParkingSpaceState;
import taco.agent.model.worldmodel.impl.ParkingSpace;

import static taco.agent.decision.behavior.IBehaviorConstants.EMERGENCY_BRAKE;

public class CrossParking extends AudiCupComplexBehavior
{
	private enum Phase {
		DRIVE_TO_SPACE,
		FORWARD_LEFT,
		BACKWARD_RIGHT,
		BACKWARD,
		ENDING,
		FINISHED,
		CORRECTION_FORWARD,
		CORRECTION_BACKWARD
	}

	private final IFollowLane followLane;

	private final DriveToPose driveToPose;

	private Phase phase;

	private ParkingSpace targetParkingSpace;

	private int desiredParkingSpace;

	private IPose3D startPosition;

	private IPose3D next;

	private IPose3D afterNext;

	public CrossParking(IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(IBehaviorConstants.CROSS_PARKING, thoughtModel, behaviors);
		this.followLane = (IFollowLane) behaviors.get(IBehaviorConstants.FOLLOW_RIGHT_LANE);
		this.driveToPose = (DriveToPose) behaviors.get(IBehaviorConstants.DRIVE_TO_POSE);
	}

	public void setDesiredParkingSpace(int id)
	{
		desiredParkingSpace = id;
	}

	@Override
	public boolean isFinished()
	{
		return phase == Phase.FINISHED;
	}

	@Override
	public void init()
	{
		super.init();
		phase = Phase.DRIVE_TO_SPACE;
		targetParkingSpace = null;
		desiredParkingSpace = 2;
		next = null;
		afterNext = null;
	}

	@Override
	protected String decideNextBasicBehavior()
	{
		if (targetParkingSpace == null) {
			targetParkingSpace = getWorldModel().getEnvironmentManager().getParkingSpaceById(desiredParkingSpace);
			if (targetParkingSpace == null) {
				return NONE;
			}

			if (startPosition == null) {
				startPosition = getWorldModel().getThisCar().getPose();
			}
		}

		IPose3D currentCarPosition = getWorldModel().getThisCar().getPose();
		IPose3D targetSpacePosition = targetParkingSpace.getPose();
		float time = getWorldModel().getGlobalTime();
		IAudiCupAgentModel agentModel = getAgentModel();

		getThoughtModel().log("parkingPhase", phase);
		switch (phase) {
		case DRIVE_TO_SPACE:
			agentModel.getLight(LightName.INDICATOR_RIGHT).turnOn();
			followLane.setSpeed(IAudiCupMotor.LOW_SPEED);
			if (next == null) {
				next = targetSpacePosition.applyTo(new Pose3D(-0.2, 0.25));

				// Crossparking with obstacle ahead
				// afterNext = targetSpacePosition.applyTo(new Pose3D(0.3, 0.7, Angle.deg(45)));
				afterNext = targetSpacePosition.applyTo(new Pose3D(0.7, 0.5, Angle.deg(15)));
				if (currentCarPosition.getDistanceTo(next) < 0.1) {
					next = targetSpacePosition.applyTo(new Pose3D(-0.5, 0.25));
					afterNext = targetSpacePosition.applyTo(new Pose3D(-1, 0.25));
				}
			}
			drawNextPoints(next, afterNext);
			driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);

			if (currentCarPosition.getDistanceTo(next) < 0.1) {
				phase = Phase.FORWARD_LEFT;
				next = null;
			}

			return IBehaviorConstants.DRIVE_TO_POSE;

		case FORWARD_LEFT:
			if (next == null) {
				// Crossparking with obstacle ahead
				// next = targetSpacePosition.applyTo(new Pose3D(0.3, 0.7, Angle.deg(45)));
				next = targetSpacePosition.applyTo(new Pose3D(0.7, 0.5, Angle.deg(15)));
				afterNext = next.applyTo(new Pose3D(1, 0));
				drawNextPoints(next, afterNext);
				driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);
			}

			if (currentCarPosition.getDistanceTo(next) < 0.1) {
				phase = Phase.BACKWARD_RIGHT;
				next = null;
			}

			return IBehaviorConstants.DRIVE_TO_POSE;

		case BACKWARD_RIGHT:
			if (next == null) {
				next = targetSpacePosition.applyTo(new Pose3D(0, 0.1, Angle.ANGLE_90));
				afterNext = next.applyTo(new Pose3D(-0.65, 0));
				drawNextPoints(next, afterNext);
				driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);
			}

			if (currentCarPosition.getDistanceTo(next) < 0.1) {
				phase = Phase.BACKWARD;
				next = null;
				agentModel.getLight(LightName.INDICATOR_RIGHT).turnOff();
			}

			return IBehaviorConstants.DRIVE_TO_POSE;

		case BACKWARD:
			if (next == null) {
				next = afterNext;
				afterNext = next.applyTo(new Pose3D(-3, 0));
				drawNextPoints(next, afterNext);
				driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true);
			}

			if (currentCarPosition.getDistanceTo(next) < 0.12) {
				phase = Phase.ENDING;
				next = null;
				agentModel.getMotor().stop();
				agentModel.getLight(LightName.WARN).turnOn();

				return NONE;
			}

			ParkingSpace closestSpace = getWorldModel().getEnvironmentManager().getClosestParkingSpace(
					getWorldModel().getThisCar().getPose());
			if (closestSpace != null) {
				getWorldModel().getEnvironmentManager().updateParkingSpace(
						closestSpace.getID(), ParkingSpaceState.OCCUPIED, getWorldModel().getGlobalTime());
			}

			return IBehaviorConstants.DRIVE_TO_POSE;

		case ENDING:
			if (time - agentModel.getLight(LightName.WARN).getModificationTime() > 4f) {
				agentModel.getLight(LightName.WARN).turnOff();

				phase = Phase.FINISHED;
			}
			break;

		case CORRECTION_FORWARD:

			correctionForward();

			return IBehaviorConstants.DRIVE_TO_POSE;

		case CORRECTION_BACKWARD:

			correctionBackward();

			return IBehaviorConstants.DRIVE_TO_POSE;

		case FINISHED:
		}

		return NONE;
	}

	private void drawNextPoints(IPose3D next, IPose3D afterNext)
	{
		getThoughtModel().getDrawings().draw("next", new Color(161, 40, 255, 128), next);
		getThoughtModel().getDrawings().draw("afterNext", new Color(255, 196, 0, 128), afterNext);
		getThoughtModel().log("next", next);
		getThoughtModel().log("afterNext", afterNext);
	}

	@Override
	public IBehavior switchFrom(IBehavior actualBehavior)
	{
		if (actualBehavior.getName().equals(EMERGENCY_BRAKE) &&
				(phase == Phase.CORRECTION_FORWARD || phase == Phase.CORRECTION_BACKWARD || phase == Phase.BACKWARD ||
						phase == Phase.BACKWARD_RIGHT || phase == Phase.FORWARD_LEFT)) {
			double distanceFront = getDistanceFront();

			double distanceRear = getDistanceBack();

			// Emergency Brake Front
			if (distanceFront < distanceRear) {
				phase = Phase.CORRECTION_BACKWARD;
			}
			// Emergency Brake Back
			else if (distanceFront >= distanceRear) {
				phase = Phase.CORRECTION_FORWARD;
			}
		}
		return super.switchFrom(actualBehavior);
	}

	private double getDistanceFront()
	{
		double d = Double.MAX_VALUE;

		// Ultrasonic
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER).getDistance(), d);
		}
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_LEFT) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_LEFT).getDistance(),
					d);
		}
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_RIGHT) != null) {
			d = Double.min(getThoughtModel()
								   .getAgentModel()
								   .getUltrasonic(UltrasonicPosition.FRONT_CENTER_RIGHT)
								   .getDistance(),
					d);
		}
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_RIGHT) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_RIGHT).getDistance(), d);
		}
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_LEFT) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_LEFT).getDistance(), d);
		}

		// Lidar
		if (getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT) != null) {
			for (int i = Lidar.MIN_ANGLE; i < Lidar.MAX_ANGLE; i++) {
				d = Double.min(getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(i), d);
			}
		}

		return d;
	}

	private double getDistanceBack()
	{
		double d = Double.MAX_VALUE;

		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_CENTER) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_CENTER).getDistance(), d);
		}

		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT).getDistance(), d);
		}

		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_RIGHT) != null) {
			d = Double.min(
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_RIGHT).getDistance(), d);
		}

		return d;
	}

	/**
	 * Emergency Brake was triggerd from a back sensor, so the correction must be through a drive to front
	 */
	private void correctionForward()
	{
		IPose3D currentCarPosition = getWorldModel().getThisCar().getPose();

		double distanceFrontRight = Double.MAX_VALUE;
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_RIGHT) != null) {
			distanceFrontRight = getThoughtModel()
										 .getAgentModel()
										 .getUltrasonic(UltrasonicPosition.FRONT_CENTER_RIGHT)
										 .getDistance();
		}
		if (getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT) != null) {
			for (int i = Lidar.MIN_ANGLE; i < 0; i++) {
				distanceFrontRight =
						Double.min(getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(i),
								distanceFrontRight);
			}
		}

		double distanceFrontLeft = Double.MAX_VALUE;
		if (getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_LEFT) != null) {
			distanceFrontLeft =
					getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_CENTER_LEFT).getDistance();
		}
		if (getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT) != null) {
			for (int i = 0; i < Lidar.MAX_ANGLE; i++) {
				distanceFrontLeft =
						Double.min(getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(i),
								distanceFrontLeft);
			}
			// distanceFrontLeft =
			// getThoughtModel().getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(60);
		}

		double correctionFactor = -0.5;
		if (distanceFrontLeft > distanceFrontRight) {
			correctionFactor *= -1;
		}

		next = currentCarPosition.applyTo(new Pose3D(getThoughtModel().getAgentModel().getCarLength() * 1.5,
				getThoughtModel().getAgentModel().getCarWidth() * correctionFactor));

		afterNext = next.applyTo(new Pose3D(1, 0));
		drawNextPoints(next, afterNext);
		driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED);

		phase = Phase.FORWARD_LEFT;
	}

	/**
	 * Emergency Brake was triggerd from a front sensor, so the correction must be through a drive to back
	 */
	private void correctionBackward()
	{
		IPose3D currentCarPosition = getWorldModel().getThisCar().getPose();

		double distanceRearRight =
				getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_RIGHT).getDistance();
		double distanceRearLeft =
				getThoughtModel().getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT).getDistance();

		double correctionFactor = -0.5;
		if (distanceRearRight > distanceRearLeft) {
			correctionFactor *= -1;
		}

		next = currentCarPosition.applyTo(new Pose3D(getThoughtModel().getAgentModel().getCarLength() * -1.5,
				getThoughtModel().getAgentModel().getCarWidth() * correctionFactor));

		afterNext = next.applyTo(new Pose3D(1, 0));
		drawNextPoints(next, afterNext);
		driveToPose.setTargetPose(next, afterNext, IAudiCupMotor.LOW_SPEED, true, true);

		phase = Phase.FORWARD_LEFT;
	}
}
