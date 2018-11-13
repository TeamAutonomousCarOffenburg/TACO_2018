package taco.agent.model.worldmodel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import hso.autonomy.agent.communication.perception.IPerception;
import hso.autonomy.agent.model.agentmodel.IAgentModel;
import hso.autonomy.agent.model.worldmodel.IVisibleObject;
import hso.autonomy.agent.model.worldmodel.impl.WorldModel;
import hso.autonomy.agent.model.worldmodel.localizer.ILocalizer;
import hso.autonomy.util.geometry.Angle;
import hso.autonomy.util.geometry.Area2D;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import hso.autonomy.util.geometry.VectorUtils;
import taco.agent.agentruntime.scenarios.IScenario;
import taco.agent.communication.action.CarState;
import taco.agent.communication.perception.IAudiCupPerception;
import taco.agent.communication.perception.IEnvironmentConfigPerceptor;
import taco.agent.communication.perception.IJuryPerceptor;
import taco.agent.communication.perception.IManeuverListPerceptor;
import taco.agent.communication.perception.IPositionPerceptor;
import taco.agent.communication.perception.ISignPerceptor;
import taco.agent.communication.perception.IVisionPerceptor;
import taco.agent.communication.perception.JuryAction;
import taco.agent.communication.perception.PerceptorName;
import taco.agent.communication.perception.RecognizedObject;
import taco.agent.communication.perception.RecognizedObjectType;
import taco.agent.communication.perception.impl.LaneMiddlePerceptor;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.ICameraSensor;
import taco.agent.model.agentmodel.IDriveStatus;
import taco.agent.model.agentmodel.ILidar;
import taco.agent.model.agentmodel.ITachometer;
import taco.agent.model.agentmodel.impl.enums.LidarPosition;
import taco.agent.model.agentmodel.impl.enums.TachometerPosition;
import taco.agent.model.worldmodel.IAudiCupWorldModel;
import taco.agent.model.worldmodel.ICrossroadSensor;
import taco.agent.model.worldmodel.ILaneMiddleSensor;
import taco.agent.model.worldmodel.IThisCar;
import taco.agent.model.worldmodel.crossroaddetection.CrossroadSensor;
import taco.agent.model.worldmodel.driveinstruction.DriveInstructionManager;
import taco.agent.model.worldmodel.driveinstruction.JuryActionManager;
import taco.agent.model.worldmodel.lanedetection.LaneMiddleSensor;
import taco.agent.model.worldmodel.odometry.GyroOdometry;
import taco.agent.model.worldmodel.signdetection.RoadSign;
import taco.agent.model.worldmodel.signdetection.RoadSignUtils;
import taco.agent.model.worldmodel.street.Direction;
import taco.agent.model.worldmodel.street.RuntimeSegment;
import taco.agent.model.worldmodel.street.Segment;
import taco.agent.model.worldmodel.street.SegmentLink;
import taco.agent.model.worldmodel.street.SegmentType;
import taco.agent.model.worldmodel.street.StreetMap;
import taco.util.SignType;

public class AudiCupWorldModel extends WorldModel implements IAudiCupWorldModel
{
	private StreetMap map;

	private DriveInstructionManager driveInstructionManager;

	private ILaneMiddleSensor laneMiddleSensor;

	private ICrossroadSensor crossroadSensor;

	private JuryActionManager juryManager;

	private EnvironmentManager environmentManager;

	private IThisCar thisCar;

	private RuntimeSegment currentSegment;

	private RuntimeSegment previousSegment;

	private GyroOdometry gyroOdometry;

	private boolean initialized = false;

	private boolean hasJuryActionChanged = false;

	private boolean wantExternalLocalization = true;

	public AudiCupWorldModel(IAgentModel agentModel, ILocalizer localizer, IScenario scenario, IPose3D startPose)
	{
		super(agentModel, localizer);

		map = scenario.getStreetMap();
		driveInstructionManager = scenario.createDriveInstructionManager();
		laneMiddleSensor = new LaneMiddleSensor();
		crossroadSensor = new CrossroadSensor();
		juryManager = new JuryActionManager();
		environmentManager = new EnvironmentManager(
				getAgentModel().getParkingSpaceActuator(), getAgentModel().getRoadSignActuator(), map);

		if (startPose == null) {
			startPose = scenario.getStartPose();
		}
		thisCar = new ThisCar(startPose);
		gyroOdometry = new GyroOdometry(getAgentModel().getCarMetaModel().getFrontAxle().getWheelDiameter(), startPose);
		resetCurrentSegment(startPose);
	}

	public void resetCurrentSegment(IPose3D startPose)
	{
		Segment startSegment = map.getCurrentStartSegment(driveInstructionManager.getCurrentSectorIndex());
		currentSegment = new RuntimeSegment(startSegment, startPose.getHorizontalAngle());
	}

	@Override
	public boolean update(IPerception perception)
	{
		hasJuryActionChanged = false;

		super.update(perception);

		IAudiCupPerception audiCupPerception = (IAudiCupPerception) perception;

		// Process maneuver list instructions
		IManeuverListPerceptor maneuverListPerceptor = audiCupPerception.getManeuverListPerceptor();
		if (maneuverListPerceptor != null) {
			driveInstructionManager.setDriveInstructions(maneuverListPerceptor.getManeuver());
			updateDrivePath();
		}

		processPosition(audiCupPerception);

		processVision(audiCupPerception);

		// loads the information from the roadsign.xml into the environment manager and into the map
		//		processEnvironmentConfiguration(audiCupPerception);

		// update roadsigns from adtf sign detection
		//		processSignDetection(audiCupPerception);

		if (!initialized) {
			// we wait for the agent model to get initialized
			if (getAgentModel().isInitialized()) {
				initializeOdometry();
				initialized = true;
			}
			return initialized;
		}

		// get information from middle of lane detection
		processLaneMiddle(audiCupPerception);

		// estimate distance to detected crossroads
		// processCrossroads(audiCupPerception);

		// check where we are on the street
		localize();

		// Process jury module instruction. After localization because we might reset car position
		processJuryInstruction(audiCupPerception);

		// update current segment information
		progressSegment();

		getAgentModel().getCarPositionActuator().setCarPose(thisCar.getPose());

		return true;
	}

	private void processPosition(IAudiCupPerception audiCupPerception)
	{
		IPositionPerceptor positionPerceptor = audiCupPerception.getPositionPerceptor();
		if (positionPerceptor != null) {
			// TODO: check also that our and their coordinate systems match
			Pose3D receivedPose = new Pose3D(
					positionPerceptor.getX(), positionPerceptor.getY(), Angle.rad(positionPerceptor.getAngle()));
			// just for debugging
			thisCar.setAlternativePose(receivedPose);
			//            System.out.println("Received position: " + receivedPose.getPosition() + " Angle: " +
			//            receivedPose.getHorizontalAngle().degrees() + " ownAngle: " +
			//            thisCar.getPose().getHorizontalAngle().degrees());

			if (wantExternalLocalization) {
				// does not work if we do not get position in ready state, so not sure if we need this if
				// if (getAgentModel().getDriveStatus().getStatus() == CarState.READY) {
				reposition(receivedPose);
				resetCurrentSegment(receivedPose);
				// accept external position only once during one try of a sector
				wantExternalLocalization = false;
				System.out.println("Relocalized through external perception to: " + receivedPose);
				// }
			}
		}
	}

	private void processVision(IAudiCupPerception audiCupPerception)
	{
		IVisionPerceptor visionPerceptor = audiCupPerception.getVisionPerceptor();
		if (visionPerceptor != null && getAgentModel().getBaslerCamera() != null) {
			List<RecognizedObject> recognizedObjects = visionPerceptor.getRecognizedObjects();
			if (recognizedObjects.isEmpty()) {
				return;
			}

			obstacles.clear();

			RecognizedObject detectedCar = null;
			RecognizedObject detectedSirene = null;
			for (RecognizedObject recognizedObject : recognizedObjects) {
				Area2D.Float globalArea;

				// this calculation works only for the simulator
				if (recognizedObject.isInCarCoordinates()) {
					Area2D.Int area = recognizedObject.getArea();
					globalArea = new Area2D.Float(area.getMinX() / 100.0, area.getMaxX() / 100.0,
							area.getMinY() / 100.0, area.getMaxY() / 100.0);
				} else {
					// here everything only works for live vision by camera

					// special handling for sirene, cause with raytrace it will never find a position/area
					if (recognizedObject.getType() == RecognizedObjectType.CAR) {
						detectedCar = recognizedObject;
					} else if (recognizedObject.getType() == RecognizedObjectType.SIREN_ON) {
						detectedSirene = recognizedObject;
					}

					// only accept sirene if car also was detected in range
					if (detectedCar != null && detectedSirene != null) {
						double yDistance = Math.abs(detectedCar.getArea().getTopLeft().getY() -
													detectedSirene.getArea().getBottomLeft().getY());

						boolean xValid =
								detectedCar.getArea().getMaxX() > detectedSirene.getArea().getCenter().getX() &&
								detectedCar.getArea().getMinX() > detectedSirene.getArea().getCenter().getX();
						// valid if both boxes are nearlier then 10 pixel
						if (yDistance < 10 && xValid) {
							// ensure we have the sirene at the current iteration..
							if (recognizedObject.getType() == RecognizedObjectType.SIREN_ON) {
								// shift the sirene area to the bottom of the car area
								int yShift = detectedCar.getArea().getMaxY() - recognizedObject.getArea().getHeight();
								Area2D.Int modifiedArea = new Area2D.Int(recognizedObject.getArea().getMinX(),
										recognizedObject.getArea().getMaxX(), yShift, detectedCar.getArea().getMaxY());
								recognizedObject = new RecognizedObject(
										RecognizedObjectType.SIREN_ON, modifiedArea, detectedSirene.getConfidence());
								recognizedObject.setInCarCoordinates(false);
							}
						}
					}

					// TODO: may switch to homography here, depending on area/angle in the image
					Area2D.Float area = getAgentModel().getBaslerCamera().pixelToCar(recognizedObject.getArea());
					if (area == null) {
						continue;
					}

					Vector3D topLeft = thisCar.getPose().applyTo(VectorUtils.to3D(area.getTopLeft()));
					Vector3D bottomRight = thisCar.getPose().applyTo(VectorUtils.to3D(area.getBottomRight()));
					globalArea =
							new Area2D.Float(topLeft.getX(), bottomRight.getX(), topLeft.getY(), bottomRight.getY());
				}

				Obstacle detectedObstacle = new Obstacle(globalTime, recognizedObject.getType(), globalArea);
				// if object has no height, lidar will not detect it (e.g. crossings/lanes)
				if (recognizedObject.getType().isObjectWithHeight() && !recognizedObject.isInCarCoordinates()) {
					detectedObstacle = searchForLidarDetection(detectedObstacle);
				}
				obstacles.add(detectedObstacle);
			}
		} else {
			obstacles = getRecognizedObjects()
								.stream()
								.filter(obstacle -> obstacle.isValid(globalTime))
								.collect(Collectors.toList());
		}
	}

	/**
	 * scans for lidar detection in the area around the vision detection to get a more reliable position.
	 * Works only for live vision, not for the simulator
	 * @param detectedObstacle detected obstacle from vision with raytrace/homography position
	 * @return detectedObstacle with corrected position (if found detection on lidar)
	 */
	private Obstacle searchForLidarDetection(Obstacle detectedObstacle)
	{
		Vector3D globalVisionDetection =
				VectorUtils.average(VectorUtils.to3D(detectedObstacle.getArea().getBottomLeft()),
						VectorUtils.to3D(detectedObstacle.getArea().getBottomRight()));

		// calculate local pos of the vision detection
		Vector3D localDetectedPos = thisCar.getPose().applyInverseTo(globalVisionDetection);

		ILidar lidar = getAgentModel().getLidar(LidarPosition.LIDAR_FRONT);
		double visionDetectionAngle = Angle.rad(localDetectedPos.getAlpha()).degrees();
		// System.out.println("Raytrace Vision Detection at position: " + localDetectedPos + " at angle: " +
		// visionDetectionAngle);
		// search for lidar values in an area +- 20 degree around the vision detection angle
		int searchingArea = 20;
		List<Vector3D> avgLidarPositions = new ArrayList<>();
		for (int i = 0; i < searchingArea * 2; i++) {
			int angle = (int) (visionDetectionAngle + (searchingArea - i));
			Vector3D detectedLidarPos = lidar.getObjectPosition(angle);
			// this may is not very clean, since the localization of vision detection gets very noisy if its further
			// away
			if (detectedLidarPos.distance(localDetectedPos) < 0.5) {
				avgLidarPositions.add(detectedLidarPos);
			}
		}
		if (avgLidarPositions.isEmpty()) {
			return null;
		}
		Vector3D avgPositionLocal = VectorUtils.average(avgLidarPositions);
		// System.out.println("Lidar Detection at position: " + avgPositionLocal + " at angle: " +
		// Angle.rad(avgPositionLocal.getAlpha()).degrees() +  "based on nrOfDetections: " + avgLidarPositions.size());

		Vector3D avgLidarPos = thisCar.getPose().applyTo(avgPositionLocal);
		if (avgLidarPos != null) {
			// TODO: select handling if we have both positions
			detectedObstacle.setPosition(avgLidarPos);
			boolean rotateObject = false;

			// only works for car
			if (detectedObstacle.getType() == RecognizedObjectType.CAR) {
				double measurementRange =
						avgLidarPositions.get(0).distance(avgLidarPositions.get(avgLidarPositions.size() - 1));
				double deviation = Math.abs(measurementRange - getAgentModel().getCarWidth());
				if (deviation < 0.1 || measurementRange < getAgentModel().getCarWidth()) {
					rotateObject = true;
				}
			}
			detectedObstacle.updateArea(rotateObject); // trust lidar more?
		}
		return detectedObstacle;
	}

	/**
	 * Initializes the gyro sensor. During this time the car has to stand still
	 */
	protected void initializeOdometry()
	{
		gyroOdometry.init(getAgentModel().getTachometer(TachometerPosition.LEFT).getTicks(),
				getAgentModel().getTachometer(TachometerPosition.RIGHT).getTicks());
	}

	private void processEnvironmentConfiguration(IAudiCupPerception perception)
	{
		IEnvironmentConfigPerceptor environmentConfigPerceptor = perception.getEnvironmentPerceptor();
		if (environmentConfigPerceptor != null) {
			// TODO: the server should stop sending this when we have received it, cause it's just for initializing
			environmentManager.update(environmentConfigPerceptor, map);
		}
	}

	private void processSignDetection(IAudiCupPerception perception)
	{
		IPose3D cameraPose = getAgentModel().getSignDetectionCamera().getSensorPose();
		IPose3D globalCameraPose = thisCar.getPose().applyTo(cameraPose);

		RoadSign roadSign = environmentManager.updateVisisbleRoadSigns(globalCameraPose, getGlobalTime());
		if (roadSign != null) {
			RoadSignUtils.removeSignFromMap(map, roadSign);
		}

		ISignPerceptor signPerceptor = perception.getSignPerceptor();
		if (signPerceptor != null) {
			for (RoadSign sign : signPerceptor.getSigns()) {
				// filter zero-pos
				if ((sign.getPose().getPosition().distance(Vector3D.ZERO) < 0.1) ||
						(sign.getSignType() == SignType.TESTCOURSE_A9) || sign.getSignType() == SignType.UNKNOWN) {
					continue;
				}

				sign.setPose(globalCameraPose.applyTo(sign.getPose()));
				// don't believe signs that are not in the plausible visible area
				if (!RoadSignUtils.isInVisibleArea(globalCameraPose, sign)) {
					return;
				}

				if (RoadSignUtils.loadReceivedRoadSignIntoMap(
							map, environmentManager.getKnownRoadSigns(), sign, getGlobalTime())) {
					sign.update(true, true, globalTime);
					environmentManager.updateRoadSign(sign);
				}
			}
		}
	}

	private void processLaneMiddle(IAudiCupPerception perception)
	{
		LaneMiddlePerceptor laneResult = perception.getLaneMiddlePerceptor(PerceptorName.LANE_MIDDLE);
		if (laneResult != null) {
			laneMiddleSensor.update(laneResult, getGlobalTime(), thisCar.getPose(),
					getRecognizedObjects(RecognizedObjectType.MIDDLE_LANE), getMap());
		}
	}

	private void processCrossroads(IAudiCupPerception perception)
	{
		IVisionPerceptor visionPerceptor = perception.getVisionPerceptor();
		ICameraSensor baslerCamera = getAgentModel().getBaslerCamera();
		if (visionPerceptor != null && baslerCamera != null) {
			crossroadSensor.update(visionPerceptor, baslerCamera, thisCar.getPose(), getGlobalTime());
		}
	}

	/**
	 * Updates the cars current pose based on odometry and visual information
	 */
	protected void localize()
	{
		// update through odometry
		ITachometer wheelSpeedLeft = getAgentModel().getTachometer(TachometerPosition.LEFT);
		ITachometer wheelSpeedRight = getAgentModel().getTachometer(TachometerPosition.RIGHT);

		IPose3D odometryPose = gyroOdometry.update(wheelSpeedLeft.getTicks(), wheelSpeedLeft.getDirection(),
				wheelSpeedRight.getTicks(), wheelSpeedRight.getDirection(),
				getAgentModel().getImuSensor().getHorizontalAngle());

		// currently we only believe in odometry
		thisCar.setPose(odometryPose);

		// do repositioning in driving direction
		IPose3D newPose =
				laneMiddleSensor.calculateSagittalRepositioning(currentSegment, getGlobalTime(), thisCar.getPose(),
						getRecognizedObjects(RecognizedObjectType.STOP_LINE_AHEAD), getAgentModel().getBaslerCamera());
		if (newPose != null) {
			reposition(newPose);
			// no further repositioning if we did this
			return;
		}

		// do lateral and angle repositioning based on lane middle detection
		newPose =
				laneMiddleSensor.calculateLateralRepositioning(currentSegment, getGlobalTime(), thisCar.getPose(), map);
		if (newPose != null) {
			reposition(newPose);
		}
	}

	public void reposition(IPose3D newPose)
	{
		thisCar.setPose(newPose);
		gyroOdometry.setPose(newPose);
	}

	private void processJuryInstruction(IAudiCupPerception perception)
	{
		IJuryPerceptor juryPerceptor = perception.getJuryPerceptor();
		if (juryPerceptor != null && juryManager.update(juryPerceptor)) {
			IDriveStatus driveStatus = getAgentModel().getDriveStatus();
			if (getJuryAction() == JuryAction.GET_READY) {
				driveStatus.setStatus(CarState.READY);
			}

			if (getJuryAction() == JuryAction.START) {
				driveStatus.setStatus(CarState.RUNNING);
			}

			// If we get STOP from the jury, we should turn into a state where we can be reactivated
			if (getJuryAction() == JuryAction.STOP) {
				driveStatus.setStatus(CarState.STARTUP);
			}

			// update maneuver-ID in driveStatus to response our current state with the current maneuver
			driveStatus.setManeuverId(juryManager.getManeuverId());
			driveInstructionManager.setStartInstructionIndex(juryManager.getManeuverId());
			updateDrivePath();

			int sector = driveInstructionManager.getSectorIndex(juryManager.getManeuverId());
			IPose3D startPose = map.getCurrentStartPose(sector);
			reposition(startPose);
			resetCurrentSegment(startPose);
			wantExternalLocalization = true;
			hasJuryActionChanged = true;
		}
	}

	/**
	 * Updates the current segment information based on the pose of the car.
	 */
	void progressSegment()
	{
		Segment[] probableSegments = null;
		if (previousSegment != null) {
			probableSegments =
					new Segment[] {previousSegment.getSegment(), previousSegment.getIntendedOption().getSegmentAfter()};
		}
		Segment whereWeAreNow = map.getSegmentContaining(thisCar.getPose().getPosition(), probableSegments);

		if (whereWeAreNow == null && probableSegments != null) {
			// it is none of the probable segments, so try to find any segment
			whereWeAreNow = map.getSegmentContaining(thisCar.getPose().getPosition(), null);
		}

		if (whereWeAreNow == null) {
			// we are not in a segment of our map
			return;
		}

		if (whereWeAreNow.getID() != currentSegment.getID()) {
			// we entered a new segment. Check if we are in some mm
			double delta = 0.005;
			Segment slightlyBehindUs = map.getSegmentContaining(
					thisCar.getPose().applyTo(new Pose3D(-delta, 0)).getPosition(), probableSegments);

			if (slightlyBehindUs != null && slightlyBehindUs.getID() == whereWeAreNow.getID()) {
				previousSegment = currentSegment;
				currentSegment.switchToSegment(whereWeAreNow, thisCar.getPose().getHorizontalAngle());
				// do lateral repositioning based on lane middle detection
				IPose3D newPose = laneMiddleSensor.calculateLateralRepositioningOnSegmentChange(
						previousSegment, currentSegment, thisCar.getPose(), delta);
				if (newPose != null) {
					reposition(newPose);
				}
			}
		}
	}

	@Override
	public IThisCar getThisCar()
	{
		return thisCar;
	}

	@Override
	protected IAudiCupAgentModel getAgentModel()
	{
		return (IAudiCupAgentModel) super.getAgentModel();
	}

	@Override
	public ILaneMiddleSensor getLaneMiddleSensor()
	{
		return laneMiddleSensor;
	}

	@Override
	public StreetMap getMap()
	{
		return map;
	}

	@Override
	public RuntimeSegment getCurrentSegment()
	{
		return currentSegment;
	}

	@Override
	public GyroOdometry getGyroOdometry()
	{
		return gyroOdometry;
	}

	@Override
	public JuryAction getJuryAction()
	{
		return juryManager.getAction();
	}

	@Override
	public JuryActionManager getJuryActionManager()
	{
		return juryManager;
	}

	@Override
	public boolean isInitialized()
	{
		return initialized;
	}

	@Override
	public DriveInstructionManager getDriveInstructionManager()
	{
		return driveInstructionManager;
	}

	public void setDriveInstructionManager(DriveInstructionManager driveInstructionManager)
	{
		this.driveInstructionManager = driveInstructionManager;
	}

	@Override
	public void updateDrivePath()
	{
		getThisCar().setPath(WaypointExtractor.extractPath(driveInstructionManager, currentSegment));
	}

	@Override
	public List<Obstacle> getRecognizedObjects()
	{
		return obstacles.stream()
				.filter(o -> o instanceof Obstacle)
				.map(o -> (Obstacle) o)
				.collect(Collectors.toList());
	}

	@Override
	public List<Obstacle> getRecognizedObjects(RecognizedObjectType type)
	{
		return getRecognizedObjects().stream().filter(o -> o.getType() == type).collect(Collectors.toList());
	}

	@Override
	public EnvironmentManager getEnvironmentManager()
	{
		return environmentManager;
	}

	@Override
	public boolean hasJuryActionChanged()
	{
		return hasJuryActionChanged;
	}

	public void setObstacles(List<IVisibleObject> obstacles)
	{
		this.obstacles = obstacles;
	}

	@Override
	public boolean isStraightStreet(int elements)
	{
		Segment segment = getCurrentSegment().getSegment();

		int i = 0;
		while (i <= elements) {
			if (segment == null || segment.getType() != SegmentType.STRAIGHT) {
				return false;
			}

			segment = getNextSegment(segment);
			i++;
		}

		return true;
	}

	@Override
	public boolean isCloseToCrosswalk()
	{
		Segment currentSegment = getCurrentSegment().getSegment();
		Segment nextSegment = getCurrentSegment().getIntendedOption().getSegmentAfter();

		if (nextSegment == null) {
			return false;
		}

		//		if (currentSegment.getType() == SegmentType.STRAIGHT_WITH_CROSSWALK ||
		//				nextSegment.getType() == SegmentType.STRAIGHT_WITH_CROSSWALK) {
		//			return true;
		//		}

		if (nextSegment.getType() == SegmentType.STRAIGHT_WITH_CROSSWALK) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isCloseToCrossing()
	{
		Segment nextSegment = getCurrentSegment().getIntendedOption().getSegmentAfter();

		if (nextSegment == null) {
			return false;
		}

		if (isCrossing(nextSegment)) {
			return true;
		}

		SegmentLink nextOptions = nextSegment.getSameLaneOutOption(getCurrentDirection());

		if (nextOptions != null && nextOptions.hasStopLine()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isCloseToCurve()
	{
		Segment currentSegment = getCurrentSegment().getSegment();
		Segment nextSegment = getCurrentSegment().getIntendedOption().getSegmentAfter();
		Segment afterNextSegment = getNextSegment(nextSegment);

		if (nextSegment == null || afterNextSegment == null) {
			return false;
		}

		if (isCurve(currentSegment) || isCurve(nextSegment) || isCurve(afterNextSegment)) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMergingStart()
	{
		SegmentType currentSegmentType = getCurrentSegment().getSegment().getType();

		if (currentSegmentType == SegmentType.MERGING_START) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMergingMiddle()
	{
		SegmentType currentSegmentType = getCurrentSegment().getSegment().getType();

		if (currentSegmentType == SegmentType.MERGING_MIDDLE) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMergingEnd()
	{
		SegmentType currentSegmentType = getCurrentSegment().getSegment().getType();

		if (currentSegmentType == SegmentType.MERGING_END) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMergingOut()
	{
		return (isMergingStart() || isMergingMiddle() || isMergingEnd());
	}

	@Override
	public boolean isMergingIn()
	{
		return getCurrentSegment().getSegment().isMergingIn();
	}

	private Segment getNextSegment(Segment current)
	{
		RuntimeSegment runtimeSegment = new RuntimeSegment(current, getThisCar().getPose().getHorizontalAngle());
		return runtimeSegment.getIntendedOption().getSegmentAfter();
	}

	private boolean isCrossing(Segment current)
	{
		return (current.getType() == SegmentType.X_CROSSING || current.getType() == SegmentType.T_CROSSING);
	}

	private boolean isCurve(Segment current)
	{
		return (current.getType() == SegmentType.CURVE_SMALL || current.getType() == SegmentType.CURVE_BIG ||
				current.getType() == SegmentType.S_CURVE_BOTTOM || current.getType() == SegmentType.S_CURVE_TOP);
	}

	private Direction getCurrentDirection()
	{
		Angle angle = getThisCar().getPose().getHorizontalAngle();
		return Direction.getDirection(angle);
	}
}
