package taco.agent.model.worldmodel.crossroaddetection;

import hso.autonomy.util.geometry.IPose3D;

import java.util.List;

public class CrossroadHistoryEntry
{
	public final List<Crossroad> crossroads;

	public final float time;

	public final IPose3D carPose;

	public CrossroadHistoryEntry(List<Crossroad> crossroads, float time, IPose3D carPose)
	{
		this.crossroads = crossroads;
		this.time = time;
		this.carPose = carPose;
	}
}
