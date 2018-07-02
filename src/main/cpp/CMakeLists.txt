cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")

add_library(renderer SHARED
            renderer.c)

target_link_libraries(renderer
                      android
                      log
                      GLESv1_CM)
