#include "LaneCutter.h"

using namespace taco;
using namespace cv;
using namespace Eigen;
using namespace std;

LaneCutter::LaneCutter(LaneDetectionConfiguration cfg) : _cfg(cfg)
{
}

LaneCutter::~LaneCutter()
{
}

LaneCutterResult LaneCutter::detect(cv::Mat &frame)
{
	// int minX = static_cast<int>(_cfg.width * 0.1);
	// int maxX = static_cast<int>(_cfg.width * 0.9);
	int minX = 1;
	int maxX = static_cast<int>(_cfg.width - 5);

	int y = 118;
	int maxDoubleLinePixelDistance = 35;
	int minPixelBetweenLines = 40;

	LaneCutterResult result(minX, maxX);
	y = 78;
	for (int i = 0; i < 5; i++) {
		std::vector<int> xValues;
		// first value is y coordinate
		xValues.push_back(y);
		ScanLine scan(minX, y, maxX, y);
		int doubleLineX = scan.findFirstDoubleLinePixel(frame, maxDoubleLinePixelDistance).x;
		while (doubleLineX > 0) {
			xValues.push_back(doubleLineX);
			doubleLineX = scan.findNextDoubleLinePixel(frame, minPixelBetweenLines, maxDoubleLinePixelDistance).x;
		}
		result._detections.push_back(xValues);
		y += 20;
	}
	return result;
}
