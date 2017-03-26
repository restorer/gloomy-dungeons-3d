cmake_minimum_required(VERSION 3.4.1)

add_library(zameJniRenderer SHARED zameJniRenderer.c)

find_library(lib-log log)
find_library(lib-gl GLESv1_CM)

target_link_libraries(zameJniRenderer ${lib-log} ${lib-gl})
