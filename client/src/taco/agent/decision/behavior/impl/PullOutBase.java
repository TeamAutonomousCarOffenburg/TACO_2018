package taco.agent.decision.behavior.impl;

import hso.autonomy.agent.decision.behavior.BehaviorMap;
import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Pose3D;
import taco.agent.decision.behavior.IBehaviorConstants;
import taco.agent.decision.behavior.base.AudiCupBehavior;
import taco.agent.model.agentmodel.IAudiCupAgentModel;
import taco.agent.model.agentmodel.IAudiCupMotor;
import taco.agent.model.agentmodel.impl.enums.LightName;
import taco.agent.model.worldmodel.ParkingSpaceState;
import taco.agent.model.worldmodel.impl.ParkingSpace;

public abstract class PullOutBase extends AudiCupBehavior
{
	protected enum Phase { STARTING, FORWARD, TURN, FINISHED }

	protected Phase phase;

	protected IPose3D startPose;

	protected ParkingSpace closestParkingSpace;

	protected final DriveToPose driveToPose;

	protected IPose3D targetForwardPose;

	protected IPose3D targetTurnPose;

	public PullOutBase(String name, IThoughtModel thoughtModel, BehaviorMap behaviors)
	{
		super(name, thoughtModel);
		this.driveToPose = (DriveToPose) behaviors.get(IBehaviorConstants.DRIVE_TO_POSE);
	}

	@Override
	public boolean isFinished()
	{
		return phase == Phase.FINISHED;
	}

	@Override
	public void init()
	{
		super.init();
		phase = Phase.STARTING;
		startPose = null;
		closestParkingSpace = null;
	}

	@Override
	public void perform()
	{
		IPose3D currentCarPose = getWorldModel().getThisCar().getPose();
		IAudiCupAgentModel agentModel = getAgentModel();

		switch (phase) {
		case STARTING:
			starting(currentCarPose, agentModel);
			closestParkingSpace = getWorldModel().getEnvironmentManager().getClosestParkingSpace(currentCarPose);
			break;

		case FORWARD:
			forward(currentCarPose, agentModel, 0.25);
			break;

		case TURN:
			turn(currentCarPose, agentModel);

			if (closestParkingSpace != null) {
				getWorldModel().getEnvironmentManager().updateParkingSpace(
						closestParkingSpace.getID(), ParkingSpaceState.FREE, getWorldModel().getGlobalTime());
			}
			break;
		}
	}

	protected void starting(IPose3D currentCarPose, IAudiCupAgentModel agentModel)
	{
		startPose = currentCarPose;
		agentModel.getLight(LightName.WARN).turnOff();
		phase = Phase.FORWARD;
	}

	protected void forward(IPose3D currentCarPose, IAudiCupAgentModel agentModel, double length)
	{
		if (targetForwardPose == null) {
			targetForwardPose = startPose.applyTo(new Pose3D(length, 0.0));

			drive(targetForwardPose);
		}
		if (currentCarPose.getDistanceTo(targetForwardPose) < 0.1) {
			targetForwardPose = null;
			phase = Phase.TURN;
		}
	}

	protected abstract void turn(IPose3D currentCarPose, IAudiCupAgentModel agentModel);

	protected void drive(IPose3D targetPose)
	{
		driveToPose.setTargetPose(targetPose, targetPose, IAudiCupMotor.DEFAULT_SPEED);
		driveToPose.perform();
	}
}
