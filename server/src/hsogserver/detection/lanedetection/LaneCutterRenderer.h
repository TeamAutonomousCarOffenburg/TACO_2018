#pragma once

#include "LaneCutterResult.h"
#include "Line.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>

namespace taco
{
class LaneCutterRenderer
{
  public:
	static void drawLine(cv::Mat &image, Line line2, cv::Scalar &color);
	static cv::Mat draw(int width, int height, const cv::Mat &image, LaneCutterResult data);
};
}