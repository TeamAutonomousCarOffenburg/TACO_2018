package taco.agent.model.thoughtmodel.impl;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.agent.model.thoughtmodel.impl.ConsecutiveTruthValue;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.model.worldmodel.IAudiCupWorldModel;
import taco.agent.model.worldmodel.impl.DrivePath;
import taco.agent.model.worldmodel.impl.DrivePoint;
import taco.agent.model.worldmodel.impl.Obstacle;
import taco.agent.model.worldmodel.street.Direction;
import taco.agent.model.worldmodel.street.RuntimeSegment;
import taco.agent.model.worldmodel.street.Segment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmergencyCarDetection extends ConsecutiveTruthValue
{
	private final int SEGMENTS_TO_CHECK_IN_DRIVEPATH = 3;

	List<Integer> sireneSegemts = new ArrayList<>();

	public EmergencyCarDetection()
	{
		// we want 3 consecutive true/false observations before we believe
		super(3, 30);
		sireneSegemts.add(58);
		sireneSegemts.add(66);
		sireneSegemts.add(67);
		sireneSegemts.add(68);
		sireneSegemts.add(69);
		sireneSegemts.add(70);
		sireneSegemts.add(71);
		sireneSegemts.add(73);
		sireneSegemts.add(74);
	}

	@Override
	public void update(IThoughtModel thoughtModel)
	{
		IAudiCupWorldModel worldModel = (IAudiCupWorldModel) thoughtModel.getWorldModel();

		boolean sirenInRange = false;
		List<Obstacle> collect = worldModel.getRecognizedObjects()
										 .stream()
										 .filter(object -> object.getType() == RecognizedObjectType.CAR)
										 .collect(Collectors.toList());

		if (!collect.isEmpty()) {
			Obstacle obstacle = collect.get(0);
			Segment sirenSegment = worldModel.getMap().getSegmentContaining(obstacle.getPosition());
			if (sirenSegment == null || !sireneSegemts.contains(sirenSegment.getID())) {
				return;
			}

			DrivePath path = worldModel.getThisCar().getPath();
			RuntimeSegment currentSegment = worldModel.getCurrentSegment();
			if (currentSegment != null && currentSegment.getID() == sirenSegment.getID()) {
				sirenInRange = true;
			}
			int segmentsToCheck = 0;
			boolean curDrivePointFound = false;
			for (DrivePoint drivePoint : path.getDrivePath()) {
				if (currentSegment != null &&
						drivePoint.getGoalLink().getSegmentBefore().getID() == currentSegment.getID()) {
					curDrivePointFound = true;
				}

				if (curDrivePointFound) {
					if (segmentsToCheck < SEGMENTS_TO_CHECK_IN_DRIVEPATH) {
						Segment segmentToCheck = drivePoint.getGoalLink().getSegmentAfter();
						if (segmentToCheck != null && segmentToCheck.getID() != sirenSegment.getID()) {
							if (segmentToCheck.isCrossing()) {
								sirenInRange = checkTwoNeighborSegments(segmentToCheck, sirenSegment);
								if (sirenInRange)
									break;
							}
							segmentsToCheck++;
						} else {
							if (segmentToCheck != null)
								sirenInRange = true;
							break;
						}
					}
				}
			}
		}

		setValidity(sirenInRange, worldModel.getGlobalTime());
	}

	private boolean checkTwoNeighborSegments(Segment segmentToCheck, Segment sirenSegment)
	{
		boolean check = false;
		if (segmentToCheck != null && sirenSegment != null) {
			for (Direction dir : Direction.values()) {
				if (segmentToCheck.hasOutOption(dir)) {
					Segment segmentNeighbor = segmentToCheck.getOutOption(dir).getSegmentAfter();
					if (segmentNeighbor != null && segmentNeighbor.getID() == sirenSegment.getID()) {
						check = true;
					}
					for (Direction dir2 : Direction.values()) {
						if (segmentNeighbor != null && segmentNeighbor.hasOutOption(dir2)) {
							Segment neighborOfNeighbor = segmentNeighbor.getOutOption(dir2).getSegmentAfter();
							if (neighborOfNeighbor != null && neighborOfNeighbor.getID() == sirenSegment.getID()) {
								check = true;
							}
						}
					}
				}
			}
		}

		return check;
	}
}
