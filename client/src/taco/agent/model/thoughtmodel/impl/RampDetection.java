package taco.agent.model.thoughtmodel.impl;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.agent.model.thoughtmodel.impl.ConsecutiveTruthValue;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.ISpeedController;

/**
 * This truthvalue detects if we are driving up/down to a ramp. The x acceleration of the IMU should then be larger then
 * 0.1
 */
public class RampDetection extends ConsecutiveTruthValue
{
	public RampDetection(int consecutiveTrueValues, int consecutiveFalseValues)
	{
		super(consecutiveTrueValues, consecutiveFalseValues);
	}

	@Override
	public void update(IThoughtModel thoughtModel)
	{
		IAudiCupAgentModel agentModel = (IAudiCupAgentModel) thoughtModel.getAgentModel();
		Vector3D acceleration = agentModel.getImuSensor().getAcceleration();

		if (acceleration != null) {
			updateValidity(acceleration.getX(), thoughtModel.getWorldModel().getGlobalTime());
		}
	}

	public void updateValidity(double xAcceleration, float time)
	{
		boolean result = Math.abs(xAcceleration) > ISpeedController.RAMP_PITCH_ACCELERATION;
		setValidity(result, time);
	}
}
