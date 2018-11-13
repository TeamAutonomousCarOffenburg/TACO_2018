#pragma once

#include <string>

#include "Perceptor.h"
#include "perception/IPositionPerceptor.h"

namespace taco
{
class PositionPerceptor : public Perceptor, public virtual IPositionPerceptor
{
  public:
	typedef boost::shared_ptr<PositionPerceptor> Ptr;
	typedef boost::shared_ptr<const PositionPerceptor> ConstPtr;

	PositionPerceptor(
			const std::string &name, const long &time, const float &posX, const float &posY, const float &posAngle)
		: Perceptor(name, time)
	{
		_posX = posX;
		_posY = posY;
		_posAngle = posAngle;
	};
	virtual ~PositionPerceptor(){};

	virtual const float getX() const
	{
		return _posX;
	}
	virtual const float getY() const
	{
		return _posY;
	}
	virtual const float getAngle() const
	{
		return _posAngle;
	}

  protected:
	float _posX;
	float _posY;
	float _posAngle;
};
}