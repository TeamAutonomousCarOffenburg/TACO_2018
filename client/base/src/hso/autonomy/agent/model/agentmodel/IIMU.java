package hso.autonomy.agent.model.agentmodel;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import hso.autonomy.util.geometry.Angle;

/**
 * @author Stefan Glaser
 */
public interface IIMU extends ISensor {
	/**
	 * Retrieve IMU orientation.
	 *
	 * @return The orientation of the IMU
	 */
	Rotation getOrientation();

	/**
	 * Retrieve the (Gyroscope drift compensation) offset around the z-axis.
	 *
	 * @return the offset around the z-axis
	 */
	Angle getZOffset();

	/**
	 * Set the offset around the z-axis for compensating Gyroscope drift.
	 *
	 * @param zOffset the offset around the z-axis
	 */
	void setZOffset(Angle zOffset);

	/**
	 * Update the z-axis offset with the given additional offset weighted by the given factor.
	 *
	 * @param delta the additional offset around the z-axis
	 * @param factor the weighting factor for the additional offset
	 */
	void updateZOffset(Angle delta, double factor);
}
