package taco.agent.model.worldmodel.odometry;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;

/**
 * Base class for all odometry classes that are able to keep track of our pose.
 */
public class Odometry
{
	/** the current pose estimation */
	protected IPose3D pose;

	/** the start pose to start with */
	protected IPose3D startPose;

	public Odometry(IPose3D startPose)
	{
		this.startPose = startPose;
		pose = startPose;
	}

	public IPose3D getPose()
	{
		return pose;
	}

	public void init()
	{
		pose = startPose;
	}

	public void setPose(IPose3D pose)
	{
		this.pose = pose;
	}

	public void setPosition(Vector3D position)
	{
		this.pose = new Pose3D(position, pose.getOrientation());
	}
}
