package taco.agent.communication.perception;

import taco.agent.communication.perception.impl.LidarValuePerceptor;

import java.util.List;

public interface ILidarPerceptor {
	List<LidarValuePerceptor> getValues();
}
