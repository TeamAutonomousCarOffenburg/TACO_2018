#pragma once

#include <perception/IPerception.h>

#include "LidarValue.h"
#include "Perceptor.h"
#include "perception/ILidarPerceptor.h"

namespace taco
{
/**
 * The Lidarperceptor class represents lidar scan values perception.
 *
 * \author Rico Schillings
 */
class LidarPerceptor : public Perceptor, public virtual ILidarPerceptor
{
  public:
	typedef boost::shared_ptr<LidarPerceptor> Ptr;
	typedef boost::shared_ptr<const LidarPerceptor> ConstPtr;

	LidarPerceptor(const std::string &name, const long &time, const std::vector<LidarValue> &lidarValues)
		: Perceptor(name, time), _lidarValues(lidarValues){

								 };
	virtual ~LidarPerceptor(){};

	virtual const std::vector<LidarValue> &getLidarValues() const
	{
		return _lidarValues;
	}

  protected:
	std::vector<LidarValue> _lidarValues;
};
}
