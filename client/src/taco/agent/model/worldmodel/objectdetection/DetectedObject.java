package taco.agent.model.worldmodel.objectdetection;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Polygon;

public class DetectedObject
{
	/** The name of this object. */
	protected String name;

	/** The pose of this object. */
	protected IPose3D pose;

	/** The bounds of this visible object. */
	protected Polygon bounds;

	public DetectedObject(String name, IPose3D pose, Polygon bounds)
	{
		this.name = name;
		this.pose = pose;
		this.bounds = bounds;
	}

	public DetectedObject(String name, IPose3D pose)
	{
		this.name = name;
		this.pose = pose;
		bounds = new Polygon(new Vector2D(0.05, 0.05), new Vector2D(0.05, -0.05), new Vector2D(-0.05, -0.05),
				new Vector2D(-0.05, 0.05));
	}

	public void update(IPose3D pose)
	{
		this.pose = pose;
	}

	public String getName()
	{
		return this.name;
	}

	public IPose3D getPose()
	{
		return this.pose;
	}

	public void setPose(IPose3D pose)
	{
		this.pose = pose;
	}

	public Polygon getBounds()
	{
		return this.bounds;
	}

	public void setBounds(Polygon bounds)
	{
		this.bounds = bounds;
	}
}
