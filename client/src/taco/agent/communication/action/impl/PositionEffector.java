package taco.agent.communication.action.impl;

import hso.autonomy.agent.communication.action.IEffector;
import taco.agent.communication.action.EffectorName;

public class PositionEffector implements IEffector
{
	private double posX;

	private double posY;

	private double angle;

	public PositionEffector(double posX, double posY, double angle)
	{
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
	}

	@Override
	public String getName()
	{
		return EffectorName.POSITION;
	}

	@Override
	public void setEffectorValues(float... values)
	{
	}

	@Override
	public void resetAfterAction()
	{
	}
}
