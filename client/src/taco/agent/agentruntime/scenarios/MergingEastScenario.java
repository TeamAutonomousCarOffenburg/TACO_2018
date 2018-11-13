package taco.agent.agentruntime.scenarios;

import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.MergingLaneEastMap;

import static taco.agent.model.worldmodel.DriveInstruction.*;

public class MergingEastScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return MergingLaneEastMap.create();
	}

	@Override
	public int getStartSector()
	{
		return 1;
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		return new DriveInstructionManager.Builder()
				.add(MERGE_LEFT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.build();
	}
}
