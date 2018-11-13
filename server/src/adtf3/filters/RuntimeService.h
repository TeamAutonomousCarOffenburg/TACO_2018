#pragma once

#include <thread>

// Include order seems to matter here...
// clang-format off
//#include <adtf_platform_inc.h>
//#include <adtf_filtersdk.h>
#include "stdafx.h"
#include <aadc_structs.h>
//#include <adtf_plugin_sdk.h>
//#include <adtf_graphics.h>
// clang-format on
//#include "../decoder/ADTFPinMessageDecoder.h"
#include "../encoder/ADTFPinMessageEncoder.h"
#include "../util/MediaDescriptionManager.h"

#include <communication/Communication.h>
#include "detection/ILaneDetection.h"
//#include "../../hsogserver/action/IValueEffector.h"

#define CID_TACO_RUNTIME_SERVICE "RuntimeService.adtf.taco"
#define PROPERTY_START_CLIENT "Start Client"
#define PROPERTY_START_VISION "Start Vision"
#define PROPERTY_CLIENT_LOGGING "Client Logging"
#define RUNTIME_FILTER_NAME "TACO Runtime Service"
#define PROPERTY_CLIENT_SCENARIO "Client Scenario"

using namespace std;
using namespace adtf_util;
using namespace ddl;
using namespace adtf::ucom;
using namespace adtf::base;
using namespace adtf::streaming;
using namespace adtf::mediadescription;
using namespace adtf::filter;
using namespace adtf::filter::ant;

class RuntimeService : public cTriggerFunction
{
  public:
	RuntimeService();
	virtual ~RuntimeService();

	tResult Process(tTimeStamp tmTimeOfTrigger);

	tResult Configure();

	ADTF_CLASS_ID_NAME(RuntimeService, CID_TACO_RUNTIME_SERVICE, RUNTIME_FILTER_NAME);
	ADTF_CLASS_DEPENDENCIES(REQUIRE_INTERFACE(adtf::services::IReferenceClock));

  private:
	// properties
	adtf::base::ant::property_variable<tBool> _startClient;
	adtf::base::ant::property_variable<tBool> _startVision;
	adtf::base::ant::property_variable<tBool> _clientLogging;
	adtf::base::ant::property_variable<cString> _clientScenario = cString("driveWaypoints");
	adtf::base::property_variable<cFilename> m_calibFile = cFilename(cString("basler_fisheye_intrinsic_calib.yml"));

	taco::MediaDescriptionManager::Ptr _mediaDescManager;

	//	taco::ADTFPinMessageDecoder::Ptr _decoder;
	taco::ADTFPinMessageEncoder::Ptr _encoder;

	//    std::map<IPin *, taco::InputPin> _inputPinMap;
	std::map<IPin *, taco::OutputPin> _outputPinMap;

	taco::ICarMetaModel::Ptr _carMetaModel;
	taco::IAction::Ptr _action;
	taco::IPerception::Ptr _perception;
	//	taco::IEventLogger::ConstPtr _logger;
	taco::ILaneDetection::Ptr _laneDetection;

	std::map<string, double> _usPerceptorValues;
	std::vector<IPerceptor::ConstPtr> perceptors;

	void CreateInputPins();
	void CreateOutputPins();
	tResult SendToActuators();
	void SendToClient();

	/*! The input iner meas unit */
	cPinReader m_oInputInerMeasUnit;
	/*! The input ultrasonic unit */
	cPinReader m_oInputUltrasonicUnit;
	/*! The inputl wheel left */
	cPinReader m_oInputlWheelLeft;
	/*! The inputl wheel right */
	cPinReader m_oInputlWheelRight;
	/*! The input laser scanner */
	cPinReader m_oInputLaserScanner;
	/*! sample reader The input jury structure */
	cPinReader m_oInputJuryStruct;
	/*! sample reader List of input maneuvers */
	cPinReader m_oInputManeuverList;
	cString m_strManeuverFileString;
	/*! sample reader for the marker-based localizer position*/
	cPinReader m_oInputPosition;
	/*! Reader of an InPin roadSign. */
	cPinReader m_oReaderRoadSign;

	/*! The Basler camera */
	adtf::streaming::tStreamImageFormat m_sCameraInputFormat;
	cPinReader m_oBaslerCamera;
	tResult ChangeType(adtf::streaming::cDynamicSampleReader &inputPin, const adtf::streaming::ant::IStreamType &oType);

	// Output
	/*! The output speed controller */
	// TODO: put in a struct for all float value signals
	cPinWriter m_oOutputSpeedController;
	/*! The output steering controller */
	// TODO: put in a struct for all float value signals
	cPinWriter m_oOutputSteeringController;
	/*! The output sample writer turn right */
	cPinWriter m_oOutputTurnRight;
	/*! The output sample writer turn left */
	cPinWriter m_oOutputTurnLeft;
	/*! The output sample writer hazard */
	cPinWriter m_oOutputHazard;
	/*! The output sample writer head light */
	cPinWriter m_oOutputHeadLight;
	/*! The output sample writer reverse light */
	cPinWriter m_oOutputReverseLight;
	/*! The output sample writer brake light */
	cPinWriter m_oOutputBrakeLight;
	/*! The output sample writer for drive struct to jury module */
	cPinWriter m_oOutputDriveStruct;
	/*! The output sample writer for position (backend) */
	cPinWriter m_oOutputPosition;

	/*! The mutex */
	std::mutex m_oMutex;

	/*! The reference clock */
	object_ptr<adtf::services::IReferenceClock> m_pClock;

	std::thread _actuatorThread;
	bool _running = false;
	bool _init = false;
	bool _clientStarted = false;
	bool _visionStarted = false;
	void actuatorThread();

	void CreateRuntime();
	void StartClient();
	void StartVision();
	void RunCommandInDirectory(std::string command, string directory);

	void Start();
	void Stop();

	Communication *communication;
};
