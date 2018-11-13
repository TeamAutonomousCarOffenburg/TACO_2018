package taco.agent.model.thoughtmodel.impl;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.agent.model.thoughtmodel.impl.ConsecutiveTruthValue;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.thoughtmodel.IAudiCupThoughtModel;

public class MovingObstacleDetection extends ConsecutiveTruthValue
{
	@Override
	public void update(IThoughtModel thoughtModel)
	{
		IAudiCupThoughtModel specificThoughtModel = (IAudiCupThoughtModel) thoughtModel;
		IAudiCupAgentModel agentModel = specificThoughtModel.getAgentModel();
		double speed = agentModel.getMotor().getTargetSpeed();
		double distance = 1;
		double obstacleDistance = specificThoughtModel.getObstacleAheadDistance(distance);

		setValidity(speed > 20 && obstacleDistance < distance, thoughtModel.getWorldModel().getGlobalTime());
	}
}
