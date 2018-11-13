#include "CameraImageRelay.h"
#include "ADTF3_OpenCV_helper.h"
#include <iostream>

using namespace std;

#define FILTER_PROPERTY_NAME_PORT "Destination Server PORT"
#define FILTER_PROPERTY_NAME_FPS "Frames per second"

ADTF_TRIGGER_FUNCTION_FILTER_PLUGIN(
		FILTER_CLASS_ID, FILTER_CLASS_LABEL, CameraImageRelay, adtf::filter::pin_trigger({"in"}));

CameraImageRelay::CameraImageRelay()
{
	firstFrame = true;
	RegisterPropertyVariable(FILTER_PROPERTY_NAME_PORT, propertyPort);
	RegisterPropertyVariable(FILTER_PROPERTY_NAME_FPS, propertyFPS);

	// create and set inital input format type
	m_InPinVideoFormat.m_strFormatName = ADTF_IMAGE_FORMAT(RGB_24);
	adtf::ucom::object_ptr<IStreamType> pTypeInput = adtf::ucom::make_object_ptr<cStreamType>(stream_meta_type_image());
	set_stream_type_image_format(*pTypeInput, m_InPinVideoFormat);
	// Register input pin
	Register(m_oReaderVideo, "in", pTypeInput);

	Register(m_oWriterVideo, "out", pTypeInput);

	// register callback for type changes
	m_oReaderVideo.SetAcceptTypeCallback(
			[this](const adtf::ucom::ant::iobject_ptr<const adtf::streaming::ant::IStreamType> &pType) -> tResult {
				return ChangeType(m_oReaderVideo, m_InPinVideoFormat, *pType.Get(), m_oWriterVideo);
			});
}

CameraImageRelay::~CameraImageRelay() = default;

tResult CameraImageRelay::Configure()
{
	//    path = propertyPath;
	msPerFrame = (1.0 / (double) propertyFPS) * 1000.0;
	serverPort = propertyPort;

	startServer();

	// get clock object
	RETURN_IF_FAILED(_runtime->GetObject(m_pClock));

	RETURN_NOERROR;
}

tResult CameraImageRelay::Process(tTimeStamp tmTimeOfTrigger)
{
	if (serverStarted && clientFD == -1) {
		clientFD = accept(socketFD, nullptr, nullptr);
		if (clientFD == -1 && (errno != EAGAIN || errno != EWOULDBLOCK)) {
			error("accept()");
			RETURN_NOERROR;
		}

		if (clientFD != -1) {
			log("Client connected");
		}
	}

	if (clientFD != -1) {
		std::chrono::system_clock::time_point now = std::chrono::system_clock::now();

		long msSinceLast = std::chrono::duration_cast<std::chrono::milliseconds>(now - lastUpdate).count();

		if (msSinceLast >= msPerFrame) {
			object_ptr<const ISample> pReadSample;
			if (IS_OK(m_oReaderVideo.GetLastSample(pReadSample))) {
				object_ptr_shared_locked<const ISampleBuffer> pReadBuffer;
				if (IS_OK(pReadSample->Lock(pReadBuffer))) {
					IplImage *img = cvCreateImageHeader(
							cvSize(m_InPinVideoFormat.m_ui32Width, m_InPinVideoFormat.m_ui32Height), IPL_DEPTH_8U, 3);

					img->imageData = (char *) pReadBuffer->GetPtr();

					Mat image(cvarrToMat(img));

					vector<int> params;
					params.push_back(CV_IMWRITE_JPEG_QUALITY);
					params.push_back(100);

					Mat destMat;
					cvtColor(image, destMat, CV_RGB2BGR);

					image = destMat.reshape(0, 1);
					size_t image_size = image.total() * image.elemSize();
					ssize_t num_bytes = send(clientFD, image.data, image_size, 0);
					writeMatToPin(m_oWriterVideo, image, m_pClock->GetStreamTime());
					lastUpdate = now;
					if (num_bytes == -1) {
						log("Client disconnected");
						shutdown(clientFD, SHUT_RDWR);
						close(clientFD);
						clientFD = -1;
					}
				}
			}
		}
	}

	RETURN_NOERROR;
}

void CameraImageRelay::Destroy()
{
	std::cout << "Filter is shutting down" << std::endl;
	close(socketFD);
	close(clientFD);
}

int CameraImageRelay::startServer()
{
	socketFD = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (socketFD == -1) {
		error("socket()");
		return 1;
	}
	fcntl(socketFD, F_SETFL, O_NONBLOCK);

	int enable = 1;
	if (setsockopt(socketFD, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0) {
		error("setsockopt(SO_REUSEADDR)");
	}

	struct sockaddr_in address;
	address.sin_family = AF_INET;
	address.sin_addr.s_addr = INADDR_ANY;
	address.sin_port = htons(serverPort);

	if (bind(socketFD, (struct sockaddr *) &address, sizeof(address)) == -1) {
		error("bind()");
		return 2;
	}

	if (listen(socketFD, 5) == -1) {
		error("listen()");
		return 3;
	}

	clientFD = -1;
	serverStarted = true;

	return 0;
}

void CameraImageRelay::log(std::string message)
{
	cout << "[CameraImageRelay] " << message << endl;
}

void CameraImageRelay::error(std::string message)
{
	cerr << "[CameraImageRelay] " << message << " failed: " << strerror(errno) << endl;
}
