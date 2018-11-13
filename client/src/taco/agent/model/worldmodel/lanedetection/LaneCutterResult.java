package taco.agent.model.worldmodel.lanedetection;

import java.util.ArrayList;
import java.util.List;

public class LaneCutterResult
{
	private int minX;
	private int maxX;
	private int[][] detections;

	public LaneCutterResult(int minX, int maxX, int[][] detections)
	{
		super();
		this.minX = minX;
		this.maxX = maxX;
		this.detections = detections;
	}

	public int getMinX()
	{
		return minX;
	}

	public int getMaxX()
	{
		return maxX;
	}

	public int getY(int lineIndex)
	{
		return detections[lineIndex][0];
	}

	public List<List<Integer>> getDetectionsList()
	{
		List<List<Integer>> result = new ArrayList<>(detections.length);
		for (int i = 0; i < detections.length; i++) {
			List<Integer> xValues = new ArrayList<>(detections[i].length - 1);
			result.add(xValues);
			for (int j = 1; j < detections[i].length; j++) {
				xValues.add(new Integer(detections[i][j]));
			}
		}
		return result;
	}

	public int[][] getDetections()
	{
		return detections;
	}
}
