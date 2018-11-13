#include "../EdgeDetector.h"
#include "../LaneCutter.h"
#include "../LaneCutterRenderer.h"
#include "../WideAngleCameraConfig.h"
#include "TestUtils.h"
#include "gtest/gtest.h"
#include <dirent.h>
#include <opencv2/highgui/highgui.hpp>

using namespace taco;
using namespace std;
using namespace cv;
using namespace Eigen;

namespace
{
class LaneCutterTest : public ::testing::Test
{
  protected:
	LaneCutterTest(){};
	virtual ~LaneCutterTest(){};

	virtual void SetUp(){};
	virtual void TearDown(){};

	// LaneDetectionConfiguration cfg;
	WideAngleCameraConfig cfg;

	void drawTestee(const Mat &image, const Mat &edges, LaneCutterResult data)
	{
		Mat temp_frame;
		image.copyTo(temp_frame);
		temp_frame = LaneCutterRenderer::draw(cfg.width, cfg.height, image, data);

		namedWindow("Display window", WINDOW_AUTOSIZE); // Create a window for display.
		imshow("Display window", temp_frame);			// Show our image inside it.

		namedWindow("Edge window", WINDOW_AUTOSIZE); // Create a window for display.
		imshow("Edge window", edges);				 // Show our image inside it.

		waitKey(0);
	}
};

TEST_F(LaneCutterTest, testWholeDirectory)
{
	DIR *dp;
	struct dirent **dirp;
	string dir = "doesnotexistforskippingthis";
	dir = "/media/kdorer/Data/Projekte/Audi/LaneDetection/2018-09-13_outerlane/";
	dir = "/media/kdorer/Data/Projekte/Audi/LaneDetection/";

	if ((dp = opendir(dir.c_str())) == NULL) {
		return;
	}

	int fileCount = scandir(dir.c_str(), &dirp, filter, alphasort);

	LaneCutter testee(cfg);

	for (int i = 0; i < fileCount; i++) {
		string file = dir + string(dirp[i]->d_name);
		std::cout << file << std::endl;
		Mat image = getMatFromImage(file);
		if (!image.data) {
			closedir(dp);
			return;
		}

		Mat edges = EdgeDetector::detectEdges(image, cfg.width, cfg.height * 0.5);

		LaneCutterResult result = testee.detect(edges);

		drawTestee(image, edges, result);
	}
	free(dirp);
	closedir(dp);
}
}
