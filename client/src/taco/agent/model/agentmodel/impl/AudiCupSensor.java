package taco.agent.model.agentmodel.impl;

import hso.autonomy.agent.model.agentmodel.ISensor;
import hso.autonomy.agent.model.agentmodel.impl.Sensor;
import hso.autonomy.util.geometry.IPose3D;
import taco.agent.model.agentmodel.IAudiCupSensor;

public abstract class AudiCupSensor extends Sensor implements IAudiCupSensor
{
	/** The time of the last measurement. */
	protected long lastMeasurementTime;

	public AudiCupSensor(String name, IPose3D pose)
	{
		super(name, name, pose);
		lastMeasurementTime = 0;
	}

	@Override
	public ISensor copy()
	{
		// we do not copy sensors here
		return null;
	}

	@Override
	public boolean isInitialized()
	{
		return true;
	}
}
