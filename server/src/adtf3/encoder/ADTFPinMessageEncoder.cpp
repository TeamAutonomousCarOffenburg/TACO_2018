#include "ADTFPinMessageEncoder.h"
#include "../util/MediaDescriptionManager.h"
#include "stdafx.h"
#include <AADCCar.h>
#include <action/IObstacleEffector.h>
#include <action/IParkingSpaceEffector.h>
#include <action/IRoadSignEffector.h>

using namespace taco;
using namespace std;
using namespace adtf_util;
using namespace adtf::ucom;
using namespace adtf_ddl;
using namespace adtf::streaming;
using namespace std::chrono;

ADTFPinMessageEncoder::ADTFPinMessageEncoder(
		IAction::Ptr action, ICarMetaModel::ConstPtr carMetaModel, MediaDescriptionManager::Ptr mediaDescManager)
	: _action(action), _mediaDescManager(mediaDescManager)
{
	for (const auto &servoDriveConfig : carMetaModel->getServoDriveConfigs()) {
		m_lastSteeringSentTime = std::chrono::system_clock::now();
		m_SteeringLastValue = -1000;
		_pins.emplace_back(servoDriveConfig->getEffectorName(), "tSignalValue");
	}

	for (const auto &config : carMetaModel->getMotorConfigs()) {
		m_lastSpeedSentTime = std::chrono::system_clock::now();
		m_SpeedLastValue = -1000;
		_pins.emplace_back(config->getEffectorName(), "tSignalValue");
	}

	for (const auto &config : carMetaModel->getLightConfigs()) {
		_pins.emplace_back(config->getEffectorName(), "tBoolSignalValue");
	}

	for (const auto &config : carMetaModel->getManeuverStatusConfigs()) {
		_pins.emplace_back(config->getEffectorName(), "tDriverStruct");
	}

	_pins.emplace_back("Position", "tPosition");
}

ADTFPinMessageEncoder::~ADTFPinMessageEncoder() = default;

int ADTFPinMessageEncoder::indexOfPin(OutputPin pin) const
{
	for (unsigned int i = 0; i < _pins.size(); i++) {
		if (_pins[i] == pin) {
			return i;
		}
	}

	return -1;
}

const std::vector<OutputPin> &ADTFPinMessageEncoder::getOutputPins()
{
	return _pins;
}

bool ADTFPinMessageEncoder::encode(const OutputPin &pin, tTimeStamp streamTime, object_ptr<ISample> &pWriteSample)
{
	int pinIndex = indexOfPin(pin);
	bool toTransmit = false;

	if (pinIndex >= 0) {
		tUInt32 timestamp = 0;
		alloc_sample(pWriteSample, streamTime);

		if (pin.signalType == "tSignalValue") {
			IDoubleValueEffector::Ptr valueEffector =
					boost::dynamic_pointer_cast<IDoubleValueEffector>(_action->getEffector(pin.name));
			if (valueEffector) {
				tFloat32 value = valueEffector->getValue();
				auto oCodec = _mediaDescManager->m_SignalValueSampleFactory.MakeCodecFor(pWriteSample);
				oCodec.SetElementValue(_mediaDescManager->m_ddlSignalValueId.timeStamp, timestamp);
				if (pin.name == AADCCar::MAIN_MOTOR) {
					//                    printf("%d Motor: %7.4f\n",
					//                           std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count(),
					//                           value);

					value *= 0.8;
					if (hasFloatChanged((tFloat32) value, m_SpeedLastValue) ||
							longEnoughSinceLastSent(m_lastSpeedSentTime)) {
						oCodec.SetElementValue(_mediaDescManager->m_ddlSignalValueId.value, value);
						m_lastSpeedSentTime = std::chrono::system_clock::now();
						m_SpeedLastValue = (tFloat32) value;
						toTransmit = true;
					}
				} else if (pin.name == AADCCar::STEERING_SERVO) {
					value *= -1;
					if (hasFloatChanged((tFloat32) value, m_SteeringLastValue) ||
							longEnoughSinceLastSent(m_lastSteeringSentTime)) {
						oCodec.SetElementValue(_mediaDescManager->m_ddlSignalValueId.value, value);
						m_lastSteeringSentTime = std::chrono::system_clock::now();
						m_SteeringLastValue = (tFloat32) value;
						//				    printf("%d Motor: %7.4f\n",
						//                           std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count(),
						//                           value);
						toTransmit = true;
					}
				}
			}
		} else if (pin.signalType == "tBoolSignalValue") {
			IBoolValueEffector::Ptr boolEffector = _action->getLightEffector(pin.name);
			if (boolEffector) {
				tBool value = boolEffector->getValue();
				tBool hasChanged = false;
				if (pin.name == AADCCar::HEAD_LIGHTS) {
					hasChanged = value != m_headLightLastValue;
					m_headLightLastValue = value;
				} else if (pin.name == AADCCar::INDICATOR_LIGHTS_LEFT) {
					hasChanged = value != m_leftLightLastValue;
					m_leftLightLastValue = value;
				} else if (pin.name == AADCCar::INDICATOR_LIGHTS_RIGHT) {
					hasChanged = value != m_rightLightLastValue;
					m_rightLightLastValue = value;
				} else if (pin.name == AADCCar::BRAKE_LIGHTS) {
					hasChanged = value != m_brakeLightLastValue;
					m_brakeLightLastValue = value;
				} else if (pin.name == AADCCar::WARN_LIGHTS) {
					hasChanged = value != m_warnlightLastValue;
					m_warnlightLastValue = value;
				} else if (pin.name == AADCCar::BACK_LIGHTS) {
					hasChanged = value != m_reverselightLastValue;
					m_reverselightLastValue = value;
				}
				if (hasChanged) {
					auto oCodec = _mediaDescManager->m_BoolSignalValueSampleFactory.MakeCodecFor(pWriteSample);
					oCodec.SetElementValue(_mediaDescManager->m_ddlBoolSignalValueId.ui32ArduinoTimestamp, timestamp);
					oCodec.SetElementValue(_mediaDescManager->m_ddlBoolSignalValueId.bValue, value);
					toTransmit = true;
				}
			}
		} else if (pin.signalType == "tDriverStruct") {
			IManeuverStatusEffector::Ptr valueEffector =
					boost::dynamic_pointer_cast<IManeuverStatusEffector>(_action->getEffector(pin.name));
			if (valueEffector && longEnoughSinceLastSent(m_lastStateSentTime)) {
				int state = valueEffector->getStatus();
				int maneuverId = valueEffector->getManeuverId();
				auto oCodec = _mediaDescManager->m_driverStructSampleFactory.MakeCodecFor(pWriteSample);
				oCodec.SetElementValue(_mediaDescManager->m_ddlDriverStructId.stateId, state);
				oCodec.SetElementValue(_mediaDescManager->m_ddlDriverStructId.maneuverEntry, maneuverId);
				m_lastStateSentTime = std::chrono::system_clock::now();
				toTransmit = true;
			}
		} else if (pin.signalType == "tPosition") {
			// TODO: may this should be limited to low frequence
			IPositionEffector::Ptr valueEffector =
					boost::dynamic_pointer_cast<IPositionEffector>(_action->getEffector(pin.name));
			if (valueEffector) {
				tFloat32 f32x = static_cast<tFloat32>(valueEffector->getPosX());
				tFloat32 f32y = static_cast<tFloat32>(valueEffector->getPosY());
				tFloat32 f32radius = NAN;
				tFloat32 f32speed = NAN;
				tFloat32 f32heading = static_cast<tFloat32>(valueEffector->getAngle());
				auto oCodec = _mediaDescManager->m_PositionSampleFactory.MakeCodecFor(pWriteSample);
				oCodec.SetElementValue(_mediaDescManager->m_ddlPositionIndex.x, f32x);
				oCodec.SetElementValue(_mediaDescManager->m_ddlPositionIndex.y, f32y);
				oCodec.SetElementValue(_mediaDescManager->m_ddlPositionIndex.radius, f32radius);
				oCodec.SetElementValue(_mediaDescManager->m_ddlPositionIndex.speed, f32speed);
				oCodec.SetElementValue(_mediaDescManager->m_ddlPositionIndex.heading, f32heading);
				toTransmit = true;
			}
		}
	}
	return toTransmit;
}

bool ADTFPinMessageEncoder::hasFloatChanged(tFloat32 value, tFloat32 oldValue)
{
	return fabs(oldValue - value) > 0.02;
}

bool ADTFPinMessageEncoder::longEnoughSinceLastSent(std::chrono::system_clock::time_point last)
{
	std::chrono::system_clock::time_point now = std::chrono::system_clock::now();

	long msSinceLast = std::chrono::duration_cast<std::chrono::milliseconds>(now - last).count();

	return msSinceLast > 1000;
}
