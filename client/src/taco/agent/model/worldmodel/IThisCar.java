package taco.agent.model.worldmodel;

import hso.autonomy.util.geometry.IPose3D;
import taco.agent.model.worldmodel.impl.DrivePath;

public interface IThisCar extends ICar {
	/**
	 * @return the global pose of this car
	 */
	IPose3D getPose();

	/**
	 * Sets the global pose of this car
	 * @param newPose the new global pose of this car
	 */
	void setPose(IPose3D newPose);

	/**
	 * Sets the path of waypoints and drive instructions for this car
	 * @param path a list of global poses this car is expected to follow
	 */
	void setPath(DrivePath path);

	/**
	 * @return a list of global poses this car is expected to follow and their corresponding drive instructions
	 */
	DrivePath getPath();

	/**
	 * @return the alternative localized pose, based on ADTF localization
	 */
	IPose3D getAlternativePose();

	void setAlternativePose(IPose3D newPose);
}
