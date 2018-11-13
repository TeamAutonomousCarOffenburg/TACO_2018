cmake_minimum_required(VERSION 2.8)

# needed for aruco on car, since its is in /opt...
set(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)

find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})
include_directories(../hsogserver/)

set(RUNTIME_FILTER_NAME taco_RuntimeService)

#included header files
set (HEADERS
        filters/RuntimeService.h
        encoder/ADTFPinMessageEncoder.h
        encoder/OutputPin.h
        util/MediaDescriptionManager.h
        )

#included source files
set (SOURCES
        filters/RuntimeService.cpp
        encoder/ADTFPinMessageEncoder.cpp
        util/MediaDescriptionManager.cpp
        )

adtf_add_filter(${RUNTIME_FILTER_NAME}
                ${SOURCES}
                ${HEADERS})

target_link_libraries(${RUNTIME_FILTER_NAME} PRIVATE ${TACO_LIB})
target_link_libraries(${RUNTIME_FILTER_NAME} PRIVATE ${TACO_DETECTION})

target_link_libraries(${RUNTIME_FILTER_NAME} PUBLIC ${OpenCV_LIBS})

# create plugin and copy it to the aadc library
adtf_install_target(${RUNTIME_FILTER_NAME} ${AADC_INSTALL_DIR})

# fix for further building, cause the prefix is set by AADC and does not fit to our aadcUser architecture
set(CMAKE_INSTALL_PREFIX "/home/aadc/AADC/_install/linux64")

# create the plugin-description file
adtf_create_plugindescription(
        TARGET
            ${RUNTIME_FILTER_NAME}
        PLUGIN_SUBDIR
            bin
)

# TODO add again once other things compile
#set(RATE_LIMIT_FILTER_NAME taco_RateLimit)
#adtf_add_filter(${RATE_LIMIT_FILTER_NAME}
#        encoder/ADTFPinMessageEncoder.h
#        encoder/ADTFPinMessageEncoder.cpp
#        encoder/OutputPin.h
#        filters/RateLimit.h
#        filters/RateLimit.cpp
#        )
#target_link_libraries(${RATE_LIMIT_FILTER_NAME} ${TACO_LIB})
#adtf_install_plugin(${RATE_LIMIT_FILTER_NAME} ${AADC_INSTALL_DIR})

#set(IMAGE_WRITER_FILTER_NAME taco_ImageWriter)
#MESSAGE(STATUS "FILTER NAME " ${IMAGE_WRITER_FILTER_NAME})
#adtf_add_filter(${IMAGE_WRITER_FILTER_NAME}
#        filters/imageWriter/ImageWriter.h
#        filters/imageWriter/ImageWriter.cpp
#        )
#
#target_link_libraries(${IMAGE_WRITER_FILTER_NAME} PUBLIC ${OpenCV_LIBS})
#adtf_install_plugin(${IMAGE_WRITER_FILTER_NAME} ${AADC_INSTALL_DIR})
#if (${TACO_CONFIG} STREQUAL TACO_2017)
add_subdirectory(filters/fisheyeUndistortionROI)
add_subdirectory(filters/imageWriter)
add_subdirectory(filters/cameraImageRelay)
#endif ()