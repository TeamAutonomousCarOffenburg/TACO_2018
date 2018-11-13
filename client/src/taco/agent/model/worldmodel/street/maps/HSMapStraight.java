package taco.agent.model.worldmodel.street.maps;

import static taco.agent.model.worldmodel.street.Direction.NORTH;
import static taco.agent.model.worldmodel.street.maps.MapUtils.appendStraights;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.street.NonCrossingSegment;
import taco.agent.model.worldmodel.street.Sector;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;

/**
 * Map we have at the Hochschule Offenburg
 */
public class HSMapStraight
{
	public static StreetMap create()
	{
		Segment.nextID = 0;
		Segment rootSegment = NonCrossingSegment.createInitialSegment(new Pose3D(), 1, Angle.ZERO);

		appendStraights(rootSegment, NORTH, 9);

		StreetMap map = new StreetMap(rootSegment);

		map.addSector(new Sector(0, map.getSegment(0).getOutOption(NORTH).getPose()));

		return map;
	}
}
