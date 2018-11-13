package taco.agent.model.agentmodel.impl;

import hso.autonomy.agent.communication.perception.IPerception;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Geometry;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import taco.agent.communication.perception.IAudiCupPerception;
import taco.agent.communication.perception.ILidarPerceptor;
import taco.agent.communication.perception.impl.LidarValuePerceptor;
import taco.agent.model.agentmeta.impl.DistanceSensorConfiguration;
import taco.agent.model.agentmodel.ILidar;

import java.util.*;

public class Lidar extends AudiCupSensor implements ILidar
{
	public static final int MIN_ANGLE = -90;

	public static final int MAX_ANGLE = 90;

	private final DistanceSensorConfiguration config;

	/** history */
	private static final int HISTORY_SIZE = 0;
	private ArrayList<Map<Integer, Double>> history = new ArrayList<>();

	/** the measured distance in m */
	private Map<Integer, Double> lidarDistances;

	public Lidar(DistanceSensorConfiguration config)
	{
		super(config.getName(), config.getPose());
		this.config = config;

		lidarDistances = new HashMap<>();
		for (int i = Lidar.MIN_ANGLE; i <= Lidar.MAX_ANGLE; i++) {
			lidarDistances.put(i, (double) config.getMaxDistance());
		}
	}

	public Lidar(DistanceSensorConfiguration config, Map<Integer, Double> distances)
	{
		super(config.getName(), config.getPose());
		this.config = config;

		lidarDistances = distances;
	}

	@Override
	public void updateFromPerception(IPerception perception)
	{
		ILidarPerceptor perceptor = ((IAudiCupPerception) perception).getLidarPerceptor();
		if (perceptor == null) {
			return;
		}

		// here we could add some filtering
		List<LidarValuePerceptor> values = perceptor.getValues();
		Map<Integer, Double> valuesAsMap = new TreeMap<>();
		for (LidarValuePerceptor value : values) {
			valuesAsMap.put(value.getAngle(), value.getDistance());
		}

		for (Map.Entry<Integer, Double> item : lidarDistances.entrySet()) {
			Double aDouble = valuesAsMap.get(item.getKey());
			if (aDouble == null || Double.isNaN(aDouble)) {
				item.setValue(getDistanceFromHistory(item.getKey()));
			} else {
				item.setValue(aDouble);
			}
		}

		addDistancesToHistory(lidarDistances);
	}

	@Override
	public DistanceSensorConfiguration getConfig()
	{
		return config;
	}

	@Override
	public Map<Integer, Double> getDistances()
	{
		return lidarDistances;
	}

	@Override
	public double getDistance(int angle)
	{
		return lidarDistances.getOrDefault(angle, (double) 0);
	}

	@Override
	public Vector3D getObjectPosition(int angle)
	{
		return getPose(angle).applyTo(new Vector3D(getDistance(angle), 0, 0));
	}

	private void addDistancesToHistory(Map<Integer, Double> distances)
	{
		history.add(0, new HashMap<>(distances));
		if (history.size() > HISTORY_SIZE) {
			history.remove(HISTORY_SIZE);
		}
	}

	private Double getDistanceFromHistory(int angle)
	{
		for (Map<Integer, Double> map : history) {
			if (map.get(angle) == null) {
				continue;
			}
			return map.get(angle);
		}
		return (double) config.getMaxDistance();
	}

	@Override
	public IPose3D getPose()
	{
		Rotation rot = Geometry.createXRotation(Angle.deg(180).radians());
		return new Pose3D(config.getPose().getPosition(), rot.applyTo(config.getPose().getOrientation()));
	}

	public IPose3D getPose(int angle)
	{
		Rotation rot = Geometry.createZRotation(Angle.deg(angle).radians());
		return new Pose3D(getPose().getPosition(), rot.applyTo(getPose().getOrientation()));
	}
}
