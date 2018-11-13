cmake_minimum_required(VERSION 2.8)

set(TACO_DETECTION detection)

add_library(${TACO_DETECTION}
        ILaneDetection.h
        IVisibleObject.h
        IRoadSign.h
        RoadSign.h
        RoadSign.cpp
        VisibleObject.h
        VisibleObject.cpp

        lanedetection/EdgeDetector.h
        lanedetection/LaneDetection.cpp
        lanedetection/LaneDetection.h
        lanedetection/LaneDetectionConfiguration.h
        lanedetection/LaneDetectionRenderer.cpp
        lanedetection/LaneDetectionRenderer.h
        lanedetection/LaneMiddle.h
        lanedetection/Line.cpp
        lanedetection/Line.h
        lanedetection/LaneAssist.cpp
        lanedetection/LaneAssist.h
        lanedetection/LaneCutter.cpp
        lanedetection/LaneCutter.h
        lanedetection/LaneCutterResult.h
        lanedetection/LaneCutterRenderer.cpp
        lanedetection/LaneCutterRenderer.h
        lanedetection/PixelToCamera.cpp
        lanedetection/PixelToCamera.h
        lanedetection/ScanLine.cpp
        lanedetection/ScanLine.h
        lanedetection/WideAngleCameraConfig.h

        signdetection/SignDetection.h
        signdetection/SignDetection.cpp
        )

include_directories(.)


target_link_libraries(${TACO_DETECTION} ${OpenCV_LIBS})
target_link_libraries(${TACO_DETECTION} ${Boost_LIBRARIES})
target_link_libraries(${TACO_DETECTION} ${TACO_LIB})
target_link_libraries(${TACO_DETECTION} ${aruco_LIBS})

set(LIBRARY_OUTPUT_PATH .)

add_subdirectory(lanedetection/test)

if(aruco_FOUND)
    add_definitions(-DARUCO_FOUND=1)
endif()

if(ADTF_FOUND)
    adtf_install_target(${TACO_DETECTION} ${AADC_INSTALL_DIR_LIBS})
endif()
