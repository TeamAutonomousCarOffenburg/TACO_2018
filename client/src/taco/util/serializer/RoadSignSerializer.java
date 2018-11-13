package taco.util.serializer;

import taco.agent.agentruntime.scenarios.Scenario;
import taco.agent.model.worldmodel.signdetection.RoadSign;
import taco.agent.model.worldmodel.street.Direction;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.util.serializer.helper.AADCConfiguration;
import taco.util.serializer.helper.AADCRoadsign;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RoadSignSerializer
{
	public static void main(String[] args)
	{
		// Just change the Scenario here to create the roadsign.xml for this map
		writeRoadSignXML(Scenario.DRIVE_WAYPOINTS, "../config/");
	}

	private static void writeRoadSignXML(Scenario scenario, String outputPath)
	{
		StreetMap streetMap = scenario.construct().getStreetMap();
		List<RoadSign> roadSigns = new ArrayList<>();
		for (Segment segment : streetMap) {
			for (Direction direction : Direction.values()) {
				if (segment.hasInOption(direction)) {
					roadSigns.addAll(segment.getRoadSigns(direction));
				}
			}
		}

		// create roadsigns
		List<AADCRoadsign> aadcRoadSigns = new ArrayList<>();
		for (RoadSign roadSign : roadSigns) {
			int id = roadSign.getSignType().getValue();
			double x = Math.round(roadSign.getPose().getX() * 1000) / 1000.0;
			double y = Math.round(roadSign.getPose().getY() * 1000) / 1000.0;
			Direction horizontalAngle = Direction.getDirection(roadSign.getPose().getHorizontalAngle());
			AADCRoadsign aadcRoadsign = new AADCRoadsign(id, x, y, 1.0, horizontalAngle.getAngle().degrees());
			aadcRoadsign.setName(roadSign.getSignType().name());
			aadcRoadSigns.add(aadcRoadsign);
		}

		// TODO: may also serialize parking spaces as they were also inside the roadsign.xml

		File outputFile = new File(outputPath + scenario.name() + "_Roadsigns"
								   + ".xml");
		JAXB.marshal(new AADCConfiguration(aadcRoadSigns), outputFile);
	}
}
