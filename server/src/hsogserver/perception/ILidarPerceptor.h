#pragma once

#include "IPerceptor.h"
#include <perception/impl/LidarValue.h>

namespace taco
{
/**
 * Interface for a lidar perceptor.
 *
 * \author Rico Schillings
 */
class ILidarPerceptor : public virtual IPerceptor
{
  public:
	typedef boost::shared_ptr<ILidarPerceptor> Ptr;
	typedef boost::shared_ptr<const ILidarPerceptor> ConstPtr;

	virtual ~ILidarPerceptor(){};

	virtual const std::vector<LidarValue> &getLidarValues() const = 0;
};
}
