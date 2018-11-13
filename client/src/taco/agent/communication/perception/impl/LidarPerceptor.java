package taco.agent.communication.perception.impl;

import taco.agent.communication.perception.ILidarPerceptor;
import taco.agent.communication.perception.PerceptorName;

import java.util.List;

public class LidarPerceptor extends AudiCupPerceptor implements ILidarPerceptor
{
	private List<LidarValuePerceptor> values;

	public LidarPerceptor(String name, long timestamp, List<LidarValuePerceptor> values)
	{
		super(PerceptorName.LIDAR_FRONT, timestamp);
		this.values = values;
	}

	@Override
	public List<LidarValuePerceptor> getValues()
	{
		return values;
	}
}
