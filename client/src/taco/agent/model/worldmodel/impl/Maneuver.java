package taco.agent.model.worldmodel.impl;

import taco.agent.communication.perception.impl.ManeuverPerceptor;
import taco.agent.model.worldmodel.DriveInstruction;

public class Maneuver
{
	private DriveInstruction driveInstruction;

	private int instructionSubID;

	private int sector;

	private int maneuverId;

	public Maneuver(ManeuverPerceptor perceptor)
	{
		parseInstruction(perceptor);
		sector = perceptor.getSector();
		maneuverId = perceptor.getManeuverId();
	}

	private void parseInstruction(ManeuverPerceptor perceptor)
	{
		String instructionName = perceptor.getDriveInstruction();
		String crossParkingName = DriveInstruction.CROSS_PARKING.serializedName;
		if (instructionName.startsWith(crossParkingName)) {
			driveInstruction = DriveInstruction.CROSS_PARKING;
			instructionSubID = perceptor.getParkingSpace();
			// TODO: this was the old 2017 handling. should we keep it or remove it and run our 2017 car with new
			// maneuver.xml
			//			try {
			//				instructionSubID =
			//						new Integer(instructionName.substring(crossParkingName.length() + 1,
			// instructionName.length())); 			} catch (NumberFormatException e) {
			//				System.err.println("Invalid parking id in '" + instructionName + "'");
			//				instructionSubID = 1;
			//			}
		} else {
			String mergeCheck = "";
			if (instructionName.length() > 4) {
				mergeCheck = instructionName.substring(0, 5);
			}
			if (mergeCheck.equalsIgnoreCase("merge")) {
				driveInstruction = DriveInstruction.MERGE_LEFT;
			} else {
				driveInstruction = DriveInstruction.fromString(instructionName);
			}
			if (driveInstruction == null) {
				System.err.println("Unknown drive instruction '" + instructionName + "', treating as 'straight'");
				driveInstruction = DriveInstruction.STRAIGHT;
			}
			instructionSubID = 0;
		}
	}

	public Maneuver(DriveInstruction driveInstruction, int sector, int maneuverId)
	{
		this(driveInstruction, 0, sector, maneuverId);
	}

	public Maneuver(DriveInstruction driveInstruction, int instructionSubID, int sector, int maneuverId)
	{
		this.driveInstruction = driveInstruction;
		this.instructionSubID = instructionSubID;
		this.sector = sector;
		this.maneuverId = maneuverId;
	}

	public DriveInstruction getDriveInstruction()
	{
		return driveInstruction;
	}

	public int getInstructionSubID()
	{
		return instructionSubID;
	}

	public int getSector()
	{
		return sector;
	}

	public int getManeuverId()
	{
		return maneuverId;
	}

	@Override
	public String toString()
	{
		return "Maneuver [driveInstruction=" + driveInstruction + ", instructionSubID=" + instructionSubID +
				", sector=" + sector + ", maneuverId=" + maneuverId + "]";
	}
}
