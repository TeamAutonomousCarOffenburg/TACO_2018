package taco.agent.model.worldmodel.lanedetection;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import hso.autonomy.agent.communication.perception.IPerception;
import hso.autonomy.util.geometry.Pose3D;
import hso.autonomy.util.misc.FuzzyCompare;
import taco.agent.model.agentmodel.impl.AudiCupSensor;

/**
 * Class dealing with lane assist camera readings from five scan lines
 * @author kdorer
 */
public class LaneCutterSensor extends AudiCupSensor
{
	// the focal point of the camera
	private int focalPointX;

	public LaneCutterSensor(int focalPointX)
	{
		super("LaneCutter", new Pose3D());
		this.focalPointX = focalPointX;
	}

	@Override
	public void updateFromPerception(IPerception perception)
	{
		// TODO process perceptor once communication gives it
	}

	// TODO only intermediate until we are sure it works and throw out LaneMiddleSensor
	//	LaneMiddlePerceptor getLaneMiddlePerceptor(
	//			List<List<Vector2D>> linePoints, int[] lanesX) // , IPose3D carPose, StreetMap map
	//	{
	//		long timestamp = 0; // TODO: get from lane cutter perceptor
	//		int wantedX = focalPointX;
	//		boolean valid = true;
	//		int middleX;
	//		float confidence;
	//		int rightLineX;
	//		int middleLineX;
	//		int leftLineX;
	//
	//		int n = linePoints.size();
	//		int middleN = n / 2;
	//
	//		if (lanesX.length < 1) {
	//			valid = false;
	//		} else {
	//			int first5er = find5erLine(linePoints, 0);
	//			int second5er = -1;
	//			if (first5er >= 0) {
	//				second5er = find5erLine(linePoints, first5er + 1);
	//			}
	//			if (second5er > 0) {
	//				// two full lines
	//			}
	//		}
	//
	//		// TODO: write a version that uses map
	//		// Vector2D[] lines = SegmentUtils.getLinePositions(carPose, LaneMiddle.SCANLINE, map);
	//
	//		return new LaneMiddlePerceptor(
	//				timestamp, middleX, wantedX, valid, confidence, rightLineX, middleLineX, leftLineX);
	//	}

	private int find5erLine(List<List<Vector2D>> linePoints, int startIndex)
	{
		for (int i = startIndex; i < linePoints.size(); i++) {
			if (linePoints.get(i).size() == 5) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Extracts x coordinates of points on middle scan line
	 * @param dataPoints the original points from lane detection
	 * @param linePoints the points we groped to lines
	 * @return x coordinates of all lines cutting the middle scan line
	 */
	int[] getPointsOnMiddleLine(LaneCutterResult dataPoints, List<List<Vector2D>> linePoints)
	{
		// middle scan line y
		int y = dataPoints.getY(dataPoints.getDetections().length / 2);
		int[] result = new int[linePoints.size()];
		for (int i = 0; i < result.length; i++) {
			List<Vector2D> currentLine = linePoints.get(i);
			Vector2D middlePoint = findPoint(currentLine, y);
			if (middlePoint != null) {
				// we prefer the observed value if existing over regression
				result[i] = (int) middlePoint.getX();
			} else {
				// we make a simple regression on the points to find the coordinate
				result[i] = regression(currentLine, y);
			}
		}

		return result;
	}

	private Vector2D findPoint(List<Vector2D> line, int y)
	{
		for (Vector2D point : line) {
			if (FuzzyCompare.eq(point.getY(), y, 0.001)) {
				return point;
			}
		}
		return null;
	}

	/**
	 * Find the x coordinate of the passed y coordinate through regression of passed line points
	 * @param measurements the points on the line
	 * @param yWanted the y coordinate for which we want x
	 * @return x coordinate of the intersection point on the middle scan line
	 */
	int regression(List<Vector2D> measurements, int yWanted)
	{
		SimpleRegression regression = new SimpleRegression();
		for (int i = 0; i < measurements.size(); i++) {
			Vector2D measurement = measurements.get(i);
			regression.addData(measurement.getY(), measurement.getX());
		}

		double result = regression.predict(yWanted);
		if (Double.isNaN(result)) {
			// we have a line going horizontally (should never happen)
			return 0;
		}

		return (int) result;
	}

	/**
	 * Extracts lists of points (in pixel space) that probably belong to a line
	 * @param dataPoints the result of lane cutter information
	 * @return List of List of points belonging to lines
	 */
	List<List<Vector2D>> extractPointsOnLine(LaneCutterResult dataPoints)
	{
		List<List<Vector2D>> result = new ArrayList<>();
		List<List<Integer>> pointList = dataPoints.getDetectionsList();

		for (int i = pointList.size() - 1; i > 0; i--) {
			int y = dataPoints.getY(i);
			List<Integer> points = pointList.get(i);
			for (int j = 0; j < points.size(); j++) {
				int x = points.get(j).intValue();
				int currentX = x;
				List<Vector2D> resultPoints = new ArrayList<>();
				resultPoints.add(new Vector2D(x, y));
				for (int k = i - 1; k >= 0; k--) {
					int previousPointY = dataPoints.getY(k);
					List<Integer> previousPoints = pointList.get(k);
					int minDeltaIndex = findBestFitting(currentX, previousPoints);
					if (minDeltaIndex >= 0) {
						int previousPointX = previousPoints.get(minDeltaIndex).intValue();
						if (Math.abs(currentX - previousPointX) < 100) {
							resultPoints.add(new Vector2D(previousPointX, previousPointY));
							currentX = previousPointX;
							previousPoints.remove(minDeltaIndex);
						}
					}
				}
				if (resultPoints.size() > 1) {
					insertSorted(result, resultPoints);
				}
			}
		}

		return result;
	}

	private void insertSorted(List<List<Vector2D>> result, List<Vector2D> resultPoints)
	{
		double value = resultPoints.get(0).getX();
		for (int i = 0; i < result.size(); i++) {
			if (value < result.get(i).get(0).getX()) {
				result.add(i, resultPoints);
				return;
			}
		}
		result.add(resultPoints);
	}

	private int findBestFitting(int x, List<Integer> points)
	{
		int minIndex = -1;
		int minDelta = Integer.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {
			int delta = Math.abs(x - points.get(i).intValue());
			if (delta < minDelta) {
				minDelta = delta;
				minIndex = i;
			}
		}
		return minIndex;
	}
}
