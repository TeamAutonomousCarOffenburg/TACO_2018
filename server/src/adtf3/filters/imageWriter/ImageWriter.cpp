#include "ImageWriter.h"
#include "ADTF3_OpenCV_helper.h"

using namespace std;

#define FILTER_PROPERTY_NAME_PATH "Folder for the images"
#define FILTER_PROPERTY_NAME_FPS "Frames per second"

ADTF_TRIGGER_FUNCTION_FILTER_PLUGIN(
		CID_TACO_IMAGE_WRITER, FILTER_CLASS_LABEL, ImageWriter, adtf::filter::pin_trigger({"in"}));

ImageWriter::ImageWriter()
{
	firstFrame = true;
	RegisterPropertyVariable(FILTER_PROPERTY_NAME_PATH, propertyPath);
	RegisterPropertyVariable(FILTER_PROPERTY_NAME_FPS, propertyFPS);

	// create and set inital input format type
	m_InPinVideoFormat.m_strFormatName = ADTF_IMAGE_FORMAT(RGB_24);
	adtf::ucom::object_ptr<IStreamType> pTypeInput = adtf::ucom::make_object_ptr<cStreamType>(stream_meta_type_image());
	set_stream_type_image_format(*pTypeInput, m_InPinVideoFormat);
	// Register input pin
	Register(m_oReaderVideo, "in", pTypeInput);

	// register callback for type changes
	m_oReaderVideo.SetAcceptTypeCallback(
			[this](const adtf::ucom::ant::iobject_ptr<const adtf::streaming::ant::IStreamType> &pType) -> tResult {
				return ChangeType(m_oReaderVideo, m_InPinVideoFormat, *pType.Get(), m_oWriterVideo);
			});
}

ImageWriter::~ImageWriter()
{
}

tResult ImageWriter::Configure()
{
	fps = propertyFPS;
	path = propertyPath;
	msPerFrame = (1.0 / (double) fps) * 1000.0;

	//    std::cout << "Write images to " << path << " with " << fps << " FPS..." << std::endl;

	// get clock object
	RETURN_IF_FAILED(_runtime->GetObject(m_pClock));

	RETURN_NOERROR;
}

tResult ImageWriter::Process(tTimeStamp tmTimeOfTrigger)
{
	std::chrono::system_clock::time_point now = std::chrono::system_clock::now();

	long msSinceLast = std::chrono::duration_cast<std::chrono::milliseconds>(now - lastUpdate).count();
	if (msSinceLast >= msPerFrame) {
		object_ptr<const ISample> pReadSample;
		if (IS_OK(m_oReaderVideo.GetLastSample(pReadSample))) {
			object_ptr_shared_locked<const ISampleBuffer> pReadBuffer;
			// lock read buffer
			if (IS_OK(pReadSample->Lock(pReadBuffer))) {
				IplImage *img = cvCreateImageHeader(
						cvSize(m_InPinVideoFormat.m_ui32Width, m_InPinVideoFormat.m_ui32Height), IPL_DEPTH_8U, 3);

				img->imageData = (char *) pReadBuffer->GetPtr();

				Mat image(cvarrToMat(img));

				vector<int> params;
				params.push_back(CV_IMWRITE_JPEG_QUALITY);
				params.push_back(100);

				unsigned long milliseconds_since_epoch = static_cast<unsigned long>(
						std::chrono::system_clock::now().time_since_epoch() / std::chrono::milliseconds(1));

				stringstream ss;
				ss << path << "/" << milliseconds_since_epoch << ".jpg";
				string filename = ss.str();

				// custom created images has to be converted before imwrite() because of openCV's BGR order
				Mat destMat;
				cvtColor(image, destMat, CV_RGB2BGR);

				imwrite(filename, destMat, params);
				lastUpdate = now;
			}
		}
	}
	RETURN_NOERROR;
}
