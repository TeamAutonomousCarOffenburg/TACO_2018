#pragma once

#include "Perceptor.h"
#include "perception/ILaneMiddlePerceptor.h"

#include <string>

namespace taco
{
/**
 * Represents a lane middle detection
 *
 * \author Klaus Dorer
 */
class LaneMiddlePerceptor : public Perceptor, public virtual ILaneMiddlePerceptor
{
  public:
	typedef boost::shared_ptr<LaneMiddlePerceptor> Ptr;
	typedef boost::shared_ptr<const LaneMiddlePerceptor> ConstPtr;

	LaneMiddlePerceptor(const std::string &name, const long &time, LaneMiddle laneMiddle)
		: Perceptor(name, time), _laneMiddle(laneMiddle){};
	virtual ~LaneMiddlePerceptor(){};

	virtual const LaneMiddle &getLaneMiddle() const
	{
		return _laneMiddle;
	};

  protected:
	LaneMiddle _laneMiddle;
};
}
