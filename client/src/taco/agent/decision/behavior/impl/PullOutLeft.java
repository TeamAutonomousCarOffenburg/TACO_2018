package taco.agent.decision.behavior.impl;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.impl.enums.LightName;

public class PullOutLeft extends PullOutBase
{
	public PullOutLeft(IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(IBehaviorConstants.PULL_OUT_LEFT, thoughtModel, behaviors);
	}

	@Override
	protected void starting(IPose3D currentCarPose, IAudiCupAgentModel agentModel)
	{
		super.starting(currentCarPose, agentModel);
		agentModel.getLight(LightName.INDICATOR_LEFT).turnOn();
	}

	@Override
	protected void forward(IPose3D currentCarPose, IAudiCupAgentModel agentModel, double length)
	{
		super.forward(currentCarPose, agentModel, 0.4);
	}

	@Override
	protected void turn(IPose3D currentCarPose, IAudiCupAgentModel agentModel)
	{
		if (targetTurnPose == null) {
			targetTurnPose = startPose.applyTo(new Pose3D(1.2, 0.5, Angle.deg(60)));

			drive(targetTurnPose);
		}
		if (currentCarPose.getDistanceTo(targetTurnPose) < 0.2) {
			agentModel.getLight(LightName.INDICATOR_LEFT).turnOff();
			targetTurnPose = null;
			phase = Phase.FINISHED;
		}
	}
}
