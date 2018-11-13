package taco.agent.model.worldmodel.lanedetection;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import hso.autonomy.util.misc.FuzzyCompare;
import taco.agent.communication.perception.RecognizedObject;
import taco.agent.communication.perception.impl.LaneMiddlePerceptor;
import taco.agent.model.agentmodel.ICameraSensor;
import taco.agent.model.worldmodel.DriveInstruction;
import taco.agent.model.worldmodel.ILaneMiddleSensor;
import taco.agent.model.worldmodel.impl.Obstacle;
import taco.agent.model.worldmodel.street.ISegment;
import taco.agent.model.worldmodel.street.RuntimeSegment;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.SegmentLink;
import taco.agent.model.worldmodel.street.SegmentType;
import taco.agent.model.worldmodel.street.StreetMap;

public class LaneMiddleSensor implements ILaneMiddleSensor
{
	public static final double DEFAULT_RELIABILITY_THRESHOLD = 0.4;

	private static final float ALLOW_LATERAL_REPOSITIONING_TIME = 3;

	private static final float ALLOW_SAGITTAL_REPOSITIONING_TIME = 2;

	private static final float KEEP_LANE_DETECTION_IN_MEMORY_TIME = 0.3f;

	/** history of valid, consecutive lane middle detections */
	private List<HistoryEntry> sensorHistory;

	/** the latest detection, may be invalid */
	private LaneMiddle current;

	/** time of last lateral repositioning */
	private float lastLateralRepositioningTime;

	/** time of last sagittal repositioning */
	private float lastSagittalRepositioningTime;

	/** time when we saw a lane middle detection from object detection */
	private float lastLaneMiddleSeen;

	/** the last lane middle object detection */
	private RecognizedObject lastLaneMiddle;

	/** the number of times follow lane was performed consecutively */
	private int consecutiveFollowRightLanePerforms;

	private IPose3D lastLateralRepositioningPose;

	private Angle angleOffset;

	private boolean useLanesFromMapIfDetectedLanesAreInvalid = false;

	private boolean turnedRight;

	public LaneMiddleSensor()
	{
		sensorHistory = new LinkedList<>();
		current = new LaneMiddle();
		lastLateralRepositioningTime = 0;
		lastSagittalRepositioningTime = 0;
		lastLaneMiddleSeen = 0;
		consecutiveFollowRightLanePerforms = 0;
		angleOffset = Angle.ZERO;
		turnedRight = false;
	}

	@Override
	public void update(
			LaneMiddlePerceptor perceptor, float time, IPose3D carPose, List<Obstacle> laneMiddles, StreetMap map)
	{
		current = new LaneMiddle();
		current.update(perceptor, carPose, map);

		// check plausibility with respect to lane middle object detection
		if (!current.isValid()) {
			// TODO: use this line to switch on plausibility check
			// if (!current.isValid() || !isPlausible(time, laneMiddles)) {
			if (useLanesFromMapIfDetectedLanesAreInvalid) {
				current.takeLaneValuesFromMap(perceptor.getTimeStamp(), carPose, map);
			} else {
				return;
			}
		}

		// check if this is a new reading after some time
		if (!sensorHistory.isEmpty() && time - sensorHistory.get(0).time > 1.0) {
			sensorHistory.clear();
		}

		// check plausibility with map
		if (current.isValid()) {
			adjustToMap(carPose, map, time);

			HistoryEntry newEntry = new HistoryEntry(current, time, carPose);
			sensorHistory.add(0, newEntry);
			if (sensorHistory.size() > 30) {
				// keep history limited
				sensorHistory.remove(sensorHistory.size() - 1);
			}
		}
	}

	private void adjustToMap(IPose3D carPose, StreetMap map, float time)
	{
		if (sensorHistory.isEmpty()) {
			return;
		}
		// checks if detection might have mixed up right, middle and left lines
		double avgDistanceNew = current.calculateAverageDistance();
		double lastAverageDistance = sensorHistory.get(0).middle.calculateAverageDistance();
		if (Math.abs(avgDistanceNew - lastAverageDistance) > 0.15 || avgDistanceNew < 0.00001) {
			// System.out.println("last " + lastAverageDistance);
			// System.out.println("new " + avgDistanceNew);
			// we have a jump
			if (avgDistanceNew > lastAverageDistance || avgDistanceNew < 0.00001) {
				// try to repair since we seem to get further away from map
				double[] distanceToMapMiddle = current.getDistancesToMapMiddle();

				double laneWidth = 0.45;
				if (distanceToMapMiddle[0] >= 0 && distanceToMapMiddle[1] >= 0 &&
						distanceToMapMiddle[0] < distanceToMapMiddle[1] && avgDistanceNew > 0.00001 &&
						FuzzyCompare.eq(current.getGlobalScanPointDistance(0, 1), laneWidth, 0.25)) {
					// mixed up right with middle line
					current.swapRightMiddle();

				} else if (distanceToMapMiddle[2] >= 0 && distanceToMapMiddle[1] >= 0 &&
						   distanceToMapMiddle[2] < distanceToMapMiddle[1] && avgDistanceNew > 0.00001 &&
						   FuzzyCompare.eq(current.getGlobalScanPointDistance(2, 1), laneWidth, 0.25)) {
					// mixed up middle with left line
					current.swapMiddleLeft();

				} else {
					// can not be repaired, do not believe
					current.setValid(false);
				}
			}
		}
		/*
		if (avgDistanceNew > 0.2 && (time < 10f || time - lastLateralRepositioningTime < 5f)) {
			System.out.println("Believe map");
			current.setValid(false);
		}
		*/

		if (current.isSwapped()) {
			// positions have changed so recalculate avg distance
			avgDistanceNew = current.calculateAverageDistance();
			if (avgDistanceNew > 0.15) {
				current.setValid(false);
			}
		}
	}

	private boolean isPlausible(float time, List<RecognizedObject> laneMiddles)
	{
		RecognizedObject lane;
		if (laneMiddles.isEmpty()) {
			if (lastLaneMiddle != null && time - lastLaneMiddleSeen < KEEP_LANE_DETECTION_IN_MEMORY_TIME) {
				// saw a lane recently
				lane = lastLaneMiddle;
			} else {
				// too long since last detection
				return true;
			}
		} else {
			// for now we take the first one, but could look for the lowest highest y value
			lane = laneMiddles.stream().reduce((a, b) -> a.getArea().getMaxY() > b.getArea().getMaxY() ? a : b).get();
			lastLaneMiddle = lane;
			lastLaneMiddleSeen = time;
		}

		int seenX = lane.getArea().getMaxX() - lane.getArea().getMinX();

		int middleLineX = current.getMiddleLineX();
		int halfLaneWidthInPixel = LaneMiddle.cameraToPixel(ISegment.LANE_HALF_WIDTH);
		if (middleLineX < 0) {
			// we did not see the middle line, so guess it from middle of lane
			middleLineX = current.getMiddleX() - halfLaneWidthInPixel;
			if (middleLineX < 0) {
				// the middle is outside the image? we better not believe
				return false;
			}
		}
		if (Math.abs(middleLineX - seenX) > halfLaneWidthInPixel) {
			// line detection too far from object detection
			return false;
		}
		return true;
	}

	@Override
	public void calculateAngleOffset(RuntimeSegment currentSegment, IPose3D carPose)
	{
		Angle angle = currentSegment.getInLink().getPose().getHorizontalAngle();
		Angle carAngle = carPose.getHorizontalAngle();
		Angle adjustedAngle = getAngleAdjustment(angle, carAngle, 360., 360.);
		if (adjustedAngle == null) {
			return;
		}
		angleOffset = adjustedAngle.subtract(carAngle);
	}

	@Override
	public IPose3D calculateLateralRepositioning(
			RuntimeSegment currentSegment, float time, IPose3D carPose, StreetMap map)
	{
		if (time - lastLateralRepositioningTime < ALLOW_LATERAL_REPOSITIONING_TIME) {
			// not long enough since last repositioning
			return null;
		}

		if (turnedRight) {
			// one segment change after turn right is still not reliable
			return null;
		}

		if (lastLateralRepositioningPose != null && carPose.getDistanceTo(lastLateralRepositioningPose) < 0.3) {
			return null;
		}

		if (sensorHistory.size() < 4) {
			return null;
		}

		for (int i = 0; i < 4; i++) {
			HistoryEntry historyEntry = sensorHistory.get(i);
			if (Math.abs(historyEntry.middle.getDeltaX()) > 15) {
				// we drive non straight
				return null;
			}
		}

		if (consecutiveFollowRightLanePerforms < 20) {
			return null;
		}

		if (currentSegment.getSegment().getType() == SegmentType.MERGING_START ||
				currentSegment.getSegment().getType() == SegmentType.MERGING_END) {
			return null;
		}

		Segment lastSegment = currentSegment.getInLink().getSegmentBefore();
		Segment nextSegment = currentSegment.getIntendedOption().getSegmentAfter();
		if (currentSegment.getSegment().isCurve() || currentSegment.getSegment().isSCurve() ||
				!(lastSegment.isStraight() || lastSegment.isMergingIn()) || !(nextSegment.isStraight()) ||
				nextSegment.isMergingIn()) {
			// we only reposition if on a straight segment
			return null;
		}

		// we drive 20cm straight now, calculate pose based on current segment
		IPose3D inPose = currentSegment.getInLink().getPose();
		double distance = carPose.getDistanceTo(inPose);
		Angle angle = inPose.getHorizontalAngle();
		Vector3D offset = angle.applyTo(new Vector3D(distance, 0, 0));
		// TODO: why does this IPose2D return a 3D vector???
		Vector3D newPosition = inPose.getPosition().add(offset);
		Angle carAngle = carPose.getHorizontalAngle();
		angle = getAngleAdjustment(angle, carAngle, 20., 3.);
		if (angle == null) {
			return null;
		}

		IPose3D newPose = new Pose3D(newPosition, angle);
		if (newPose.getDistanceTo(carPose) > 0.5) {
			// do not allow large repositioning
			return null;
		}

		lastLateralRepositioningTime = time;
		lastLateralRepositioningPose = carPose;
		// System.out.println("segmentId: " + currentSegment.getID() + "segmentType: " +
		//				   currentSegment.getSegment().getType() + " Car Angle " + carAngle + " Angle new " + angle);
		return newPose;
	}

	@Override
	public IPose3D calculateLateralRepositioningOnSegmentChange(
			RuntimeSegment previousSegment, RuntimeSegment currentSegment, IPose3D carPose, double delta)
	{
		if (previousSegment.getDriveInstruction() == DriveInstruction.RIGHT) {
			// we just turned right, so no reposition
			turnedRight = true;
			return null;
		}

		if (turnedRight) {
			// one segment change after turn right is still not reliable
			turnedRight = false;
			return null;
		}

		if (sensorHistory.size() < 4) {
			return null;
		}

		for (int i = 0; i < 4; i++) {
			HistoryEntry historyEntry = sensorHistory.get(i);
			if (Math.abs(historyEntry.middle.getDeltaX()) > 15) {
				// we drive non straight
				return null;
			}
		}

		if (consecutiveFollowRightLanePerforms < 30) {
			return null;
		}

		if (currentSegment.getSegment().getType() == SegmentType.MERGING_START ||
				currentSegment.getSegment().getType() == SegmentType.MERGING_END) {
			return null;
		}

		// we are confident that after so many follow lane performs we are in the middle of the track
		IPose3D mapPose = currentSegment.getInLink().getPose();
		if (mapPose.getDistanceTo(carPose) > 0.5) {
			// do not allow large repositioning
			return null;
		}

		Angle newAngle = carPose.getHorizontalAngle();
		Segment lastSegment = currentSegment.getInLink().getSegmentBefore();
		if (!currentSegment.getSegment().isCurve() && !currentSegment.getSegment().isSCurve() &&
				(lastSegment.isStraight() || lastSegment.isMergingIn())) {
			// when switching between two straights, we also reset the angle
			newAngle = getAngleAdjustment(mapPose.getHorizontalAngle(), newAngle, 20., 3.);
			if (newAngle == null) {
				return null;
			}
		}

		// we reposition slightly into the new segment, since the test does the same
		// mapPose = mapPose.applyTo(new Pose2D(delta, 0));

		// we do only sidewise adjustment
		IPose3D localCarPose = mapPose.applyInverseTo(carPose);
		IPose3D adjustedLocalCarPose =
				new Pose3D(new Vector3D(localCarPose.getX(), 0, localCarPose.getZ()), localCarPose.getOrientation());
		IPose3D newPose = mapPose.applyTo(adjustedLocalCarPose);
		/*System.out.println("On Segment changed segmentId: " + currentSegment.getID() +
						   "segmentType: " + currentSegment.getSegment().getType() + " Car Angle " +
						   carPose.getHorizontalAngle() + " Angle new " + newAngle);*/
		return new Pose3D(newPose.getPosition(), newAngle);
	}

	@Override
	public IPose3D calculateSagittalRepositioning(
			RuntimeSegment currentSegment, float time, IPose3D carPose, List<Obstacle> stopLines, ICameraSensor camera)
	{
		if (stopLines.isEmpty()) {
			return null;
		}

		if (time - lastSagittalRepositioningTime < ALLOW_SAGITTAL_REPOSITIONING_TIME) {
			// not long enough since last repositioning
			return null;
		}

		int aheadLineY = current.getAheadLineY();
		if (!isReliable(DEFAULT_RELIABILITY_THRESHOLD) || aheadLineY < 0) {
			// no reliable stop line
			return null;
		}

		Obstacle stopLine = stopLines.get(0);
		if (aheadLineY < stopLine.getArea().getMinY() || aheadLineY > stopLine.getArea().getMaxY()) {
			// the two stop lines do not match
			return null;
		}

		// find crossing we are heading to
		SegmentLink nextSegmentLink = currentSegment.getIntendedOption();
		Segment nextSegment = nextSegmentLink.getSegmentAfter();
		if (nextSegment != null && !nextSegment.isCrossing()) {
			// look for second next
			if (!nextSegment.isStraight()) {
				return null;
			}
			nextSegmentLink = nextSegment.getOutOption(currentSegment.getIntendedOption().getDirection());

			if (nextSegmentLink == null) {
				return null;
			}

			if (!nextSegmentLink.getSegmentAfter().isCrossing()) {
				return null;
			}
		}

		// calculate distance from pixel y coordinate
		Vector3D position = camera.pixelToCar(new Vector2D(current.getMiddleX(), aheadLineY));
		if (position == null) {
			return null;
		}
		double distance = position.getX();
		IPose3D newPose = nextSegmentLink.getPose().applyTo(new Pose3D(-distance, 0));
		if (newPose.getDistanceTo(carPose) > 0.8 || Math.abs(newPose.getDeltaHorizontalAngle(carPose).degrees()) > 5) {
			// do not allow large repositioning
			return null;
		}

		lastSagittalRepositioningTime = time;
		return newPose;
	}

	private Angle getAngleAdjustment(Angle angle, Angle carAngle, double maxDelta, double maxAdjustment)
	{
		double deltaAngle = Math.abs(carAngle.subtract(angle).degrees());
		if (deltaAngle > maxDelta) {
			return null;
		}
		double deltaAdjust = maxAdjustment;
		if (angle.isRightOf(carAngle)) {
			deltaAdjust = -deltaAdjust;
		}
		if (deltaAngle > maxAdjustment) {
			return carAngle.add(Angle.deg(deltaAdjust));
		}
		return angle;
	}

	@Override
	public int getDeltaX()
	{
		return current.getDeltaX();
	}

	@Override
	public int getLeftDeltaX()
	{
		int deltaX = current.getLeftDeltaX();

		if (deltaX == -1) {
			deltaX = getValidLaneMiddle();

			if (deltaX == -1) {
				return 0;
			}
		}

		return deltaX;
	}

	private int getValidLaneMiddle()
	{
		for (HistoryEntry entry : sensorHistory) {
			int deltaX = entry.middle.getLeftDeltaX();
			if (deltaX >= 0) {
				return deltaX;
			}
		}

		return -1;
	}

	@Override
	public int getConsecutiveFollowRightLanePerforms()
	{
		return consecutiveFollowRightLanePerforms;
	}

	@Override
	public int getLeftMiddleX()
	{
		return current.getLeftMiddleX();
	}

	@Override
	public LaneMiddle getCurrent()
	{
		return current;
	}

	@Override
	public boolean isReliable(double requiredConfidence)
	{
		return current.isValid() && current.getConfidence() >= requiredConfidence;
	}

	@Override
	public void updateConsecutiveFollowRightLanePerforms(int performs)
	{
		consecutiveFollowRightLanePerforms = performs;
	}

	@Override
	public List<HistoryEntry> getSensorHistory()
	{
		return sensorHistory;
	}

	@Override
	public int getHistorySize()
	{
		return sensorHistory.size();
	}

	@Override
	public HistoryEntry getHistoryEntry(int index)
	{
		return sensorHistory.get(index);
	}

	@Override
	public Angle getAngleOffset()
	{
		return angleOffset;
	}

	@Override
	public void setUseLanesFromMapIfDetectedLanesAreInvalid(boolean useLanesFromMapIfDetectedLanesAreInvalid)
	{
		this.useLanesFromMapIfDetectedLanesAreInvalid = useLanesFromMapIfDetectedLanesAreInvalid;
	}
}
