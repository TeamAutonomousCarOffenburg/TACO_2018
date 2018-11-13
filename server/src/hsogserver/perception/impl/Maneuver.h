#pragma once

#include <string>

namespace taco
{
class Maneuver
{
  public:
	Maneuver(const std::string &driveInstruction, const int16_t &sector, const int16_t &maneuverID)
	{
		_driveInstruction = driveInstruction;
		_sector = sector;
		_maneuverID = maneuverID;
		_parkingSpace = -1;
	};

	Maneuver(const std::string &driveInstruction, const int16_t &sector, const int16_t &maneuverID,
			const int16_t &parkingSpace)
	{
		_driveInstruction = driveInstruction;
		_sector = sector;
		_maneuverID = maneuverID;
		_parkingSpace = parkingSpace;
	};

	virtual ~Maneuver(){};

	virtual const std::string &getDriveInstruction() const
	{
		return _driveInstruction;
	}

	virtual const std::int16_t &getSector() const
	{
		return _sector;
	}

	virtual const std::int16_t &getManeuverId() const
	{
		return _maneuverID;
	}

	virtual const std::int16_t &getParkingSpace() const
	{
		return _parkingSpace;
	}

  protected:
	std::string _driveInstruction;
	std::int16_t _sector;
	std::int16_t _maneuverID;
	std::int16_t _parkingSpace;
};
}
