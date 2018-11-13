package taco.agent.model.worldmodel.impl;

import hso.autonomy.agent.model.worldmodel.impl.VisibleObject;
import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.VectorUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import taco.agent.communication.perception.RecognizedObjectType;

import static hso.autonomy.util.geometry.VectorUtils.to3D;

public class Obstacle extends VisibleObject
{
	private static final int TTL = 1;

	private float perceptionTime;

	private final RecognizedObjectType type;

	/** position of the obstacle in the global coordinate system */
	private Area2D.Float area;

	public Obstacle(float perceptionTime, RecognizedObjectType type, Area2D.Float area)
	{
		super(type.name());
		this.perceptionTime = perceptionTime;
		this.type = type;
		this.area = area;
		this.position = to3D(area.getCenter());
	}

	public RecognizedObjectType getType()
	{
		return type;
	}

	public Area2D.Float getArea()
	{
		return area;
	}

	@Override
	public void setPosition(Vector3D pos)
	{
		super.setPosition(pos);
	}

	/**
	 * recalculate the area if we set the position via the lidar detection
	 * we assume that the position of the lidar is the center of the bottom, cause its the average of the lidar-scan
	 * area
	 */
	public void updateArea(boolean rotateObject)
	{
		float minX = area.getMinX();
		float minY = area.getMinY();
		float maxX = area.getMaxX();
		float maxY = area.getMaxY();

		Vector3D bottomCenterOld =
				VectorUtils.average(VectorUtils.to3D(area.getBottomLeft()), VectorUtils.to3D(area.getTopLeft()));
		double xOffset = (position.getX() - bottomCenterOld.getX());
		double yOffset = (position.getY() - bottomCenterOld.getY());

		area = new Area2D.Float(minX + xOffset, maxX + xOffset, minY + yOffset, maxY + yOffset);

		if (rotateObject) {
			float areaHeight = area.getHeight();
			float areaWidthHalf = area.getWidth() / 2;
			area = new Area2D.Float(position.getX(), position.getX() + areaHeight, position.getY() - areaWidthHalf,
					position.getY() + areaWidthHalf);
		}
	}

	public boolean isValid(float currentTime)
	{
		return currentTime - perceptionTime < TTL;
	}

	@Override
	public String toString()
	{
		return type.name();
	}
}
