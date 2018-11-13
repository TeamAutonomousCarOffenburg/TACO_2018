#pragma once

#include "LaneCutterResult.h"
#include "LaneDetectionConfiguration.h"
#include "ScanLine.h"
#include <opencv2/core/core.hpp>

namespace taco
{
class LaneCutter
{
  public:
	typedef boost::shared_ptr<LaneCutter> Ptr;
	typedef boost::shared_ptr<const LaneCutter> ConstPtr;

	LaneCutter(LaneDetectionConfiguration cfg);
	virtual ~LaneCutter();

	LaneCutterResult detect(cv::Mat &frame);

  private:
	LaneDetectionConfiguration _cfg;
};
}
