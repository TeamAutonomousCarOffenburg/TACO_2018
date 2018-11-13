package taco.agent.model.agentmodel.impl;

import java.util.Map;

import hso.autonomy.agent.communication.action.IEffector;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.communication.action.IAudiCupAction;
import taco.agent.communication.action.impl.PositionEffector;
import taco.agent.model.agentmodel.IPositionActuator;

public class PositionActuator extends AudiCupActuator implements IPositionActuator
{
	private IPose3D carPose;

	public PositionActuator(String name)
	{
		super(name);
		carPose = new Pose3D();
	}

	@Override
	public void setCarPose(IPose3D carPose)
	{
		this.carPose = carPose;
	}

	@Override
	public boolean createAction(IAudiCupAction action, Map<String, IEffector> effectors)
	{
		effectors.put(getName(),
				new PositionEffector(carPose.getX(), carPose.getY(), carPose.getHorizontalAngle().radians()));
		return true;
	}
}
