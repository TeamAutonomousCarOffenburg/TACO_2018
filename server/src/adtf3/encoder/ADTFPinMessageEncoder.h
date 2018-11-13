#pragma once

#include "../util/MediaDescriptionManager.h"
#include "OutputPin.h"
#include "action/IAction.h"
#include "action/IEffector.h"
#include "action/IManeuverStatusEffector.h"
#include "meta/ICarMetaModel.h"

#include <adtf_platform_inc.h>
//#include <adtf_plugin_sdk.h>
#include <adtf_filtersdk.h>
#include <boost/smart_ptr.hpp>
#include <vector>

namespace taco
{
/**
 * The ADTFPinMessageEncoder is responsible for mapping the internal effectors to output-pins a an ADTF-Filter.
 *
 * \author Stefan Glaser
 */
class ADTFPinMessageEncoder
{
  public:
	typedef boost::shared_ptr<ADTFPinMessageEncoder> Ptr;
	typedef boost::shared_ptr<const ADTFPinMessageEncoder> ConstPtr;

	ADTFPinMessageEncoder(
			IAction::Ptr action, ICarMetaModel::ConstPtr carMetaModel, MediaDescriptionManager::Ptr _mediaDescManager);

	virtual ~ADTFPinMessageEncoder();

	virtual int indexOfPin(OutputPin pin) const;
	virtual const std::vector<OutputPin> &getOutputPins();
	virtual bool encode(const OutputPin &pin, tTimeStamp streamTime, object_ptr<ISample> &pWriteSample);

  protected:
	/** The Action component. */
	IAction::Ptr _action;

	MediaDescriptionManager::Ptr _mediaDescManager;

	/** The outout pins of the encoder. */
	std::vector<taco::OutputPin> _pins;

  private:
	std::chrono::system_clock::time_point m_lastSpeedSentTime;
	tFloat32 m_SpeedLastValue;

	std::chrono::system_clock::time_point m_lastSteeringSentTime;
	tFloat32 m_SteeringLastValue;

	tBool m_headLightLastValue;
	tBool m_brakeLightLastValue;
	tBool m_reverselightLastValue;
	tBool m_leftLightLastValue;
	tBool m_rightLightLastValue;
	tBool m_warnlightLastValue;

	std::chrono::system_clock::time_point m_lastStateSentTime;

	bool hasFloatChanged(tFloat32 value, tFloat32 oldValue);
	bool longEnoughSinceLastSent(std::chrono::system_clock::time_point last);
};
}
