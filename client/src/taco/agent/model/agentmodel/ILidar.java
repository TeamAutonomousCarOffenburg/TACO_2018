package taco.agent.model.agentmodel;

import hso.autonomy.util.geometry.IPose3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import taco.agent.model.agentmeta.impl.DistanceSensorConfiguration;

import java.util.Map;

public interface ILidar extends IAudiCupSensor {
	/**
	 * @return the distances in m
	 */
	Map<Integer, Double> getDistances();

	/**
	 * @return the distance in m
	 */
	double getDistance(int angle);

	/**
	 * @return the position of an object sensed in car coordinate system
	 */
	Vector3D getObjectPosition(int angle);

	DistanceSensorConfiguration getConfig();

	/**
	 * @return thie pose of the sensor with a 180 degree x-rotation
	 */
	IPose3D getPose();
}