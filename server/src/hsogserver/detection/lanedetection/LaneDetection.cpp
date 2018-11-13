#include "LaneDetection.h"
#include <opencv2/imgproc.hpp>

#include "EdgeDetector.h"
#include "LaneDetectionConfiguration.h"
#include "PixelToCamera.h"

using namespace taco;

LaneDetection::LaneDetection(LaneDetectionConfiguration cfg) : _cfg(cfg), detector(cfg)
{
}

LaneDetection::~LaneDetection()
{
}

/**
 * The caller has to check if this is a new and non empty frame (frame.empty())
 */
taco::LaneAssist::Ptr LaneDetection::detectLanes(cv::Mat &frame, const Eigen::Vector3d &upVector)
{
	// image conversions and edge detection
	cv::Mat edges = EdgeDetector::detectEdges(frame, _cfg.width, _cfg.height * 0.5);
	PixelToCamera::ConstPtr pixelToCam = boost::make_shared<PixelToCamera>(
			upVector, _cfg.focalVertical, _cfg.focalHorizontal, _cfg.focalPointX, _cfg.focalPointY - _cfg.height * 0.5);
	// TODO: integrate pixelToCamera class into LaneAssist
	detector.setPixelToCamera(pixelToCam);
	detector.detectLaneMiddle(edges);

	return boost::make_shared<LaneAssist>(detector);
}
