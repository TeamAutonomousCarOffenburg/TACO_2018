package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.util.SignType;

import static taco.agent.model.agentmodel.IAudiCupMotor.*;
import static taco.agent.model.worldmodel.street.Direction.*;
import static taco.agent.model.worldmodel.street.maps.MapUtils.*;

/**
 * Map used during the AADC 2018 test event
 */
public class AADC2018FinalMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(0, 7.5), 1, Angle.ZERO);
		Segment current = appendStraights(rootSegment, NORTH, 3);

		/* BOTTOM HALF */
		Segment crossing1 = current.getOutOption(NORTH).appendXCrossing();
		current = crossing1.getOutOption(EAST).appendStraightSegment();
		current = current.getOutOption(EAST).appendCurveSmallLeft();
		current = current.getOutOption(NORTH).appendStraightSegment();
		Segment crossing2 = current.getOutOption(NORTH).appendTCrossingSR();
		// Ramp and MergingOut
		current = appendStraights(crossing2, NORTH, 4);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, DEFAULT_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, DEFAULT_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, DEFAULT_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(NORTH).appendCurveSmallLeft(LOW_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(WEST).appendStraightSegment(LOW_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(WEST).appendCurveSmallLeft(LOW_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, -7, DEFAULT_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, -7, DEFAULT_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, 15, DEFAULT_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment();
		Segment mergingStart = current.getOutOption(SOUTH).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(SOUTH).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(SOUTH).appendMergingEnd();
		// Bottom Curve and MergingIn
		current = crossing2.getOutOption(EAST).appendStraightSegment();
		current = current.getOutOption(EAST).appendCurveBigLeft();
		current = current.getOutOption(NORTH).appendStraightSegment();
		Segment crosswalkBottom = appendCrosswalk(current, NORTH);
		current = crosswalkBottom.getOutOption(NORTH).appendStraightSegment();
		current = current.getOutOption(NORTH).appendSCurveBottom();
		current = current.getOutOption(NORTH).appendStraightSegment();
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		current = appendStraights(current, WEST, 3);
		Segment crossing3 = current.getOutOption(WEST).appendTCrossingLS();
		current = appendStraights(crossing3, SOUTH, 8);
		Segment mergingIn1 = current.getOutOption(SOUTH).appendMergingInSR();
		Segment mergingIn2 = mergingIn1.getOutOption(SOUTH).appendMergingInSR();
		Segment mergingIn3 = mergingIn2.getOutOption(SOUTH).appendMergingInSR();
		current = appendStraights(mergingIn3, SOUTH, 2);
		// Close Lower Half
		mergingMiddle1.getOutOption(EAST).connectMergingLaneToInLink(mergingIn1.getInOption(WEST));
		mergingMiddle2.getOutOption(EAST).connectMergingLaneToInLink(mergingIn2.getInOption(WEST));
		mergingEnd.getOutOption(EAST).connectMergingLaneToInLink(mergingIn3.getInOption(WEST));
		current.getOutOption(SOUTH).connectToInLink(crossing1.getInOption(NORTH));

		/* UPPER HALF */
		// City
		current = crossing1.getOutOption(WEST).appendStraightSegment();
		Segment crossing4 = current.getOutOption(WEST).appendTCrossingSR();
		current = crossing4.getOutOption(NORTH).appendStraightSegment(LOW_SPEED, LOW_SPEED);
		Segment crosswalkTop = appendCrosswalk(current, NORTH);
		current = appendStraights(crosswalkTop, NORTH, 2, LOW_SPEED);
		Segment crossing5 = current.getOutOption(NORTH).appendTCrossingLS();
		current = appendStraights(crossing5, NORTH, 2, LOW_SPEED);
		current = current.getOutOption(NORTH).appendCurveSmallLeft();
		Segment crossing6 = current.getOutOption(WEST).appendTCrossingLS();
		current = appendStraights(crossing6, WEST, 2);
		Segment crossing7 = current.getOutOption(WEST).appendTCrossingLR();
		current = appendStraights(crossing6, SOUTH, 3, LOW_SPEED);
		Segment crossing8 = current.getOutOption(SOUTH).appendXCrossing();
		current = appendStraights(crossing8, WEST, 2);
		Segment crossing9 = current.getOutOption(WEST).appendTCrossingLR();
		current = crossing8.getOutOption(EAST).appendStraightSegment();
		current.getOutOption(EAST).connectToInLink(crossing5.getInOption(WEST));
		current = appendStraights(crossing8, SOUTH, 4, LOW_SPEED);
		Segment crossing10 = current.getOutOption(SOUTH).appendTCrossingLR();
		current = crossing10.getOutOption(EAST).appendStraightSegment();
		current.getOutOption(EAST).connectToInLink(crossing4.getInOption(WEST));
		// Outer Curve
		current = crossing10.getOutOption(WEST).appendStraightSegment();
		current = current.getOutOption(WEST).appendCurveSmallRight();
		current = appendStraights(current, NORTH, 3);
		current.getOutOption(NORTH).connectToInLink(crossing9.getInOption(SOUTH));
		current = appendStraights(crossing9, NORTH, 3);
		current.getOutOption(NORTH).connectToInLink(crossing7.getInOption(SOUTH));
		current = crossing7.getOutOption(NORTH).appendStraightSegment();
		current = current.getOutOption(NORTH).appendCurveSmallRight();
		current = current.getOutOption(EAST).appendStraightSegment();
		current = current.getOutOption(EAST).appendSCurveBottom();
		current = current.getOutOption(EAST).appendStraightSegment();
		// Close the two Halfs
		current.getOutOption(EAST).connectToInLink(crossing3.getInOption(WEST));

		// create the map
		StreetMap map = new StreetMap(rootSegment);

		// add traffic signs
		addSign(crossing1, SOUTH, SignType.STOP);
		addSign(crossing1, NORTH, SignType.STOP);
		addSign(crossing1, EAST, SignType.HAVE_WAY);
		addSign(crossing1, WEST, SignType.HAVE_WAY);

		addSign(crossing2, SOUTH, SignType.HAVE_WAY);
		addSign(crossing2, NORTH, SignType.HAVE_WAY);
		addSign(crossing2, EAST, SignType.STOP);

		addSign(crossing3, WEST, SignType.HAVE_WAY);
		addSign(crossing3, EAST, SignType.HAVE_WAY);
		addSign(crossing3, SOUTH, SignType.STOP);

		addSign(crossing4, WEST, SignType.HAVE_WAY);
		addSign(crossing4, EAST, SignType.HAVE_WAY);
		addSign(crossing4, NORTH, SignType.STOP);

		addSign(crossing5, SOUTH, SignType.HAVE_WAY);
		addSign(crossing5, NORTH, SignType.HAVE_WAY);
		addSign(crossing5, WEST, SignType.STOP);

		addSign(crossing6, WEST, SignType.HAVE_WAY);
		addSign(crossing6, EAST, SignType.HAVE_WAY);
		addSign(crossing6, SOUTH, SignType.STOP);

		addSign(crossing7, SOUTH, SignType.HAVE_WAY);
		addSign(crossing7, NORTH, SignType.HAVE_WAY);
		addSign(crossing7, EAST, SignType.STOP);

		addSign(crossing8, EAST, SignType.STOP);
		addSign(crossing8, WEST, SignType.STOP);
		addSign(crossing8, SOUTH, SignType.HAVE_WAY);
		addSign(crossing8, NORTH, SignType.HAVE_WAY);

		addSign(crossing9, SOUTH, SignType.HAVE_WAY);
		addSign(crossing9, NORTH, SignType.HAVE_WAY);
		addSign(crossing9, EAST, SignType.STOP);

		addSign(crossing10, WEST, SignType.HAVE_WAY);
		addSign(crossing10, EAST, SignType.HAVE_WAY);
		addSign(crossing10, NORTH, SignType.STOP);

		// MapUtils.addSignsByXML(map, "config/finalevent_2018_roadSigns_v3.xml");

		// create parking spaces

		boolean[] occupied1 = {true, false, true, false};
		addParkingVertical(map, 0, SOUTH, 1, occupied1);

		boolean[] occupied2 = {true, false, true, false};
		addParkingVertical(map, 44, SOUTH, 5, occupied2);

		// add stop lines
		addStopLine(crossing1, SOUTH);
		addStopLine(crossing1, NORTH);
		addStopLine(crossing2, EAST);
		addStopLine(crossing3, SOUTH);
		addStopLine(crossing4, NORTH);
		addStopLine(crossing5, WEST);
		addStopLine(crossing6, SOUTH);
		addStopLine(crossing7, EAST);
		addStopLine(crossing8, EAST);
		addStopLine(crossing8, WEST);
		addStopLine(crossing9, EAST);
		addStopLine(crossing10, NORTH);

		// create sectors
		map.addSector(new Sector(93, map.getSegment(93).getOutOption(WEST).getPose().applyTo(new Pose3D(-.1, 1., 0))));
		map.addSector(new Sector(10, map.getSegment(10).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(77, map.getSegment(77).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(87, map.getSegment(87).getOutOption(SOUTH).getPose()));
		map.addSector(new Sector(39, map.getSegment(39).getOutOption(SOUTH).getPose()));
		map.addSector(new Sector(29, map.getSegment(29).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(48, map.getSegment(48).getOutOption(NORTH).getPose()));

		/*
		map.addSector(new Sector(93, map.getSegment(93).getOutOption(WEST).getPose().applyTo(new Pose3D(-.1, 1., 0))));
		map.addSector(new Sector(89, map.getSegment(89).getOutOption(SOUTH).getPose()));
		map.addSector(new Sector(10, map.getSegment(10).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(80, map.getSegment(80).getOutOption(WEST).getPose()));
		map.addSector(new Sector(56, map.getSegment(56).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(46, map.getSegment(46).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(94, map.getSegment(94).getOutOption(WEST).getPose().applyTo(new Pose3D(-.1, 1., 0))));
		map.addSector(new Sector(27, map.getSegment(27).getOutOption(EAST).getPose()));
		*/

		return map;
	}
}
