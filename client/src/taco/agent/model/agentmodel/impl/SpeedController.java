package taco.agent.model.agentmodel.impl;

import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.ISpeedController;
import taco.agent.model.thoughtmodel.impl.RampDetection;

public class SpeedController implements ISpeedController
{
	private final RampDetection rampDetection;

	private int accelerateCounter = 0;

	private int accelerationValue = 0;

	private double speedToKeep = 0.0;

	public SpeedController()
	{
		this.rampDetection =
				new RampDetection(ISpeedController.RAMP_DETECTION_BELIEVE, ISpeedController.RAMP_DETECTION_FORGET);
	}

	@Override
	public boolean useSpeedControl(boolean rampUp, double currentSpeedKMH)
	{
		// calculate difference between current und desired speed
		double delta = currentSpeedKMH - DESIRED_SPEED_ON_RAMP;

		// check if the difference is larger then a allowed tolerance
		if (rampUp) {
			return delta < MAX_DELTA_TOLERANCE;
		} else {
			return delta > MAX_DELTA_TOLERANCE;
		}
	}

	@Override
	public double calculateSpeedOnRamp(double targetSpeedPercent, double xAccelerationGyro, double currentSpeedKMH)
	{
		if (rampDetection.isValid()) {
			double newSpeed = 0.0;
			if (xAccelerationGyro > 0.0) {
				// do we need more speed to move up
				if (useSpeedControl(true, currentSpeedKMH)) {
					accelerateCounter++;
					// increase the speed each X cycles
					if (accelerateCounter % ISpeedController.RAMP_DETECTION_BELIEVE == 0) {
						// increase desired speed
						accelerationValue += ISpeedController.INCREASE_SPEED_VALUE;
						speedToKeep = targetSpeedPercent + accelerationValue;
					}
				}
				// limit speed to high speed on the ramp
				newSpeed = Math.min(speedToKeep, IAudiCupMotor.HIGH_SPEED);
			} else if (xAccelerationGyro < 0.0) {
				// do we need brake/less speed -> are we too fast
				if (useSpeedControl(false, currentSpeedKMH)) {
					accelerateCounter++;
					if (accelerateCounter % ISpeedController.RAMP_DETECTION_BELIEVE == 0) {
						// decrease desired speed
						accelerationValue -= ISpeedController.DECREASE_SPEED_VALUE;
						speedToKeep = targetSpeedPercent + accelerationValue;
					}
				}
				// limit brakes, otherwise we would stop or drive backwards on the ramp
				newSpeed = Math.max(speedToKeep, IAudiCupMotor.RAMP_BRAKING_SPEED);
			}
			return newSpeed;
		} else {
			// if we are leaving the ramp, reset the values
			accelerationValue = 0;
			accelerateCounter = 0;
			speedToKeep = 0.0;
			return targetSpeedPercent;
		}
	}

	@Override
	public RampDetection getRampDetection()
	{
		return rampDetection;
	}
}
