#pragma once

#define CID_TACO_IMAGE_WRITER "ImageWriter.adtf.taco"
#define FILTER_CLASS_LABEL "TACO Image Writer"

#define FILTER_PIN_NAME_VIDEOIN "Video_input"
#define UPDATE_RATE_PROPERTY "Update rate in ms"

// ADTF headers
#include "stdafx.h"
#include <aadc_structs.h>

#include <chrono>

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

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
  this filter creates a video file from the received images
 */
class ImageWriter : public cTriggerFunction
{
	ADTF_CLASS_ID_NAME(ImageWriter, CID_TACO_IMAGE_WRITER, FILTER_CLASS_LABEL);

  public:
	ImageWriter();
	virtual ~ImageWriter();
	tResult Process(tTimeStamp tmTimeOfTrigger) override;

	tResult Configure() override;

  private:
	// Pins
	/*! Reader for the video. */
	cPinReader m_oReaderVideo;
	/*! Writer for the video. */
	cPinWriter m_oWriterVideo;

	// Stream Formats
	/*! The input format */
	adtf::streaming::tStreamImageFormat m_InPinVideoFormat;

	/*! The clock */
	object_ptr<adtf::services::IReferenceClock> m_pClock;

	ImageWriter *writer;
	bool firstFrame;
	cString path;
	adtf::base::property_variable<cFilepath> propertyPath = cFilepath(cString("/home/aadc/imagewriter"));
	adtf::base::property_variable<int> propertyFPS = 15;
	int fps;
	double msPerFrame;
	std::chrono::system_clock::time_point now;
	std::chrono::system_clock::time_point lastUpdate;
};
