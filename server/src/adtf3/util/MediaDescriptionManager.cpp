#include "MediaDescriptionManager.h"

using namespace taco;

MediaDescriptionManager::MediaDescriptionManager()
{
	usedStructs = {"tBoolSignalValue", "tSignalValue", "tUltrasonicStruct", "tLaserScannerData", "tWheelData",
			"tInerMeasUnitData", "tJuryStruct", "tDriverStruct", "tPosition", "tRoadSignExt"};
}

MediaDescriptionManager::~MediaDescriptionManager()
{
}

void MediaDescriptionManager::loadMediaDescriptors()
{
	object_ptr<IStreamType> mediaDesc;

	cString structName = usedStructs.at(0);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_BoolSignalValueSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			(access_element::find_index(m_BoolSignalValueSampleFactory, cString("ui32ArduinoTimestamp"),
					m_ddlBoolSignalValueId.ui32ArduinoTimestamp));
			(access_element::find_index(
					m_BoolSignalValueSampleFactory, cString("bValue"), m_ddlBoolSignalValueId.bValue));
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(1);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_SignalValueSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			(access_element::find_index(
					m_SignalValueSampleFactory, cString("ui32ArduinoTimestamp"), m_ddlSignalValueId.timeStamp));
			(access_element::find_index(m_SignalValueSampleFactory, cString("f32Value"), m_ddlSignalValueId.value));
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(2);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_USDataSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			(access_element::find_index(m_USDataSampleFactory, cString("tSideLeft") + cString(".ui32ArduinoTimestamp"),
					m_ddlUltrasonicStructIndex.SideLeft.timeStamp));
			(access_element::find_index(m_USDataSampleFactory, cString("tSideLeft") + cString(".f32Value"),
					m_ddlUltrasonicStructIndex.SideLeft.value));
			(access_element::find_index(m_USDataSampleFactory, cString("tSideRight") + cString(".ui32ArduinoTimestamp"),
					m_ddlUltrasonicStructIndex.SideRight.timeStamp));
			(access_element::find_index(m_USDataSampleFactory, cString("tSideRight") + cString(".f32Value"),
					m_ddlUltrasonicStructIndex.SideRight.value));
			(access_element::find_index(m_USDataSampleFactory, cString("tRearLeft") + cString(".ui32ArduinoTimestamp"),
					m_ddlUltrasonicStructIndex.RearLeft.timeStamp));
			(access_element::find_index(m_USDataSampleFactory, cString("tRearLeft") + cString(".f32Value"),
					m_ddlUltrasonicStructIndex.RearLeft.value));
			(access_element::find_index(m_USDataSampleFactory,
					cString("tRearCenter") + cString(".ui32ArduinoTimestamp"),
					m_ddlUltrasonicStructIndex.RearCenter.timeStamp));
			(access_element::find_index(m_USDataSampleFactory, cString("tRearCenter") + cString(".f32Value"),
					m_ddlUltrasonicStructIndex.RearCenter.value));
			(access_element::find_index(m_USDataSampleFactory, cString("tRearRight") + cString(".ui32ArduinoTimestamp"),
					m_ddlUltrasonicStructIndex.RearRight.timeStamp));
			(access_element::find_index(m_USDataSampleFactory, cString("tRearRight") + cString(".f32Value"),
					m_ddlUltrasonicStructIndex.RearRight.value));
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(3);
	if (ERR_NOERROR == adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
							   structName.GetPtr(), mediaDesc, m_LSStructSampleFactory)) {
		// find the indexes of the element for faster access in the process method.
		//        LogNamedMessage("Found mediadescription for tLaserScannerData!");
		// get all the member indices
		_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
		(access_element::find_index(m_LSStructSampleFactory, "ui32Size", m_ddlLSDataId.size));
		(access_element::find_array_index(m_LSStructSampleFactory, "tScanArray", m_ddlLSDataId.scanArray));
		nrOfLoadedDescs++;
	}

	structName = usedStructs.at(4);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_WheelDataSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			access_element::find_index(
					m_WheelDataSampleFactory, "ui32ArduinoTimestamp", m_ddlWheelDataIndex.ArduinoTimestamp);
			access_element::find_index(m_WheelDataSampleFactory, "ui32WheelTach", m_ddlWheelDataIndex.WheelTach);
			access_element::find_index(m_WheelDataSampleFactory, "i8WheelDir", m_ddlWheelDataIndex.WheelDir);
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(5);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_IMUDataSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			access_element::find_index(
					m_IMUDataSampleFactory, "ui32ArduinoTimestamp", m_ddlInerMeasUnitDataIndex.timeStamp);
			access_element::find_index(m_IMUDataSampleFactory, "f32A_x", m_ddlInerMeasUnitDataIndex.A_x);
			access_element::find_index(m_IMUDataSampleFactory, "f32A_y", m_ddlInerMeasUnitDataIndex.A_y);
			access_element::find_index(m_IMUDataSampleFactory, "f32A_z", m_ddlInerMeasUnitDataIndex.A_z);
			access_element::find_index(m_IMUDataSampleFactory, "f32G_x", m_ddlInerMeasUnitDataIndex.G_x);
			access_element::find_index(m_IMUDataSampleFactory, "f32G_y", m_ddlInerMeasUnitDataIndex.G_y);
			access_element::find_index(m_IMUDataSampleFactory, "f32G_z", m_ddlInerMeasUnitDataIndex.G_z);
			access_element::find_index(m_IMUDataSampleFactory, "f32M_x", m_ddlInerMeasUnitDataIndex.M_x);
			access_element::find_index(m_IMUDataSampleFactory, "f32M_y", m_ddlInerMeasUnitDataIndex.M_y);
			access_element::find_index(m_IMUDataSampleFactory, "f32M_z", m_ddlInerMeasUnitDataIndex.M_z);
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(6);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_juryStructSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			(adtf_ddl::access_element::find_index(
					m_juryStructSampleFactory, cString("i16ActionID"), m_ddlJuryStructId.actionId));
			(adtf_ddl::access_element::find_index(
					m_juryStructSampleFactory, cString("i16ManeuverEntry"), m_ddlJuryStructId.maneuverEntry));
			nrOfLoadedDescs++;
		}

	structName = usedStructs.at(7);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_driverStructSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			(adtf_ddl::access_element::find_index(
					m_driverStructSampleFactory, cString("i16StateID"), m_ddlDriverStructId.stateId));
			(adtf_ddl::access_element::find_index(
					m_driverStructSampleFactory, cString("i16ManeuverEntry"), m_ddlDriverStructId.maneuverEntry));
			nrOfLoadedDescs++;
		}

	// the position struct
	structName = usedStructs.at(8);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_PositionSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			adtf_ddl::access_element::find_index(m_PositionSampleFactory, "f32x", m_ddlPositionIndex.x);
			adtf_ddl::access_element::find_index(m_PositionSampleFactory, "f32y", m_ddlPositionIndex.y);
			adtf_ddl::access_element::find_index(m_PositionSampleFactory, "f32radius", m_ddlPositionIndex.radius);
			adtf_ddl::access_element::find_index(m_PositionSampleFactory, "f32speed", m_ddlPositionIndex.speed);
			adtf_ddl::access_element::find_index(m_PositionSampleFactory, "f32heading", m_ddlPositionIndex.heading);
			nrOfLoadedDescs++;
		}

	// the roadsign struct
	structName = usedStructs.at(9);
	if
		IS_OK(adtf::mediadescription::ant::create_adtf_default_stream_type_from_service(
				structName.GetPtr(), mediaDesc, m_RoadSignSampleFactory))
		{
			_mediaDescriptors.insert(make_pair(structName.GetPtr(), mediaDesc));
			adtf_ddl::access_element::find_index(m_RoadSignSampleFactory, "i16Identifier", m_ddlRoadSignIndex.id);
			adtf_ddl::access_element::find_index(m_RoadSignSampleFactory, "f32Imagesize", m_ddlRoadSignIndex.size);
			adtf_ddl::access_element::find_index(m_RoadSignSampleFactory, "af32TVec", m_ddlRoadSignIndex.tvec);
			adtf_ddl::access_element::find_index(m_RoadSignSampleFactory, "af32RVec", m_ddlRoadSignIndex.rvec);
			nrOfLoadedDescs++;
		}

	LOG_INFO("Loaded Mediadescriptions (loaded / required) = %d / %d\n", nrOfLoadedDescs, usedStructs.size());
}

object_ptr<IStreamType> MediaDescriptionManager::getMediaDescription(string type)
{
	object_ptr<IStreamType> mediaDesc;
	for (auto &description : _mediaDescriptors) {
		string nameOfDesc = description.first;
		if (type.compare(nameOfDesc) == 0) {
			mediaDesc = description.second;
			break;
		}
	}
	return mediaDesc;
}
