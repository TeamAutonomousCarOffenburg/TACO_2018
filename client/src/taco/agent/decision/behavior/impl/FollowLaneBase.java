package taco.agent.decision.behavior.impl;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.controller.PTController;
import hso.autonomy.util.geometry.Angle;
import taco.agent.decision.behavior.IFollowLane;
import taco.agent.decision.behavior.base.AudiCupBehavior;
import taco.agent.model.agentmeta.impl.ServoDriveConfiguration;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.util.drive.AngleUtils;

public abstract class FollowLaneBase extends AudiCupBehavior implements IFollowLane
{
	protected PTController controller;

	protected double speed;

	public FollowLaneBase(String name, IThoughtModel thoughtModel, FollowLaneParameters params)
	{
		super(name, thoughtModel);
		float inputFactor = 0.13f;
		if (params != null) {
			// change e.g. in FollowLaneParameters
			inputFactor = params.getInputFactor();
		}
		controller = new PTController(inputFactor, 0.1f, 0.9f);
	}

	@Override
	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	@Override
	public void init()
	{
		super.init();
		speed = IAudiCupMotor.DEFAULT_SPEED;
		// FIXEME: this if is just necessary, because we violate do not call overridables in a constructor principle
		if (controller != null) {
			controller.init();
		}
	}

	protected abstract int getDeltaX();

	@Override
	public void perform()
	{
		Angle steeringAngle = calculateSteeringAngle(getDeltaX());
		getAgentModel().getSteering().steer(steeringAngle);
		getAgentModel().getMotor().drive(speed);
		// getAgentModel().getMotor().drive(speed +
		// (getAgentModel().getUltrasonic(UltrasonicPosition.REAR_CENTER).getDistance() % 100) / 1000);
	}

	/**
	 * Calculates the angle for steering.
	 * Copied from the C++ code.
	 */
	protected Angle calculateSteeringAngle(int deltaX)
	{
		float pT1SteeringOut = controller.getOutput(deltaX);
		ServoDriveConfiguration config = getAgentModel().getCarMetaModel().getServoDriveConfigs()[0];
		Angle steeringAngle = Angle.deg(-pT1SteeringOut);
		return AngleUtils.limit(steeringAngle, config.getMin(), config.getMax());
	}
}
