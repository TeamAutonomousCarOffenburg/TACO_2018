package taco.agent.model.agentmodel;

import hso.autonomy.util.geometry.IPose3D;

public interface IPositionActuator extends IAudiCupActuator {
	void setCarPose(IPose3D carPose);
}
