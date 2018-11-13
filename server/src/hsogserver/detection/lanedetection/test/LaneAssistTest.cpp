#include "../EdgeDetector.h"
#include "../LaneDetection.h"
#include "../LaneDetectionRenderer.h"
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
class LaneAssistTest : public ::testing::Test
{
  protected:
	LaneAssistTest(){};
	virtual ~LaneAssistTest(){};

	virtual void SetUp(){};
	virtual void TearDown(){};

	// LaneDetectionConfiguration cfg;
	WideAngleCameraConfig cfg;

	void drawTestee(const Mat &image, const Mat &edges, LaneAssist testee)
	{
		Mat temp_frame;
		image.copyTo(temp_frame);
		temp_frame = LaneDetectionRenderer::draw(cfg.width, cfg.height, image, testee);

		// initial detection
		// scan = Line(320, 240, 320, 10);
		// drawLine(temp_frame, scan, orange);

		// scan = Line(100, 180, 500, 180);
		// drawLine(temp_frame, scan, orange);

		namedWindow("Display window", WINDOW_AUTOSIZE); // Create a window for display.
		imshow("Display window", temp_frame);			// Show our image inside it.

		namedWindow("Edge window", WINDOW_AUTOSIZE); // Create a window for display.
		imshow("Edge window", edges);				 // Show our image inside it.

		waitKey(0);
	}
};

TEST_F(LaneAssistTest, testLine)
{
	Mat image;
	//      image = getMatFromImage("XCROSSROAD.png");
	//      image = getMatFromImage("TRIGHT.png");
	//      image = getMatFromImage("TLEFT.png");
	//      image = getMatFromImage("SCRIGHT.png");
	//      image = getMatFromImage("BCRIGHT.png");
	//      image = getMatFromImage("SCLEFT.png");
	//      image = getMatFromImage("BCLEFT.png");
	//      image = getMatFromImage("TLEFTRIGHT.png");
	//      image = getMatFromImage("TLRFAR.jpeg");
	//      image = getMatFromImage("STRAIGHT.jpeg");

	if (!image.data) {
		return;
	}
	Mat edges = EdgeDetector::detectEdges(image, cfg.width, cfg.height / 2);
	LaneAssist testee(cfg);
	testee.detectLaneMiddle(edges);
	drawTestee(image, edges, testee);
}

TEST_F(LaneAssistTest, testWholeDirectory)
{
	DIR *dp;
	struct dirent **dirp;
	string dir = "doesnotexistforskippingthis";
	// dir = "./";
	// dir = "/media/kdorer/Data/Programmierung/aadc/AADC/src/aadcUser/src/HSOG_Runtime/eclipse/UnitTests/";
	// dir = "/home/aadc/images/";
	// dir = "/media/kdorer/Data/Projekte/Audi/Bilder/LaneDetection/straight/";
	// dir = "/media/kdorer/Data/Projekte/Audi/Bilder/LaneDetection/turnRight/";
	// dir = "/media/kdorer/Data/Projekte/Audi/Bilder/LaneDetection/curveLeft/";
	dir = "/media/kdorer/Data/Projekte/Audi/LaneDetection/2018-09-13_outerlane/";

	if ((dp = opendir(dir.c_str())) == NULL) {
		return;
	}

	int fileCount = scandir(dir.c_str(), &dirp, filter, alphasort);

	// use this one if you need history over the images
	LaneAssist testee(cfg);

	for (int i = 0; i < fileCount; i++) {
		string file = dir + string(dirp[i]->d_name);
		std::cout << file << std::endl;
		Mat image = getMatFromImage(file);
		if (!image.data) {
			closedir(dp);
			return;
		}

		Mat edges = EdgeDetector::detectEdges(image, cfg.width, cfg.height * 0.5);

		PixelToCamera::ConstPtr pixelToCam = boost::make_shared<PixelToCamera>(Eigen::Vector3d(0, 0, 1),
				cfg.focalVertical, cfg.focalHorizontal, cfg.focalPointX, cfg.focalPointY - cfg.height * 0.5);
		// TODO: integrate pixelToCamera class into LaneAssist
		testee.setPixelToCamera(pixelToCam);
		testee.detectLaneMiddle(edges);

		// use this one to have a fresh detector for each image
		// LaneAssist testee;
		// testee.detectLaneMiddle(edges);

		drawTestee(image, edges, testee);
	}
	free(dirp);
	closedir(dp);
}
}
