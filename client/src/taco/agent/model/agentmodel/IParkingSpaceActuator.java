package taco.agent.model.agentmodel;

import taco.agent.model.worldmodel.impl.ParkingSpace;

public interface IParkingSpaceActuator extends IAudiCupActuator {
	void setParkingSpace(ParkingSpace parkingSpace);
}
