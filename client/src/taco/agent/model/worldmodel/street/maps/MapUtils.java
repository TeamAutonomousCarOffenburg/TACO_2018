package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.worldmodel.signdetection.RoadSign;
import taco.agent.model.worldmodel.street.*;
import taco.util.SignType;
import taco.util.serializer.helper.AADCConfiguration;
import taco.util.serializer.helper.AADCRoadsign;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static utils for Maps
 */
public class MapUtils
{
	private static final IPose3D signTranslation = new Pose3D(-0.2, -0.25);

	private static final IPose3D crosswalkSignTranslation = new Pose3D(0.0, -0.25);

	public static Segment appendStraights(Segment segment, Direction dir, int n)
	{
		return appendStraights(segment, dir, n, IAudiCupMotor.DEFAULT_SPEED);
	}

	public static Segment appendStraights(Segment segment, Direction dir, int n, double speed)
	{
		return appendStraights(segment, dir, n, speed, speed);
	}

	public static Segment appendStraights(
			Segment segment, Direction dir, int n, double speedOurLane, double speedOtherLane)
	{
		for (int i = 0; i < n; i++) {
			segment = segment.getOutOption(dir).appendStraightSegment(speedOurLane, speedOtherLane);
		}
		return segment;
	}

	public static Segment appendCrosswalk(Segment segment, Direction dir)
	{
		segment = segment.getOutOption(dir).appendStraightSegmentWithCrosswalk(
				1, IAudiCupMotor.LOW_SPEED, IAudiCupMotor.LOW_SPEED);
		addCrosswalkSign(segment, dir);
		addCrosswalkSign(segment, Direction.getOppositeDirection(dir));
		return segment;
	}

	public static void addSign(Segment segment, Direction dir, SignType type)
	{
		if (segment.hasInOption(dir)) {
			segment.getInOption(dir).addRoadSign(
					new RoadSign(type, segment.getInOption(dir).getPose().applyTo(signTranslation)));
		}
	}

	public static void addStopLine(Segment segment, Direction dir)
	{
		segment.getOutOption(dir).getSegmentAfter().getOutOption(Direction.getOppositeDirection(dir)).addStopLine();
	}

	public static void addCrosswalkSign(Segment segment, Direction dir)
	{
		if (segment.hasInOption(dir)) {
			segment.getInOption(dir).addRoadSign(new RoadSign(
					SignType.CROSSWALK, segment.getInOption(dir).getPose().applyTo(crosswalkSignTranslation)));
		}
	}

	public static void addParkingVertical(StreetMap map, int segmentID, Direction dir, int firstID, boolean[] occupied)
	{
		addParking(map, segmentID, dir, SegmentType.PARKING_SPACE_VERTICAL, firstID, occupied, 0.25);
	}

	public static void addParkingVertical(
			StreetMap map, int segmentID, Direction dir, int firstID, boolean ascending, boolean[] occupied)
	{
		addParking(map, segmentID, dir, SegmentType.PARKING_SPACE_VERTICAL, firstID, ascending, occupied, 0.25);
	}

	public static void addParkingHorizontal(
			StreetMap map, int segmentID, Direction dir, int firstID, boolean[] occupied)
	{
		addParking(map, segmentID, dir, SegmentType.PARKING_SPACE_HORIZONTAL, firstID, occupied, 0.25);
	}

	public static ParkingSegment addParking(StreetMap map, int segmentID, Direction dir, SegmentType type, int firstID,
			boolean[] occupied, double translation)
	{
		return addParking(map, segmentID, dir, type, firstID, true, occupied, translation);
	}

	public static ParkingSegment addParking(StreetMap map, int segmentID, Direction dir, SegmentType type, int firstID,
			boolean ascending, boolean[] occupied, double translation)
	{
		Segment segment = map.getSegment(segmentID);
		SegmentLink parking = segment.getInOption(dir);
		ParkingSegment parkingSegment =
				new ParkingSegment(map, type, firstID, ascending, parking, translation, occupied);
		parking.getSegmentAfter().setAttachedOption(parkingSegment.getInOptionByParkingID(firstID));
		addSign(segment, dir, SignType.PARKING_AREA);
		return parkingSegment;
	}

	/**
	 * Loads the roadsigns from a given roadsign.xml and adds the signs to the given map
	 * @param map map to add signs into
	 * @param path filepath to the roadsign.xml
	 */
	public static void addSignsByXML(StreetMap map, String path)
	{
		List<RoadSign> roadSigns = loadRoadSignsFromXML(path);
		for (RoadSign roadSign : roadSigns) {
			IPose3D roadSignPose = roadSign.getPose();
			Segment segment = map.getSegmentContainingOnlyXorY(roadSignPose.getPosition(), Collections.emptyList());
			if (segment != null) {
				Angle roadSignAngle = roadSignPose.getHorizontalAngle();
				if (segment.hasInOption(Direction.getDirection(roadSignAngle))) {
					segment.getInOption(Direction.getDirection(roadSignAngle))
							.addRoadSign(new RoadSign(roadSign.getSignType(), roadSignPose));
				} else {
					// check again with a filter to get really the required segment
					List<Segment> checked = new ArrayList<>();
					checked.add(segment);
					segment = map.getSegmentContainingOnlyXorY(roadSignPose.getPosition(), checked);
					if (segment != null) {
						if (segment.hasInOption(Direction.getDirection(roadSignAngle))) {
							segment.getInOption(Direction.getDirection(roadSignAngle))
									.addRoadSign(new RoadSign(roadSign.getSignType(), roadSignPose));
						}
					}
				}
			}
		}
	}

	private static List<RoadSign> loadRoadSignsFromXML(String path)
	{
		AADCConfiguration aadcConfiguration = JAXB.unmarshal(new File(path), AADCConfiguration.class);
		List<AADCRoadsign> roadSigns = aadcConfiguration.getRoadSigns();
		List<RoadSign> signs = new ArrayList<>();
		for (AADCRoadsign aadcRoadsign : roadSigns) {
			for (SignType value : SignType.values()) {
				if (value == SignType.CROSSWALK || value == SignType.PARKING_AREA) {
					continue;
				}
				if (aadcRoadsign.getInit() == 1) {
					aadcRoadsign.markAsSectorSign();
				}
				if (value.getValue() == aadcRoadsign.getId()) {
					RoadSign newRoadsign = new RoadSign(
							value, new Pose3D(aadcRoadsign.getX(), aadcRoadsign.getY(),
										   Direction.getDirection(Angle.deg(aadcRoadsign.getDirection())).getAngle()));
					newRoadsign.setInitSign(aadcRoadsign.getInit() == 1);
					signs.add(newRoadsign);
				}
			}
		}
		return signs;
	}
}
