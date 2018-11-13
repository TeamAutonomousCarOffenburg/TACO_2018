cmake_minimum_required(VERSION 2.8.4)

add_subdirectory(hsogserver)
if(${TACO_CONFIG} STREQUAL TACO_2018)
    add_subdirectory(adtf3)
else()
    add_subdirectory(adtf)
endif()