cmake_minimum_required(VERSION 2.8)

find_package(GTest)

if(GTEST_FOUND)
	find_package(OpenCV REQUIRED)

	include_directories(${Boost_INCLUDE_DIRS})
	include_directories(${OpenCV_INCLUDE_DIR})

	add_executable(laneAssistTests LaneAssistTest.cpp)
	target_link_libraries(laneAssistTests gtest gtest_main ${TACO_DETECTION} ${TACO_LIB} ${OpenCV_LIBS})
	add_test(AllTestsInFoo laneAssistTests)

	add_executable(laneCutterTests LaneCutterTest.cpp)
	target_link_libraries(laneCutterTests gtest gtest_main ${TACO_DETECTION} ${TACO_LIB} ${OpenCV_LIBS})
	add_test(AllTestsInFoo laneCutterTests)

endif()


