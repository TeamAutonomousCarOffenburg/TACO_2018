package taco.util.drive;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.misc.ValueUtil;

/*
 * Class collecting needed Angle methods from c++.
 */
public class AngleUtils
{
	/**
	 * Returns a 2D-Vector from a radian value that represents an Angle.
	 */
	public static Vector2D getVectorFromAngle(double radAngle, double length)
	{
		return new Vector2D(Math.cos(radAngle) * length, Math.sin(radAngle) * length);
	}

	public static Angle oppositeAngle(Angle radAngle)
	{
		return radAngle.add(Angle.ANGLE_180);
	}

	public static Angle to(Vector2D point)
	{
		if (point.getX() == 0 && point.getY() == 0) {
			return Angle.ZERO;
		}

		return Angle.rad((Math.atan2(point.getY(), point.getX())));
	}

	public static Angle to(double x, double y)
	{
		if (x == 0 && y == 0) {
			return Angle.ZERO;
		}

		return Angle.rad((Math.atan2(y, x)));
	}

	public static Vector2D rotate(Vector2D vec, double radAngle)
	{
		double cosAngle = Math.cos(radAngle);
		double sinAngle = Math.sin(radAngle);

		// Calculate new position
		Vector2D res = new Vector2D(
				cosAngle * vec.getX() - sinAngle * vec.getY(), sinAngle * vec.getX() + cosAngle * vec.getY());
		return res;
	}

	public static Angle limit(Angle angle, Angle min, Angle max)
	{
		return Angle.deg(ValueUtil.limitValue(angle.degrees(), min.degrees(), max.degrees()));
	}

	// Added to get direction of cars for EmergencyCar Simulation.
	// ToDo: Check for an other solution or use this Method in src/taco/agent/decision/behavior/impl/OvertakeObstacle to
	// avoid redundant code
	public static Angle getDirection(Angle theAngle)
	{
		double angle = theAngle.degrees();
		if (angle >= -45 && angle < 45) {
			return Angle.ZERO;
		} else if (angle >= 45 && angle < 135) {
			return Angle.ANGLE_90;
		} else if (angle >= -135 && angle < -45) {
			return Angle.ANGLE_90.negate();
		}

		return Angle.ANGLE_180;
	}
}
