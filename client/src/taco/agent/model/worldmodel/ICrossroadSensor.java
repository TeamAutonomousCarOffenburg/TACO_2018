package taco.agent.model.worldmodel;

import hso.autonomy.util.geometry.IPose3D;
import taco.agent.communication.perception.IVisionPerceptor;
import taco.agent.model.agentmodel.ICameraSensor;
import taco.agent.model.worldmodel.crossroaddetection.Crossroad;
import taco.agent.model.worldmodel.crossroaddetection.CrossroadHistoryEntry;

import java.util.List;

public interface ICrossroadSensor {
	void update(IVisionPerceptor visionPerceptor, ICameraSensor baslerCamera, IPose3D carPose, float time);

	/**
	 *
	 * @return crossroad history size
	 */
	int getHistorySize();

	/**
	 *
	 * @param index
	 * @return historyEntry with all Crossrads detected at index
	 */
	CrossroadHistoryEntry getHistoryEntry(int index);

	/**
	 *
	 * @return True if crossroad was detected recently (via TTL)
	 */
	boolean isCrossroadInSight(float time);

	/**
	 *
	 * @return latest crossroad with shortest euclidean distance to car. Null if no crossroads detected recently (via
	 * TTL)
	 */
	Crossroad getNearestCrossroad();

	/**
	 *
	 * @return latest sighted crossroad. Empty list if no crossroads detected recently (via
	 * TTL)
	 */
	List<Crossroad> getCurrentCrossroads();
}
