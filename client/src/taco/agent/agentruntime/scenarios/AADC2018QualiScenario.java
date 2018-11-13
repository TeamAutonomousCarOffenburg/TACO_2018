package taco.agent.agentruntime.scenarios;

import taco.agent.model.worldmodel.DriveInstruction;
import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.AADC2018QualiMap;

import static taco.agent.model.worldmodel.DriveInstruction.*;

public class AADC2018QualiScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return AADC2018QualiMap.create();
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		return new DriveInstructionManager.Builder()
				.add(DriveInstruction.PULL_OUT_RIGHT, 0)
				.add(STRAIGHT, 0)
				.add(LEFT, 0)
				.add(STRAIGHT, 0)
				.add(STRAIGHT, 0)
				.add(LEFT, 0)
				.add(STRAIGHT, 1)
				.add(RIGHT, 2)
				.add(STRAIGHT, 2)
				.add(RIGHT, 2)
				.add(STRAIGHT, 2)
				.add(LEFT, 2)
				.add(RIGHT, 2)
				.add(STRAIGHT, 3)
				.add(STRAIGHT, 3)
				.add(MERGE_LEFT, 3)
				.add(LEFT, 3)
				.add(STRAIGHT, 4)
				.add(RIGHT, 5)
				.add(LEFT, 5)
				.add(STRAIGHT, 5)
				.add(RIGHT, 5)
				.add(STRAIGHT, 5)
				.add(RIGHT, 6)
				.add(RIGHT, 6)
				.add(STRAIGHT, 6)
				.add(RIGHT, 6)
				.add(LEFT, 7)
				.add(DriveInstruction.CROSS_PARKING, 2, 7)
				.build();
	}
}
