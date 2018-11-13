package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

import static taco.agent.model.worldmodel.street.Direction.*;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendStraights;

public class MergingLaneSouthOverlapMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 0.5), 1, Angle.ZERO);

		// right loop
		Segment current = appendStraights(rootSegment, NORTH, 1);
		Segment xNorthCrossing = current.getOutOption(NORTH).appendXCrossing();
		current = appendStraights(xNorthCrossing, WEST, 7);
		current = current.getOutOption(WEST).appendCurveSmallLeft();
		current = current.getOutOption(SOUTH).appendStraightSegment();
		Segment westCrossing = current.getOutOption(SOUTH).appendTCrossingLS();
		current = appendStraights(westCrossing, EAST, 8);
		Segment xCrossing = current.getOutOption(EAST).appendXCrossing();
		xCrossing.getOutOption(NORTH).connectToInLink(rootSegment.getInOption(SOUTH));
		current = appendStraights(xCrossing, EAST, 1);
		Segment eastCrossing = current.getOutOption(EAST).appendTCrossingLR();
		current = appendStraights(eastCrossing, NORTH, 1);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current.getOutOption(WEST).connectToInLink(xNorthCrossing.getInOption(EAST));

		// left loop
		current = appendStraights(eastCrossing, SOUTH, 1);
		current = current.getOutOption(SOUTH).appendCurveSmallRight();
		Segment southCrossing = current.getOutOption(WEST).appendTCrossingSR();
		Segment mergingIn1 = southCrossing.getOutOption(WEST).appendMergingInLS();
		Segment mergingIn2 = mergingIn1.getOutOption(WEST).appendMergingInLS();
		Segment mergingIn3 = mergingIn2.getOutOption(WEST).appendMergingInLS();
		current = appendStraights(mergingIn3, WEST, 4);
		current = current.getOutOption(WEST).appendCurveSmallRight();
		current = appendStraights(current, NORTH, 1);
		current.getOutOption(NORTH).connectToInLink(westCrossing.getInOption(SOUTH));
		current = appendStraights(southCrossing, NORTH, 2);
		current.getOutOption(NORTH).connectToInLink(xCrossing.getInOption(SOUTH));

		// outer loop
		current = appendStraights(xNorthCrossing, NORTH, 1);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current = appendStraights(current, WEST, 4);
		current = current.getOutOption(WEST).appendCurveSmallLeft();
		current = appendStraights(current, SOUTH, 7);
		current = current.getOutOption(SOUTH).appendCurveSmallLeft();
		current = appendStraights(current, EAST, 1);

		Segment mergingStart = current.getOutOption(EAST).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(EAST).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(EAST).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(EAST).appendMergingEnd();

		mergingMiddle1.getOutOption(NORTH).connectMergingLaneToInLink(mergingIn3.getInOption(SOUTH));
		mergingMiddle2.getOutOption(NORTH).connectMergingLaneToInLink(mergingIn2.getInOption(SOUTH));
		mergingEnd.getOutOption(NORTH).connectMergingLaneToInLink(mergingIn1.getInOption(SOUTH));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(52, map.getSegment(52).getOutOption(SOUTH).getPose()));

		return map;
	}
}
