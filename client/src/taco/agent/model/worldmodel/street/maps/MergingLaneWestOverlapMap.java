package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

import static taco.agent.model.worldmodel.street.Direction.*;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendStraights;

public class MergingLaneWestOverlapMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 0.5), 1, Angle.ZERO);

		// circle
		Segment current = appendStraights(rootSegment, SOUTH, 6);
		Segment tCrossing = current.getOutOption(SOUTH).appendTCrossingLR();
		current = tCrossing.getOutOption(WEST).appendCurveSmallRight();
		current = appendStraights(current, NORTH, 2);
		Segment mergingIn3 = current.getOutOption(NORTH).appendMergingInLS();
		Segment mergingIn2 = mergingIn3.getOutOption(NORTH).appendMergingInLS();
		Segment mergingIn1 = mergingIn2.getOutOption(NORTH).appendMergingInLS();
		current = appendStraights(mergingIn1, NORTH, 6);
		current = current.getOutOption(NORTH).appendCurveSmallRight();
		current = current.getOutOption(EAST).appendStraightSegment();
		current = current.getOutOption(EAST).appendCurveSmallRight();
		current = appendStraights(current, SOUTH, 11);
		Segment curveSW = current.getOutOption(SOUTH).appendCurveSmallRight();

		// right outer loop
		current = appendStraights(rootSegment, NORTH, 2);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current = current.getOutOption(WEST).appendCurveSmallLeft();
		current = appendStraights(current, SOUTH, 2);
		Segment mergingStart = current.getOutOption(SOUTH).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(SOUTH).appendMergingEnd();

		// close links
		curveSW.getOutOption(WEST).connectToInLink(tCrossing.getInOption(EAST));
		mergingMiddle1.getOutOption(EAST).connectMergingLaneToInLink(mergingIn1.getInOption(WEST));
		mergingMiddle2.getOutOption(EAST).connectMergingLaneToInLink(mergingIn2.getInOption(WEST));
		mergingEnd.getOutOption(EAST).connectMergingLaneToInLink(mergingIn3.getInOption(WEST));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));

		return map;
	}
}
