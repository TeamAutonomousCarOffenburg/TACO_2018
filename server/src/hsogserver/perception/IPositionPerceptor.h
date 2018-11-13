#pragma once

#include "IPerceptor.h"

namespace taco
{
class IPositionPerceptor : public virtual IPerceptor
{
  public:
	typedef boost::shared_ptr<IPositionPerceptor> Ptr;
	typedef boost::shared_ptr<const IPositionPerceptor> ConstPtr;

	virtual ~IPositionPerceptor(){};

	virtual const float getX() const = 0;
	virtual const float getY() const = 0;
	virtual const float getAngle() const = 0;
};
}