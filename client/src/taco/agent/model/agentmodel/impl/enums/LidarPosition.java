package taco.agent.model.agentmodel.impl.enums;

import taco.agent.communication.perception.PerceptorName;

public enum LidarPosition {
	LIDAR_FRONT(PerceptorName.LIDAR_FRONT);

	public final String perceptorName;

	LidarPosition(String perceptorName)
	{
		this.perceptorName = perceptorName;
	}

	public static LidarPosition fromPerceptorName(String perceptorName)
	{
		for (LidarPosition position : values()) {
			if (position.perceptorName.equals(perceptorName)) {
				return position;
			}
		}
		return null;
	}
}
