package taco.agent.agentruntime.scenarios;

import static taco.agent.model.worldmodel.DriveInstruction.LEFT;
import static taco.agent.model.worldmodel.DriveInstruction.MERGE_LEFT;
import static taco.agent.model.worldmodel.DriveInstruction.STRAIGHT;

import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.MergingLaneSouthMap;
import taco.agent.model.worldmodel.street.maps.MergingLaneSouthOverlapMap;

public class MergingSouthScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return MergingLaneSouthOverlapMap.create();
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
				.add(LEFT, 1)
				.add(STRAIGHT, 1)
				.add(LEFT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.build();
	}
}
