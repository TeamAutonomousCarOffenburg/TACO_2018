package taco.agent.model.worldmodel.street;

import static taco.agent.model.agentmodel.IAudiCupMotor.DEFAULT_SPEED;
import static taco.agent.model.worldmodel.street.Direction.NORTH;
import static taco.agent.model.worldmodel.street.Direction.SOUTH;
import static taco.agent.model.worldmodel.street.ISegment.CROSSING_HALF_LENGTH;
import static taco.agent.model.worldmodel.street.ISegment.CROSSING_LENGTH;
import static taco.agent.model.worldmodel.street.ISegment.LANE_HALF_WIDTH;
import static taco.agent.model.worldmodel.street.ISegment.LANE_WIDTH;

import java.util.ArrayList;
import java.util.List;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.worldmodel.signdetection.RoadSign;

public class SegmentLink
{
	/** poses of the out links of a crossing relative to the south in link */
	static IPose3D[] OUT_POSES = {
			new Pose3D(CROSSING_LENGTH, 0), // North
			new Pose3D(CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, -CROSSING_HALF_LENGTH + LANE_HALF_WIDTH,
					Angle.ANGLE_90.negate()),			// East
			new Pose3D(0, LANE_WIDTH, Angle.ANGLE_180), // South
			new Pose3D(CROSSING_HALF_LENGTH + LANE_HALF_WIDTH, CROSSING_HALF_LENGTH + LANE_HALF_WIDTH,
					Angle.ANGLE_90), // West
	};

	/** poses of the out links of s curves relative to the south in link */
	static IPose3D[] S_POSES = {
			new Pose3D(3, 2),  // left
			new Pose3D(3, -2), // right
	};

	static double MERGE_ANGLE = 26.565;

	/** pose of the out link of a starting merging lane (road narrows) relative to the south in link */
	static IPose3D M_START_POSE = new Pose3D(CROSSING_LENGTH, LANE_WIDTH /*, Angle.deg(MERGE_ANGLE)*/);

	/** poses of the out links of a merging lane (middle/main part) relative to the south in link */
	static IPose3D[] M_MIDDLE_POSES = {
			new Pose3D(CROSSING_HALF_LENGTH, CROSSING_HALF_LENGTH - LANE_HALF_WIDTH,
					Angle.deg(MERGE_ANGLE)), // leave first merging lane
			new Pose3D(CROSSING_LENGTH, 0),  // enter merging lane end
	};

	/** poses of the out link of an ending merging lane (road narrows) relative to the south in link */
	static IPose3D M_END_POSE =
			new Pose3D(CROSSING_HALF_LENGTH, CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, Angle.deg(MERGE_ANGLE));

	/** poses of the out links of a merging in lane relative to the south in link */
	static IPose3D[] M_IN_POSES = {
			new Pose3D(CROSSING_HALF_LENGTH + LANE_HALF_WIDTH,
					CROSSING_HALF_LENGTH /*, Angle.deg(180 - MERGE_ANGLE)*/),				  // left (west)
			new Pose3D(LANE_HALF_WIDTH, -CROSSING_HALF_LENGTH /*, Angle.deg(-MERGE_ANGLE)*/), // right (east)
	};

	/** pose of the in link of a merging in lane relative to the south in link */
	static IPose3D[] M_IN_IN_POSES = {
			new Pose3D(CROSSING_HALF_LENGTH, CROSSING_HALF_LENGTH + LANE_HALF_WIDTH,
					Angle.deg(180 + MERGE_ANGLE)), // merging in lane not the same as south in
			new Pose3D(CROSSING_HALF_LENGTH, -CROSSING_HALF_LENGTH + LANE_HALF_WIDTH,
					Angle.deg(MERGE_ANGLE)) // merging in lane the same as south in
	};

	/** global pose of the link */
	private IPose3D pose;

	/** segment this link links from */
	Segment segmentBefore;

	/** segment this link links to */
	Segment segmentAfter;

	/** a list of road signs attached to this segmentLink (semantically to the in link) */
	List<RoadSign> roadSigns;

	/** the speed limit for the segment before this out link */
	private double speed;

	private boolean hasStopLine;

	SegmentLink(IPose3D pose, Segment segmentBefore, Segment segmentAfter)
	{
		this(pose, segmentBefore, segmentAfter, IAudiCupMotor.DEFAULT_SPEED);
	}

	SegmentLink(IPose3D pose, Segment segmentBefore, Segment segmentAfter, double speed)
	{
		this.pose = pose;
		this.segmentBefore = segmentBefore;
		this.segmentAfter = segmentAfter;
		this.speed = speed;
		roadSigns = new ArrayList<>();
		hasStopLine = false;
	}

	public List<RoadSign> getRoadSigns()
	{
		return roadSigns;
	}

	void addRoadSigns(List<RoadSign> roadSigns)
	{
		this.roadSigns.addAll(roadSigns);
	}

	public void addRoadSign(RoadSign roadSign)
	{
		this.roadSigns.add(roadSign);
	}

	void setPose(IPose3D pose)
	{
		this.pose = pose;
	}

	public IPose3D getPose()
	{
		return pose;
	}

	public double getSpeed()
	{
		return speed;
	}

	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	public Direction getDirection()
	{
		return getDirection(pose);
	}

	public static Direction getDirection(IPose3D pose)
	{
		return Direction.getDirection(pose.getHorizontalAngle());
	}

	public Segment getSegmentBefore()
	{
		return segmentBefore;
	}

	public Segment getSegmentAfter()
	{
		return segmentAfter;
	}

	boolean hasSegmentBefore()
	{
		if (segmentBefore != null) {
			return true;
		}

		return false;
	}

	public boolean hasSegmentAfter()
	{
		return segmentAfter != null;
	}

	/**
	 * @param pose the pose to check
	 * @return true if the passed pose is in front of this pose
	 */
	public boolean hasPassedLink(IPose3D pose)
	{
		Angle angleToPose = this.pose.getHorizontalAngleTo(pose);
		return Math.abs(angleToPose.degrees()) < 90;
	}

	void setSegmentAfter(Segment segment)
	{
		segmentAfter = segment;
	}

	void setSegmentBefore(Segment segment)
	{
		segmentBefore = segment;
	}

	/**
	 * @return the in link that is on the other lane at the same end of the street
	 */
	public SegmentLink getCorrespondingInLink()
	{
		return getSegmentBefore().getInOption(getDirection());
	}

	/**
	 * @return the out link that is on the other lane at the same end of the street
	 */
	public SegmentLink getCorrespondingOutLink()
	{
		return getSegmentAfter().getOutOption(Direction.getOppositeDirection(getDirection()));
	}

	/**
	 * @return the out link that is on the same lane at the straight option
	 */
	public SegmentLink getSameLaneOutOption()
	{
		return getSegmentAfter().getSameLaneOutOption(this.getDirection());
	}

	// create methods
	public NonCrossingSegment appendStraightSegment()
	{
		return appendStraightSegment(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendStraightSegment(double speedOur, double speedOther)
	{
		return appendStraightSegment(1, speedOur, speedOther);
	}

	public NonCrossingSegment appendStraightSegment(double length, double speedOur, double speedOther)
	{
		return appendNonCrossing(length, Angle.ZERO, SegmentType.STRAIGHT, speedOur, speedOther);
	}

	public NonCrossingSegment appendStraightSegmentWithCrosswalk()
	{
		return appendStraightSegmentWithCrosswalk(1.0, DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendStraightSegmentWithCrosswalk(double length, double speedOur, double speedOther)
	{
		return appendNonCrossing(length, Angle.ZERO, SegmentType.STRAIGHT_WITH_CROSSWALK, speedOur, speedOther);
	}

	public NonCrossingSegment appendCurveSmallLeft()
	{
		return appendCurveSmallLeft(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendCurveSmallLeft(double speedOur, double speedOther)
	{
		return appendNonCrossing(3 * Math.PI / 4, Angle.ANGLE_90, SegmentType.CURVE_SMALL, speedOur, speedOther);
	}

	public NonCrossingSegment appendCurveSmallRight()
	{
		return appendCurveSmallRight(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendCurveSmallRight(double speedOur, double speedOther)
	{
		return appendNonCrossing(
				3 * Math.PI / 4, Angle.ANGLE_90.negate(), SegmentType.CURVE_SMALL, speedOur, speedOther);
	}

	public NonCrossingSegment appendCurveBigLeft()
	{
		return appendCurveBigLeft(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendCurveBigLeft(double speedOur, double speedOther)
	{
		return appendNonCrossing(5 * Math.PI / 4, Angle.ANGLE_90, SegmentType.CURVE_BIG, speedOur, speedOther);
	}

	public NonCrossingSegment appendCurveBigRight()
	{
		return appendCurveBigRight(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendCurveBigRight(double speedOur, double speedOther)
	{
		return appendNonCrossing(5 * Math.PI / 4, Angle.ANGLE_90.negate(), SegmentType.CURVE_BIG, speedOur, speedOther);
	}

	public NonCrossingSegment appendSCurveBottom()
	{
		return appendSCurveBottom(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendSCurveBottom(double speedOur, double speedOther)
	{
		return appendSCrossing(S_POSES[0], SegmentType.S_CURVE_BOTTOM, speedOur, speedOther);
	}

	public NonCrossingSegment appendSCurveTop()
	{
		return appendSCurveTop(DEFAULT_SPEED, DEFAULT_SPEED);
	}

	public NonCrossingSegment appendSCurveTop(double speedOur, double speedOther)
	{
		return appendSCrossing(S_POSES[1], SegmentType.S_CURVE_TOP, speedOur, speedOther);
	}

	public NonCrossingSegment appendMergingStart()
	{
		return appendMergingStart(DEFAULT_SPEED);
	}

	public NonCrossingSegment appendMergingStart(double speed)
	{
		Direction dirOut = getDirection();
		Direction dirIn = Direction.getOppositeDirection(dirOut);
		return appendMerging(M_START_POSE, dirIn, dirOut, SegmentType.MERGING_START, speed);
	}

	public NonCrossingSegment appendMergingMiddle()
	{
		return appendMergingMiddle(DEFAULT_SPEED);
	}

	public NonCrossingSegment appendMergingMiddle(double speed)
	{
		return appendMergingMiddle(SegmentType.MERGING_MIDDLE, speed);
	}

	public NonCrossingSegment appendMergingEnd()
	{
		return appendMergingEnd(DEFAULT_SPEED);
	}

	public NonCrossingSegment appendMergingEnd(double speed)
	{
		Direction direction = getDirection();
		Direction dirIn = Direction.getOppositeDirection(direction);
		Direction dirOut = Direction.getRelativeLeftDirection(direction);
		return appendMerging(M_END_POSE, dirIn, dirOut, SegmentType.MERGING_END, speed);
	}

	public NonCrossingSegment appendMergingInLS()
	{
		return appendMergingInLS(DEFAULT_SPEED);
	}

	public NonCrossingSegment appendMergingInLS(double speed)
	{
		Direction dirMergingIn = Direction.getRelativeLeftDirection(getDirection());
		return appendMergingIn(M_IN_IN_POSES[0], dirMergingIn, SegmentType.MERGING_IN, speed, speed);
	}

	public NonCrossingSegment appendMergingInLS(double speedOur, double speedOther)
	{
		Direction dirMergingIn = Direction.getRelativeLeftDirection(getDirection());
		return appendMergingIn(M_IN_IN_POSES[0], dirMergingIn, SegmentType.MERGING_IN, speedOur, speedOther);
	}

	public NonCrossingSegment appendMergingInSR()
	{
		return appendMergingInSR(DEFAULT_SPEED);
	}

	public NonCrossingSegment appendMergingInSR(double speed)
	{
		Direction dirMergingIn = Direction.getRelativeRightDirection(getDirection());
		return appendMergingIn(M_IN_IN_POSES[1], dirMergingIn, SegmentType.MERGING_IN, speed, speed);
	}

	public NonCrossingSegment appendMergingInSR(double speedOur, double speedOther)
	{
		Direction dirMergingIn = Direction.getRelativeRightDirection(getDirection());
		return appendMergingIn(M_IN_IN_POSES[1], dirMergingIn, SegmentType.MERGING_IN, speedOur, speedOther);
	}

	/**
	 *
	 * @param deltaPose the relative pose of the out link same lane
	 * @return the new segment created
	 */
	private NonCrossingSegment appendSCrossing(IPose3D deltaPose, SegmentType type, double speedOur, double speedOther)
	{
		// Create a new segment
		// 2 * 1/4 circumference(1.5)  - 1
		NonCrossingSegment newSegment = new NonCrossingSegment(3.7, Angle.ZERO, type);

		// Wire up new segment with this link
		connectToSegment(newSegment, speedOther);

		// create straight out option
		IPose3D outPose = pose.applyTo(deltaPose);
		createOption(newSegment, getDirection(outPose), outPose, speedOur);

		return newSegment;
	}

	/**
	 *
	 * @param length the length of the segment middle line
	 * @param bendingAngle the angle the segment bends (positive is to left)
	 * @return the new segment created
	 */
	private NonCrossingSegment appendNonCrossing(
			double length, Angle bendingAngle, SegmentType type, double speedOur, double speedOther)
	{
		// Create a new segment
		NonCrossingSegment newSegment = new NonCrossingSegment(length, bendingAngle, type);

		// Wire up new segment with this link
		connectToSegment(newSegment, speedOther);

		// create straight out option
		IPose3D outPose = NonCrossingSegment.getCurveOutPose(pose, length, bendingAngle);
		Direction direction = getDirection(outPose);
		createOption(newSegment, direction, outPose, speedOur);

		return newSegment;
	}

	/**
	 *
	 * @param deltaPose the relative pose of the out links same lane
	 * @return the new segment created
	 */
	private NonCrossingSegment appendMerging(
			IPose3D deltaPose, Direction dirIn, Direction dirOut, SegmentType type, double speed)
	{
		// calculate lane length
		// double length = 1.118; // x² = sqrt(1² + 0.5²)
		double length = 1;

		//  Create a new segment
		NonCrossingSegment newSegment = new NonCrossingSegment(length, Angle.ZERO, type);

		// create out option + connect out link of prev. segment with in link
		IPose3D outPose = pose.applyTo(deltaPose);
		createMergingOptions(newSegment, dirIn, dirOut, outPose, speed);

		return newSegment;
	}

	/**
	 *
	 * @return the new segment created
	 */
	private NonCrossingSegment appendMergingMiddle(SegmentType type, double speed)
	{
		// calculate lane length
		double length = 1.;

		//  Create a new segment
		NonCrossingSegment newSegment = new NonCrossingSegment(length, Angle.ZERO, type);

		// create out options + connect out link of prev. segment with in link
		Direction dirMain = getDirection();
		Direction dirIn = Direction.getOppositeDirection(dirMain);
		Direction dirOut = Direction.getRelativeLeftDirection(dirMain);
		createMergingMiddleOptions(newSegment, dirMain, dirIn, dirOut, speed);

		return newSegment;
	}

	private NonCrossingSegment appendMergingIn(
			IPose3D deltaInPose, Direction dirMergingIn, SegmentType type, double speedOur, double speedOther)
	{
		Direction[] directions = {NORTH, SOUTH};
		double length = 1.;

		//  Create a new segment
		NonCrossingSegment newSegment = new NonCrossingSegment(length, Angle.ZERO, type);

		// Wire up new segment with preceding one
		Direction dirAtNew = connectToSegment(newSegment, speedOther);

		// create merging_in  inlink
		Direction ourDirection = getDirection();
		IPose3D inPose = pose.applyTo(deltaInPose);
		SegmentLink inLink = new SegmentLink(inPose, null, newSegment);
		newSegment.setInOption(dirMergingIn, inLink);

		// create links
		for (int i = 0; i < directions.length; i++) {
			Direction currentDir = directions[i];
			Direction absoluteDir = Direction.getAbsoluteFromRelativeDirection(ourDirection, currentDir);
			if (absoluteDir != dirAtNew) {
				IPose3D outPose = pose.applyTo(OUT_POSES[currentDir.ordinal()]);
				createOption(newSegment, absoluteDir, outPose, speedOur);
			}
		}
		setSegmentAfter(newSegment);

		return newSegment;
	}

	private void createOption(Segment newSegment, Direction direction, IPose3D outPose, double speed)
	{
		SegmentLink outLink = new SegmentLink(outPose, newSegment, null, speed);
		newSegment.setOutOption(direction, outLink);

		// create straight in option
		IPose3D inPose = Segment.getInPoseFromCorrespondingOutPose(outPose);
		SegmentLink inLink = new SegmentLink(inPose, null, newSegment);
		newSegment.setInOption(direction, inLink);
	}

	private void createMergingOptions(
			Segment newSegment, Direction dirIn, Direction dirOut, IPose3D outPose, double speed)
	{
		SegmentLink outLink = new SegmentLink(outPose, newSegment, null, speed);
		newSegment.setOutOption(dirOut, outLink);
		newSegment.setInOption(dirIn, this);
		setSegmentAfter(newSegment);
	}

	private void createMergingMiddleOptions(
			Segment newSegment, Direction dirMain, Direction dirIn, Direction dirOut, double speed)
	{
		IPose3D poseCopy = pose;
		IPose3D deltaPoseMain = M_MIDDLE_POSES[1];
		IPose3D poseMain = poseCopy.applyTo(deltaPoseMain);
		IPose3D deltaPoseOut = M_MIDDLE_POSES[0];
		IPose3D poseOut = poseCopy.applyTo(deltaPoseOut);

		SegmentLink outLink = new SegmentLink(poseMain, newSegment, null, speed);
		newSegment.setOutOption(dirMain, outLink);
		SegmentLink outLink2 = new SegmentLink(poseOut, newSegment, null, speed);
		newSegment.setOutOption(dirOut, outLink2);

		// create in option for opposite site
		newSegment.setInOption(dirIn, this);
		setSegmentAfter(newSegment);
	}

	/**
	 * @param newSegment the segment to link to
	 * @return the direction that is set on the new segment
	 */
	private Direction connectToSegment(Segment newSegment, double speed)
	{
		Direction direction = getDirection();
		Direction oppositeDirection = Direction.getOppositeDirection(direction);
		newSegment.setInOption(oppositeDirection, this);
		setSegmentAfter(newSegment);
		SegmentLink otherInOption = getSegmentBefore().getInOption(direction);
		// set the speed of the new segment's out option (other lane)
		otherInOption.setSpeed(speed);
		newSegment.setOutOption(oppositeDirection, otherInOption);
		otherInOption.setSegmentBefore(newSegment);
		return oppositeDirection;
	}

	// Crossings
	public Segment appendXCrossing()
	{
		return appendXCrossing(DEFAULT_SPEED);
	}

	public Segment appendXCrossing(double speed)
	{
		Direction[] dirs = {NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		return appendCrossing(dirs, SegmentType.X_CROSSING, speed);
	}

	public Segment appendTCrossingLS()
	{
		return appendTCrossingLS(DEFAULT_SPEED);
	}

	public Segment appendTCrossingLS(double speed)
	{
		Direction[] dirs = {NORTH, Direction.SOUTH, Direction.WEST};
		return appendCrossing(dirs, SegmentType.T_CROSSING, speed);
	}

	public Segment appendTCrossingLR()
	{
		return appendTCrossingLR(DEFAULT_SPEED);
	}

	public Segment appendTCrossingLR(double speed)
	{
		Direction[] dirs = {Direction.EAST, Direction.SOUTH, Direction.WEST};
		return appendCrossing(dirs, SegmentType.T_CROSSING, speed);
	}

	public Segment appendTCrossingSR()
	{
		return appendTCrossingSR(DEFAULT_SPEED);
	}

	public Segment appendTCrossingSR(double speed)
	{
		Direction[] dirs = {NORTH, Direction.EAST, Direction.SOUTH};
		return appendCrossing(dirs, SegmentType.T_CROSSING, speed);
	}

	/**
	 * @param directions relative directions so this links pose direction.
	 * (straight = North, right = East, back = South, left = West);
	 * @return the new segment
	 */
	private Segment appendCrossing(Direction[] directions, SegmentType type, double speed)
	{
		// Create a new segment
		Segment newSegment = new Segment(type);

		// Wire up new segment with preceding one
		Direction dirAtNew = connectToSegment(newSegment, speed);

		// create links
		Direction ourDirection = getDirection();
		for (Direction currentDir : directions) {
			Direction absoluteDir = Direction.getAbsoluteFromRelativeDirection(ourDirection, currentDir);
			if (absoluteDir != dirAtNew) {
				IPose3D outPose = pose.applyTo(OUT_POSES[currentDir.ordinal()]);
				createOption(newSegment, absoluteDir, outPose, speed);
			}
		}

		return newSegment;
	}

	/**
	 * Connects this link (must be an out link) with the passed link (must be an inLink). Assumes
	 * that the two poses are close with respect to position and angle. Throws an IllegalArgumentException if this is
	 * not the case.
	 * @param connectTo the in link to connect to
	 */
	public void connectToInLink(SegmentLink connectTo)
	{
		double length = pose.getDistanceTo(connectTo.getPose());
		if (length > 0.1) {
			throw new IllegalArgumentException("Pose not close on connection: " + length + " this: " + toString() +
											   " other: " + connectTo.toString());
		}
		Angle bendingAngle = pose.getDeltaHorizontalAngle(connectTo.getPose());
		if (Math.abs(bendingAngle.degrees()) > 1.0) {
			throw new IllegalArgumentException("Pose angles not fitting on connection: " + bendingAngle.degrees() +
											   " this: " + toString() + " other: " + connectTo.toString());
		}

		// same spot, so merge the in with the out and the out with the in link
		this.segmentAfter = connectTo.segmentAfter;
		connectTo.getSegmentAfter().replaceLink(connectTo, this);

		SegmentLink inLink = getCorrespondingInLink();
		SegmentLink outLink = connectTo.getCorrespondingOutLink();
		inLink.segmentBefore = outLink.segmentBefore;
		outLink.getSegmentBefore().replaceLink(outLink, inLink);
	}

	/**
	 * Connects this link (must be an out link - mergingMiddle or mergingStart)
	 * @param connectTo the in link to connect to (where the car drives out of the merging lane)
	 */
	public void connectMergingLaneToInLink(SegmentLink connectTo)
	{
		this.segmentAfter = connectTo.segmentAfter;
		connectTo.getSegmentAfter().replaceLink(connectTo, this);
	}

	@Override
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append((segmentBefore == null) ? "None - " : segmentBefore.toString() + " - ");
		result.append(pose.toString());
		result.append((segmentAfter == null) ? " - None" : " - " + segmentAfter.toString());
		return result.toString();
	}

	public boolean hasStopLine()
	{
		return hasStopLine;
	}

	public void addStopLine()
	{
		hasStopLine = true;
	}
}
