#pragma once

#define FILTER_CLASS_ID "CameraImageRelay.adtf.taco"
#define FILTER_CLASS_LABEL "TACO Camera Image Relay"
#define FILTER_VERSION_MAJOR 1
#define FILTER_VERSION_MINOR 0
#define FILTER_VERSION_BUILD 0
#define FILTER_VERSION_LABEL "Beta Version"

#define FILTER_PIN_NAME_VIDEOIN "Video_input"

#include "stdafx.h"

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <chrono>
#include <netinet/in.h>
#include <sys/socket.h>

using namespace cv;
using namespace std;
using namespace adtf_util;
using namespace ddl;
using namespace adtf::ucom;
using namespace adtf::base;
using namespace adtf::streaming;
using namespace adtf::mediadescription;
using namespace adtf::filter;
using namespace adtf::filter::ant;
/*!
  this filter creates sends the images to an python server
 */
class CameraImageRelay : public cTriggerFunction
{
	ADTF_CLASS_ID_NAME(CameraImageRelay, FILTER_CLASS_ID, FILTER_CLASS_LABEL);

  public:
	CameraImageRelay();
	~CameraImageRelay();
	tResult Process(tTimeStamp tmTimeOfTrigger) override;
	void Destroy();

	tResult Configure() override;

  protected:
	cPinReader m_oReaderVideo;
	/*! Writer for the video. */
	cPinWriter m_oWriterVideo;

	// Stream Formats
	/*! The input format */
	adtf::streaming::tStreamImageFormat m_InPinVideoFormat;

	/*! The clock */
	object_ptr<adtf::services::IReferenceClock> m_pClock;

  private:
	void initializeVideoParams();
	tResult PropertyChanged(const tChar *propertyName);
	int startServer();
	bool firstFrame;
	int fps;
	double msPerFrame;
	std::chrono::system_clock::time_point now;
	std::chrono::system_clock::time_point lastUpdate;
	int serverPort;
	int socketFD;
	int clientFD = -1;
	bool serverStarted = false;

	// filter properties
	adtf::base::property_variable<int> propertyPort = 1337;
	adtf::base::property_variable<int> propertyFPS = 15;

	void log(std::string message);
	void error(std::string message);
};
