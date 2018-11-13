package taco.agent.model.worldmodel.impl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Polygon;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.worldmodel.ParkingSpaceState;

public class ParkingSpace
{
	public static final float MIN_WIDTH = 0.45f;

	public static final float MIN_DEPTH = 0.85f;

	public static final float LANE_MIDDLE_DISTANCE = 0.23f;

	private final int id;

	private final IPose3D pose;

	private ParkingSpaceState state;

	private float lastModified;

	private transient Area2D.Float area;

	public ParkingSpace(int id, IPose3D pose, ParkingSpaceState state)
	{
		this.id = id;
		this.state = state;
		this.pose = pose;
		lastModified = 0;
	}

	void update(ParkingSpaceState state, float globalTime)
	{
		if (this.state != state) {
			this.state = state;
			lastModified = globalTime;
		}
	}

	public IPose3D getPose()
	{
		return pose;
	}

	public Area2D.Float getArea()
	{
		if (area == null) {
			float halfSpace = MIN_WIDTH / 2;
			IPose3D topLeft = pose.applyTo(new Pose3D(-halfSpace, 0));
			IPose3D bottomRight = pose.applyTo(new Pose3D(halfSpace, -MIN_DEPTH));

			area = new Area2D.Float(topLeft.getX(), bottomRight.getX(), topLeft.getY(), bottomRight.getY());
		}
		return area;
	}

	public Polygon getMeasurementArea()
	{
		return getMeasurementArea(0.0, 0.0);
	}

	public Polygon getMeasurementArea(double moveX)
	{
		return getMeasurementArea(moveX, 0.0);
	}

	public Polygon getMeasurementArea(double moveX, double moveY)
	{
		double minWidthHalf = MIN_WIDTH / 2;
		IPose3D bl = pose.applyTo(new Pose3D(new Vector3D(-minWidthHalf - moveX, -moveY, 0), pose.getOrientation()));
		IPose3D br = pose.applyTo(new Pose3D(new Vector3D(+minWidthHalf - moveX, -moveY, 0), pose.getOrientation()));
		IPose3D tl = pose.applyTo(new Pose3D(
				new Vector3D(-minWidthHalf - moveX, +LANE_MIDDLE_DISTANCE * 2.5 - moveY, 0), pose.getOrientation()));
		IPose3D tr = pose.applyTo(new Pose3D(
				new Vector3D(+minWidthHalf - moveX, +LANE_MIDDLE_DISTANCE * 2.5 - moveY, 0), pose.getOrientation()));

		return new Polygon(new Vector2D(bl.getX(), bl.getY()), new Vector2D(br.getX(), br.getY()),
				new Vector2D(tr.getX(), tr.getY()), new Vector2D(tl.getX(), tl.getY()));
	}

	public int getID()
	{
		return id;
	}

	public float getLastModified()
	{
		return lastModified;
	}

	public ParkingSpaceState getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "\nid=" + id + ", pose=" + pose + ", area=" + getArea() + ", state=" + state +
				", lastModified=" + lastModified;
	}
}
