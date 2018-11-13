package taco.agent.model.worldmodel.impl;

import hso.autonomy.util.geometry.IPose3D;
import taco.agent.model.worldmodel.DriveInstruction;
import taco.agent.model.worldmodel.street.SegmentLink;

/**
 * A single drive point including pose and drive instruction
 */
public class DrivePoint
{
	/** the corresponding segmentLink we drive to */
	private SegmentLink goalLink;

	/** the pose we aim to drive to */
	private IPose3D pose;

	/** the maneuver that we perform when driving this drive point */
	private Maneuver maneuver;

	/** the index in the instruction list */
	private int instructionIndex;

	public DrivePoint(SegmentLink goalLink, Maneuver maneuver)
	{
		this(goalLink, goalLink.getPose(), maneuver);
	}

	public DrivePoint(SegmentLink goalLink, IPose3D pose, Maneuver maneuver)
	{
		super();
		this.goalLink = goalLink;
		this.pose = pose;
		this.maneuver = maneuver;
		this.instructionIndex = maneuver.getManeuverId();
	}

	public IPose3D getPose()
	{
		return pose;
	}

	public DriveInstruction getInstruction()
	{
		return maneuver.getDriveInstruction();
	}

	public Maneuver getManeuver()
	{
		return maneuver;
	}

	public SegmentLink getGoalLink()
	{
		return goalLink;
	}

	public int getInstructionIndex()
	{
		return instructionIndex;
	}

	public double getSpeed()
	{
		return goalLink.getSpeed();
	}

	@Override
	public String toString()
	{
		return "DrivePoint [goalLink=" + goalLink + ", pose=" + pose + ", maneuver=" + maneuver +
				", instructionIndex=" + instructionIndex + "]";
	}
}