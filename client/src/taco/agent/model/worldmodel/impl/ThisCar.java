package taco.agent.model.worldmodel.impl;

import hso.autonomy.util.geometry.IPose3D;
import taco.agent.model.worldmodel.IThisCar;

public class ThisCar extends Car implements IThisCar
{
	/** the global pose of this object */
	private IPose3D pose;

	private DrivePath drivePath;

	private IPose3D alternativePose;

	public ThisCar(IPose3D startPose)
	{
		super("TacoCar", 0.02f);
		setPose(startPose);
		setAlternativePose(startPose);
		drivePath = new DrivePath();
	}

	@Override
	public IPose3D getPose()
	{
		return pose;
	}

	@Override
	public void setPose(IPose3D newPose)
	{
		pose = newPose;
		position = newPose.getPosition();
	}

	@Override
	public void setPath(DrivePath path)
	{
		this.drivePath = path;
	}

	@Override
	public DrivePath getPath()
	{
		return drivePath;
	}

	@Override
	public IPose3D getAlternativePose()
	{
		return alternativePose;
	}

	@Override
	public void setAlternativePose(IPose3D newPose)
	{
		alternativePose = newPose;
	}
}
