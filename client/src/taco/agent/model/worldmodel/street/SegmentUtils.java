package taco.agent.model.worldmodel.street;

import static taco.agent.model.worldmodel.street.SegmentType.CURVE_BIG;
import static taco.agent.model.worldmodel.street.SegmentType.CURVE_SMALL;
import static taco.agent.model.worldmodel.street.SegmentType.MERGING_END;
import static taco.agent.model.worldmodel.street.SegmentType.MERGING_IN;
import static taco.agent.model.worldmodel.street.SegmentType.MERGING_MIDDLE;
import static taco.agent.model.worldmodel.street.SegmentType.MERGING_START;
import static taco.agent.model.worldmodel.street.SegmentType.PARKING_SPACE_HORIZONTAL;
import static taco.agent.model.worldmodel.street.SegmentType.PARKING_SPACE_VERTICAL;
import static taco.agent.model.worldmodel.street.SegmentType.STRAIGHT;
import static taco.agent.model.worldmodel.street.SegmentType.STRAIGHT_WITH_CROSSWALK;
import static taco.agent.model.worldmodel.street.SegmentType.S_CURVE_BOTTOM;
import static taco.agent.model.worldmodel.street.SegmentType.S_CURVE_TOP;
import static taco.agent.model.worldmodel.street.SegmentType.T_CROSSING;
import static taco.agent.model.worldmodel.street.SegmentType.X_CROSSING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.IPose2D;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.VectorUtils;

public class SegmentUtils
{
	private static final Map<SegmentType, Vector2D[][]> LINES;

	private static final Map<SegmentType, Vector2D[][]> MIDDLE_LINES;

	public enum SpecialStreetLine { CROSSWALK, STOP_LINE_BOTH, STOP_LINE_LEFT, STOP_LINE_RIGHT, L0, L1, L2, L3 }

	private static final Map<SpecialStreetLine, Vector2D[][]> SPECIAL_LINES;

	//######################################################
	//
	// INIT SPECIAL_LINES LIST
	//
	//######################################################

	static
	{
		Map<SpecialStreetLine, Vector2D[][]> lines = new HashMap<>();

		lines.put(SpecialStreetLine.CROSSWALK,
				new Vector2D[][] {//
						new Vector2D[] {new Vector2D(0.250, 0.200), new Vector2D(0.750, 0.200)},
						new Vector2D[] {new Vector2D(0.250, 0.400), new Vector2D(0.750, 0.400)},
						new Vector2D[] {new Vector2D(0.250, 0.600), new Vector2D(0.750, 0.600)},
						new Vector2D[] {new Vector2D(0.250, 0.800), new Vector2D(0.750, 0.800)}

				});

		lines.put(SpecialStreetLine.STOP_LINE_BOTH,
				new Vector2D[][] {
						//
						// LEFT
						new Vector2D[] {
								new Vector2D(0.10225, 0.5), new Vector2D(0.10225 + 0.29775, 0.5)}, // middle line part
						new Vector2D[] {new Vector2D(0.07725, 0.98), new Vector2D(0.07725, 0.49)}, // stop line part
						new Vector2D[] {new Vector2D(0, 0.965), new Vector2D(0.07725, 0.965)},	 // side line part

						// RIGHT
						new Vector2D[] {new Vector2D(0.10225 + 0.29775 + 0.2, 0.5),
								new Vector2D(0.10225 + 0.2 + 2 * 0.29775, 0.5)},				   // middle line part
						new Vector2D[] {new Vector2D(0.92275, 0.02), new Vector2D(0.92275, 0.51)}, // stop line part
						new Vector2D[] {new Vector2D(0.92275, 0.035), new Vector2D(1, 0.035)}	  // side line part
				});

		lines.put(SpecialStreetLine.STOP_LINE_LEFT,
				new Vector2D[][] {
						//
						new Vector2D[] {
								new Vector2D(0.10225, 0.5), new Vector2D(0.10225 + 0.29775, 0.5)}, // middle line part
						new Vector2D[] {new Vector2D(0.07725, 0.98), new Vector2D(0.07725, 0.49)}, // stop line part
						new Vector2D[] {new Vector2D(0, 0.965), new Vector2D(0.07725, 0.965)}	  // side line part
				});

		lines.put(SpecialStreetLine.STOP_LINE_RIGHT,
				new Vector2D[][] {
						//
						new Vector2D[] {new Vector2D(0.10225 + 0.29775 + 0.2, 0.5),
								new Vector2D(0.10225 + 0.2 + 2 * 0.29775, 0.5)},				   // middle line part
						new Vector2D[] {new Vector2D(0.92275, 0.02), new Vector2D(0.92275, 0.51)}, // stop line part
						new Vector2D[] {new Vector2D(0.92275, 0.035), new Vector2D(1, 0.035)}	  // side line part
				});

		lines.put(SpecialStreetLine.L0,
				new Vector2D[][] {
						getQuarterCirclePoints(0.035, 10, 1), getQuarterCirclePoints(0.035, 50, 1),
				});

		lines.put(SpecialStreetLine.L1, new Vector2D[][] {
												getQuarterCirclePoints(0.035, 10, 0, 0, Angle.deg(90)),
												getQuarterCirclePoints(0.035, 50, 0, 0, Angle.deg(90)),
										});

		lines.put(SpecialStreetLine.L2, new Vector2D[][] {
												getQuarterCirclePoints(0.035, 10, 0, 1, Angle.deg(180)),
												getQuarterCirclePoints(0.035, 50, 0, 1, Angle.deg(180)),
										});

		lines.put(SpecialStreetLine.L3, new Vector2D[][] {
												getQuarterCirclePoints(0.035, 10, 1, 1, Angle.deg(-90)),
												getQuarterCirclePoints(0.035, 50, 1, 1, Angle.deg(-90)),
										});

		SPECIAL_LINES = Collections.unmodifiableMap(lines);
	}

	//######################################################
	//
	// INIT LINES LIST
	//
	//######################################################

	static
	{
		Map<SegmentType, Vector2D[][]> lines = new HashMap<>();
		lines.put(STRAIGHT, //
				new Vector2D[][] {new Vector2D[] {new Vector2D(0, 0.035), new Vector2D(1, 0.035)},
						new Vector2D[] {new Vector2D(0, 0.5), new Vector2D(1, 0.5)},
						new Vector2D[] {new Vector2D(0, 0.965), new Vector2D(1, 0.965)}});

		lines.put(STRAIGHT_WITH_CROSSWALK, lines.get(STRAIGHT));

		lines.put(CURVE_SMALL, //
				new Vector2D[][] {
						getQuarterCirclePoints(1.965, 14, 2), getQuarterCirclePoints(1.5, 14, 2),
						getQuarterCirclePoints(1.035, 14, 2),
				});

		lines.put(CURVE_BIG, //
				new Vector2D[][] {
						getQuarterCirclePoints(2.965, 14, 3), getQuarterCirclePoints(2.5, 14, 3),
						getQuarterCirclePoints(2.035, 14, 3),
				});

		// TCrossing
		Vector2D[][] corners = Stream.concat(Arrays.stream(SPECIAL_LINES.get(SpecialStreetLine.L0)),
											 Arrays.stream(SPECIAL_LINES.get(SpecialStreetLine.L1)))
									   .toArray(Vector2D[][] ::new);
		Vector2D[][] tCrossing = new Vector2D[][] {
				new Vector2D[] {new Vector2D(0, 0.965), new Vector2D(1, 0.965)},
				new Vector2D[] {new Vector2D(0, 0.5), new Vector2D(1, 0.5)},
		};
		tCrossing = Stream.concat(Arrays.stream(tCrossing), Arrays.stream(corners)).toArray(Vector2D[][] ::new);

		lines.put(T_CROSSING, tCrossing);

		// XCrossing
		corners = Stream.concat(Arrays.stream(corners), Arrays.stream(SPECIAL_LINES.get(SpecialStreetLine.L2)))
						  .toArray(Vector2D[][] ::new);
		corners = Stream.concat(Arrays.stream(corners), Arrays.stream(SPECIAL_LINES.get(SpecialStreetLine.L3)))
						  .toArray(Vector2D[][] ::new);

		lines.put(X_CROSSING, corners);

		lines.put(PARKING_SPACE_VERTICAL, //
				new Vector2D[][] {		  //
						new Vector2D[] {
								new Vector2D(0, 0), new Vector2D(0, 0.8), new Vector2D(2, 0.8), new Vector2D(2, 0)},
						new Vector2D[] {new Vector2D(0.5, 0.8), new Vector2D(0.5, 0)},
						new Vector2D[] {new Vector2D(1, 0.8), new Vector2D(1, 0)},
						new Vector2D[] {new Vector2D(1.5, 0.8), new Vector2D(1.5, 0)}});

		lines.put(PARKING_SPACE_HORIZONTAL, //
				new Vector2D[][] {new Vector2D[] {new Vector2D(0, 0), new Vector2D(4, 0), new Vector2D(4, 0.5),
										  new Vector2D(0, 0.5), new Vector2D(0, 0)},
						new Vector2D[] {new Vector2D(0.8, 0.5), new Vector2D(0.8, 0)},
						new Vector2D[] {new Vector2D(1.6, 0.5), new Vector2D(1.6, 0)},
						new Vector2D[] {new Vector2D(2.4, 0.5), new Vector2D(2.4, 0)},
						new Vector2D[] {new Vector2D(3.2, 0.5), new Vector2D(3.2, 0)}});

		double delta = 0.13;
		lines.put(S_CURVE_TOP, //
				new Vector2D[][] {
						concat(true, getCirclePoints(1.965 + delta, 6, 0, 1 - delta, Angle.deg(21.3), Angle.deg(90)),
								getCirclePoints(1.035 + delta, 6, 3, 2 + delta, Angle.deg(-158.7), Angle.deg(-90))),
						//
						concat(true, getCirclePoints(1.5 + delta, 6, 0, 1 - delta, Angle.deg(21.3), Angle.deg(90)),
								getCirclePoints(1.5 + delta, 6, 3, 2 + delta, Angle.deg(-158.7), Angle.deg(-90))),
						//
						concat(true, getCirclePoints(1.035 + delta, 6, 0, 1 - delta, Angle.deg(21.3), Angle.deg(90)),
								getCirclePoints(1.965 + delta, 6, 3, 2 + delta, Angle.deg(-158.7), Angle.deg(-90))),
				});

		lines.put(S_CURVE_BOTTOM, //
				new Vector2D[][] {
						concat(false, getCirclePoints(1.965 + delta, 6, 0, 2 + delta, Angle.deg(-90), Angle.deg(-21.3)),
								getCirclePoints(1.035 + delta, 6, 3, 1 - delta, Angle.deg(90), Angle.deg(158.7))),
						//
						concat(false, getCirclePoints(1.5 + delta, 6, 0, 2 + delta, Angle.deg(-90), Angle.deg(-21.3)),
								getCirclePoints(1.5 + delta, 6, 3, 1 - delta, Angle.deg(90), Angle.deg(158.7))),
						//
						concat(false, getCirclePoints(1.035 + delta, 6, 0, 2 + delta, Angle.deg(-90), Angle.deg(-21.3)),
								getCirclePoints(1.965 + delta, 6, 3, 1 - delta, Angle.deg(90), Angle.deg(158.7)))});

		lines.put(MERGING_START, new Vector2D[][] {new Vector2D[] {new Vector2D(0., 0.5), new Vector2D(1., 0.965)},
										 new Vector2D[] {new Vector2D(0., 0.035), new Vector2D(1., 0.5)},
										 new Vector2D[] {new Vector2D(0., 0.035), new Vector2D(1., 0.035)}});

		lines.put(MERGING_MIDDLE, new Vector2D[][] {new Vector2D[] {new Vector2D(0., 0.5), new Vector2D(1., 0.5)}});

		lines.put(MERGING_END, new Vector2D[][] {new Vector2D[] {new Vector2D(0., 0.035), new Vector2D(1., 0.5)} /*,
									   new Vector2D[] {new Vector2D(0., 0.035), new Vector2D(1., 0.035)}*/});

		lines.put(MERGING_IN, new Vector2D[][] {new Vector2D[] {new Vector2D(0., 0.035), new Vector2D(1., 0.035)},
									  new Vector2D[] {new Vector2D(0, 0.5), new Vector2D(1, 0.5)},
									  new Vector2D[] {new Vector2D(0, 0.965), new Vector2D(1, 0.965)}});

		LINES = Collections.unmodifiableMap(lines);
	}

	//######################################################
	//
	// INIT MIDDLE_LINES LIST
	//
	//######################################################

	static
	{
		Map<SegmentType, Vector2D[][]> lines = new HashMap<>();

		lines.put(STRAIGHT, new Vector2D[][] {// 1.
									new Vector2D[] {new Vector2D(0.10225, 0.5), new Vector2D(0.10225 + 0.29775, 0.5)},
									// 2.
									new Vector2D[] {new Vector2D(0.10225 + 0.29775 + 0.2, 0.5),
											new Vector2D(0.10225 + 0.2 + 2 * 0.29775, 0.5)}});

		lines.put(STRAIGHT_WITH_CROSSWALK, lines.get(STRAIGHT));
		lines.put(MERGING_IN, lines.get(STRAIGHT));

		double radius = 1.49325;
		double lineLength = 10.9;
		double lineSpace = 7.03;
		double startingSpace = 3.7;

		lines.put(CURVE_SMALL, createMiddleLines(radius, 10, CurveType.CURVE_SMALL.NUM_OF_MIDDLINES, 2, 0, 90.0,
									   lineLength, lineSpace, startingSpace));

		radius = 2.48878;
		lineLength = 6.68;
		lineSpace = 4.54;
		startingSpace = 3.7;

		lines.put(SegmentType.CURVE_BIG, createMiddleLines(radius, 10, CurveType.CURVE_BIG.NUM_OF_MIDDLINES, 3, 0, 90.0,
												 lineLength, lineSpace, startingSpace));

		lines.put(T_CROSSING,
				new Vector2D[][] {// Mittel Linie  1.
						new Vector2D[] {new Vector2D(0.10225, 0.5), new Vector2D(0.10225 + 0.29775, 0.5)},

						// 2.
						new Vector2D[] {new Vector2D(0.10225 + 0.29775 + 0.2, 0.5),
								new Vector2D(0.10225 + 0.2 + 2 * 0.29775, 0.5)}});

		double delta = 0.13;
		radius = 1.62107;
		lineLength = 10.9;
		lineSpace = 5.5;
		lines.put(S_CURVE_TOP, //
				new Vector2D[][] {

						// 1. middle line
						getCirclePoints(radius, 10, 0, 1 - delta,
								Angle.deg(90.0 + -1 * (3.7 + 0 * lineLength + 0 * lineSpace)),
								Angle.deg(90.0 + -1 * (3.7 + 1 * lineLength + 0 * lineSpace))),

						// 2.
						getCirclePoints(radius, 10, 0, 1 - delta,
								Angle.deg(90.0 + -1 * (3.7 + 1 * lineLength + 1 * lineSpace)),
								Angle.deg(90.0 + -1 * (3.7 + 2 * lineLength + 1 * lineSpace))),

						// 3.
						getCirclePoints(radius, 10, 0, 1 - delta,
								Angle.deg(90.0 + -1 * (3.7 + 2 * lineLength + 2 * lineSpace)),
								Angle.deg(90.0 + -1 * (3.7 + 3 * lineLength + 2 * lineSpace))),

						// 4.
						getCirclePoints(radius, 10, 0, 1 - delta,
								Angle.deg(90.0 + -1 * (3.7 + 3 * lineLength + 3 * lineSpace)),
								Angle.deg(90.0 + -1 * (3.7 + 4 * lineLength + 3 * lineSpace))),

						// 5. middle line
						getCirclePoints(radius, 10, 3, 2 + delta,
								Angle.deg(-90.0 + 3.7 + 0 * lineLength + 0 * lineSpace),
								Angle.deg(-90.0 + 3.7 + 1 * lineLength + 0 * lineSpace)),

						// 6.
						getCirclePoints(radius, 10, 3, 2 + delta,
								Angle.deg(-90.0 + 3.7 + 1 * lineLength + 1 * lineSpace),
								Angle.deg(-90.0 + 3.7 + 2 * lineLength + 1 * lineSpace)),

						// 7.
						getCirclePoints(radius, 10, 3, 2 + delta,
								Angle.deg(-90.0 + 3.7 + 2 * lineLength + 2 * lineSpace),
								Angle.deg(-90.0 + 3.7 + 3 * lineLength + 2 * lineSpace)),

						// 8.
						getCirclePoints(radius, 10, 3, 2 + delta,
								Angle.deg(-90.0 + 3.7 + 3 * lineLength + 3 * lineSpace),
								Angle.deg(-90.0 + 3.7 + 4 * lineLength + 3 * lineSpace)),

				});

		lines.put(S_CURVE_BOTTOM, //
				new Vector2D[][] {

						// 1. middle line
						getCirclePoints(radius, 10, 0, 2 + delta,
								Angle.deg(-1 * 90.0 + 3.7 + 0 * lineLength + 0 * lineSpace),
								Angle.deg(-1 * 90.0 + 3.7 + 1 * lineLength + 0 * lineSpace)),

						// 2.
						getCirclePoints(radius, 10, 0, 2 + delta,
								Angle.deg(-1 * 90.0 + 3.7 + 1 * lineLength + 1 * lineSpace),
								Angle.deg(-1 * 90.0 + 3.7 + 2 * lineLength + 1 * lineSpace)),

						// 3.
						getCirclePoints(radius, 10, 0, 2 + delta,
								Angle.deg(-1 * 90.0 + 3.7 + 2 * lineLength + 2 * lineSpace),
								Angle.deg(-1 * 90.0 + 3.7 + 3 * lineLength + 2 * lineSpace)),

						// 4.
						getCirclePoints(radius, 10, 0, 2 + delta,
								Angle.deg(-1 * 90.0 + 3.7 + 3 * lineLength + 3 * lineSpace),
								Angle.deg(-1 * 90.0 + 3.7 + 4 * lineLength + 3 * lineSpace)),

						// 5. middle line
						getCirclePoints(radius, 10, 3, 1 - delta,
								Angle.deg(90.0 + 3.7 + 0 * lineLength + 0 * lineSpace),
								Angle.deg(90.0 + 3.7 + 1 * lineLength + 0 * lineSpace)),

						// 6.
						getCirclePoints(radius, 10, 3, 1 - delta,
								Angle.deg(90.0 + 3.7 + 1 * lineLength + 1 * lineSpace),
								Angle.deg(90.0 + 3.7 + 2 * lineLength + 1 * lineSpace)),

						// 7.
						getCirclePoints(radius, 10, 3, 1 - delta,
								Angle.deg(90.0 + 3.7 + 2 * lineLength + 2 * lineSpace),
								Angle.deg(90.0 + 3.7 + 3 * lineLength + 2 * lineSpace)),

						// 8.
						getCirclePoints(radius, 10, 3, 1 - delta,
								Angle.deg(90.0 + 3.7 + 3 * lineLength + 3 * lineSpace),
								Angle.deg(90.0 + 3.7 + 4 * lineLength + 3 * lineSpace)),

				});

		// kleine Kreislinien for XCrossing
		//						getQuarterCirclePoints(0.035, 50, 0, 0, Angle.deg(90)),
		// getQuarterCirclePoints(0.035, 50, 1),
		//						getQuarterCirclePoints(0.035, 50, 1, 1, Angle.deg(-90)),
		//						getQuarterCirclePoints(0.035, 50, 0, 1, Angle.deg(180))

		lines.put(MERGING_IN, new Vector2D[][] {new Vector2D[] {new Vector2D(0.05, 0.965), new Vector2D(0.2, 0.965)},
									  new Vector2D[] {new Vector2D(0.3, 0.965), new Vector2D(0.45, 0.965)},
									  new Vector2D[] {new Vector2D(0.55, 0.965), new Vector2D(0.7, 0.965)},
									  new Vector2D[] {new Vector2D(0.8, 0.965), new Vector2D(0.95, 0.965)}});

		// lines.put(MERGING_END, lines.get(MERGING_MIDDLE));

		MIDDLE_LINES = Collections.unmodifiableMap(lines);
	}

	public static List<StreetLine> getLines(Segment segment)
	{
		return transformLines(VectorUtils.to2D(segment.getPosition()), segment.getRotation(), segment.getType(),
				LINES.get(segment.getType()));
	}

	public static List<StreetLine> getMiddleLines(Segment segment)
	{
		return transformLines(VectorUtils.to2D(segment.getPosition()), segment.getRotation(), segment.getType(),
				MIDDLE_LINES.get(segment.getType()));
	}

	public static List<StreetLine> getSpecialLines(Segment segment, SpecialStreetLine lineType)
	{
		return transformLines(VectorUtils.to2D(segment.getPosition()), segment.getRotation(), segment.getType(),
				SPECIAL_LINES.get(lineType));
	}

	private static Vector2D[] concat(boolean reverseFirst, Vector2D[] points1, Vector2D[] points2)
	{
		Vector2D[] result = new Vector2D[points1.length + points2.length - 1];
		if (reverseFirst) {
			Collections.reverse(Arrays.asList(points1));
		} else {
			Collections.reverse(Arrays.asList(points2));
		}
		System.arraycopy(points1, 0, result, 0, points1.length);
		System.arraycopy(points2, 1, result, points1.length, points2.length - 1);
		return result;
	}

	private static List<StreetLine> transformLines(Vector2D position, Angle angle, SegmentType type, Vector2D[][] lines)
	{
		if (lines == null) {
			return null;
		}

		List<StreetLine> transformed = new ArrayList<>();
		int count = 0;
		for (Vector2D[] line : lines) {
			Vector2D prev = null;
			for (Vector2D point : line) {
				if (prev != null) {
					Vector2D start = prev.add(position);
					Vector2D end = point.add(position);

					Vector2D pivot = getRotationPoint(type.width, type.height, angle).add(position);
					start = VectorUtils.rotateAround(start, pivot, angle);
					end = VectorUtils.rotateAround(end, pivot, angle);

					LineType lineType = LineType.OUTER;
					if (type.isParkingSpace()) {
						lineType = LineType.PARKING;
					} else if (count == 1 && type != X_CROSSING) {
						// we have made sure that the middle line is always defined second line
						lineType = LineType.MIDDLE;
					}

					transformed.add(new StreetLine(new SubLine(start, end, 0.0001), lineType));
				}

				prev = point;
			}
			count++;
		}

		return transformed;
	}

	public static Vector2D getRotationPoint(int width, int height, Angle angle)
	{
		double x = width / 2.0;
		double y = height / 2.0;

		// rectangular segments have to be rotated differently to pertain the location
		Direction direction = Direction.getDirection(angle);
		if (direction == Direction.WEST) {
			if (width > height) {
				x = x / width;
			} else if (width < height) {
				y = y / height;
			}
		}
		if (direction == Direction.EAST) {
			if (width > height) {
				y = x;
			} else if (width < height) {
				x = y;
			}
		}
		return new Vector2D(x, y);
	}

	private static Vector2D[] getQuarterCirclePoints(double radius, int segments, double centerX)
	{
		return getCirclePoints(radius, segments, centerX, 0, Angle.ANGLE_90, Angle.ANGLE_180);
	}

	private static Vector2D[] getQuarterCirclePoints(
			double radius, int segments, double centerX, double centerY, Angle rotation)
	{
		return getCirclePoints(radius, segments, centerX, centerY, Angle.ANGLE_90.subtract(rotation),
				Angle.ANGLE_180.subtract(rotation));
	}

	/**
	 * @param radius the radius of the circle in m
	 * @param segments the number of lines with which to approximate a circle
	 * @param centerX the shift of x circle center coordinate
	 * @return an array of segments+1 points on the part of the circle specified
	 */
	private static Vector2D[] getCirclePoints(
			double radius, int segments, double centerX, double centerY, Angle angleStart, Angle angleEnd)
	{
		Vector2D[] result = new Vector2D[segments + 1];
		double angle = angleStart.radians();
		double howMuch = Math.abs(angleEnd.subtract(angleStart).degrees()) / 360;
		double angleInc = howMuch * 2 * Math.PI / segments;
		for (int i = 0; i < result.length; angle += angleInc, i++) {
			double x = radius * Math.cos(angle) + centerX;
			double y = radius * Math.sin(angle) + centerY;
			result[i] = new Vector2D(x, y);
		}
		return result;
	}

	/**
	 * Returns the position of the lane lines which intersect with the scanline. Does not check whether the lines belong
	 * to the current segment or the segment we want to drive next (therefore, see method
	 * getLinePositionsForTargetSegment below).
	 */
	public static Vector2D[] getLinePositions(IPose3D carPose3D, SubLine measurementLine, StreetMap map)
	{
		// TODO 3D no proper 3D here
		IPose2D carPose = carPose3D.get2DPose();
		Vector2D[] result = new Vector2D[3];
		SubLine detectionLine = getDetectionLine(carPose, measurementLine);
		List<Intersection> intersections = getLineIntersections(map, detectionLine);

		if (intersections.isEmpty()) {
			return result;
		}

		int countMidlines = countMidlineIndex(intersections);
		if (countMidlines > 1) {
			// we change the array size to indicate that we had two or more  midline candidates
			result = new Vector2D[4];
		}

		int mostMiddleIndex = getMidlineIndex(intersections, carPose3D.getPosition());

		if (mostMiddleIndex >= 0) {
			// we have a middle line, so take right and left with respect to middle
			result[1] = carPose.applyInverseTo(intersections.get(mostMiddleIndex).getPoint());
			if (mostMiddleIndex > 0) {
				result[0] = carPose.applyInverseTo(intersections.get(mostMiddleIndex - 1).getPoint());
			}
			if (mostMiddleIndex < intersections.size() - 1) {
				result[2] = carPose.applyInverseTo(intersections.get(mostMiddleIndex + 1).getPoint());
			}
		}
		return result;
	}

	/**
	 * Returns the position of the streetlines which intersect with the scanline AND lie on the current segment or on
	 * the segment which we want to drive next. If no lines are found, we call the getLinePositions method (see method
	 * above), which just looks for intersections with the scanline.
	 */
	public static Vector2D[] getLinePositionsForTargetSegment(IPose3D carPose3D, SubLine measurementLine, StreetMap map)
	{
		// TODO 3D no proper 3D here
		IPose2D carPose = carPose3D.get2DPose();
		Vector2D[] result = new Vector2D[3];
		if (map.isCarOnOverlappingSegment(carPose3D)) {
			return getLinePositions(carPose3D, measurementLine, map);
		}
		try {
			Segment currentSegment = map.getSegmentContaining(carPose3D.getPosition());
			Direction drivingDir = Direction.getDirection(carPose3D.getHorizontalAngle());
			SegmentLink intendedOutLink = currentSegment.getOutOption(drivingDir);
			Segment targetSegment = intendedOutLink.getSegmentAfter();
			SubLine detectionLine = getDetectionLine(carPose, measurementLine);
			List<Intersection> intersectionList = getLineIntersections(map, detectionLine);
			List<Intersection> intersections = new ArrayList<>();

			if (intersectionList.isEmpty()) {
				return getLinePositions(carPose3D, measurementLine, map);
			}
			Iterator<Intersection> iterator = intersectionList.iterator();

			while (iterator.hasNext()) {
				Intersection intersection = iterator.next();
				if (targetSegment.contains(
							new Vector3D(intersection.getPoint().getX(), intersection.getPoint().getY(), 0.)) ||
						currentSegment.contains(
								new Vector3D(intersection.getPoint().getX(), intersection.getPoint().getY(), 0.))) {
					intersections.add(intersection);
				} else if (((targetSegment.isMergingOut() && targetSegment.getType() != MERGING_START) ||
								   (currentSegment.isMergingOut() && currentSegment.getType() != MERGING_START)) &&
						   intersection.getType() == LineType.OUTER) {
					intersections.add(intersection);
				}
			}

			int countMidlines = countMidlineIndex(intersections);
			if (countMidlines > 1) {
				// two middlelines = invalid
				return getLinePositions(carPose3D, measurementLine, map);
			}

			int mostMiddleIndex = getMidlineIndex(intersections, carPose3D.getPosition());

			if (mostMiddleIndex >= 0) {
				// we have a middle line, so take right and left with respect to middle
				result[1] = carPose.applyInverseTo(intersections.get(mostMiddleIndex).getPoint());
				if (mostMiddleIndex > 0) {
					result[0] = carPose.applyInverseTo(intersections.get(mostMiddleIndex - 1).getPoint());
				}
				if (mostMiddleIndex < intersections.size() - 1) {
					result[2] = carPose.applyInverseTo(intersections.get(mostMiddleIndex + 1).getPoint());
				}
			} else if (targetSegment.isMergingOut() && intersections.size() == 2) {
				// we did not detect the middle line on a merging out lane
				result[0] = carPose.applyInverseTo(intersections.get(0).getPoint());
				result[1] = carPose.applyInverseTo(intersections.get(1).getPoint());
			}
		} catch (NullPointerException ex) {
			return getLinePositions(carPose3D, measurementLine, map);
		}
		return result;
	}

	public static List<Intersection> getLineIntersections(StreetMap map, SubLine line)
	{
		Vector2D start = line.getSegments().get(0).getStart();

		List<Intersection> intersections = new ArrayList<>();
		for (Segment segment : map) {
			if (segment.isParkingSpace()) {
				// for now we ignore parking spaces
				continue;
			}
			for (SegmentUtils.StreetLine streetLine : SegmentUtils.getLines(segment)) {
				Vector2D intersection = line.intersection(streetLine.line, true);
				if (intersection == null) {
					continue;
				}
				intersections.add(
						new Intersection(intersection, streetLine.type, start.distance(intersection), segment));
			}
		}
		if (intersections.isEmpty()) {
			return intersections;
		}

		Collections.sort(intersections);
		Iterator<Intersection> iterator = intersections.iterator();
		Intersection previous = null;
		// remove duplicates from lines being piecewise segments
		while (iterator.hasNext()) {
			Intersection current = iterator.next();
			if (previous != null && current.point.distance(previous.point) < 0.01) {
				iterator.remove();
			} else {
				previous = current;
			}
		}
		return intersections;
	}

	private static int countMidlineIndex(List<Intersection> intersections)
	{
		Iterator<Intersection> iterator = intersections.iterator();
		int result = 0;
		while (iterator.hasNext()) {
			Intersection current = iterator.next();
			if (current.getType() == LineType.MIDDLE) {
				result++;
			}
		}
		return result;
	}

	public static SubLine getDetectionLine(IPose2D carPose, SubLine measurementLine)
	{
		Vector2D start = carPose.applyTo(measurementLine.getSegments().get(0).getStart());
		Vector2D end = carPose.applyTo(measurementLine.getSegments().get(0).getEnd());
		// Vector2D mid = start.add(end.subtract(start).scalarMultiply(0.5));
		return new SubLine(start, end, 0.0001);
	}

	public static int getMidlineIndex(List<Intersection> intersections, Vector3D carPose)
	{
		Iterator<Intersection> iterator = intersections.iterator();
		Intersection mostMiddle = null;
		int mostMiddleIndex = -1;
		int i = 0;
		while (iterator.hasNext()) {
			Intersection current = iterator.next();
			if (current.getType() == LineType.MIDDLE) {
				if (mostMiddle == null || current.getSegment().isConnectedTo(carPose)) {
					mostMiddle = current;
					mostMiddleIndex = i;
				}
			}
			i++;
		}
		return mostMiddleIndex;
	}

	private static Vector2D[][] createMiddleLines(double radius, int segmentPerLine, int lineCount, double centerX,
			double centerY, double startAngle, double lineLength, double lineSpace, double startingSpace)
	{
		Vector2D[][] middelLines = new Vector2D[lineCount][segmentPerLine];

		for (int i = 0; i < lineCount; i++) {
			middelLines[i] = getCirclePoints(radius, segmentPerLine, centerX, centerY,
					Angle.deg(startAngle + startingSpace + i * lineLength + i * lineSpace),
					Angle.deg(startAngle + startingSpace + (i + 1) * lineLength + i * lineSpace));
		}

		return middelLines;
	}

	public enum LineType { MIDDLE, OUTER, PARKING, STOP }

	public static class StreetLine
	{
		public final SubLine line;

		public final LineType type;

		public StreetLine(SubLine line, LineType type)
		{
			this.line = line;
			this.type = type;
		}
	}
}
