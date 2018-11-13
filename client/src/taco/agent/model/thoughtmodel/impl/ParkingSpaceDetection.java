package taco.agent.model.thoughtmodel.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Polygon;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.model.agentmodel.ILidar;
import taco.agent.model.agentmodel.IUltrasonic;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.UltrasonicPosition;
import taco.agent.model.thoughtmodel.IAudiCupThoughtModel;
import taco.agent.model.worldmodel.IAudiCupWorldModel;
import taco.agent.model.worldmodel.ParkingSpaceState;
import taco.agent.model.worldmodel.impl.ParkingSpace;

public class ParkingSpaceDetection
{
	private final IAudiCupThoughtModel thoughtModel;

	private List<Vector3D> positions = new ArrayList<>();

	private boolean measurementRunning = false;

	private ParkingSpace previousClosestSpace;

	public ParkingSpaceDetection(IAudiCupThoughtModel thoughtModel)
	{
		this.thoughtModel = thoughtModel;
	}

	public void update()
	{
		IAudiCupWorldModel worldModel = thoughtModel.getWorldModel();

		IUltrasonic usSideRight = thoughtModel.getAgentModel().getUltrasonic(UltrasonicPosition.SIDE_RIGHT);

		IUltrasonic usFrontRight = null;
		if (thoughtModel.getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_RIGHT) != null) {
			usFrontRight = thoughtModel.getAgentModel().getUltrasonic(UltrasonicPosition.FRONT_RIGHT);
		}

		ILidar lidarFront = null;
		if (thoughtModel.getAgentModel().getLidar(LidarPosition.LIDAR_FRONT) != null) {
			lidarFront = thoughtModel.getAgentModel().getLidar(LidarPosition.LIDAR_FRONT);
		}

		IPose3D usPose = usSideRight.getConfig().getPose();
		IPose3D carPose = worldModel.getThisCar().getPose();
		IPose3D globalUSPose = carPose.applyTo(new Pose3D(usPose.getX(), usPose.getY()));

		ParkingSpace closestSpace = worldModel.getEnvironmentManager().getClosestParkingSpace(globalUSPose);
		if (closestSpace == null) {
			return;
		}

		if (previousClosestSpace != null && previousClosestSpace.getID() != closestSpace.getID()) {
			measurementRunning = false;
			positions.clear();
		}

		Polygon measurementArea = closestSpace.getMeasurementArea();
		Vector2D usPos2D = new Vector2D(globalUSPose.getX(), globalUSPose.getY());

		if (measurementArea.contains(usPos2D)) {
			positions.add(carPose.applyTo(usSideRight.getObjectPosition()));

			if (usFrontRight != null) {
				positions.add(carPose.applyTo(usFrontRight.getObjectPosition()));
			}
			if (lidarFront != null) {
				positions.add(carPose.applyTo(lidarFront.getObjectPosition(-45)));
			}

			measurementRunning = true;
		} else if (measurementRunning) {
			measurementRunning = false;
			finishMeasurement(closestSpace);
		}

		thoughtModel.getDrawings().draw("measurementPoints", Color.WHITE, positions.toArray(new Vector3D[0]));
		thoughtModel.getDrawings().draw("measurementArea", new Color(240, 255, 0, 128), measurementArea);
		previousClosestSpace = closestSpace;
	}

	private void finishMeasurement(ParkingSpace space)
	{
		IAudiCupWorldModel worldModel = thoughtModel.getWorldModel();

		if (positions.size() >= 20) {
			boolean occupied = positions.stream().anyMatch(position -> space.getArea().contains(position));
			ParkingSpaceState newState = occupied ? ParkingSpaceState.OCCUPIED : ParkingSpaceState.FREE;
			worldModel.getEnvironmentManager().updateParkingSpace(space.getID(), newState, worldModel.getGlobalTime());
		}

		positions.clear();
	}
}
