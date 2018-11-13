package taco.util.drive;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose2D;
import hso.autonomy.util.geometry.Pose2D;

public class Circle2D extends hso.autonomy.util.geometry.Circle2D implements IBehaviorGeometry
{
	private Vector2D origin;

	public Circle2D(double x, double y, double radius)
	{
		super(x, y, (radius >= 0 ? radius : -radius));
		origin = Vector2D.ZERO;
	}

	Circle2D(IPose2D tangent, Vector2D point)
	{
		// transform point in to an tangent touching point local coordinate system (tangent angle = x-axis + pi)
		Vector2D tmpVector = point.subtract(tangent.getPosition());
		Vector2D rotatedVector = AngleUtils.rotate(tmpVector, -tangent.getAngle().radians());

		double my =
				(-Math.pow(rotatedVector.getX(), 2) - Math.pow(rotatedVector.getY(), 2)) / (-2 * rotatedVector.getY());

		// transform back to world coordinate system

		Vector2D shiftedCircleOrigin = AngleUtils.rotate(new Vector2D(0, my), tangent.getAngle().radians());
		origin = shiftedCircleOrigin.add(tangent.getPosition());
		setRadius(Vector2D.distance(origin, tangent.getPosition()));
		setX(origin.getX());
		setY(origin.getY());
	}

	public Angle slope(Vector2D vector)
	{
		// calculate slope on upper semicircle
		double slope = -0.5 * Math.sqrt(1 / (Math.pow(getRadius(), 2) - Math.pow(vector.getX() - origin.getX(), 2))) *
					   (2 * vector.getX() - 2 * origin.getX());

		// check if point is on lower semicircle
		if (origin.getY() > vector.getY()) {
			slope *= -1;
		}
		return Angle.rad(Math.atan(slope));
	}

	public int getDirectionOfRotation(IPose2D nextWaypoint)
	{
		return getDirectionOfRotation(nextWaypoint.getPosition(), nextWaypoint.getAngle());
	}

	public int getDirectionOfRotation(Vector2D point, Angle angle)
	{
		Angle angle2 = getAngle(point);
		angle2 = angle2.add(Angle.rad(Math.PI * 0.5));
		double angleResult = Math.abs(angle2.radians() - angle.radians()) + (Math.PI * 0.5);

		if (angleResult < Math.PI || angleResult > 2 * Math.PI) {
			return 1;
		} else {
			return -1;
		}
	}

	Angle getAngle(Vector2D point)
	{
		Vector2D shiftedPoint = point.subtract(origin);
		// point is equal to origin
		if (Math.abs(point.getX() - origin.getX()) < 0.001 && Math.abs(point.getY() - origin.getY()) < 0.001)
			return Angle.ZERO;
		// threshold x = 0
		if (Math.abs(shiftedPoint.getX()) < 0.0001) {
			if (shiftedPoint.getY() > 0) {
				return Angle.deg(180 * 0.5);
			} else
				return Angle.deg(180 * -0.5);
		}

		double angle = Math.atan2(shiftedPoint.getY(), shiftedPoint.getX());
		return Angle.rad(angle);
	}

	Angle getAngle(double segmentLength)
	{
		return Angle.rad(segmentLength / getRadius());
	}

	boolean isOnCircle(Vector2D point)
	{
		double distance = origin.distance(point);
		double diff = Math.abs(distance - getRadius());
		return (diff < 0.000001);
	}

	@Override
	public IPose2D getClosestPose(Vector2D virtualPoint)
	{
		Angle angle = AngleUtils.to(virtualPoint.subtract(origin));
		Vector2D closestPoint = getPointOnCircle(angle).getPosition();
		return new Pose2D(closestPoint, slope(closestPoint));
	}

	public Vector2D getOrigin()
	{
		return origin;
	}

	public static Circle2D average(ArrayList<Circle2D> circles)
	{
		int size = circles.size();
		if (size == 0) {
			return new Circle2D(0, 0, 1);
		} else if (size == 1) {
			return circles.get(0);
		}

		double x = 0;
		double y = 0;
		double radius = 0;

		for (Circle2D circle : circles) {
			x += circle.getOrigin().getX();
			y += circle.getOrigin().getY();
			radius += circle.getRadius();
		}

		return new Circle2D(x / size, y / size, radius / size);
	}

	public Vector2D getVectorPointOnCircle(Angle angle)
	{
		return new Vector2D(origin.getX() + Math.cos(angle.radians()) * getRadius(),
				origin.getY() + Math.sin(angle.radians()) * getRadius());
	}
}
