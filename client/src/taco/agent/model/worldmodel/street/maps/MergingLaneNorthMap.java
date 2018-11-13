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

public class MergingLaneNorthMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 0.5), 1, Angle.ZERO);

		// left loop
		Segment centralCrossing = rootSegment.getOutOption(NORTH).appendTCrossingLR();
		Segment current = centralCrossing.getOutOption(WEST).appendStraightSegment();
		Segment zebra = current.getOutOption(WEST).appendStraightSegment();
		Segment westCrossing = zebra.getOutOption(WEST).appendTCrossingLR();
		current = westCrossing.getOutOption(SOUTH).appendCurveSmallLeft();
		current = current.getOutOption(EAST).appendStraightSegment();
		Segment southCrossing = current.getOutOption(EAST).appendTCrossingLS();
		current = southCrossing.getOutOption(EAST).appendStraightSegment();
		current = current.getOutOption(EAST).appendCurveSmallLeft();
		Segment xEastCrossing = current.getOutOption(NORTH).appendXCrossing();
		current = xEastCrossing.getOutOption(WEST).appendStraightSegment();
		current = current.getOutOption(WEST).appendStraightSegment();
		// connect
		southCrossing.getOutOption(NORTH).connectToInLink(rootSegment.getInOption(SOUTH));
		current.getOutOption(WEST).connectToInLink(centralCrossing.getInOption(EAST));

		// right inner loop
		current = xEastCrossing.getOutOption(NORTH).appendCurveSmallLeft();
		Segment mergingIn1 = current.getOutOption(WEST).appendMergingInSR();
		Segment mergingIn2 = mergingIn1.getOutOption(WEST).appendMergingInSR();
		Segment mergingIn3 = mergingIn2.getOutOption(WEST).appendMergingInSR();
		current = mergingIn3.getOutOption(WEST).appendCurveSmallLeft();
		// connect
		current.getOutOption(SOUTH).connectToInLink(westCrossing.getInOption(NORTH));

		// right outer loop
		Segment curveOut = xEastCrossing.getOutOption(EAST).appendCurveSmallLeft();
		Segment curveIn = curveOut.getOutOption(NORTH).appendCurveSmallLeft();
		Segment westStraight = curveIn.getOutOption(WEST).appendStraightSegment();
		Segment mergingStart = westStraight.getOutOption(WEST).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(WEST).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(WEST).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(WEST).appendMergingEnd();
		// connect
		mergingMiddle1.getOutOption(SOUTH).connectMergingLaneToInLink(mergingIn1.getInOption(NORTH));
		mergingMiddle2.getOutOption(SOUTH).connectMergingLaneToInLink(mergingIn2.getInOption(NORTH));
		mergingEnd.getOutOption(SOUTH).connectMergingLaneToInLink(mergingIn3.getInOption(NORTH));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(20, map.getSegment(20).getOutOption(WEST).getPose()));

		return map;
	}
}
