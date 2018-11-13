package taco.agent.model.agentmodel;

import hso.autonomy.agent.model.agentmodel.ISensor;

public interface IAudiCupSensor extends ISensor {
	/**
	 * @return true if this sensor is finished initializing
	 */
	boolean isInitialized();
}