#pragma once

#include "../perception/IPerception.h"
#include "PackageDecoder.h"
#include "PackageEncoder.h"
#include <action/IAction.h>
#include <chrono>
#include <string>
#include <thread>

static const int PREFIX_SIZE = 4;
static const std::chrono::duration<double, std::milli> RESPONSIVENESS_THRESHOLD = std::chrono::milliseconds(100);

class Communication
{
  public:
	void start();
	void stop();
	Communication(taco::ICarMetaModel::Ptr carMetaModel, taco::IAction::Ptr action, taco::IPerception::Ptr perception,
			uint16_t port = 63236);
	~Communication();
	bool update();
	void receive();
	bool isClientConnected();
	bool isClientDisconnected();

  private:
	taco::ICarMetaModel::Ptr _carMetaModel;
	taco::IAction::Ptr _action;
	PackageDecoder *decoder;
	PackageEncoder *encoder;
	bool serverRunning;
	bool clientResponsive;
	bool clientHasDisconnected;
	uint16_t port;
	int socketFD;
	int clientFD;
	std::thread receiveThread;
	std::chrono::system_clock::time_point currentTime;
	std::chrono::system_clock::time_point lastClientConnectionTime;
	void openSocket();
	void checkResponsiveness(std::chrono::system_clock::time_point lastReceiveTime);
	void onClientDisconnected();

	void log(std::string message);
	void error(std::string message);
};
