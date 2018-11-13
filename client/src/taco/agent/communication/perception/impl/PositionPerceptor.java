package taco.agent.communication.perception.impl;

import taco.agent.communication.perception.IPositionPerceptor;
import taco.agent.communication.perception.PerceptorName;

public class PositionPerceptor extends AudiCupPerceptor implements IPositionPerceptor
{
	double posX;
	double posY;
	double posAngle;

	public PositionPerceptor(long timestamp, double posX, double posY, double posAngle)
	{
		super(PerceptorName.POSITION, timestamp);
		this.posX = posX;
		this.posY = posY;
		this.posAngle = posAngle;
	}

	@Override
	public double getX()
	{
		return posX;
	}

	@Override
	public double getY()
	{
		return posY;
	}

	@Override
	public double getAngle()
	{
		return posAngle;
	}
}
