package taco.agent.model.agentmodel;

import taco.agent.model.thoughtmodel.impl.RampDetection;

public interface ISpeedController {
	// max allowed difference between current and desired speed (in KM/H)
	double MAX_DELTA_TOLERANCE = 0.3;

	// 2.7 should be normal when driving LOW_SPEED on the floor (in KM/H)
	double DESIRED_SPEED_ON_RAMP = 2.0;

	// number of cycles to believe we are on the ramp
	int RAMP_DETECTION_BELIEVE = 10;

	// number of cycles to believe we have left the ramp
	int RAMP_DETECTION_FORGET = 10;

	// min acceleration on x-axis of the gyro to believe the car has a pitch like on the ramp
	double RAMP_PITCH_ACCELERATION = 0.1;

	// acceleration value to increase speed
	int INCREASE_SPEED_VALUE = 2;

	// acceleration value to decrease speed
	int DECREASE_SPEED_VALUE = 10;

	/**
	 * Checks if a dynamic control for the speed is required
	 * @param rampUp true if driving up, false if drivin down
	 * @param currentSpeedKMH
	 * @return true if speed is too less for driving up or too high for driving down
	 */
	boolean useSpeedControl(boolean rampUp, double currentSpeedKMH);

	/**
	 * calculates the new speed if speed control is required
	 * @param targetSpeedPercent desired speed, defined by a behavior
	 * @param xAccelerationGyro current pitch to check if up or down
	 * @param currentSpeedKMH
	 * @return
	 */
	double calculateSpeedOnRamp(double targetSpeedPercent, double xAccelerationGyro, double currentSpeedKMH);

	RampDetection getRampDetection();
}
