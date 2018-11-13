package taco.agent.agentruntime.scenarios;

import static taco.agent.model.worldmodel.DriveInstruction.STRAIGHT_FOREVER;

import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.AADC2017QualiMap;

public class OvertakeScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return AADC2017QualiMap.create();
	}

	@Override
	public int getStartSector()
	{
		return 3;
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		return new DriveInstructionManager.Builder().add(STRAIGHT_FOREVER, 3).build();
	}
}