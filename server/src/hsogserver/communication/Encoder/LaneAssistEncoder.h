#pragma once

#include "EncoderUtilities.h"
#include "IEncoder.h"
#include "detection/ILaneDetection.h"
#include <perception/ILaneMiddlePerceptor.h>

class LaneAssistEncoder : public PerceptorEncoder<taco::ILaneMiddlePerceptor>
{
  private:

	void writeValue(Writer<StringBuffer> *writer, taco::ILaneMiddlePerceptor::ConstPtr perceptor)
	{
		LaneMiddle middle = perceptor->getLaneMiddle();
		writer->Key("valid");
		writer->Bool(middle.valid);
		writer->Key("confidence");
		writer->Double(middle.confidence);
		writer->Key("invalidSince");
		writer->Int(middle.invalidSince);
		writer->Key("wantedX");
		writer->Int(middle.wantedX);
		writer->Key("middleX");
		writer->Int(middle.middleX);
		writer->Key("middleXCamera");
		EncoderUtilities::encodeVector3d(writer, middle.middleXCamera);
		writer->Key("rightLineX");
		writer->Int(middle.rightLineX);
		writer->Key("middleLineX");
		writer->Int(middle.middleLineX);
		writer->Key("leftLineX");
		writer->Int(middle.leftLineX);
		writer->Key("scanRightStartX");
		writer->Int(middle.scanRightStartX);
		writer->Key("scanRightStartY");
		writer->Int(middle.scanRightStartY);
		writer->Key("scanRightEndX");
		writer->Int(middle.scanRightEndX);
		writer->Key("inCrossing");
		writer->Bool(middle.inCrossing);
	}

  public:
	LaneAssistEncoder(taco::IPerception::Ptr p, std::string perceptorName) : PerceptorEncoder(p, perceptorName)
	{
	}

	virtual ~LaneAssistEncoder(){};
};
