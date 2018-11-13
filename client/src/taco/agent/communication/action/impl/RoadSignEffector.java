package taco.agent.communication.action.impl;

import hso.autonomy.agent.communication.action.IEffector;
import taco.agent.communication.action.EffectorName;

public class RoadSignEffector implements IEffector
{
	private int id;

	private double posX;

	private double posY;

	private double angle;

	public RoadSignEffector(int id, double posX, double posY, double angle)
	{
		super();
		this.id = id;
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
	}

	@Override
	public String getName()
	{
		return EffectorName.ROAD_SIGN;
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
