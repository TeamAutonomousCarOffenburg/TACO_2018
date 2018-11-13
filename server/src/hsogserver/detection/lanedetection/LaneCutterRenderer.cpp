#include "LaneCutterRenderer.h"

using namespace taco;
using namespace std;
using namespace cv;

void LaneCutterRenderer::drawLine(Mat &image, Line line2, Scalar &color)
{
	line(image, line2.start, line2.end, color);
}

Mat LaneCutterRenderer::draw(int width, int height, const Mat &image, LaneCutterResult data)
{
	Rect rect(0, height / 2, width, height / 2);
	Mat temp_frame = image(rect);
	Scalar red = Scalar(0, 0, 255);
	Scalar green = Scalar(0, 255, 0);
	Scalar blue = Scalar(255, 0, 0);
	Scalar orange = Scalar(0, 130, 200);

	cout << "int[][] data = {";
	for (int i = 0; i < data._detections.size(); i++) {
		std::vector<int> lineData = data._detections[i];
		// scanline
		int y = lineData[0];
		cout << "{" << y;
		drawLine(temp_frame, Line(data._minX, y, data._maxX, y), blue);
		for (int j = 1; j < lineData.size(); j++) {
			// double line detections
			int x = lineData[j];
			drawLine(temp_frame, Line(x, y - 5, x, y + 5), red);
			cout << ", " << x;
		}
		cout << "}," << endl;
	}
	cout << "};" << endl << endl;

	return temp_frame;
}