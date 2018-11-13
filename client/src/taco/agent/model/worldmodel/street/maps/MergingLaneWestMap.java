package taco.agent.model.worldmodel.street.maps;

import static taco.agent.model.worldmodel.street.Direction.EAST;
import static taco.agent.model.worldmodel.street.Direction.NORTH;
import static taco.agent.model.worldmodel.street.Direction.SOUTH;
import static taco.agent.model.worldmodel.street.Direction.WEST;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendStraights;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

public class MergingLaneWestMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 0.5), 1, Angle.ZERO);

		// right inner loop
		Segment current = appendStraights(rootSegment, NORTH, 1);
		Segment xNorthCrossing = current.getOutOption(NORTH).appendXCrossing();
		current = xNorthCrossing.getOutOption(WEST).appendCurveSmallLeft();
		current = current.getOutOption(SOUTH).appendStraightSegment();
		Segment mergingIn1 = current.getOutOption(SOUTH).appendMergingInSR();
		Segment mergingIn2 = mergingIn1.getOutOption(SOUTH).appendMergingInSR();
		Segment mergingIn3 = mergingIn2.getOutOption(SOUTH).appendMergingInSR();
		Segment westCrossing = mergingIn3.getOutOption(SOUTH).appendTCrossingLS();
		current = appendStraights(westCrossing, EAST, 1);
		Segment xCrossing = current.getOutOption(EAST).appendXCrossing();
		current = appendStraights(xCrossing, NORTH, 2);
		Segment straightNorth = appendStraights(current, NORTH, 1);
		straightNorth.getOutOption(NORTH).connectToInLink(rootSegment.getInOption(SOUTH));
		current = appendStraights(xCrossing, EAST, 1);
		Segment eastCrossing = current.getOutOption(EAST).appendTCrossingLR();
		current = appendStraights(eastCrossing, NORTH, 4);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current.getOutOption(WEST).connectToInLink(xNorthCrossing.getInOption(EAST));

		// left loop
		current = appendStraights(eastCrossing, SOUTH, 1);
		current = current.getOutOption(SOUTH).appendCurveSmallRight();
		Segment southCrossing = current.getOutOption(WEST).appendTCrossingSR();
		current = southCrossing.getOutOption(WEST).appendCurveSmallRight();
		current = appendStraights(current, NORTH, 1);
		current.getOutOption(NORTH).connectToInLink(westCrossing.getInOption(SOUTH));
		current = appendStraights(southCrossing, NORTH, 2);
		current.getOutOption(NORTH).connectToInLink(xCrossing.getInOption(SOUTH));

		// right outer loop
		current = xNorthCrossing.getOutOption(NORTH).appendCurveSmallLeft();
		current = current.getOutOption(WEST).appendCurveSmallLeft();
		current = appendStraights(current, SOUTH, 2);
		Segment mergingStart = current.getOutOption(SOUTH).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(SOUTH).appendMergingEnd();

		mergingMiddle1.getOutOption(EAST).connectMergingLaneToInLink(mergingIn1.getInOption(WEST));
		mergingMiddle2.getOutOption(EAST).connectMergingLaneToInLink(mergingIn2.getInOption(WEST));
		mergingEnd.getOutOption(EAST).connectMergingLaneToInLink(mergingIn3.getInOption(WEST));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));

		return map;
	}
}
