package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

import static taco.agent.model.worldmodel.street.Direction.*;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendCrosswalk;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendStraights;

public class MergingLaneEastMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 0.5), 1, Angle.ZERO);

		// right loop
		Segment current = appendStraights(rootSegment, NORTH, 1);
		Segment northCrossing = current.getOutOption(NORTH).appendTCrossingLR();
		current = northCrossing.getOutOption(WEST).appendCurveSmallLeft();
		current = current.getOutOption(SOUTH).appendStraightSegment();
		Segment westCrossing = current.getOutOption(SOUTH).appendTCrossingLS();
		current = appendStraights(westCrossing, EAST, 1);
		Segment xCrossing = current.getOutOption(EAST).appendXCrossing();
		xCrossing.getOutOption(NORTH).connectToInLink(rootSegment.getInOption(SOUTH));
		current = appendStraights(xCrossing, EAST, 1);
		Segment eastCrossing = current.getOutOption(EAST).appendTCrossingLR();
		current = appendStraights(eastCrossing, NORTH, 1);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current.getOutOption(WEST).connectToInLink(northCrossing.getInOption(EAST));

		// left inner loop
		current = appendStraights(eastCrossing, SOUTH, 1);
		Segment mergingIn3 = current.getOutOption(SOUTH).appendMergingInLS();
		Segment mergingIn2 = mergingIn3.getOutOption(SOUTH).appendMergingInLS();
		Segment mergingIn1 = mergingIn2.getOutOption(SOUTH).appendMergingInLS();
		current = mergingIn1.getOutOption(SOUTH).appendCurveSmallRight();
		Segment xSouthCrossing = current.getOutOption(WEST).appendXCrossing();
		current = xSouthCrossing.getOutOption(WEST).appendCurveSmallRight();
		current = appendStraights(current, NORTH, 4);
		current.getOutOption(NORTH).connectToInLink(westCrossing.getInOption(SOUTH));
		current = appendStraights(xSouthCrossing, NORTH, 5);
		current.getOutOption(NORTH).connectToInLink(xCrossing.getInOption(SOUTH));

		// left outer loop
		current = xSouthCrossing.getOutOption(SOUTH).appendCurveSmallLeft();
		current = current.getOutOption(EAST).appendCurveSmallLeft();
		current = appendStraights(current, NORTH, 1);
		Segment mergingStart = current.getOutOption(NORTH).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(NORTH).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(NORTH).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(NORTH).appendMergingEnd();
		mergingMiddle1.getOutOption(WEST).connectMergingLaneToInLink(mergingIn1.getInOption(EAST));
		mergingMiddle2.getOutOption(WEST).connectMergingLaneToInLink(mergingIn2.getInOption(EAST));
		mergingEnd.getOutOption(WEST).connectMergingLaneToInLink(mergingIn3.getInOption(EAST));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(29, map.getSegment(29).getOutOption(NORTH).getPose()));

		return map;
	}
}
