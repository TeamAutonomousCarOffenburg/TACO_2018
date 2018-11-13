#pragma once

#include "PerceptorEncoder.h"
#include <perception/IPositionPerceptor.h>

class PositionPerceptorEncoder : public PerceptorEncoder<taco::IPositionPerceptor>
{
  public:
	PositionPerceptorEncoder(taco::IPerception::Ptr p, std::string perceptorName) : PerceptorEncoder(p, perceptorName)
	{
	}

	virtual ~PositionPerceptorEncoder()
	{
	}

  private:
	void writeValue(Writer<StringBuffer> *writer, taco::IPositionPerceptor::ConstPtr perceptor)
	{
		writer->Key("posX");
		writer->Double(perceptor->getX());
		std::cout << "write Value to client " << perceptor->getX() << std::endl;
		writer->Key("posY");
		writer->Double(perceptor->getY());
		writer->Key("posAngle");
		writer->Double(perceptor->getAngle());
	}
};
