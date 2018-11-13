package taco.agent.model.worldmodel.crossroaddetection;

import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.IPose3D;
import taco.agent.communication.perception.IVisionPerceptor;
import taco.agent.communication.perception.RecognizedObject;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.model.agentmodel.ICameraSensor;
import taco.agent.model.worldmodel.ICrossroadSensor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CrossroadSensor implements ICrossroadSensor
{
	private static final int HISTORY_SIZE = 30;

	private LinkedList<CrossroadHistoryEntry> history;

	private List<Crossroad> currentCrossroads;

	private final int TTL = 1;

	public CrossroadSensor()
	{
		history = new LinkedList<>();
		currentCrossroads = new ArrayList<>();
	}

	@Override
	public void update(IVisionPerceptor visionPerceptor, ICameraSensor baslerCamera, IPose3D carPose, float time)
	{
		// check for multiple crossroads

		if (visionPerceptor == null)
			return;

		// remove last detected crossroads if they are no longer valid
		if (currentCrossroads.size() > 0 && !currentCrossroads.get(0).isValid(time)) {
			currentCrossroads = new ArrayList<>();
		}

		// look for new crossroads
		List<RecognizedObject> detectedCrossroads =
				visionPerceptor.getRecognizedObjects()
						.stream()
						.filter(f
								-> f.getType() == RecognizedObjectType.T_CROSSING_BOTH ||
										   f.getType() == RecognizedObjectType.T_CROSSING_LEFT ||
										   f.getType() == RecognizedObjectType.T_CROSSING_RIGHT ||
										   f.getType() == RecognizedObjectType.X_CROSSING)
						.collect(Collectors.toList());

		// check for double tags via overlaps
		//		float overlapFactor = 0.5f;
		//		boolean overlaps = detectedCrossroads.stream().anyMatch(o1
		//				-> detectedCrossroads.stream().anyMatch(
		//						o2 -> o1 != o2 && checkDoubleDetection(o1, o2, overlapFactor)));

		if (detectedCrossroads.size() > 0) {
			List<Crossroad> currCrossroads = new ArrayList<>();
			for (RecognizedObject crossroad : detectedCrossroads) {
				currCrossroads.add(new Crossroad(crossroad, time, baslerCamera));
			}

			// sort by euclidean distance. Nearest always at index 0
			currentCrossroads = currCrossroads.stream()
										.sorted(Comparator.comparingDouble(Crossroad::getEuclideanDistance))
										.collect(Collectors.toList());

			// add history entry
			CrossroadHistoryEntry newEntry = new CrossroadHistoryEntry(currentCrossroads, time, carPose);
			history.add(0, newEntry);
			if (history.size() > HISTORY_SIZE) {
				// keep history limited
				history.remove(history.size() - 1);
			}
		}
	}

	@Override
	public int getHistorySize()
	{
		return history.size();
	}

	@Override
	public CrossroadHistoryEntry getHistoryEntry(int index)
	{
		return history.get(index);
	}

	@Override
	public boolean isCrossroadInSight(float time)
	{
		return currentCrossroads.get(0).isValid(time);
	}

	@Override
	public Crossroad getNearestCrossroad()
	{
		return currentCrossroads.size() > 0 ? currentCrossroads.get(0) : null;
	}

	@Override
	public List<Crossroad> getCurrentCrossroads()
	{
		return currentCrossroads;
	}
}
