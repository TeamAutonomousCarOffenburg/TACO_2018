package taco.agent.communication.perception.impl;

import taco.agent.communication.perception.IDoubleArrayPerceptor;

public class DoubleArrayPerceptor extends ValuePerceptor<double[]> implements IDoubleArrayPerceptor
{
	public DoubleArrayPerceptor(String name, long timestamp, double[] value)
	{
		super(name, timestamp, value);
	}
}
