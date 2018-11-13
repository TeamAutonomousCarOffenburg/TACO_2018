package taco.agent.model.agentmodel.impl;

import java.util.Map;

import hso.autonomy.agent.communication.action.IEffector;
import taco.agent.communication.action.IAudiCupAction;
import taco.agent.communication.action.impl.ParkingSpaceEffector;
import taco.agent.model.agentmodel.IParkingSpaceActuator;
import taco.agent.model.worldmodel.impl.ParkingSpace;

public class ParkingSpaceActuator extends AudiCupActuator implements IParkingSpaceActuator
{
	private ParkingSpace parkingSpace;

	public ParkingSpaceActuator(String name)
	{
		super(name);
	}

	@Override
	public void setParkingSpace(ParkingSpace parkingSpace)
	{
		this.parkingSpace = parkingSpace;
	}

	@Override
	public boolean createAction(IAudiCupAction action, Map<String, IEffector> effectors)
	{
		if (parkingSpace == null) {
			return false;
		}
		effectors.put(getName(), new ParkingSpaceEffector(parkingSpace.getID(), parkingSpace.getPose().getX(),
										 parkingSpace.getPose().getY(), parkingSpace.getState().ordinal()));
		parkingSpace = null;
		return true;
	}
}
