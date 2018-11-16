package hso.autonomy.agent.communication.perception;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

/**
 * The IMU perceptor measures the orientation (and acceleration) in the three-dimensional space.
 *
 * @author Stefan Glaser
 */
public interface IIMUPerceptor extends IPerceptor {
	/**
	 * Get gyro orientation
	 */
	Rotation getOrientation();
}
