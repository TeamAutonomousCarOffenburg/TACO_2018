package hso.autonomy.agent.communication.perception;

import java.util.Map;

public interface IPerceptionLogger {
	void start();

	void stop();

	void log(Map<String, IPerceptor> perceptorMap);
}
