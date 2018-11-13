#include "gtest/gtest.h"
#include <dirent.h>
#include <opencv2/highgui/highgui.hpp>

using namespace taco;
using namespace std;
using namespace cv;
using namespace Eigen;

bool ends_with(std::string const &value, std::string const &ending)
{
	if (ending.size() > value.size())
		return false;
	return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
}

int filter(const struct dirent *entry)
{
	return ends_with(entry->d_name, ".png") || ends_with(entry->d_name, ".jpeg") || ends_with(entry->d_name, ".jpg") ||
		   ends_with(entry->d_name, ".bmp");
}

const Mat getMatFromImage(string path)
{
	const Mat image = imread(path.data(), CV_LOAD_IMAGE_COLOR); // Read the file
	if (!image.data)											// Check for invalid input
	{
		cout << "Could not open or find the image" << std::endl;
	}
	return image;
}
