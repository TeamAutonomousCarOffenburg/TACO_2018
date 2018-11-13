package taco.agent.model.worldmodel.street;

import static taco.agent.model.worldmodel.street.ISegment.CROSSING_HALF_LENGTH;
import static taco.agent.model.worldmodel.street.ISegment.CROSSING_LENGTH;
import static taco.agent.model.worldmodel.street.ISegment.LANE_HALF_WIDTH;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import hso.autonomy.util.misc.FuzzyCompare;
import taco.util.drive.DriveGeometry;

public class NonCrossingSegment extends Segment
{
	private double length;

	private Angle bendingAngle;

	NonCrossingSegment(double length, Angle bendingAngle, SegmentType type)
	{
		super(type);
		this.length = length;
		this.bendingAngle = bendingAngle;
	}

	void setBendingAngle(SegmentLink inLink, Angle bendingAngle)
	{
		this.bendingAngle = bendingAngle;
		adjustShape(inLink, length, bendingAngle);
	}

	private void adjustShape(SegmentLink inLink, double length, Angle bendingAngle)
	{
		SegmentLink otherOutOption = inLink.getCorrespondingOutLink();
		if (otherOutOption != null) {
			IPose3D localEndPose = getCurveOutPose(inLink.getPose(), length, bendingAngle);
			IPose3D outPose = inLink.getPose().applyTo(localEndPose);
			otherOutOption.setPose(outPose);

			SegmentLink otherInOption = otherOutOption.getCorrespondingInLink();
			otherInOption.setPose(outPose.applyTo(new Pose3D(0, 0.4, Angle.ANGLE_180)));
		}
	}

	Angle getBendingAngle()
	{
		return bendingAngle;
	}

	void setLength(SegmentLink inLink, double length)
	{
		if (length < 0) {
			this.length = 0;
		} else {
			this.length = length;
		}

		adjustShape(inLink, length, bendingAngle);
	}

	double getLength()
	{
		return length;
	}

	void set(SegmentLink inLink, double length, Angle exitAngle)
	{
		if (length < 0) {
			this.length = 0;
		} else {
			this.length = length;
		}
		bendingAngle = exitAngle;

		adjustShape(inLink, this.length, bendingAngle);
	}

	@Override
	public boolean isCrossing()
	{
		return false;
	}

	@Override
	public boolean isStraight()
	{
		return type == SegmentType.STRAIGHT || type == SegmentType.STRAIGHT_WITH_CROSSWALK;
	}

	@Override
	public boolean isMerging()
	{
		return type == SegmentType.MERGING_START || type == SegmentType.MERGING_MIDDLE ||
				type == SegmentType.MERGING_IN || type == SegmentType.MERGING_END;
	}

	@Override
	public boolean isMergingOut()
	{
		return (isMerging() && !isMergingIn());
	}

	@Override
	public boolean isMergingIn()
	{
		return type == SegmentType.MERGING_IN;
	}

	@Override
	public boolean isCurve()
	{
		return type == SegmentType.CURVE_BIG || type == SegmentType.CURVE_SMALL;
	}

	@Override
	public boolean isSCurve()
	{
		return type == SegmentType.S_CURVE_BOTTOM || type == SegmentType.S_CURVE_TOP;
	}

	boolean isLeftCurve()
	{
		return isCurve() && bendingAngle.radians() >= 0;
	}

	boolean isRightCurve()
	{
		return isCurve() && bendingAngle.radians() < 0;
	}

	/**
	 * @return the position of this segment as the south-west corner
	 */
	@Override
	public Vector3D getPosition()
	{
		if (isStraight()) {
			return super.getPosition();
		}

		if (isMerging()) {
			if (type == SegmentType.MERGING_START) {
				return super.getPosition();
			} else if (type == SegmentType.MERGING_IN) {
				if (!hasInOption(Direction.NORTH)) {
					IPose3D pose = getInOption(Direction.EAST).getPose();
					return new Vector3D(pose.getX() - CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, pose.getY(), pose.getZ());
				} else if (!hasInOption(Direction.WEST)) {
					IPose3D pose = getInOption(Direction.SOUTH).getPose();
					return new Vector3D(pose.getX(), pose.getY() - CROSSING_HALF_LENGTH + LANE_HALF_WIDTH, pose.getZ());
				} else if (!hasInOption(Direction.SOUTH)) {
					IPose3D pose = getInOption(Direction.EAST).getPose();
					return new Vector3D(pose.getX() - CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, pose.getY(), pose.getZ());
				} else { // WEST
					IPose3D pose = getInOption(Direction.SOUTH).getPose();
					return new Vector3D(pose.getX(), pose.getY() - CROSSING_HALF_LENGTH + LANE_HALF_WIDTH, pose.getZ());
				}
			} else { // merging middle or merging end
				if (hasInOption(Direction.SOUTH)) {
					IPose3D pose = getInOption(Direction.SOUTH).getPose();
					return new Vector3D(pose.getX(), pose.getY() - CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, pose.getZ());
				} else if (hasInOption(Direction.EAST)) {
					IPose3D pose = getInOption(Direction.EAST).getPose();
					return new Vector3D(pose.getX() - CROSSING_HALF_LENGTH + LANE_HALF_WIDTH, pose.getY(), pose.getZ());
				} else if (hasInOption(Direction.NORTH)) {
					IPose3D pose = getInOption(Direction.NORTH).getPose();
					return new Vector3D(pose.getX() - CROSSING_LENGTH,
							pose.getY() - CROSSING_HALF_LENGTH + LANE_HALF_WIDTH, pose.getZ());
				} else { // WEST
					IPose3D pose = getInOption(Direction.WEST).getPose();
					return new Vector3D(pose.getX() - CROSSING_HALF_LENGTH - LANE_HALF_WIDTH,
							pose.getY() - CROSSING_LENGTH, pose.getZ());
				}
			}
		}

		// curves
		if (hasInOption(Direction.SOUTH) && hasInOption(Direction.WEST)) {
			return super.getPosition();
		}

		if (hasInOption(Direction.EAST) && hasInOption(Direction.NORTH)) {
			return super.getPosition();
		}

		if (hasInOption(Direction.SOUTH) && hasInOption(Direction.EAST)) {
			double factor = 2.5;
			if (type == SegmentType.CURVE_SMALL) {
				factor = 1.5;
			}
			IPose3D pose = getInOption(Direction.SOUTH).getPose();
			return new Vector3D(pose.getX(), pose.getY() - factor * CROSSING_LENGTH + LANE_HALF_WIDTH, pose.getZ());
		}

		if (hasInOption(Direction.WEST) && hasInOption(Direction.NORTH)) {
			double factor = 2.5;
			if (type == SegmentType.CURVE_SMALL) {
				factor = 1.5;
			}
			IPose3D pose = getInOption(Direction.NORTH).getPose();
			return new Vector3D(pose.getX() - (factor + 0.5) * CROSSING_LENGTH,
					pose.getY() - CROSSING_HALF_LENGTH - LANE_HALF_WIDTH, pose.getZ());
		}

		// s curves
		if (type == SegmentType.S_CURVE_TOP) {
			if (hasInOption(Direction.SOUTH)) {
				IPose3D pose = getInOption(Direction.SOUTH).getPose();
				return new Vector3D(pose.getX(), pose.getY() - 2.5 * CROSSING_LENGTH + LANE_HALF_WIDTH, pose.getZ());
			} else {
				return super.getPosition();
			}
		}

		else {
			// s curve bottom
			if (hasInOption(Direction.SOUTH)) {
				return super.getPosition();
			} else {
				IPose3D pose = getInOption(Direction.EAST).getPose();
				return new Vector3D(pose.getX() - 2.5 * CROSSING_LENGTH - LANE_HALF_WIDTH, pose.getY(), pose.getZ());
			}
		}
	}

	/**
	 * @return the rotation of the segment with respect to the AADC manual's tile orientation
	 */
	@Override
	public Angle getRotation()
	{
		if (isStraight()) {
			if (hasOutOption(Direction.EAST)) {
				return Angle.ANGLE_90;
			}
			return Angle.ZERO;
		}

		if (isMerging()) {
			if (getType() == SegmentType.MERGING_IN) {
				if (!hasInOption(Direction.SOUTH)) {
					return Angle.ANGLE_90;
				} else if (!hasInOption(Direction.EAST)) {
					return Angle.ANGLE_180;
				} else if (!hasInOption(Direction.NORTH)) {
					return Angle.ANGLE_90.negate();
				} else { // no in option WEST
					return Angle.ZERO;
				}
			}
			if (hasInOption(Direction.SOUTH)) {
				return Angle.ANGLE_180;
			} else if (hasInOption(Direction.EAST)) {
				return Angle.ANGLE_90.negate();
			} else if (hasInOption(Direction.NORTH)) {
				return Angle.ZERO;
			} else { // WEST
				return Angle.ANGLE_90;
			}
		}

		if (isSCurve()) {
			if (getType() == SegmentType.S_CURVE_BOTTOM) {
				if (hasOutOption(Direction.WEST)) {
					return Angle.ANGLE_90;
				}
				return Angle.ZERO;
			}
		}

		// we have a curve
		if (hasOutOption(Direction.NORTH) && hasOutOption(Direction.WEST)) {
			return Angle.ANGLE_90;
		} else if (hasOutOption(Direction.EAST) && hasOutOption(Direction.SOUTH)) {
			return Angle.ANGLE_90.negate();
		} else if (hasOutOption(Direction.WEST) && hasOutOption(Direction.SOUTH)) {
			return Angle.ANGLE_180;
		}
		return Angle.ZERO;
	}

	@Override
	public Segment getNextOrCurrentSegmentFromPose(SegmentLink inLink, IPose3D pose)
	{
		// set middle of in link as origin
		IPose3D shiftToMiddleOfRoad = new Pose3D(0, ISegment.LANE_HALF_WIDTH);
		IPose3D beginLinkPose = inLink.getPose().applyTo(shiftToMiddleOfRoad);
		SegmentLink endLink = getSameLaneOutOption(inLink.getDirection());
		IPose3D endLinkPose = endLink.getPose().applyTo(shiftToMiddleOfRoad);

		// get the middlePoint between begin and end and set coordinate system, so its easy to use

		// TODO 3D
		Angle middleAngle = beginLinkPose.getHorizontalAngle().add(beginLinkPose.getHorizontalAngleTo(endLinkPose));
		IPose3D middleConnection = new Pose3D(new Vector3D((beginLinkPose.getX() + endLinkPose.getX()) * 0.5,
													  (beginLinkPose.getY() + endLinkPose.getY()) * 0.5,
													  (beginLinkPose.getZ() + endLinkPose.getZ()) * 0.5),
				middleAngle);

		IPose3D poseRelativeToMiddleConnection = middleConnection.applyInverseTo(pose);
		// these are guaranteed to be on this segment
		if (poseRelativeToMiddleConnection.getX() < 0) {
			return this;
		}

		IPose3D poseRelativeToEnd = endLinkPose.applyInverseTo(pose);
		if (poseRelativeToEnd.getX() < 0) {
			return this;
		}

		return endLink.getSegmentAfter();
	}

	/**
	 * @param thisOutOption the direction of the out option we do not want to have
	 * @return the other out option with respect to the passed direction
	 */
	public SegmentLink getOtherOutOption(Direction thisOutOption)
	{
		for (Direction current : Direction.values()) {
			if (hasOutOption(current) && current != thisOutOption) {
				return getOutOption(current);
			}
		}
		// should never happen
		return null;
	}

	/**
	 * @param thisInOption the direction of the in option we do not want to have
	 * @return the other in option with respect to the passed direction
	 */
	public SegmentLink getOtherInOption(Direction thisInOption)
	{
		for (Direction current : Direction.values()) {
			if (hasInOption(current) && current != thisInOption) {
				return getInOption(current);
			}
		}
		// should never happen
		return null;
	}

	/**
	 * @param facingDirection the direction we are facing when entering the segment
	 * @return the out link of the lane specified by in link
	 */
	@Override
	public SegmentLink getSameLaneOutOption(Direction facingDirection)
	{
		return getOtherOutOption(Direction.getOppositeDirection(facingDirection));
	}

	/**
	 * @param facingDirection the direction we are facing when entering the segment
	 * @return the in link of the lane specified by facingDirection
	 */
	public SegmentLink getSameLaneInOption(Direction facingDirection)
	{
		return getOtherInOption(Direction.getOppositeDirection(facingDirection));
	}

	/**
	 * @param inPose the pose that marks the start of the curve
	 * @param length the length of the middle of the full lane
	 * @param bendingAngle the angle of the curve, 0 if straight
	 * @return the out pose of a curve or straight from the passed pose
	 */
	public static IPose3D getCurveOutPose(IPose3D inPose, double length, Angle bendingAngle)
	{
		double lengthOut = getCurveLength(length, bendingAngle);
		return inPose.applyTo(new Pose3D(DriveGeometry.calculateArcPose(lengthOut, bendingAngle)));
	}

	/**
	 * @param middleLength the length of the curve in the middle of the street
	 * @param exitAngle the angle in which we exit the curve
	 * @return the length of the curve on our (right) lane
	 */
	private static double getCurveLength(double middleLength, Angle exitAngle)
	{
		double angle = exitAngle.radians();
		if (FuzzyCompare.isZero(angle)) {
			return middleLength;
		}

		double radius = middleLength / angle;
		// we always add since the 'radius' is negative on right curves
		radius += ISegment.LANE_HALF_WIDTH;

		return Math.max(0, radius * angle);
	}

	// ===== create methods ==========
	/**
	 * Creates a first straight or curve segment facing north and its two in and out links.
	 * @param initialPose the pose we start with
	 * @param length the length we want to have it
	 * @param angle the bending angle if not straight
	 * @return the first segment
	 */
	public static NonCrossingSegment createInitialSegment(IPose3D initialPose, double length, Angle angle)
	{
		NonCrossingSegment initialSegment = new NonCrossingSegment(length, Angle.ZERO, SegmentType.STRAIGHT);
		IPose3D initialInPose = initialPose.applyTo(new Pose3D(0, -ISegment.LANE_HALF_WIDTH));
		SegmentLink initialInLink = new SegmentLink(initialInPose, null, initialSegment);
		IPose3D initialOutPose = initialPose.applyTo(new Pose3D(0, ISegment.LANE_HALF_WIDTH, Angle.ANGLE_180));
		SegmentLink initialOutLink = new SegmentLink(initialOutPose, initialSegment, null);

		Direction direction = initialInLink.getDirection();
		Direction oppositeDir = Direction.getOppositeDirection(direction);
		initialSegment.setInOption(oppositeDir, initialInLink);
		initialSegment.setOutOption(oppositeDir, initialOutLink);

		SegmentLink otherOutLink =
				new SegmentLink(NonCrossingSegment.getCurveOutPose(initialInPose, length, angle), initialSegment, null);
		SegmentLink otherInLink = new SegmentLink(
				NonCrossingSegment.getInPoseFromCorrespondingOutPose(otherOutLink.getPose()), null, initialSegment);
		Direction outDirection = otherOutLink.getDirection();
		initialSegment.setInOption(outDirection, otherInLink);
		initialSegment.setOutOption(outDirection, otherOutLink);

		return initialSegment;
	}
}
