package taco.agent.decision.behavior.impl;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.decision.behavior.IBehavior;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.*;
import hso.autonomy.util.geometry.Polygon;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.decision.behavior.base.AudiCupComplexBehavior;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.ILidar;
import taco.agent.model.agentmodel.IUltrasonic;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.LightName;
import taco.agent.model.agentmodel.impl.enums.UltrasonicPosition;
import taco.agent.model.worldmodel.ILaneMiddleSensor;
import taco.agent.model.worldmodel.impl.DrivePoint;
import taco.agent.model.worldmodel.street.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Merging Behaviour: leave the car on the last merging segment, wait on the last segment if merging in lane is blocked.
 **/
public class Merging extends AudiCupComplexBehavior
{
	/**
	 * the time we wait at the end of a merging lane before checking again if we have to give way (in s)
	 */
	private static final float GIVE_WAY_WAIT_TIME = 0.2f;

	private static final Polygon MERGING_AREA = new Polygon(new Area2D.Float(0.5, 2.5, -0.25, 0.25));

	private static final Polygon MERGING_START_CHECK_AREA = new Polygon(new Area2D.Float(-1, 2., -0.25, 0.25));

	private static final Polygon AREA_BEFORE_MERGING_IN = new Polygon(new Area2D.Float(-1.5, -.7, -0.25, 0.25));

	private static final Polygon WAITING_AREA = new Polygon(new Area2D.Float(-.5, 1., -0.25, 0.25));

	private static final String DRAWING_NAME = "merging";

	private static int waitingCounter = 0;

	private enum Phase { CHECKING_ACTION, ANALYZE_BEFORE_WAITING, WAITING, BACKWARDS, OVERTAKE, MERGE_OUT, FINISHED }

	private Phase phase;

	private SegmentLink mergingStartLink;

	/**
	 * the time we started waiting before checking decision again
	 */
	private float waitStartTime;

	private float waitDuration;

	private boolean firstTimeOnMergingStart = false;

	private boolean mergingStartTrigger = true;

	private boolean lowSpeed = false;

	private double speed = IAudiCupMotor.DEFAULT_SPEED;
	private static final double MERGING_LOW_SPEED = 28.;

	private DriveToPose driveToPose;

	private FollowRightLane followRightLane;

	private double xOvertake = -.1;
	private double yOvertake = .5;
	private boolean overtakPosesCalculated = false;

	private double backwardsAnalysisCounter = 0;
	private boolean shouldOvertake = true;

	private IPose3D startPose;
	private IPose3D nextOvertakePose;
	private IPose3D afterNextOvertakePose;
	private IPose3D backwardsNextPose;
	private IPose3D backwardsAfterNextPose;

	private static final double THRESHOLD_L15 = 1.2;
	private static final double THRESHOLD_L20 = 1.2;
	private static final double THRESHOLD_L25 = 1.;
	private static final double THRESHOLD_L30 = 1.;
	private static final double THRESHOLD_L35 = .8;
	private static final double THRESHOLD_L40 = .8;
	private static final double THRESHOLD_L45 = .8;
	private static final double THRESHOLD_L60 = .7;
	private static final double THRESHOLD_L90 = .4;
	private static final double THRESHOLD_US_LEFT = .4;
	private static final double THRESHOLD_US_REAR = .7;

	private double distanceLidar15;
	private double distanceLidar20;
	private double distanceLidar25;
	private double distanceLidar30;
	private double distanceLidar35;
	private double distanceLidar40;
	private double distanceLidar45;
	private double distanceLidar60;
	private double distanceLidar90;
	private double distanceUSLeft;
	private double distanceUSRear;

	private int obstacleBehindNotMovingCounter = 0;
	private int obstacleSideNotMovingCounter = 0;
	private int obstacleFrontNotMovingCounter = 0;
	private int behindFreeCounter = 0;
	private int sideFreeCounter = 0;
	private int frontFreeCounter = 0;
	private int beforeWaitingCounter = 0;

	public Merging(IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(IBehaviorConstants.MERGING, thoughtModel, behaviors);
		driveToPose = (DriveToPose) behaviors.get(IBehaviorConstants.DRIVE_TO_POSE);
		followRightLane = (FollowRightLane) behaviors.get(IBehaviorConstants.FOLLOW_RIGHT_LANE);
	}

	@Override
	public void init()
	{
		super.init();
		phase = Phase.CHECKING_ACTION;
		waitStartTime = 0;
		waitDuration = 0;
	}

	@Override
	public boolean isFinished()
	{
		return phase == Phase.FINISHED;
	}

	@Override
	public void onLeavingBehavior(IBehavior newBehavior)
	{
		finish();
		super.onLeavingBehavior(newBehavior);
	}

	@Override
	protected String decideNextBasicBehavior()
	{
		getAgentModel().getLight(LightName.INDICATOR_LEFT).turnOn();
		List<DrivePoint> path = getWorldModel().getThisCar().getPath().getDrivePath();
		DrivePoint nextPoint = path.get(1);
		DrivePoint pointAfterNext = path.get(2);
		ILaneMiddleSensor laneMiddleSensor = getWorldModel().getLaneMiddleSensor();
		Segment currentSegment = nextPoint.getGoalLink().getSegmentBefore();
		IPose3D currentPose = getWorldModel().getThisCar().getPose();
		boolean onMergingOut = currentSegment.isMergingOut();
		// System.out.println(phase);

		if (!onMergingOut && phase != Phase.OVERTAKE) {
			finish();
		} else {
			switch (phase) {
			case CHECKING_ACTION:
				Vector2D currentPosition = getWorldModel().getThisCar().getPose().get2DPose().getPosition();
				boolean onMergingMiddle = currentSegment.getType() == SegmentType.MERGING_MIDDLE;
				boolean nextSegmentOnMergingEnd =
						nextPoint.getGoalLink().getSegmentAfter().getType() == SegmentType.MERGING_END;
				boolean onWaitingArea = nextSegmentOnMergingEnd &&
										WAITING_AREA.transform(nextPoint.getPose()).contains(currentPosition);
				firstTimeOnMergingStart = getWorldModel().isMergingStart() && mergingStartTrigger;

				if (firstTimeOnMergingStart) {
					getAgentModel().getLight(LightName.INDICATOR_LEFT).turnOn();
				}

				if (isOtherCarNearMergingOutLane(nextPoint)) {
					if (firstTimeOnMergingStart) {
						// if (isOtherCarFarEnoughBehind()) {
						lowSpeed = true;
						speed = MERGING_LOW_SPEED;
						/*} else {
							speed = IAudiCupMotor.LOW_SPEED;
						}*/
						mergingStartTrigger = false;
					} else if (onWaitingArea) {
						stopAndAnalyzeBeforeWait();
					} else if (nextSegmentOnMergingEnd) {
						speed = MERGING_LOW_SPEED;
					} else if (onMergingMiddle && lowSpeed) {
						// speed = IAudiCupMotor.HIGH_SPEED;
						speed = MERGING_LOW_SPEED;
					}
				} else if (onWaitingArea) {
					speed = IAudiCupMotor.DEFAULT_SPEED;
				} else if (nextSegmentOnMergingEnd && !lowSpeed) {
					speed = IAudiCupMotor.DEFAULT_SPEED;
				}
				break;
			case ANALYZE_BEFORE_WAITING:
				setDistanceValues();
				break;
			case WAITING:
				if (getWorldModel().getGlobalTime() - waitStartTime >= waitDuration) {
					waitingCounter++;
				}
				analyzeBlockingObstacle();
				if (phase == Phase.BACKWARDS) {
					speed = IAudiCupMotor.LOW_SPEED;
					getAgentModel().getSteering().reset();
					startPose = getWorldModel().getThisCar().getPose();
					backwardsNextPose = getWorldModel().getCurrentSegment().getInLink().getPose();
					backwardsAfterNextPose = backwardsNextPose.applyTo(new Pose3D(-.8, 0., 0.));
				}
				if (waitingCounter > 75) {
					// waited 15 seconds, blocking vehicles seem to be not moving
					phase = Phase.MERGE_OUT;
				}
				break;
			case BACKWARDS:
				if (currentPose.getDistanceTo(startPose) > .8) {
					getAgentModel().getMotor().stop();
					speed = 0.;
					double distanceL30 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(30);
					double distanceL35 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(35);
					double distanceL40 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(40);
					double distanceL45 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(45);
					double distanceL60 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(60);
					Direction mergingDir = nextPoint.getGoalLink().getDirection();
					Segment mergingOutToOvertakeSegment = getSegmentToCheckBeforeOvertaking(nextPoint, mergingDir);
					boolean otherCarDetectedByVisionOnMergingOut = isOtherCarOnSegment(mergingOutToOvertakeSegment);
					if (distanceL30 < THRESHOLD_L30 || distanceL35 < THRESHOLD_L35 || distanceL40 < THRESHOLD_L40 ||
							distanceL45 < THRESHOLD_L45 || distanceL60 < THRESHOLD_L60 ||
							otherCarDetectedByVisionOnMergingOut) {
						shouldOvertake = false;
					}
					if (++backwardsAnalysisCounter >= 50) {
						if (shouldOvertake) {
							phase = Phase.OVERTAKE;
							startPose = currentPose;
							backwardsAnalysisCounter = 0;
							shouldOvertake = true;
							getWorldModel().getLaneMiddleSensor().setUseLanesFromMapIfDetectedLanesAreInvalid(true);
							return NONE;
						} else {
							startPose = currentPose;
							backwardsNextPose = backwardsNextPose.applyTo(new Pose3D(-.7, -.1, 0.));
							backwardsAfterNextPose = backwardsNextPose.applyTo(new Pose3D(-.1, 0., 0.));
							xOvertake = 0.;
							yOvertake = .35;
							backwardsAnalysisCounter = 0;
							shouldOvertake = true;
							speed = IAudiCupMotor.LOW_SPEED;
						}
					} else {
						return NONE;
					}
				} else {
					driveToPose.setTargetPose(backwardsNextPose, backwardsAfterNextPose, speed, true, true);
					return IBehaviorConstants.DRIVE_TO_POSE;
				}
				break;
			case OVERTAKE:
				if (!overtakPosesCalculated) {
					SegmentLink outLink = nextPoint.getGoalLink().getSegmentAfter().getClosestLinkToPose(
							nextPoint.getGoalLink().getPose().applyTo(new Pose3D(.5, .5, 0.)));
					nextOvertakePose =
							/*nextPoint.getGoalLink().getPose();*/ currentPose.applyTo(new Pose3D(0.03, 0., 0.));
					afterNextOvertakePose = outLink.getPose().applyTo(new Pose3D(xOvertake, 0., 0.));
					speed = IAudiCupMotor.DEFAULT_SPEED;
					overtakPosesCalculated = true;
				}
				if (currentSegment.isMergingIn()) {
					return IBehaviorConstants.FOLLOW_LEFT_LANE;
				}
				if (startPose.getDistanceTo(currentPose) > 3.) {
					finish();
					return IBehaviorConstants.FOLLOW_RIGHT_LANE;
				}
				driveToPose.setTargetPose(nextOvertakePose, afterNextOvertakePose, speed, false, false);
				return IBehaviorConstants.DRIVE_TO_POSE;
			case MERGE_OUT:
				speed = IAudiCupMotor.LOW_SPEED;
				break;
			}
		}
		followRightLane.setSpeed(speed);
		driveToPose.setTargetPose(nextPoint.getPose(), pointAfterNext.getPose(), speed, false, false);
		return laneMiddleSensor.isReliable(0.4) ? IBehaviorConstants.FOLLOW_RIGHT_LANE
												: IBehaviorConstants.DRIVE_TO_POSE;
	}

	private boolean isOtherCarNearMergingOutLane(DrivePoint inPoint)
	{
		SegmentLink inLink = inPoint.getGoalLink();
		Direction drivingDir = inLink.getDirection();

		if (getWorldModel().isMergingStart()) {
			return isOtherCarNearMergingStart(inPoint, drivingDir);
		} else {
			return isOtherCarNearMergingOutLane(inPoint, drivingDir);
		}
	}

	private void analyzeBlockingObstacle()
	{
		double currentDistanceL15 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(15);
		double currentDistanceL20 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(20);
		double currentDistanceL25 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(25);
		double currentDistanceL30 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(30);
		double currentDistanceL35 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(35);
		double currentDistanceL40 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(40);
		double currentDistanceL45 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(40);
		double currentDistanceL90 = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(90);
		double currentDistanceUSSide = getAgentModel().getUltrasonic(UltrasonicPosition.SIDE_LEFT).getDistance();
		double currentDistanceUSRear = getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT).getDistance();
		boolean otherCarDetectedInFrontByVision = otherCarDetectedByVisionOnLastMergingIn();

		if (currentDistanceL15 > THRESHOLD_L15 && currentDistanceL20 > THRESHOLD_L20 &&
				currentDistanceL25 > THRESHOLD_L25 && currentDistanceL30 > THRESHOLD_L30 &&
				currentDistanceL35 > THRESHOLD_L35 && currentDistanceL40 > THRESHOLD_L40 &&
				currentDistanceL45 > THRESHOLD_L45 && !otherCarDetectedInFrontByVision) {
			if (++frontFreeCounter >= 100) {
				obstacleFrontNotMovingCounter = 0;
			}
		} else {
			frontFreeCounter = 0;
			if ((Math.abs(currentDistanceL15 - distanceLidar15) <= 5 && currentDistanceL15 < THRESHOLD_L15) ||
					(Math.abs(currentDistanceL20 - distanceLidar20) <= 5 && distanceLidar20 < THRESHOLD_L20) ||
					(Math.abs(currentDistanceL25 - distanceLidar25) <= 5 && distanceLidar25 < THRESHOLD_L25) ||
					(Math.abs(currentDistanceL30 - distanceLidar30) <= 5 && distanceLidar30 < THRESHOLD_L30) ||
					(Math.abs(currentDistanceL35 - distanceLidar35) <= 5 && distanceLidar35 < THRESHOLD_L35) ||
					otherCarDetectedInFrontByVision) {
				obstacleFrontNotMovingCounter++;
			} else if (obstacleFrontNotMovingCounter > 0) {
				obstacleFrontNotMovingCounter--;
			}
		}
		if (currentDistanceL90 > THRESHOLD_L90 && currentDistanceUSSide > THRESHOLD_US_LEFT) {
			if (++sideFreeCounter >= 150) {
				obstacleSideNotMovingCounter = 0;
			}
		} else {
			sideFreeCounter = 0;
			if ((Math.abs(currentDistanceL90 - distanceLidar90) <= 5 && currentDistanceL90 < THRESHOLD_L90) ||
					(Math.abs(currentDistanceUSSide - distanceUSLeft) <= 5 &&
							currentDistanceUSSide < THRESHOLD_US_LEFT)) {
				obstacleSideNotMovingCounter++;
			} else if (obstacleSideNotMovingCounter > 0) {
				obstacleSideNotMovingCounter--;
			}
		}
		if (currentDistanceUSRear > THRESHOLD_US_REAR) {
			if (++behindFreeCounter >= 150) {
				obstacleBehindNotMovingCounter = 0;
			}
		} else {
			behindFreeCounter = 0;
			if (Math.abs(currentDistanceUSRear - distanceUSRear) <= 5) {
				obstacleBehindNotMovingCounter++;
			} else if (obstacleBehindNotMovingCounter > 0) {
				obstacleBehindNotMovingCounter--;
			}
		}

		// If front is free and behind + side are not moving or free, double check with vision
		// --> if it is really free, merge out else drive backwards to overtake
		if ((obstacleBehindNotMovingCounter >= 150 || behindFreeCounter >= 250) &&
				(obstacleSideNotMovingCounter >= 150 || sideFreeCounter >= 250) && frontFreeCounter >= 250) {
			phase = Phase.MERGE_OUT;
		}
		if (obstacleFrontNotMovingCounter >= 250) {
			phase = Phase.BACKWARDS;
		}
		waitStartTime = getWorldModel().getGlobalTime();
	}

	private boolean otherCarDetectedByVisionOnLastMergingIn()
	{
		List<DrivePoint> path = getWorldModel().getThisCar().getPath().getDrivePath();
		Segment mergingInSegment = null;
		for (DrivePoint point : path) {
			Segment nextSegment = point.getGoalLink().getSegmentAfter();
			if (nextSegment.isMergingIn()) {
				mergingInSegment = nextSegment;
				break;
			}
		}
		return isOtherCarOnSegment(mergingInSegment);
	}

	private void finish()
	{
		speed = IAudiCupMotor.DEFAULT_SPEED;
		getAgentModel().getLight(LightName.INDICATOR_LEFT).turnOff();
		getThoughtModel().getDrawings().remove(DRAWING_NAME);
		getWorldModel().getLaneMiddleSensor().setUseLanesFromMapIfDetectedLanesAreInvalid(false);
		lowSpeed = false;
		overtakPosesCalculated = false;
		mergingStartTrigger = true;
		phase = Phase.FINISHED;
	}

	private boolean isOtherCarFarEnoughBehind()
	{
		Polygon checkArea = AREA_BEFORE_MERGING_IN.transform(mergingStartLink.getPose());
		boolean otherCarFarEnoughBehind = isOtherCarInAreaToCheck(checkArea);
		drawCheckArea(DRAWING_NAME, checkArea, otherCarFarEnoughBehind);
		return otherCarFarEnoughBehind;
	}

	private void stopAndWait(float waitDuration)
	{
		this.waitDuration = waitDuration;

		speed = 0.;
		getAgentModel().getMotor().stop();
		phase = Phase.WAITING;
	}

	private void stopAndAnalyzeBeforeWait()
	{
		speed = 0.;
		getAgentModel().getMotor().stop();
		resetDistanceValues();
		phase = Phase.ANALYZE_BEFORE_WAITING;
	}

	private void resetDistanceValues()
	{
		distanceLidar15 = 12.;
		distanceLidar20 = 12.;
		distanceLidar25 = 12.;
		distanceLidar30 = 12.;
		distanceLidar35 = 12.;
		distanceLidar40 = 12.;
		distanceLidar45 = 12.;
		distanceLidar60 = 12.;
		distanceLidar90 = 12.;
		distanceUSLeft = 12.;
		distanceUSRear = 12.;
	}

	private void setDistanceValues()
	{
		double currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(15);
		distanceLidar15 = currentMeasurement < distanceLidar15 ? currentMeasurement : distanceLidar15;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(20);
		distanceLidar20 = currentMeasurement < distanceLidar20 ? currentMeasurement : distanceLidar20;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(25);
		distanceLidar25 = currentMeasurement < distanceLidar25 ? currentMeasurement : distanceLidar25;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(30);
		distanceLidar30 = currentMeasurement < distanceLidar30 ? currentMeasurement : distanceLidar30;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(35);
		distanceLidar35 = currentMeasurement < distanceLidar35 ? currentMeasurement : distanceLidar35;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(40);
		distanceLidar40 = currentMeasurement < distanceLidar40 ? currentMeasurement : distanceLidar40;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(45);
		distanceLidar45 = currentMeasurement < distanceLidar45 ? currentMeasurement : distanceLidar45;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(60);
		distanceLidar60 = currentMeasurement < distanceLidar60 ? currentMeasurement : distanceLidar60;

		currentMeasurement = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT).getDistance(90);
		distanceLidar90 = currentMeasurement < distanceLidar90 ? currentMeasurement : distanceLidar90;

		currentMeasurement = getAgentModel().getUltrasonic(UltrasonicPosition.SIDE_LEFT).getDistance();
		distanceUSLeft = currentMeasurement < distanceUSLeft ? currentMeasurement : distanceUSLeft;

		currentMeasurement = getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT).getDistance();
		distanceUSRear = currentMeasurement < distanceUSRear ? currentMeasurement : distanceUSRear;

		if (++beforeWaitingCounter > 10) { // check the distance measurements for 10 times because they can be invalid
			stopAndWait(GIVE_WAY_WAIT_TIME);
		}
	}

	private boolean isOtherCarNearMergingStart(DrivePoint inPoint, Direction drivingDir)
	{
		SegmentLink mergingInLink = getMergingLinkForMergingStart(inPoint, drivingDir);
		Polygon checkArea = MERGING_START_CHECK_AREA.transform(mergingInLink.getPose());
		return isOtherCarInAreaToCheck(checkArea);
	}

	private boolean isOtherCarNearMergingOutLane(DrivePoint inPoint, Direction drivingDir)
	{
		SegmentLink mergingInLink = getMergingLinkForMergingOutLane(inPoint, drivingDir);
		return isOtherCarInMergingArea(mergingInLink);
	}

	private boolean isOtherCarOnSegment(Segment segmentToCheck)
	{
		Polygon checkArea = new Polygon(segmentToCheck.getArea());
		return getWorldModel()
				.getRecognizedObjects(RecognizedObjectType.CAR)
				.stream()
				.anyMatch(car -> checkArea.intersects(car.getArea()));
	}

	/**
	 * Returns the centerSegment as well as the two segments before and two segments after that one
	 */
	private ArrayList<Segment> getAreaToCheck(Segment centerSegment, Direction drivingDir)
	{
		ArrayList<Segment> area = new ArrayList<>();
		area.add(centerSegment);
		getThoughtModel().log("Driving dir", drivingDir);
		getThoughtModel().log("Center Segment: ", centerSegment.getID());

		Segment firstAfterCenter = centerSegment.getOutOption(drivingDir).getSegmentAfter();
		// Segment secondAfterCenter = firstAfterCenter.getOutOption(drivingDir).getSegmentAfter();
		area.add(firstAfterCenter);
		// area.add(secondAfterCenter);
		getThoughtModel().log("After Center: ", firstAfterCenter.getID());
		// getThoughtModel().log("2 After Center: ", secondAfterCenter.getID());

		Segment firstBeforeCenter =
				centerSegment.getOutOption(Direction.getOppositeDirection(drivingDir)).getSegmentAfter();
		// Segment secondBeforeCenter = firstBeforeCenter.getInOption(drivingDir).getSegmentBefore();
		area.add(firstBeforeCenter);
		// area.add(secondBeforeCenter);
		getThoughtModel().log("Before Center: ", firstBeforeCenter.getID());

		return area;
	}

	private Segment getCenterOfAreaToCheckForMergingOutLane(DrivePoint inPoint, Direction mergingDir)
	{
		SegmentLink inLink = inPoint.getGoalLink();
		Segment currentSegment = inLink.getSegmentBefore();

		Direction mergingOutDir = Direction.getRelativeLeftDirection(mergingDir);
		SegmentLink mergingOutLink = currentSegment.getOutOption(mergingOutDir);
		Segment mergingIn = mergingOutLink.getSegmentAfter();
		return mergingIn;
	}

	private Segment getCenterOfAreaToCheckForMergingStart(DrivePoint inPoint, Direction mergingDir)
	{
		SegmentLink inLink = inPoint.getGoalLink();
		Segment mergingMiddle = inLink.getSegmentAfter();

		Direction mergingOutDir = Direction.getRelativeLeftDirection(mergingDir);
		SegmentLink mergingOutLink = mergingMiddle.getOutOption(mergingOutDir);
		Segment mergingIn = mergingOutLink.getSegmentAfter();

		return mergingIn;
	}

	private Segment getSegmentToCheckBeforeOvertaking(DrivePoint inPoint, Direction mergingDir)
	{
		return getCenterOfAreaToCheckForMergingStart(inPoint, mergingDir);
	}

	private SegmentLink getMergingLinkForMergingOutLane(DrivePoint inPoint, Direction mergingDir)
	{
		SegmentLink inLink = inPoint.getGoalLink();
		Segment currentSegment = inLink.getSegmentBefore();

		Direction mergingOutDir = Direction.getRelativeLeftDirection(mergingDir);
		SegmentLink mergingOutLink = currentSegment.getOutOption(mergingOutDir);
		Segment mergingIn = mergingOutLink.getSegmentAfter();
		SegmentLink mergingLink = mergingIn.getInOption(Direction.getOppositeDirection(mergingDir));
		return mergingLink;
	}

	private SegmentLink getMergingLinkForMergingStart(DrivePoint inPoint, Direction mergingDir)
	{
		SegmentLink inLink = inPoint.getGoalLink();
		Segment mergingMiddle = inLink.getSegmentAfter();

		Direction mergingOutDir = Direction.getRelativeLeftDirection(mergingDir);
		SegmentLink mergingOutLink = mergingMiddle.getOutOption(mergingOutDir);
		Segment mergingIn = mergingOutLink.getSegmentAfter();
		mergingStartLink = mergingIn.getInOption(Direction.getOppositeDirection(mergingDir));
		return mergingStartLink;
	}

	private boolean isOtherCarInMergingArea(SegmentLink mergingIn)
	{
		Polygon mergingArea = MERGING_AREA.transform(mergingIn.getPose());
		boolean otherCarInMergingArea = isOtherCarInAreaToCheck(mergingArea);

		return otherCarInMergingArea;
	}

	private boolean isOtherCarInAreaToCheck(Polygon checkArea)
	{
		boolean otherCarInMergingArea = getWorldModel()
												.getRecognizedObjects(RecognizedObjectType.CAR)
												.stream()
												.anyMatch(car -> checkArea.intersects(car.getArea()));
		if (!otherCarInMergingArea) {
			ILidar lidar = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT);
			otherCarInMergingArea = (lidar.getDistance(75) < THRESHOLD_L90 || lidar.getDistance(90) < THRESHOLD_L90);
			if (!otherCarInMergingArea && !firstTimeOnMergingStart) {
				double distanceL15 = lidar.getDistance(15);
				double distanceL20 = lidar.getDistance(20);
				double distanceL25 = lidar.getDistance(25);
				double distanceL30 = lidar.getDistance(30);
				double distanceL35 = lidar.getDistance(35);
				double distanceL40 = lidar.getDistance(40);
				double distanceL45 = lidar.getDistance(45);
				double distanceL60 = lidar.getDistance(60);
				otherCarInMergingArea = (distanceL15 < THRESHOLD_L15 || distanceL20 < THRESHOLD_L20 ||
										 distanceL25 < THRESHOLD_L25 || distanceL30 < THRESHOLD_L30 ||
										 distanceL35 < THRESHOLD_L35 || distanceL40 < THRESHOLD_L40 ||
										 distanceL45 < THRESHOLD_L45 || distanceL60 < THRESHOLD_L60);
			}
			if (!otherCarInMergingArea) {
				IUltrasonic ultrasonicRearLeft = getAgentModel().getUltrasonic(UltrasonicPosition.REAR_LEFT);
				IUltrasonic ultrasonicSideLeft = getAgentModel().getUltrasonic(UltrasonicPosition.SIDE_LEFT);
				otherCarInMergingArea = ((ultrasonicRearLeft.getDistance() < THRESHOLD_US_REAR) ||
										 ultrasonicSideLeft.getDistance() < THRESHOLD_US_LEFT);
			}
		}
		drawCheckArea(DRAWING_NAME, checkArea, otherCarInMergingArea);
		return otherCarInMergingArea;
	}

	private boolean isOtherCarInAreaToCheck(ArrayList<Segment> areaToCheck, Direction drivingDir)
	{
		// boolean otherCarInAreaToCheck = false;
		for (int i = 0; i < areaToCheck.size(); i++) {
			Polygon segmentToCheck = new Polygon(areaToCheck.get(i).getArea().applyBorder(0.17f));

			boolean otherCarInSegmentToCheck = getWorldModel()
													   .getRecognizedObjects(RecognizedObjectType.CAR)
													   .stream()
													   .anyMatch(car -> segmentToCheck.intersects(car.getArea()));
			drawCheckArea(DRAWING_NAME, segmentToCheck, otherCarInSegmentToCheck);
			System.out.println(areaToCheck.get(i).getID() + ": " + otherCarInSegmentToCheck);
			// otherCarInAreaToCheck = otherCarInAreaToCheck ? otherCarInAreaToCheck : otherCarInSegmentToCheck;
			if (otherCarInSegmentToCheck) {
				return true;
			}
		}
		return false;
	}

	private void drawCheckArea(String name, Polygon mergingArea, boolean containsObstacle)
	{
		if (mergingArea == null) {
			removeDrawings(name);
		} else {
			getThoughtModel().getDrawings().draw(
					name, containsObstacle ? new Color(255, 0, 0, 75) : new Color(0, 255, 0, 75), mergingArea);
		}
	}

	private void removeDrawings(String drawingName)
	{
		getThoughtModel().getDrawings().remove(drawingName);
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
}