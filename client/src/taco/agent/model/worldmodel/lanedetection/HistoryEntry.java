package taco.agent.model.worldmodel.lanedetection;

import hso.autonomy.util.geometry.IPose3D;

public class HistoryEntry
{
	public final LaneMiddle middle;

	public final float time;

	public final IPose3D carPose;

	public HistoryEntry(LaneMiddle middle, float time, IPose3D carPose)
	{
		this.middle = middle;
		this.time = time;
		this.carPose = carPose;
	}
}
