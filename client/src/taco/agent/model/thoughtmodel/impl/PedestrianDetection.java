package taco.agent.model.thoughtmodel.impl;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;
import hso.autonomy.agent.model.thoughtmodel.impl.ConsecutiveTruthValue;
import hso.autonomy.util.geometry.Geometry;
import hso.autonomy.util.geometry.IPose3D;
import hso.autonomy.util.geometry.Polygon;
import hso.autonomy.util.geometry.VectorUtils;
import taco.agent.model.thoughtmodel.IAudiCupThoughtModel;
import taco.agent.model.worldmodel.IAudiCupWorldModel;

public class PedestrianDetection extends ConsecutiveTruthValue
{
	private double distance;

	public PedestrianDetection()
	{
		// we want 3 consecutive false observations before we believe
		super(1, 3);
		distance = 0.6;
	}

	@Override
	public void update(IThoughtModel thoughtModel)
	{
		IAudiCupThoughtModel specificThoughtModel = (IAudiCupThoughtModel) thoughtModel;
		IAudiCupWorldModel worldModel = specificThoughtModel.getWorldModel();
		double validityTime = 0;
		if (isValid()) {
			if (distance < 0.6) {
				distance -= 0.01;
			} else {
				validityTime = getValidityTime(worldModel.getGlobalTime());
				distance = 0.6 - Geometry.getLinearFuzzyValue(5, 10, true, validityTime) * 0.6;
			}
		} else {
			if (getInValidityTime(worldModel.getGlobalTime()) > 3) {
				distance = 0.6;
			}
		}
		boolean result = worldModel.getRecognizedObjects()
								 .stream()
								 .filter(object -> object.getType().isPedestrian())
								 .anyMatch(pedestrian -> {
									 Polygon polygon = new Polygon(pedestrian.getArea());
									 IPose3D carPose = worldModel.getThisCar().getPose();
									 return specificThoughtModel.getDriveWay().isPositionInWay(
											 carPose.applyInverseTo(VectorUtils.to3D(polygon.getCentroid())), distance);
								 });

		setValidity(result, worldModel.getGlobalTime());
	}
}
