package taco.agent.communication.action.impl;

import hso.autonomy.agent.communication.action.IEffector;
import taco.agent.communication.action.EffectorName;

public class ParkingSpaceEffector implements IEffector
{
	private int id;

	private double posX;

	private double posY;

	private int state;

	public ParkingSpaceEffector(int id, double posX, double posY, int state)
	{
		super();
		this.id = id;
		this.posX = posX;
		this.posY = posY;
		this.state = state;
	}

	@Override
	public String getName()
	{
		return EffectorName.PARKING_SPACE;
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
