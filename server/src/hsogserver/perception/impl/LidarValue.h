#pragma once

namespace taco
{
class LidarValue
{
  public:
	LidarValue(const int &angle, const double &distance)
	{
		_angle = angle;
		_distance = distance;
	}

	virtual const int &getAngle() const
	{
		return _angle;
	}

	virtual const double &getDistance() const
	{
		return _distance;
	}

  protected:
	int _angle;
	double _distance;
};
}
