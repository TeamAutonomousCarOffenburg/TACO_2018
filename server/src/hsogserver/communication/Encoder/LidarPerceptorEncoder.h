#pragma once

#include "PerceptorEncoder.h"
#include "perception/impl/LidarValue.h"
#include <perception/ILidarPerceptor.h>

using namespace taco;

class LidarPerceptorEncoder : public PerceptorEncoder<taco::ILidarPerceptor>
{
  public:
	LidarPerceptorEncoder(taco::IPerception::Ptr p, std::string perceptorName) : PerceptorEncoder(p, perceptorName)
	{
	}

	virtual ~LidarPerceptorEncoder()
	{
	}

  private:
	void writeValue(Writer<StringBuffer> *writer, taco::ILidarPerceptor::ConstPtr perceptor)
	{
		writer->Key("values");
		writer->StartArray();
		for (const taco::LidarValue lidarValue : perceptor->getLidarValues()) {
			int angle = lidarValue.getAngle();
			// we have our zero angle in front, +90 left, -90 right
			// ADTF gives us 270 left, 360/0 in front and 90 to the right
			if (angle > 90) {
				int index = angle - 270;
				angle -= (180 + (index * 2));
			} else {
				angle *= -1;
			}

			int distance = (int) lidarValue.getDistance();
			// only send data if lidar detection is closer then 3m
			// distance 0.0 means no detection ->12m
			if (distance > 0 && distance < 3000) {
				writer->StartObject();
				writer->Key("angle");
				writer->Int(angle);
				writer->Key("distance");
				// unit is in mm, client needs mtr
				writer->Double((double) distance / 1000.0);
				writer->EndObject();
			}
		}
		writer->EndArray();
	}
};