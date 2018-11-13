package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

import static taco.agent.model.worldmodel.street.Direction.*;

public class HSMapEmergencyCar
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
		Segment xEastCrossing = current.getOutOption(NORTH).appendTCrossingLS();
		current = xEastCrossing.getOutOption(WEST).appendStraightSegment();
		current = current.getOutOption(WEST).appendStraightSegment();
		// connect
		southCrossing.getOutOption(NORTH).connectToInLink(rootSegment.getInOption(SOUTH));
		current.getOutOption(WEST).connectToInLink(centralCrossing.getInOption(EAST));

		// right inner loop
		current = xEastCrossing.getOutOption(NORTH).appendCurveSmallLeft();
		Segment straight1 = current.getOutOption(WEST).appendStraightSegment();
		Segment straight12 = straight1.getOutOption(WEST).appendStraightSegment();
		Segment straight13 = straight12.getOutOption(WEST).appendStraightSegment();
		current = straight13.getOutOption(WEST).appendCurveSmallLeft();
		// connect
		current.getOutOption(SOUTH).connectToInLink(westCrossing.getInOption(NORTH));

		StreetMap map = new StreetMap(rootSegment);

		// define sectors
		// sector 0 -> left crossing
		map.addSector(new Sector(0, map.getSegment(0).getOutOption(SOUTH).getPose()));
		// sector 1 -> lower crossing
		map.addSector(new Sector(12, map.getSegment(12).getOutOption(EAST).getPose()));
		// sector 2 -> lower straight segment
		map.addSector(new Sector(10, map.getSegment(10).getOutOption(NORTH).getPose()));
		// sector 3 ->
		map.addSector(new Sector(12, map.getSegment(12).getOutOption(WEST).getPose()));

		return map;
	}
}
