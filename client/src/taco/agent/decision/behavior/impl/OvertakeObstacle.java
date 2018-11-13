package taco.agent.decision.behavior.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.decision.behavior.base.AudiCupComplexBehavior;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.ILidar;
import taco.agent.model.agentmodel.IUltrasonic;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.LightName;
import taco.agent.model.agentmodel.impl.enums.UltrasonicPosition;

public class OvertakeObstacle extends AudiCupComplexBehavior
{
	private enum Phase { DRIVE, BACKWARDS, FORWARD_LEFT, OVERTAKE, FORWARD_RIGHT, ENDING, FINISHED }

	private Phase phase;

	private List<Double> distancesSide;

	private IPose3D overtakenPosition;

	private IPose3D startPosition;

	private boolean isMoving, initCheck, onCurve;

	private FollowLeftLane followLeftLane;

	private DriveToPose driveToPose;

	private IPose3D next;

	private IPose3D afterNext;

	public OvertakeObstacle(IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(IBehaviorConstants.OVERTAKE_OBSTACLE, thoughtModel, behaviors);
		followLeftLane = (FollowLeftLane) behaviors.get(IBehaviorConstants.FOLLOW_LEFT_LANE);
		driveToPose = (DriveToPose) behaviors.get(IBehaviorConstants.DRIVE_TO_POSE);
	}

	@Override
	public void init()
	{
		super.init();
		phase = Phase.BACKWARDS;
		distancesSide = new ArrayList<>();
		overtakenPosition = null;
		startPosition = null;
		isMoving = false;
		initCheck = false;
		next = null;
		afterNext = null;
		onCurve = false;
	}

	@Override
	protected String decideNextBasicBehavior()
	{
		IAudiCupAgentModel agentModel = getAgentModel();
		IPose3D currentCarPosition = getWorldModel().getThisCar().getPose();
		Angle currentDirection = getDirection(currentCarPosition.getHorizontalAngle());

		checkInit();

		if (phase == Phase.FORWARD_LEFT || phase == Phase.OVERTAKE) {
			measureUSDistances();
		}

		getThoughtModel().log("overtakeMovPhase", phase);
		getThoughtModel().log("onSideFront", onSide());

		switch (phase) {
		case BACKWARDS:
			agentModel.getMotor().driveBackward();
			// agentModel.getSteering().reset();

			if (currentCarPosition.getDistanceTo(startPosition) > 0.8) {
				agentModel.getMotor().stop();
				agentModel.getLight(LightName.INDICATOR_LEFT).turnOn();

				phase = Phase.FORWARD_LEFT;
			}
			break;

		case FORWARD_LEFT:
			agentModel.getLight(LightName.INDICATOR_LEFT).turnOn();

			if (!isMoving && next == null && !onCurve) {
				Angle deltaAngle = startPosition.getDeltaHorizontalAngle(new Pose3D(Vector3D.ZERO, currentDirection));
				next = startPosition.applyTo(new Pose3D(0.8, 0.48, deltaAngle));
				afterNext = next.applyTo(new Pose3D(1, 0));
				drawNextPoints(next, afterNext);
				driveToPose.setTargetPose(next, afterNext);
			}

			if (onSide()) {
				phase = Phase.OVERTAKE;
				next = null;
				agentModel.getLight(LightName.INDICATOR_LEFT).turnOff();
			}

			if (checkAbort(currentCarPosition)) {
				getThoughtModel().log("overtakeAbort", true);
				phase = Phase.FORWARD_RIGHT;
			}

			return isMoving || (!isMoving && onCurve) ? IBehaviorConstants.FOLLOW_LEFT_LANE
													  : IBehaviorConstants.DRIVE_TO_POSE;

		case OVERTAKE:
			// check for Obstacle in front of the vehicle, wee need this because we are use this behavior to overtake
			// an object
			IUltrasonic usFront = agentModel.getUltrasonic(UltrasonicPosition.FRONT_CENTER);
			ILidar lidarFront = agentModel.getLidar(LidarPosition.LIDAR_FRONT);

			double distance = 0;
			if (usFront != null) {
				distance = usFront.getDistance();
			} else if (lidarFront != null) {
				distance = lidarFront.getDistance(0);
			}

			if (distance < 0.5) {
				agentModel.getMotor().stop();
				finish();
				return NONE;
			}

			if (isMoving) {
				followLeftLane.setSpeed(IAudiCupMotor.HIGH_SPEED);
			}

			if (!isMoving && !onCurve) {
				if (next == null || currentCarPosition.getDistanceTo(next) < 0.1) {
					next = afterNext;
					afterNext = next.applyTo(new Pose3D(1, 0));
					drawNextPoints(next, afterNext);
					driveToPose.setTargetPose(next, afterNext);
				}
			}

			if (overtakenPosition == null && !onSide()) {
				agentModel.getLight(LightName.INDICATOR_RIGHT).turnOn();
				overtakenPosition = currentCarPosition;
				phase = Phase.FORWARD_RIGHT;
				next = null;
			}

			if (checkAbort(currentCarPosition)) {
				getThoughtModel().log("overtakeAbort", true);
				phase = Phase.FORWARD_RIGHT;
			}

			return isMoving || (!isMoving && onCurve) ? IBehaviorConstants.FOLLOW_LEFT_LANE
													  : IBehaviorConstants.DRIVE_TO_POSE;

		case FORWARD_RIGHT:
			if (overtakenPosition == null || currentCarPosition.getDistanceTo(overtakenPosition) > 0.3) {
				if (isMoving) {
					followLeftLane.setSpeed(IAudiCupMotor.DEFAULT_SPEED);
				}
				finish();
			}
			return IBehaviorConstants.FOLLOW_RIGHT_LANE;
		}

		return NONE;
	}

	private void finish()
	{
		getAgentModel().getLight(LightName.INDICATOR_RIGHT).turnOff();
		getAgentModel().getLight(LightName.INDICATOR_LEFT).turnOff();
		drawNextPoints(null, null);
		distancesSide.clear();
		phase = Phase.FINISHED;
	}

	private void checkInit()
	{
		if (!initCheck) {
			if (getThoughtModel().isMovingObstacleAhead()) {
				isMoving = true;
				phase = Phase.FORWARD_LEFT;
			}

			if (getWorldModel().isCloseToCurve()) {
				onCurve = true;
			}

			getThoughtModel().log("overtakeAbort", false);
			initCheck = true;
		}

		if (startPosition == null) {
			startPosition = getWorldModel().getThisCar().getPose();
		}
	}

	private void measureUSDistances()
	{
		IUltrasonic usSideRight = getAgentModel().getUltrasonic(UltrasonicPosition.SIDE_RIGHT);

		distancesSide.add(usSideRight.getDistance());

		if (distancesSide.size() > 40) {
			// keep history limited
			distancesSide.remove(0);
		}
	}

	private boolean onSide()
	{
		return distancesSide.size() > 10 && distancesSide.stream().filter(distance -> distance < 0.6).count() > 5;
	}

	private boolean checkAbort(IPose3D currentCarPosition)
	{
		return startPosition.getDistanceTo(currentCarPosition) > 3 && !onSide();
	}

	private void drawNextPoints(IPose3D next, IPose3D afterNext)
	{
		getThoughtModel().getDrawings().draw("next", new Color(161, 40, 255, 128), next);
		getThoughtModel().getDrawings().draw("afterNext", new Color(255, 196, 0, 128), afterNext);
		getThoughtModel().log("next", next);
		getThoughtModel().log("afterNext", afterNext);
	}

	public Angle getDirection(Angle theAngle)
	{
		double angle = theAngle.degrees();
		if (angle >= -45 && angle < 45) {
			return Angle.ZERO;
		} else if (angle >= 45 && angle < 135) {
			return Angle.ANGLE_90;
		} else if (angle >= -135 && angle < -45) {
			return Angle.ANGLE_90.negate();
		}

		return Angle.ANGLE_180;
	}

	@Override
	public boolean isFinished()
	{
		return phase == Phase.FINISHED;
	}
}