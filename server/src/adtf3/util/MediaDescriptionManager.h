#pragma once

#ifndef TACO_SERVER_MEDIADESCRIPTIONMANAGER_H
#define TACO_SERVER_MEDIADESCRIPTIONMANAGER_H

#include "stdafx.h"
#include <aadc_structs.h>
#include <boost/shared_ptr.hpp>

using namespace adtf_util;
using namespace ddl;
using namespace adtf::ucom;
using namespace adtf::base;
using namespace adtf::streaming;
using namespace adtf::mediadescription;
using namespace adtf::filter;
using namespace adtf::filter::ant;

namespace taco
{
class MediaDescriptionManager
{
  public:
	typedef boost::shared_ptr<MediaDescriptionManager> Ptr;
	typedef boost::shared_ptr<const MediaDescriptionManager> ConstPtr;

	MediaDescriptionManager();
	virtual ~MediaDescriptionManager();

	void loadMediaDescriptors();

	object_ptr<IStreamType> getMediaDescription(string type);

	std::map<string, object_ptr<IStreamType>> _mediaDescriptors;

	/*! A bool signal value identifier. */
	struct tBoolSignalValueId
	{
		tSize ui32ArduinoTimestamp;
		tSize bValue;
	} m_ddlBoolSignalValueId;

	/*! The signal value sample factory */
	cSampleCodecFactory m_BoolSignalValueSampleFactory;

	/*! A signal value identifier. */
	struct tSignalValueId
	{
		tSize timeStamp;
		tSize value;
	} m_ddlSignalValueId;

	/*! The signal value sample factory */
	cSampleCodecFactory m_SignalValueSampleFactory;

	/*! A ddl ultrasonic structure index. */
	struct
	{
		tSignalValueId SideLeft;
		tSignalValueId SideRight;
		tSignalValueId RearLeft;
		tSignalValueId RearCenter;
		tSignalValueId RearRight;

	} m_ddlUltrasonicStructIndex;
	cSampleCodecFactory m_USDataSampleFactory;

	/*! A ddl laser scanner data identifier. */
	struct ddlLaserScannerDataId
	{
		tSize size;
		tSize scanArray;
	} m_ddlLSDataId;

	/*! The ls structure sample factory */
	cSampleCodecFactory m_LSStructSampleFactory;

	/*! A ddl wheel data index. */
	struct
	{
		tSize ArduinoTimestamp;
		tSize WheelTach;
		tSize WheelDir;
	} m_ddlWheelDataIndex;
	cSampleCodecFactory m_WheelDataSampleFactory;

	/*! A ddl iner meas unit data index. */
	struct
	{
		tSize timeStamp;
		tSize A_x;
		tSize A_y;
		tSize A_z;
		tSize G_x;
		tSize G_y;
		tSize G_z;
		tSize M_x;
		tSize M_y;
		tSize M_z;
	} m_ddlInerMeasUnitDataIndex;

	/*! The imu data sample factory */
	cSampleCodecFactory m_IMUDataSampleFactory;

	// Media Descriptions
	struct tJuryStructId
	{
		tSize actionId;
		tSize maneuverEntry;
	} m_ddlJuryStructId;

	/*! The jury structure sample factory */
	cSampleCodecFactory m_juryStructSampleFactory;

	struct tDriverStructId
	{
		tSize stateId;
		tSize maneuverEntry;
	} m_ddlDriverStructId;

	/*! The driver structure sample factory */
	cSampleCodecFactory m_driverStructSampleFactory;

	/*! The ddl indices for a tPosition */
	struct
	{
		tSize x;
		tSize y;
		tSize radius;
		tSize speed;
		tSize heading;
	} m_ddlPositionIndex;

	/*! The position sample factory */
	adtf::mediadescription::cSampleCodecFactory m_PositionSampleFactory;

	/*! The ddl indices for a tRoadSignExt */
	struct
	{
		tSize id;
		tSize size;
		tSize tvec;
		tSize rvec;
	} m_ddlRoadSignIndex;

	/*! The road sign sample factory */
	adtf::mediadescription::cSampleCodecFactory m_RoadSignSampleFactory;

  private:
	std::vector<cString> usedStructs;
	int nrOfLoadedDescs = 0;
};
#endif // TACO_SERVER_MEDIADESCRIPTIONMANAGER_H
}
