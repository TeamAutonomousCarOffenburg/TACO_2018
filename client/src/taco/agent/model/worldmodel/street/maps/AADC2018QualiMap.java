package taco.agent.model.worldmodel.street.maps;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

import static taco.agent.model.agentmodel.IAudiCupMotor.*;
import static taco.agent.model.worldmodel.street.Direction.*;
import static taco.agent.model.worldmodel.street.maps.MapUtils.*;

/**
 * Map used during the AADC 2018 test event
 */
public class AADC2018QualiMap
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(2, 1.5), 1, Angle.ZERO);

		// middle segments
		Segment current = appendStraights(rootSegment, NORTH, 7, HIGH_SPEED, DEFAULT_SPEED);
		Segment mergingIn1 = current.getOutOption(NORTH).appendMergingInSR(HIGH_SPEED, DEFAULT_SPEED);
		Segment mergingIn2 = mergingIn1.getOutOption(NORTH).appendMergingInSR(HIGH_SPEED, DEFAULT_SPEED);
		Segment mergingIn3 = mergingIn2.getOutOption(NORTH).appendMergingInSR(HIGH_SPEED, DEFAULT_SPEED);
		current = appendStraights(mergingIn3, NORTH, 4, HIGH_SPEED, DEFAULT_SPEED);
		Segment c1 = current.getOutOption(NORTH).appendTCrossingLS();
		current = appendStraights(c1, NORTH, 1);
		current = current.getOutOption(NORTH).appendSCurveBottom(25, 25);
		current = current.getOutOption(NORTH).appendSCurveBottom(25, 25);
		current = current.getOutOption(NORTH).appendCurveBigLeft();
		current = appendStraights(current, WEST, 3);
		current = current.getOutOption(WEST).appendCurveBigLeft();
		current = appendStraights(current, SOUTH, 6);
		current = current.getOutOption(SOUTH).appendCurveSmallLeft();
		current = appendStraights(current, EAST, 10);
		c1.getOutOption(WEST).connectToInLink(current.getInOption(EAST));

		// left loop
		current = rootSegment.getOutOption(SOUTH).appendCurveSmallRight();
		current = appendStraights(current, WEST, 2);
		Segment c2 = current.getOutOption(WEST).appendTCrossingSR();
		current = appendStraights(c2, WEST, 1, LOW_SPEED, DEFAULT_SPEED);
		current = appendCrosswalk(current, WEST);
		current = appendStraights(current, WEST, 1, DEFAULT_SPEED, LOW_SPEED);
		current = appendStraights(current, WEST, 1);
		Segment c3 = current.getOutOption(WEST).appendTCrossingSR();
		current = appendStraights(c3, WEST, 1, LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(WEST).appendCurveSmallRight(LOW_SPEED, LOW_SPEED);
		current = appendStraights(current, NORTH, 1);
		Segment c4 = current.getOutOption(NORTH).appendTCrossingSR();
		current = appendStraights(c4, NORTH, 2, LOW_SPEED, LOW_SPEED);
		Segment c5 = current.getOutOption(NORTH).appendTCrossingSR();
		current = appendStraights(c5, NORTH, 1, LOW_SPEED, DEFAULT_SPEED);
		current = appendCrosswalk(current, NORTH);
		current = appendStraights(current, NORTH, 1, DEFAULT_SPEED, LOW_SPEED);
		current = appendStraights(current, NORTH, 4);
		current = current.getOutOption(NORTH).appendCurveSmallRight();
		current = appendStraights(current, EAST, 1);
		Segment c6 = current.getOutOption(EAST).appendTCrossingSR();
		current = appendStraights(c6, EAST, 4);
		current = current.getOutOption(EAST).appendCurveSmallRight();
		current = appendStraights(current, SOUTH, 2);
		Segment c7 = current.getOutOption(SOUTH).appendTCrossingSR();
		current = appendStraights(c7, SOUTH, 2);
		current = appendStraights(current, SOUTH, 1, HIGH_SPEED, DEFAULT_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, HIGH_SPEED, HIGH_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, HIGH_SPEED, HIGH_SPEED);
		current = current.getOutOption(SOUTH).appendStraightSegment(1.02, HIGH_SPEED, HIGH_SPEED);
		current = current.getOutOption(SOUTH).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		current = appendStraights(current, EAST, 1);
		current = current.getOutOption(EAST).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(NORTH).appendStraightSegment(1.02, LOW_SPEED, LOW_SPEED);
		current = appendStraights(current, NORTH, 1);

		Segment mergingStart = current.getOutOption(NORTH).appendMergingStart();
		Segment mergingMiddle1 = mergingStart.getOutOption(NORTH).appendMergingMiddle();
		Segment mergingMiddle2 = mergingMiddle1.getOutOption(NORTH).appendMergingMiddle();
		Segment mergingEnd = mergingMiddle2.getOutOption(NORTH).appendMergingEnd();
		mergingMiddle1.getOutOption(WEST).connectMergingLaneToInLink(mergingIn1.getInOption(EAST));
		mergingMiddle2.getOutOption(WEST).connectMergingLaneToInLink(mergingIn2.getInOption(EAST));
		mergingEnd.getOutOption(WEST).connectMergingLaneToInLink(mergingIn3.getInOption(EAST));

		// circle
		current = appendStraights(c7, WEST, 3);
		Segment c8 = current.getOutOption(WEST).appendTCrossingLR();
		current = c8.getOutOption(NORTH).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		Segment c9 = current.getOutOption(WEST).appendTCrossingSR();
		current = c9.getOutOption(WEST).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		current = appendStraights(current, SOUTH, 1, LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(SOUTH).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		Segment c11 = current.getOutOption(EAST).appendTCrossingSR();
		current = c11.getOutOption(EAST).appendCurveSmallLeft(LOW_SPEED, LOW_SPEED);
		c8.getOutOption(SOUTH).connectToInLink(current.getInOption(NORTH));
		current = appendStraights(c9, NORTH, 1);
		c6.getOutOption(SOUTH).connectToInLink(current.getInOption(NORTH));

		// rest
		current = appendStraights(c11, SOUTH, 2, LOW_SPEED, LOW_SPEED);
		Segment c12 = current.getOutOption(SOUTH).appendXCrossing();
		current = appendStraights(c12, SOUTH, 2, LOW_SPEED, LOW_SPEED);
		Segment c13 = current.getOutOption(SOUTH).appendXCrossing();
		current = appendStraights(c13, SOUTH, 2, LOW_SPEED, LOW_SPEED);
		c3.getOutOption(NORTH).connectToInLink(current.getInOption(SOUTH));

		current = appendStraights(c12, EAST, 3, LOW_SPEED, LOW_SPEED);
		current = current.getOutOption(EAST).appendCurveSmallRight(LOW_SPEED, LOW_SPEED);
		current = appendStraights(current, SOUTH, 1, LOW_SPEED, LOW_SPEED);
		Segment c14 = current.getOutOption(SOUTH).appendTCrossingSR();
		current = appendStraights(c14, SOUTH, 2, LOW_SPEED, LOW_SPEED);
		c2.getOutOption(NORTH).connectToInLink(current.getInOption(SOUTH));

		current = appendStraights(c12, WEST, 2, LOW_SPEED, LOW_SPEED);
		c5.getOutOption(EAST).connectToInLink(current.getInOption(WEST));

		current = appendStraights(c13, WEST, 2, LOW_SPEED, LOW_SPEED);
		c4.getOutOption(EAST).connectToInLink(current.getInOption(WEST));
		current = appendStraights(c13, EAST, 4, LOW_SPEED, LOW_SPEED);
		c14.getOutOption(WEST).connectToInLink(current.getInOption(EAST));

		// create the map
		StreetMap map = new StreetMap(rootSegment);

		// add traffic signs
		//		addSign(c2, EAST, SignType.HAVE_WAY);
		//		addSign(c2, WEST, SignType.HAVE_WAY);
		//		addSign(c2, NORTH, SignType.STOP);
		//
		//		addSign(c3, EAST, SignType.HAVE_WAY);
		//		addSign(c3, WEST, SignType.HAVE_WAY);
		//		addSign(c3, NORTH, SignType.GIVE_WAY);
		//
		//		addSign(c4, NORTH, SignType.HAVE_WAY);
		//		addSign(c4, SOUTH, SignType.HAVE_WAY);
		//		addSign(c4, EAST, SignType.STOP);
		//
		//		addSign(c5, NORTH, SignType.HAVE_WAY);
		//		addSign(c5, SOUTH, SignType.HAVE_WAY);
		//		addSign(c5, EAST, SignType.STOP);
		//
		//		addSign(c6, EAST, SignType.HAVE_WAY);
		//		addSign(c6, WEST, SignType.HAVE_WAY);
		//		addSign(c6, SOUTH, SignType.STOP);
		//
		//		addSign(c7, NORTH, SignType.HAVE_WAY);
		//		addSign(c7, SOUTH, SignType.HAVE_WAY);
		//		addSign(c7, WEST, SignType.STOP);
		//
		//		addSign(c8, EAST, SignType.GIVE_WAY);
		//		addSign(c9, NORTH, SignType.GIVE_WAY);
		//		addSign(c11, SOUTH, SignType.GIVE_WAY);
		//
		//		addSign(c8, SOUTH, SignType.HAVE_WAY);
		//		addSign(c9, EAST, SignType.HAVE_WAY);
		//		addSign(c11, WEST, SignType.HAVE_WAY);
		//
		//		addSign(c12, NORTH, SignType.HAVE_WAY);
		//		addSign(c12, SOUTH, SignType.HAVE_WAY);
		//		addSign(c12, EAST, SignType.STOP);
		//		addSign(c12, WEST, SignType.STOP);
		//
		//		addSign(c13, NORTH, SignType.HAVE_WAY);
		//		addSign(c13, SOUTH, SignType.HAVE_WAY);
		//		addSign(c13, EAST, SignType.STOP);
		//		addSign(c13, WEST, SignType.STOP);
		//
		//		addSign(c14, NORTH, SignType.HAVE_WAY);
		//		addSign(c14, SOUTH, SignType.HAVE_WAY);
		//		addSign(c14, WEST, SignType.GIVE_WAY);

		MapUtils.addSignsByXML(map, "config/AADC2018_Quali_Roadsigns.xml");

		// create parking spaces
		boolean[] occupied1 = {true, false, true, false};
		addParkingVertical(map, 69, EAST, 1, occupied1);

		boolean[] occupied2 = {true, false, true, false};
		addParkingVertical(map, 34, EAST, 5, false, occupied2);

		// add stop lines
		addStopLine(c1, WEST);
		addStopLine(c2, NORTH);
		addStopLine(c3, NORTH);
		addStopLine(c4, EAST);
		addStopLine(c5, EAST);
		addStopLine(c6, SOUTH);
		addStopLine(c7, WEST);
		addStopLine(c8, EAST);
		addStopLine(c9, NORTH);
		addStopLine(c11, SOUTH);
		addStopLine(c12, EAST);
		addStopLine(c12, WEST);
		addStopLine(c13, EAST);
		addStopLine(c13, WEST);
		addStopLine(c14, WEST);

		// create sectors
		map.addSector(
				new Sector(128, map.getSegment(128).getOutOption(SOUTH).getPose().applyTo(new Pose3D(-0.1, 0.5, 0))));
		map.addSector(new Sector(43, map.getSegment(43).getOutOption(EAST).getPose()));
		map.addSector(new Sector(38, map.getSegment(38).getOutOption(EAST).getPose()));
		map.addSector(new Sector(62, map.getSegment(62).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(38, map.getSegment(38).getOutOption(WEST).getPose()));
		map.addSector(new Sector(7, map.getSegment(7).getOutOption(SOUTH).getPose()));
		map.addSector(new Sector(58, map.getSegment(58).getOutOption(NORTH).getPose()));
		map.addSector(new Sector(94, map.getSegment(94).getOutOption(EAST).getPose()));

		return map;
	}
}
