#pragma once

#include "IPerceptor.h"
#include <detection/lanedetection/LaneMiddle.h>
#include <opencv/cv.h>

namespace taco
{
/**
 * Interface for lane middle detection perceptor.
 *
 * \author Klaus Dorer
 */
class ILaneMiddlePerceptor : public virtual IPerceptor
{
  public:
	typedef boost::shared_ptr<ILaneMiddlePerceptor> Ptr;
	typedef boost::shared_ptr<const ILaneMiddlePerceptor> ConstPtr;

	virtual ~ILaneMiddlePerceptor(){};

	virtual const LaneMiddle &getLaneMiddle() const = 0;
};
}
