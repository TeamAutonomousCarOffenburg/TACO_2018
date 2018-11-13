package taco.agent.agentruntime.scenarios;

import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.HSMapEmergencyCar;
import taco.agent.model.worldmodel.street.maps.MergingLaneNorthMap;

import static taco.agent.model.worldmodel.DriveInstruction.*;

public class EmergencyCarScenario extends ScenarioBase
{
	private int startSector;

	public EmergencyCarScenario(int startSector)
	{
		this.startSector = startSector;
	}

	@Override
	public StreetMap createStreetMap()
	{
		return HSMapEmergencyCar.create();
	}

	@Override
	public int getStartSector()
	{
		return startSector;
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		if (startSector == 3) {
			return new DriveInstructionManager.Builder()
					.add(LEFT, 3)
					.add(STRAIGHT, 3)
					.add(RIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.add(STRAIGHT, 3)
					.build();
		}
		if (startSector == 2) {
			return new DriveInstructionManager.Builder()
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.add(STRAIGHT, 2)
					.build();
		}
		if (startSector == 1) {
			return new DriveInstructionManager.Builder()
					.add(LEFT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.add(STRAIGHT, 1)
					.build();
		} else {
			return new DriveInstructionManager.Builder()
					.add(LEFT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.add(STRAIGHT, 0)
					.build();
		}
	}
}
