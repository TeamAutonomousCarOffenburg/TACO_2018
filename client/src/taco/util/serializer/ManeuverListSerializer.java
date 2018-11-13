package taco.util.serializer;

import taco.agent.agentruntime.scenarios.Scenario;
import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.impl.Maneuver;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This util class can be used to create a maneuverlist.xml by using the
 * coded DriveInstructions in our map
 */
@SuppressWarnings("unused")
public class ManeuverListSerializer
{
	public static void main(String[] args)
	{
		writeManeuverListXml(Scenario.DRIVE_WAYPOINTS, "../config/");
	}

	public static void writeManeuverListXml(Scenario scenario, String outputPath)
	{
		DriveInstructionManager driveInstructionManager = scenario.construct().createDriveInstructionManager();

		List<AADCSector> sectors = new ArrayList<>();
		List<AADCManeuver> maneuvers = new ArrayList<>();
		int sectorId = 0;

		List<Maneuver> originalManeuvers = driveInstructionManager.getManeuvers();
		for (int i = 0; i < originalManeuvers.size(); i++) {
			Maneuver maneuver = originalManeuvers.get(i);

			if (maneuver.getSector() > sectorId) {
				sectors.add(new AADCSector(maneuvers, sectorId));
				maneuvers = new ArrayList<>();
				sectorId++;
			}

			// combine instruction with the ID to serialize the parking ID correctly
			String action = maneuver.getDriveInstruction().serializedName;
			if (maneuver.getInstructionSubID() > 0) {
				maneuvers.add(new AADCManeuver(i, action, maneuver.getInstructionSubID()));
			} else {
				maneuvers.add(new AADCManeuver(i, action));
			}
		}
		sectors.add(new AADCSector(maneuvers, sectorId));

		File outputFile = new File(outputPath + scenario.name() + "_Maneuverlist"
								   + ".xml");
		JAXB.marshal(new AADCManeuverList(sectors), outputFile);
	}

	@XmlRootElement(name = "AADC-Maneuver-List")
	private static class AADCManeuverList
	{
		@XmlElement(name = "AADC-Sector")
		private List<AADCSector> sectors;

		public AADCManeuverList()
		{
		}

		public AADCManeuverList(List<AADCSector> sectors)
		{
			this.sectors = sectors;
		}
	}

	private static class AADCSector
	{
		@XmlElement(name = "AADC-Maneuver")
		private List<AADCManeuver> maneuvers;

		@XmlAttribute(name = "id")
		private int id;

		public AADCSector()
		{
		}

		public AADCSector(List<AADCManeuver> instructions, int id)
		{
			this.maneuvers = instructions;
			this.id = id;
		}
	}

	private static class AADCManeuver
	{
		@XmlAttribute(name = "id", required = true)
		private int id;

		@XmlAttribute(name = "action", required = true)
		private String action;

		@XmlAttribute(name = "extra")
		private Integer extra;

		public AADCManeuver()
		{
		}

		public AADCManeuver(int id, String action)
		{
			this.id = id;
			this.action = action;
		}

		public AADCManeuver(int id, String action, Integer extra)
		{
			this.id = id;
			this.action = action;
			this.extra = extra;
		}
	}
}
