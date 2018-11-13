#include "RuntimeService.h"
#include "../util/MediaDescriptionManager.h"
#include "general/ComponentFactory.h"
#include "general/LogComponentFactory.h"
#include "perception/ICameraPerceptor.h"
#include "perception/impl/CameraPerceptor.h"
#include <AADCCar.h>
#include <ADTF3_helper.h>
#include <aadc_jury.h>
#include <aadc_structs.h>
#include <perception/ILidarPerceptor.h>
#include <perception/impl/IMUPerceptor.h>
#include <perception/impl/JuryPerceptor.h>
#include <perception/impl/LaneMiddlePerceptor.h>
#include <perception/impl/LidarPerceptor.h>
#include <perception/impl/ManeuverListPerceptor.h>
#include <perception/impl/PositionPerceptor.h>
#include <perception/impl/RoadSignPerceptor.h>
#include <perception/impl/ValuePerceptor.h>
#include <perception/impl/WheelTickPerceptor.h>
#include <sys/prctl.h>
#include <utils/driveinstructionreader/DriveInstructionReader.h>

// TODO: heinclude_directories(../adtf/)re the list of all pins?
ADTF_TRIGGER_FUNCTION_FILTER_PLUGIN(CID_TACO_RUNTIME_SERVICE, RUNTIME_FILTER_NAME, RuntimeService,
		adtf::filter::pin_trigger(
				{"ultrasonic_struct", "iner_meas_unit", "wheel_left", "wheel_right", "laser_scanner"}));

using namespace taco;
using namespace adtf_util;
using namespace adtf::ucom;
using namespace adtf_ddl;
using namespace adtf::streaming;
using namespace std;
using namespace std::chrono;

RuntimeService::RuntimeService()
{
	SetName("RuntimeService");

	// properties
	RegisterPropertyVariable(PROPERTY_START_CLIENT, _startClient);
	//	_startClient = true;
	RegisterPropertyVariable(PROPERTY_START_VISION, _startVision);
	//	_startVision = true;
	RegisterPropertyVariable(PROPERTY_CLIENT_LOGGING, _clientLogging);
	//	_clientLogging = true;
	RegisterPropertyVariable(PROPERTY_CLIENT_SCENARIO, _clientScenario);
	RegisterPropertyVariable("calibration file", m_calibFile);

	// pins
	CreateRuntime();
	CreateInputPins();
	CreateOutputPins();
}

tResult RuntimeService::Configure()
{
	Start();
	RETURN_NOERROR;
}

void RuntimeService::CreateRuntime()
{
	_runtime->GetObject(m_pClock);
	// Check for unique initialization
	if (!_init) {
		IComponentFactory::ConstPtr factory(new ComponentFactory());
		//			IComponentFactory::ConstPtr factory(new LogComponentFactory());
		_init = true;
		_carMetaModel = factory->createCarMetaModel();
		_action = factory->createAction(_carMetaModel);
		_perception = factory->createPerception(_carMetaModel);
		_laneDetection = factory->createLaneDetection(_carMetaModel);
		_mediaDescManager = MediaDescriptionManager::Ptr(new MediaDescriptionManager());
		_mediaDescManager->loadMediaDescriptors();
		//		_logger = factory->createEventLogger();
		// Init default perceptor values
		//			_usPerceptorValues.insert(make_pair(AADCCar::US_FRONT_CENTER_LEFT, -1));
		//			_usPerceptorValues.insert(make_pair(AADCCar::US_FRONT_CENTER, -1));
		//			_usPerceptorValues.insert(make_pair(AADCCar::US_FRONT_CENTER_RIGHT, -1));
	}

	_encoder = ADTFPinMessageEncoder::Ptr(new ADTFPinMessageEncoder(_action, _carMetaModel, _mediaDescManager));
	//	_decoder = ADTFPinMessageDecoder::Ptr(new ADTFPinMessageDecoder(_perception, _carMetaModel, _logger));
}

RuntimeService::~RuntimeService()
{
	Stop();
}

void RuntimeService::CreateInputPins()
{
	// TODO: reactivate PinMessageDecoder to auto-generate those
	string imuDesc = "tInerMeasUnitData";
	object_ptr<IStreamType> mediaDesc = _mediaDescManager->getMediaDescription(imuDesc);
	Register(m_oInputInerMeasUnit, "iner_meas_unit", mediaDesc);

	string wheelDesc = "tWheelData";
	mediaDesc = _mediaDescManager->getMediaDescription(wheelDesc);
	Register(m_oInputlWheelLeft, "wheel_left", mediaDesc);
	Register(m_oInputlWheelRight, "wheel_right", mediaDesc);

	string lidarDesc = "tLaserScannerData";
	mediaDesc = _mediaDescManager->getMediaDescription(lidarDesc);
	Register(m_oInputLaserScanner, "laser_scanner", mediaDesc);

	string USdesc = "tUltrasonicStruct";
	mediaDesc = _mediaDescManager->getMediaDescription(USdesc);
	Register(m_oInputUltrasonicUnit, "ultrasonic_struct", mediaDesc);

	string juryDesc = "tJuryStruct";
	mediaDesc = _mediaDescManager->getMediaDescription(juryDesc);
	Register(m_oInputJuryStruct, "jury_struct", mediaDesc);

	string posDesc = "tPosition";
	mediaDesc = _mediaDescManager->getMediaDescription(posDesc);
	Register(m_oInputPosition, "marker_position", mediaDesc);

	string roadsignDesc = "tRoadSignExt";
	mediaDesc = _mediaDescManager->getMediaDescription(roadsignDesc);
	Register(m_oReaderRoadSign, "road_sign_ext", mediaDesc);

	// camera
	m_sCameraInputFormat.m_strFormatName = ADTF_IMAGE_FORMAT(RGB_24);
	adtf::ucom::object_ptr<IStreamType> pType = adtf::ucom::make_object_ptr<cStreamType>(stream_meta_type_image());
	set_stream_type_image_format(*pType, m_sCameraInputFormat);

	// Register input pin
	Register(m_oBaslerCamera, "camera", pType);

	object_ptr<IStreamType> pTypeDefault = adtf::ucom::make_object_ptr<cStreamType>(stream_meta_type_anonymous());
	Register(m_oInputManeuverList, "maneuver_list", pTypeDefault);

	// register callback for type changes
	m_oBaslerCamera.SetAcceptTypeCallback(
			[this](const adtf::ucom::ant::iobject_ptr<const adtf::streaming::ant::IStreamType> &pType) -> tResult {
				return ChangeType(m_oBaslerCamera, *pType.Get());
			});
}

tResult RuntimeService::ChangeType(
		adtf::streaming::cDynamicSampleReader &inputPin, const adtf::streaming::ant::IStreamType &oType)
{
	if (oType == adtf::streaming::stream_meta_type_image()) {
		adtf::ucom::object_ptr<const adtf::streaming::IStreamType> pTypeInput;
		// get pType from input reader
		inputPin >> pTypeInput;
		adtf::streaming::get_stream_type_image_format(m_sCameraInputFormat, *pTypeInput);
	} else {
		RETURN_ERROR(ERR_INVALID_TYPE);
	}

	RETURN_NOERROR;
}

void RuntimeService::CreateOutputPins()
{
	vector<OutputPin> outputPins = _encoder->getOutputPins();

	int index = 0;
	for (auto it = outputPins.begin(); it != outputPins.end(); ++it, index++) {
		string sensorSignalType = (*it).signalType;
		adtf::streaming::cOutPin *pin = new adtf::streaming::cOutPin();
		object_ptr<IStreamType> mediaDesc = _mediaDescManager->getMediaDescription(sensorSignalType);
		//		_mediaDescriptors.find(sensorSignalType);
		bool pinRegistrated = false;
		if ((*it).name.compare(AADCCar::STEERING_SERVO) == 0) {
			Register(m_oOutputSteeringController, "steering", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::MAIN_MOTOR) == 0) {
			Register(m_oOutputSpeedController, "speed", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::HEAD_LIGHTS) == 0) {
			Register(m_oOutputHeadLight, "head_light", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::INDICATOR_LIGHTS_LEFT) == 0) {
			Register(m_oOutputTurnLeft, "turn_signal_left", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::INDICATOR_LIGHTS_RIGHT) == 0) {
			Register(m_oOutputTurnRight, "turn_signal_right", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::BRAKE_LIGHTS) == 0) {
			Register(m_oOutputBrakeLight, "brake_light", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::WARN_LIGHTS) == 0) {
			Register(m_oOutputHazard, "hazard_light", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::BACK_LIGHTS) == 0) {
			Register(m_oOutputReverseLight, "reverse_light", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare(AADCCar::DRIVE_STATUS) == 0) {
			Register(m_oOutputDriveStruct, "driver_struct", mediaDesc);
			pinRegistrated = true;
		} else if ((*it).name.compare("Position") == 0) {
			Register(m_oOutputPosition, "position", mediaDesc);
			pinRegistrated = true;
		}

		if (pinRegistrated) {
			_outputPinMap.insert(make_pair(pin, *it));
		}
	}
}

void RuntimeService::actuatorThread()
{
	system_clock::time_point startTime = system_clock::now();

	system_clock::time_point beforeCycle;
	system_clock::time_point afterCycle;

	while (_running) {
		long elapsedTimeMs;
		beforeCycle = system_clock::now();

		if (beforeCycle - startTime > milliseconds(100)) {
			if (!_visionStarted && _startVision) {
				StartVision();
				_visionStarted = true;
			}
		}

		if (beforeCycle - startTime > seconds(5)) {
			if (!_clientStarted && _startClient) {
				StartClient();
				_clientStarted = true;
			}
		}

		// only send to client or car actuators if client is connected
		if (communication->isClientConnected()) {
			SendToActuators();
			SendToClient();
		}

		// when client lost connection, we should be able to send stop to the motor
		if (communication->isClientDisconnected()) {
			SendToActuators();
		}

		afterCycle = system_clock::now();

		elapsedTimeMs = duration_cast<milliseconds>(afterCycle - beforeCycle).count();

		milliseconds duration(20 - elapsedTimeMs);
		this_thread::sleep_for(duration);
	}
}

void RuntimeService::SendToClient()
{
	{
		// synchronization block for perceptors access
		std::lock_guard<std::mutex> oGuard(m_oMutex);
		if (!perceptors.empty()) {
			// Create perceptor map
			std::map<std::string, IPerceptor::ConstPtr> perceptorMap;
			bool haveCamera = false;
			for (int i = perceptors.size() - 1; i >= 0; i--) {
				auto &perceptor = perceptors[i];
				perceptorMap.insert(make_pair(perceptor->getName(), perceptor));

#if TACO_CONFIG != TACO_2016
				string camera = AADCCar::BASLER_CAMERA;
#else
				string camera = AADCCar::XTION_CAMERA_RGB;
#endif
				if (!haveCamera && perceptor->getName() == camera) {
					haveCamera = true;
					ICameraPerceptor::ConstPtr p = boost::dynamic_pointer_cast<const ICameraPerceptor>(perceptor);

					cv::Mat image = p->getImage();
					if (!image.empty()) {
						static Eigen::Vector3d floorNormal = Eigen::Vector3d(0, 0, 1);
						LaneMiddle detection = _laneDetection->detectLanes(image, floorNormal)->laneMiddle;
						perceptorMap.insert(make_pair(AADCCar::LANE_ASSIST,
								boost::make_shared<LaneMiddlePerceptor>(AADCCar::LANE_ASSIST, 0.0, detection)));
					}
				}
			}

			// Publish new perceptions
			_perception->updatePerceptors(perceptorMap);
		}
		perceptors.clear();
	}

	while (_perception->nextPerceptorMap()) {
		communication->update();
	}
}

void RuntimeService::Start()
{
	communication = new Communication(_carMetaModel, _action, _perception);
	communication->start();
	_running = true;
	_actuatorThread = thread(&RuntimeService::actuatorThread, this);

#ifdef __linux__
	auto handle = _actuatorThread.native_handle();
	pthread_setname_np(handle, "ActuatorThread");
#endif
}

void RuntimeService::StartClient()
{
	string metaModelDirectory = string(TACO_DIRECTORY) + "/config";
#if TACO_CONFIG == TACO_2018
	string version = "taco2018";
#elif TACO_CONFIG == TACO_2017
	string version = "taco2017";
#elif TACO_CONFIG == TACO_2016
	string version = "taco2016";
#endif

	string command = string("bash start.sh --metaModelDirectory=") + metaModelDirectory + " --version=" + version;

	cString scenario = _clientScenario;
	if (!scenario.IsEmpty()) {
		command = command + " --scenario=" + scenario.GetPtr();
	}

	if (_clientLogging) {
		command = command + " --log";
	}

	string directory = string(TACO_DIRECTORY) + "/client/jar/out/tacoAgent";

	RunCommandInDirectory(command, directory);
	LOG_INFO("Client started");
}

void RuntimeService::StartVision()
{
	RunCommandInDirectory(string("bash start.sh -v &"), string(TACO_DIRECTORY) + "/vision");
}

void RuntimeService::RunCommandInDirectory(string command, string directory)
{
	char cwd[1024];
	if (getcwd(cwd, sizeof(cwd)) == nullptr) {
		cerr << "RunCommandInDirectory(): unable to get the working directory: " << strerror(errno) << endl;
		return;
	}

	if (chdir(directory.c_str()) == -1) {
		cerr << "RunCommandInDirectory(): chdir(" << directory << ") failed: " << strerror(errno) << endl;
		return;
	}

	cout << directory << ": " << command << endl;
	if (system(command.c_str()) == -1) {
		cerr << "RunCommandInDirectory(): system() failed: " << strerror(errno) << endl;
	}

	if (chdir(cwd) == -1) {
		cerr << "RunCommandInDirectory(): chdir(" << cwd << ") failed: " << strerror(errno) << endl;
		return;
	}
}

void RuntimeService::Stop()
{
	try {
		std::string command = "pkill -f -SIGUSR1 object_detection_server.py";
		cout << command << endl;
		if (system(command.c_str()) == -1) {
			cout << "Could not kill object detection" << endl;
		}
		//
		_running = false;
		_actuatorThread.join();
		communication->stop();
		delete communication;

		//			_decoder.reset();
		//			_encoder.reset();
	} catch (const exception &ex) {
		cout << " exception: " << ex.what() << endl;
	} catch (...) {
		exception_ptr eptr = current_exception();

		try {
			rethrow_exception(eptr);
		} catch (const exception &e) {
			cout << " exception: " << e.what() << endl;
		}
	}
}

tResult RuntimeService::Process(tTimeStamp tmTimeOfTrigger)
{
	std::lock_guard<std::mutex> oGuard(m_oMutex);

	// this will empty the Reader queue and return the last sample received.
	object_ptr<const ISample> pSampleFromIMU;

	while (IS_OK(m_oInputInerMeasUnit.GetNextSample(pSampleFromIMU))) {
		auto oDecoderIMU = _mediaDescManager->m_IMUDataSampleFactory.MakeDecoderFor(*pSampleFromIMU);

		RETURN_IF_FAILED(oDecoderIMU.IsValid());

		// retrieve the values (using convenience methods that return a variant)
		// IMU
		tInerMeasUnitData IMU_data;

		RETURN_IF_FAILED(oDecoderIMU.GetElementValue(
				_mediaDescManager->m_ddlInerMeasUnitDataIndex.timeStamp, &IMU_data.ui32ArduinoTimestamp));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.A_x, &IMU_data.f32A_x));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.A_y, &IMU_data.f32A_y));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.A_z, &IMU_data.f32A_z));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.G_x, &IMU_data.f32G_x));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.G_y, &IMU_data.f32G_y));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.G_z, &IMU_data.f32G_z));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.M_x, &IMU_data.f32M_x));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.M_y, &IMU_data.f32M_y));
		RETURN_IF_FAILED(
				oDecoderIMU.GetElementValue(_mediaDescManager->m_ddlInerMeasUnitDataIndex.M_z, &IMU_data.f32M_z));

		tUInt32 time = IMU_data.ui32ArduinoTimestamp;
		//        printf("IMU arduino time: %i\n", time);
		static tUInt32 oldTime = 0;
		static Eigen::Quaterniond quat(Eigen::AngleAxisd(0, Eigen::Vector3d(0, 0, 0)));
		if (oldTime != 0) {
			double td = (time - oldTime) / 1000000.0;

			//	static double avg = 0;
			// static int count = 0;
			// avg = (avg * count + IMU_data.f32G_z) / (++count);
			// cout << td << ";" << IMU_data.f32G_z << ";" << avg << ";" << count << endl;

			Eigen::AngleAxisd yawAngle(Angle::toRadians(IMU_data.f32G_z) * td, Eigen::Vector3d::UnitZ());
			quat = quat * yawAngle;
			perceptors.push_back(boost::make_shared<IMUPerceptor>(AADCCar::CAR_IMU, long(time), double(IMU_data.f32A_x),
					double(IMU_data.f32A_y), double(IMU_data.f32A_z), quat.w(), quat.x(), quat.y(), quat.z()));

			//            printf("Timestamp IMU Sensor: %d\n", std::chrono::duration_cast<std::chrono::milliseconds>(
			//                                                         std::chrono::system_clock::now().time_since_epoch())
			//                                                         .count());

			//
			//            perceptors.emplace_back(boost::make_shared<taco::IIMUPerceptor>(_perceptorName,
			//            long(time), double(IMU_data.f32A_x), double(IMU_data.f32A_y), double(IMU_data.f32A_z),
			//            quat.w(), quat.x(), quat.y(), quat.z()));
			//
		}
		oldTime = time;
	}

	object_ptr<const ISample> pSampleFromUS;

	if (IS_OK(m_oInputUltrasonicUnit.GetLastSample(pSampleFromUS))) {
		auto oDecoderUS = _mediaDescManager->m_USDataSampleFactory.MakeDecoderFor(*pSampleFromUS);

		RETURN_IF_FAILED(oDecoderUS.IsValid());

		// retrieve the values (using convenience methods that return a variant)
		// IMU
		tUltrasonicStruct US_data;

		// TODO example had no timestamp information, not sure if it works like this
		//		RETURN_IF_FAILED(oDecoderUS.GetElementValue(m_ddlUltrasonicStructIndex.SideLeft.timeStamp,
		//&US_data.tSideLeft.ui32ArduinoTimestamp));

		RETURN_IF_FAILED(oDecoderUS.GetElementValue(
				_mediaDescManager->m_ddlUltrasonicStructIndex.SideLeft.value, &US_data.tSideLeft.f32Value));
		RETURN_IF_FAILED(oDecoderUS.GetElementValue(
				_mediaDescManager->m_ddlUltrasonicStructIndex.SideRight.value, &US_data.tSideRight.f32Value));
		RETURN_IF_FAILED(oDecoderUS.GetElementValue(
				_mediaDescManager->m_ddlUltrasonicStructIndex.RearLeft.value, &US_data.tRearLeft.f32Value));
		RETURN_IF_FAILED(oDecoderUS.GetElementValue(
				_mediaDescManager->m_ddlUltrasonicStructIndex.RearCenter.value, &US_data.tRearCenter.f32Value));
		RETURN_IF_FAILED(oDecoderUS.GetElementValue(
				_mediaDescManager->m_ddlUltrasonicStructIndex.RearRight.value, &US_data.tRearRight.f32Value));

		tUInt32 time = US_data.tSideLeft.ui32ArduinoTimestamp;
		if (US_data.tSideLeft.f32Value != -1)
			perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
					"US_Side_Left", time, double(US_data.tSideLeft.f32Value / 100)));
		if (US_data.tSideRight.f32Value != -1)
			perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
					"US_Side_Right", time, double(US_data.tSideRight.f32Value / 100)));
		if (US_data.tRearLeft.f32Value != -1)
			perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
					"US_Rear_Left", time, double(US_data.tRearLeft.f32Value / 100)));
		if (US_data.tRearRight.f32Value != -1)
			perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
					"US_Rear_Right", time, double(US_data.tRearRight.f32Value / 100)));
		if (US_data.tRearCenter.f32Value != -1) {
			perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
					"US_Rear_Center", time, double(US_data.tRearCenter.f32Value / 100)));

			//            static long counter = 0;
			//            counter++;
			//            perceptors.push_back(boost::make_shared<taco::DoubleValuePerceptor>(
			//                    "US_Rear_Center", time, double(counter)));
			//            printf("%d US Sensor: %d\n",
			//                   std::chrono::duration_cast<std::chrono::milliseconds>(
			//                           std::chrono::system_clock::now().time_since_epoch()).count(), counter);
		}
	}

	object_ptr<const ISample> pSampleFromWheelLeft;

	if (IS_OK(m_oInputlWheelLeft.GetLastSample(pSampleFromWheelLeft))) {
		auto oDecoderWheel = _mediaDescManager->m_WheelDataSampleFactory.MakeDecoderFor(*pSampleFromWheelLeft);

		RETURN_IF_FAILED(oDecoderWheel.IsValid());

		// retrieve the values (using convenience methods that return a variant)
		tWheelData wheelData;

		RETURN_IF_FAILED(oDecoderWheel.GetElementValue(
				_mediaDescManager->m_ddlWheelDataIndex.ArduinoTimestamp, &wheelData.ui32ArduinoTimestamp));
		RETURN_IF_FAILED(oDecoderWheel.GetElementValue(
				_mediaDescManager->m_ddlWheelDataIndex.WheelTach, &wheelData.ui32WheelTach));
		RETURN_IF_FAILED(
				oDecoderWheel.GetElementValue(_mediaDescManager->m_ddlWheelDataIndex.WheelDir, &wheelData.i8WheelDir));

		// TODO replace with Decoder code as above if above works
		//				printf("Timestamp WheelTick Sensor left: %d  -- Value: %d\n",
		//						std::chrono::duration_cast<std::chrono::milliseconds>(
		//								std::chrono::system_clock::now().time_since_epoch())
		//								.count(),
		//						int(wheelData.ui32WheelTach));

		perceptors.push_back(boost::make_shared<taco::WheelTickPerceptor>("WH_WheelSpeed_Sensor_Left",
				long(wheelData.ui32ArduinoTimestamp), int(wheelData.ui32WheelTach),
				wheelData.i8WheelDir == 0 ? 1 : -1));
	}

	object_ptr<const ISample> pSampleFromWheelRight;

	if (IS_OK(m_oInputlWheelRight.GetLastSample(pSampleFromWheelRight))) {
		auto oDecoderWheel = _mediaDescManager->m_WheelDataSampleFactory.MakeDecoderFor(*pSampleFromWheelRight);

		RETURN_IF_FAILED(oDecoderWheel.IsValid());

		// retrieve the values (using convenience methods that return a variant)
		tWheelData wheelData;

		RETURN_IF_FAILED(oDecoderWheel.GetElementValue(
				_mediaDescManager->m_ddlWheelDataIndex.ArduinoTimestamp, &wheelData.ui32ArduinoTimestamp));
		RETURN_IF_FAILED(oDecoderWheel.GetElementValue(
				_mediaDescManager->m_ddlWheelDataIndex.WheelTach, &wheelData.ui32WheelTach));
		RETURN_IF_FAILED(
				oDecoderWheel.GetElementValue(_mediaDescManager->m_ddlWheelDataIndex.WheelDir, &wheelData.i8WheelDir));

		// TODO replace with Decoder code as above if above works
		perceptors.push_back(boost::make_shared<taco::WheelTickPerceptor>("WH_WheelSpeed_Sensor_Right",
				long(wheelData.ui32ArduinoTimestamp), int(wheelData.ui32WheelTach),
				wheelData.i8WheelDir == 0 ? 1 : -1));

		//        printf("Timestamp WheelTick Sensor right: %d  -- Value: %d\n",
		//               std::chrono::duration_cast<std::chrono::milliseconds>(
		//                       std::chrono::system_clock::now().time_since_epoch())
		//                       .count(),
		//               int(wheelData.ui32WheelTach));
	}

	object_ptr<const ISample> pSampleFromLS;

	if (IS_OK(m_oInputLaserScanner.GetLastSample(pSampleFromLS))) {
		auto oDecoder = _mediaDescManager->m_LSStructSampleFactory.MakeDecoderFor(*pSampleFromLS);

		RETURN_IF_FAILED(oDecoder.IsValid());
		tSize numOfScanPoints = 0;
		tResult res = oDecoder.GetElementValue(_mediaDescManager->m_ddlLSDataId.size, &numOfScanPoints);

		const tPolarCoordiante *pCoordinates = reinterpret_cast<const tPolarCoordiante *>(
				oDecoder.GetElementAddress(_mediaDescManager->m_ddlLSDataId.scanArray));
		std::vector<LidarValue> lidarValues;
		for (tSize i = 0; i < numOfScanPoints; ++i) {
			tFloat32 angle = pCoordinates[i].f32Angle;
			tFloat32 radius = pCoordinates[i].f32Radius;
			lidarValues.push_back(LidarValue(static_cast<const int &>(angle), radius));
		}
		perceptors.push_back(boost::make_shared<LidarPerceptor>(AADCCar::LIDAR_FRONT, 0.0, lidarValues));
	}

	// camera
	object_ptr<const ISample> pReadSample;
	if (IS_OK(m_oBaslerCamera.GetNextSample(pReadSample))) {
		object_ptr_shared_locked<const ISampleBuffer> pReadBuffer;
		// lock read buffer
		if (IS_OK(pReadSample->Lock(pReadBuffer))) {
			// create a opencv matrix from the media sample buffer
			Mat inputImage = Mat(cv::Size(m_sCameraInputFormat.m_ui32Width, m_sCameraInputFormat.m_ui32Height), CV_8UC3,
					(uchar *) pReadBuffer->GetPtr());
			Mat img = inputImage.clone();

			// Update internal values
			long _time = duration_cast<milliseconds>(high_resolution_clock::now().time_since_epoch()).count();
			// Create new CameraPerceptor
			perceptors.push_back(boost::make_shared<CameraPerceptor>(AADCCar::BASLER_CAMERA, _time, img));

			//            printf("Timestamp Camera: %d\n",
			//                   std::chrono::duration_cast<std::chrono::milliseconds>(
			//                           std::chrono::system_clock::now().time_since_epoch())
			//                           .count());
		}
	}

	object_ptr<const ISample> pSampleFromJury;
	if (IS_OK(m_oInputJuryStruct.GetNextSample(pSampleFromJury))) {
		auto oDecoder = _mediaDescManager->m_juryStructSampleFactory.MakeDecoderFor(*pSampleFromJury);
		RETURN_IF_FAILED(oDecoder.IsValid());
		tJuryStruct juryInput;
		RETURN_IF_FAILED(oDecoder.GetElementValue(
				_mediaDescManager->m_ddlJuryStructId.maneuverEntry, &juryInput.i16ManeuverEntry));
		RETURN_IF_FAILED(
				oDecoder.GetElementValue(_mediaDescManager->m_ddlJuryStructId.actionId, &juryInput.i16ActionID));

		tInt8 actionID = juryInput.i16ActionID;
		tInt16 maneuverEntry = juryInput.i16ManeuverEntry;
		long _time = duration_cast<milliseconds>(high_resolution_clock::now().time_since_epoch()).count();

		// TODO: May we should check if the jury command has changed to only send new commands to the client?
		perceptors.push_back(boost::make_shared<JuryPerceptor>(AADCCar::JURY_COMMAND, _time, actionID, maneuverEntry));
	}

	bool readManeuverList = false;
	object_ptr<const ISample> pSampleAnonymous;
	while (IS_OK(m_oInputManeuverList.GetNextSample(pSampleAnonymous))) {
		std::vector<tChar> data;
		object_ptr_shared_locked<const ISampleBuffer> pSampleBuffer;
		RETURN_IF_FAILED(pSampleAnonymous->Lock(pSampleBuffer));
		data.resize(pSampleBuffer->GetSize());
		memcpy(data.data(), pSampleBuffer->GetPtr(), pSampleBuffer->GetSize());
		if (data.size() > 0) { // maneuverlist
			m_strManeuverFileString.Set(data.data(), data.size());
			readManeuverList = true;
		}
	}
	if (readManeuverList) {
		std::vector<Maneuver> driveInstructions;
		DriveInstructionReader::loadFromString(std::string(m_strManeuverFileString.GetPtr()), driveInstructions);
		perceptors.push_back(boost::make_shared<ManeuverListPerceptor>(AADCCar::MANEUVER_LIST, 0, driveInstructions));
		readManeuverList = false;
	}

	tFloat32 f32x, f32y, f32heading;
	// Get Sample
	adtf::ucom::object_ptr<const adtf::streaming::ISample> pPosSample;
	while (IS_OK(m_oInputPosition.GetNextSample(pPosSample))) {
		// Get Position values
		auto oDecoder = _mediaDescManager->m_PositionSampleFactory.MakeDecoderFor(pPosSample);
		f32x = adtf_ddl::access_element::get_value(oDecoder, _mediaDescManager->m_ddlPositionIndex.x);
		f32y = adtf_ddl::access_element::get_value(oDecoder, _mediaDescManager->m_ddlPositionIndex.y);
		f32heading = adtf_ddl::access_element::get_value(oDecoder, _mediaDescManager->m_ddlPositionIndex.heading);
		perceptors.push_back(boost::make_shared<PositionPerceptor>("Position", 0, f32x, f32y, f32heading));
		//		printf("f32x: %f --- f32y: %f\n", f32x, f32y);
	}

	object_ptr<const ISample> pSampleFromRoadsign;
	if (IS_OK(m_oReaderRoadSign.GetNextSample(pSampleFromRoadsign))) {
		// parse the data
		auto oDecoder = _mediaDescManager->m_RoadSignSampleFactory.MakeDecoderFor(pSampleFromRoadsign);
		/*! currently processed road-sign */
		tInt16 m_i16ID = access_element::get_value(oDecoder, _mediaDescManager->m_ddlRoadSignIndex.id);
		/*! Size of the 32 marker */
		tFloat32 m_f32MarkerSize = access_element::get_value(oDecoder, _mediaDescManager->m_ddlRoadSignIndex.size);

		const tVoid *pArray;
		tSize size;

		// fetch marker translation and rotation arrays
		access_element::get_array(oDecoder, "af32TVec", pArray, size);
		/*! translation vector */
		Mat m_Tvec = Mat(3, 1, CV_32F, Scalar::all(0));
		m_Tvec.data = const_cast<uchar *>(static_cast<const uchar *>(pArray));

		access_element::get_array(oDecoder, "af32RVec", pArray, size);
		/*! rotation vector */
		Mat m_Rvec = Mat(3, 1, CV_32F, Scalar::all(0));
		m_Rvec.data = const_cast<uchar *>(static_cast<const uchar *>(pArray));

		perceptors.push_back(
				boost::make_shared<RoadSignPerceptor>(AADCCar::SIGNS, 0, m_i16ID, m_f32MarkerSize, m_Tvec, m_Rvec));
	}

	RETURN_NOERROR;
}

tResult RuntimeService::SendToActuators()
{
	// TODO get the timestamp and value from encoder, for now we could create attributes for them?
	//		tUInt32 ui32arduinoTimestamp = 0;
	//		tFloat32 f32speed = 30;
	//	//
	//		RETURN_IF_FAILED(transmitSignalValue(m_oOutputSpeedController, m_pClock->GetStreamTime(),
	//											 m_SignalValueSampleFactory, m_ddlSignalValueId.timeStamp,
	//	ui32arduinoTimestamp, 										 m_ddlSignalValueId.value, f32speed));
	list<pair<IPin *, OutputPin>> pinsByModificationTime;
	for (auto &pin : _outputPinMap) {
		pinsByModificationTime.emplace_back(pin.first, pin.second);
	}

	// sort by modification time so that most recently modified effectors have priority (#27)
	pinsByModificationTime.sort([&](const pair<IPin *, OutputPin> &a, const pair<IPin *, OutputPin> &b) {
		return _action->getEffector(a.second.name)->getTime() < _action->getEffector(b.second.name)->getTime();
	});

	for (auto &pin : pinsByModificationTime) {
		std::lock_guard<std::mutex> oGuard(m_oMutex);
		object_ptr<ISample> pWriteSample;
		if (_encoder->encode(pin.second, m_pClock->GetStreamTime(), pWriteSample)) {
			if (pin.second.name == AADCCar::MAIN_MOTOR) {
				m_oOutputSpeedController << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::STEERING_SERVO) {
				m_oOutputSteeringController << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::HEAD_LIGHTS) {
				m_oOutputHeadLight << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::INDICATOR_LIGHTS_LEFT) {
				m_oOutputTurnLeft << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::INDICATOR_LIGHTS_RIGHT) {
				m_oOutputTurnRight << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::BRAKE_LIGHTS) {
				m_oOutputBrakeLight << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::WARN_LIGHTS) {
				m_oOutputHazard << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::BACK_LIGHTS) {
				m_oOutputReverseLight << pWriteSample << flush << trigger;
			} else if (pin.second.name == AADCCar::DRIVE_STATUS) {
				m_oOutputDriveStruct << pWriteSample << flush << trigger;
			}
		}
	}
	RETURN_NOERROR;
}