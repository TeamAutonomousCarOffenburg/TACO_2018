package taco.agent.agentruntime.scenarios;

import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.MergingLaneWestMap;
import taco.agent.model.worldmodel.street.maps.MergingLaneWestOverlapMap;

import static taco.agent.model.worldmodel.DriveInstruction.*;

public class MergingWestScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return MergingLaneWestOverlapMap.create();
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		return new DriveInstructionManager.Builder()
				.add(MERGE_LEFT, 0)
				.add(STRAIGHT, 0)
				.add(LEFT, 0)
				.add(STRAIGHT, 0)
				.add(LEFT, 0)
				.add(STRAIGHT, 0)
				.add(STRAIGHT, 0)
				.build();
	}
}
