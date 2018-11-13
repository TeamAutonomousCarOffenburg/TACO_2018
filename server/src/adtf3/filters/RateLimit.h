#pragma once

//#include "../encoder/ADTFPinMessageEncoder.h"
//#include <adtf_platform_inc.h>
//#include <adtf_plugin_sdk.h>
#include "stdafx.h"
#include <aadc_structs.h>

//#include <adtf_filtersdk.h>
#include "general/ComponentFactory.h"
#include "general/IComponentFactory.h"
#include <chrono>

#define CID_TACO_RATE_LIMIT_SERVICE "RateLimit.adtf.taco"
#define RATE_LIMIT_FILTER_NAME "TACO Rate Limit"

using namespace std;
using namespace adtf_util;
using namespace ddl;
using namespace adtf::ucom;
using namespace adtf::base;
using namespace adtf::streaming;
using namespace adtf::mediadescription;
using namespace adtf::filter;
using namespace adtf::filter::ant;

class RateLimit : public cTriggerFunction
{
  public:
	RateLimit();
	virtual ~RateLimit();

	tResult Process(tTimeStamp tmTimeOfTrigger);

	tResult Configure();

	ADTF_CLASS_ID_NAME(RateLimit, CID_TACO_RATE_LIMIT_SERVICE, RATE_LIMIT_FILTER_NAME);
	ADTF_CLASS_DEPENDENCIES(REQUIRE_INTERFACE(adtf::services::IReferenceClock));

	//	tResult Init(tInitStage eStage, __exception = NULL) override;
	//	tResult OnPinEvent(adtf::IPin *pSource, tInt nEventCode, tInt nParam1, tInt nParam2,
	//			adtf::IMediaSample *pMediaSample) override;

  protected:
	tResult Start(__exception = NULL) override;
	tResult Stop(__exception = NULL) override;
	//	tResult Shutdown(tInitStage eStage, __exception = NULL) override;

  private:
	tResult CreatePins(__exception = NULL);
	tResult CreateDescriptors();

	bool longEnoughSinceLastSent(adtf::IPin *pSource);

	std::map<adtf::IPin *, taco::OutputPin> _inputPinMap;
	std::map<adtf::IPin *, adtf::cOutputPin *> _inputPinToOutputMap;
	std::map<adtf::IPin *, tFloat32> _tsignalLastValue;
	std::map<adtf::IPin *, tBool> _boolLastValue;
	std::map<adtf::IPin *, std::pair<tInt8, tInt16>> _driverStructLastValue;

	std::map<adtf::IPin *, std::chrono::system_clock::time_point> _lastSentTime;
	std::map<adtf::IPin *, cObjectPtr<adtf::IMediaTypeDescription>> _descriptors;

	std::vector<taco::OutputPin> _carOutputPins;
	adtf::IMediaDescriptionManager *_pDescManager;
};
