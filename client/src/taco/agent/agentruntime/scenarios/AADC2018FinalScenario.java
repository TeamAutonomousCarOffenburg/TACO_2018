package taco.agent.agentruntime.scenarios;

import taco.agent.model.worldmodel.DriveInstruction;
import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.agent.model.worldmodel.street.maps.AADC2018FinalMap;

public class AADC2018FinalScenario extends ScenarioBase
{
	@Override
	public StreetMap createStreetMap()
	{
		return AADC2018FinalMap.create();
	}

	@Override
	public DriveInstructionManager createDriveInstructionManager()
	{
		return new DriveInstructionManager
				.Builder()
				// pull out and speed sector
				.add(DriveInstruction.PULL_OUT_RIGHT, 0)
				.add(DriveInstruction.STRAIGHT, 0)
				.add(DriveInstruction.RIGHT, 0)
				.add(DriveInstruction.RIGHT, 0)

				// ramp and merge
				.add(DriveInstruction.MERGE_LEFT, 1)
				.add(DriveInstruction.RIGHT, 1)
				.add(DriveInstruction.STRAIGHT, 1)
				.add(DriveInstruction.RIGHT, 1)

				// emergency car
				.add(DriveInstruction.STRAIGHT, 2)
				.add(DriveInstruction.LEFT, 2)
				.add(DriveInstruction.LEFT, 2)

				// city with crosswalk
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.LEFT, 3)
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.RIGHT, 3)
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.RIGHT, 3)
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.STRAIGHT, 3)
				.add(DriveInstruction.RIGHT, 3)

				// child
				.add(DriveInstruction.LEFT, 4)
				.add(DriveInstruction.RIGHT, 4)

				// overtake
				.add(DriveInstruction.STRAIGHT, 5)
				.add(DriveInstruction.STRAIGHT, 5)
				.add(DriveInstruction.STRAIGHT, 5)
				.add(DriveInstruction.STRAIGHT, 5)
				.add(DriveInstruction.STRAIGHT, 5)
				.add(DriveInstruction.LEFT, 5)

				// crossparking
				.add(DriveInstruction.CROSS_PARKING, 7, 6)
				.build();
		/*
		return new DriveInstructionManager.Builder()
				.add(DriveInstruction.PULL_OUT_RIGHT, 0)
				.add(STRAIGHT, 0)
				.add(LEFT, 0)
				.add(LEFT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.add(RIGHT, 1)
				.add(RIGHT, 1)
				.add(STRAIGHT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(RIGHT, 1)
				.add(RIGHT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(LEFT, 1)
				.add(STRAIGHT, 1)
				.add(RIGHT, 1)
				.add(RIGHT, 1)
				.add(RIGHT, 1)
				.add(LEFT, 1)
				.add(RIGHT, 1)
				.add(LEFT, 1)
				.add(STRAIGHT, 1)
				.add(STRAIGHT, 1)
				.add(MERGE_LEFT, 2)
				.add(RIGHT, 2)
				.add(STRAIGHT, 2)
				.add(STRAIGHT, 2)
				.add(STRAIGHT, 3)
				.add(STRAIGHT, 3)
				.add(STRAIGHT, 3)
				.add(LEFT, 3)
				.add(STRAIGHT, 3)
				.add(RIGHT, 3)
				.add(STRAIGHT, 4)
				.add(STRAIGHT, 4)
				.add(RIGHT, 4)
				.add(RIGHT, 4)
				.add(RIGHT, 4)
				.add(RIGHT, 4)
				.add(LEFT, 4)
				.add(LEFT, 4)
				.add(LEFT, 4)
				.add(STRAIGHT, 4)
				.add(LEFT, 4)
				.add(DriveInstruction.CROSS_PARKING, 6, 5)
				.add(DriveInstruction.PULL_OUT_LEFT, 6, 6)
				.add(DriveInstruction.LEFT, 6)
				.add(DriveInstruction.RIGHT, 6)
				.add(DriveInstruction.STRAIGHT, 6)
				.build();
				*/
	}
}
