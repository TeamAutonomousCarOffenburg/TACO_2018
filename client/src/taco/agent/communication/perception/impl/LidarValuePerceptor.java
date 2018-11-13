package taco.agent.communication.perception.impl;

public class LidarValuePerceptor
{
	private int angle;

	private double distance;

	public LidarValuePerceptor(int angle, double distance)
	{
		this.angle = angle;
		this.distance = distance;
	}

	public int getAngle()
	{
		return angle;
	}

	public double getDistance()
	{
		return distance;
	}
}
