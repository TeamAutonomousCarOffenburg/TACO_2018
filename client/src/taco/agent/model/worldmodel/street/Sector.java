package taco.agent.model.worldmodel.street;

import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;

/**
 * Representation of what Audi calls a sector, i.e. a block of drive instructions
 */
public class Sector
{
	private static final IPose3D OFFSET = new Pose3D(-0.5, 0);

	/** the ID of the segment of the underlying map that is the start segment of this sector */
	private int segmentIndex;

	private IPose3D startPose;

	/**
	 * @param segmentIndex the ID of the segment of the underlying map that is the start segment of this sector
	 * @param startPose the pose of the car where to start the sector
	 */
	public Sector(int segmentIndex, IPose3D startPose)
	{
		this.segmentIndex = segmentIndex;
		this.startPose = startPose.applyTo(OFFSET);
	}

	public int getSegmentIndex()
	{
		return segmentIndex;
	}

	public void setSegmentIndex(int segmentIndex)
	{
		this.segmentIndex = segmentIndex;
	}

	public IPose3D getStartPose()
	{
		return startPose;
	}
}
