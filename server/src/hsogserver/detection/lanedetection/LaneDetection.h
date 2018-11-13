#pragma once

#include <eigen3/Eigen/Dense>
#include <opencv2/core/core.hpp>

#include "../ILaneDetection.h"
#include "LaneAssist.h"
#include "ScanLine.h"

#include "utils/concurrency/Worker.h"

namespace taco
{
class LaneDetection : public virtual taco::ILaneDetection
{
  public:
	LaneDetection(LaneDetectionConfiguration cfg);
	~LaneDetection();

	taco::LaneAssist::Ptr detectLanes(cv::Mat &frame, const Eigen::Vector3d &upVector);

  private:
	LaneDetectionConfiguration _cfg;
	taco::LaneAssist detector;
};
}