package taco.agent.model.thoughtmodel.impl;

import hso.autonomy.util.geometry.Geometry;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.impl.SpeedController;
import taco.agent.model.agentmodel.impl.enums.TachometerPosition;
import taco.agent.model.thoughtmodel.IAudiCupThoughtModel;

public class CruiseControl
{
	private IAudiCupThoughtModel thoughtModel;

	private SpeedController speedController;

	public CruiseControl(IAudiCupThoughtModel thoughtModel)
	{
		this.thoughtModel = thoughtModel;
		this.speedController = new SpeedController();
	}

	/**
	 * With this behavior we set the speed dynamicly to the speed of the moving obstacle in front of us
	 */
	public void checkForFollowCar()
	{
		IAudiCupAgentModel agentModel = thoughtModel.getAgentModel();
		if (thoughtModel.isMovingObstacleAhead()) {
			double speed = agentModel.getMotor().getTargetSpeed();
			double distance = 1;
			double obstacleDistance = thoughtModel.getObstacleAheadDistance(distance);

			double limitedSpeed = Geometry.getLinearFuzzyValue(0.3, 2.3, true, obstacleDistance) * 80;
			if (speed > limitedSpeed) {
				speed = limitedSpeed;
				if (speed < 21) {
					speed = 21;
				}
			}
			agentModel.getMotor().drive(speed);
		}
	}

	/**
	 * with this behavior we increase the speed when we need more then set by the behavior to move. This would be e.g.
	 * while driving up/down to the ramp. if acceleration on X-Axis of the gyro is positive, we are driving up,
	 * otherwise down.
	 */
	public void checkForRampHandling()
	{
		IAudiCupAgentModel agentModel = thoughtModel.getAgentModel();
		double xAcceleration = agentModel.getImuSensor().getAcceleration().getX();

		double leftTachoSpeed = agentModel.getTachometer(TachometerPosition.LEFT).getSpeed();
		double rightTachoSpeed = agentModel.getTachometer(TachometerPosition.RIGHT).getSpeed();
		double currentSpeedKMH = Math.abs(leftTachoSpeed + rightTachoSpeed) / 2;
		double targetSpeedPercent = agentModel.getMotor().getTargetSpeed();

		double newSpeed = speedController.calculateSpeedOnRamp(targetSpeedPercent, xAcceleration, currentSpeedKMH);
		if (Math.abs(newSpeed - targetSpeedPercent) > 0.1) {
			agentModel.getMotor().drive(newSpeed);
		}
	}

	protected void update()
	{
		speedController.getRampDetection().update(thoughtModel);
	}
}
