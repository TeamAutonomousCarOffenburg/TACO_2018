#pragma once

#include <boost/smart_ptr.hpp>
#include <vector>

namespace taco
{
class LaneCutterResult
{
  public:
	typedef boost::shared_ptr<LaneCutterResult> Ptr;
	typedef boost::shared_ptr<const LaneCutterResult> ConstPtr;

	LaneCutterResult(int minX, int maxX) : _minX(minX), _maxX(maxX){};
	virtual ~LaneCutterResult(){};

	std::vector<std::vector<int>> _detections;
	int _minX;
	int _maxX;
};
}
