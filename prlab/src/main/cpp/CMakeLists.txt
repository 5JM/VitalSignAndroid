# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("prlab")

set(CPP_DIR ${CMAKE_SOURCE_DIR})
set(OpenCV_DIR ${CPP_DIR}/opencv)
set(ONNX_DIR ${CPP_DIR}/onnxruntime)

add_library( lib_opencv SHARED IMPORTED )
add_library( lib_onnxruntime SHARED IMPORTED )

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CPP_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)
set_target_properties(
        lib_onnxruntime
        PROPERTIES
        IMPORTED_LOCATION ${CPP_DIR}/libs/${ANDROID_ABI}/libonnxruntime.so
)
#set_target_properties(
#        lib_onnxruntime
#        PROPERTIES
#        IMPORTED_LOCATION ${CPP_DIR}/libs/${ANDROID_ABI}/libonnxruntime4j_jni.so
#)

include_directories(native-lib PRIVATE ${OpenCV_DIR}/include)
include_directories(${OpenCV_DIR}/include)

include_directories(native-lib PRIVATE ${ONNX_DIR}/include)
include_directories(${ONNX_DIR}/include)

set(CMAKE_VERBOSE_MAKEFILE on)

set( RPPG_CPP_SOURCE_FILES
        labdata.h
        ffttools.h
        recttools.h
        filter.h filter.cpp
        fhog.h fhog.cpp
        kcftracker.h kcftracker.cpp
        ultraface.h ultraface.cpp
        detector.h detector.cpp
        skinseg.h skinseg.cpp
        rppg.h rppg.cpp
        peakdetect.h peakdetect.cpp
        hrv.h hrv.cpp
        onnxruntime_inference.h onnxruntime_inference.cpp
        onnxruntime_bloodpresure.h onnxruntime_bloodpresure.cpp
        preprocess.h preprocess_bp.h
        postprocess.h
        )
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        prlab
        # Sets the library as a shared library.
        SHARED
        ${RPPG_CPP_SOURCE_FILES}
        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
find_library( # Sets the name of the path variable.
        jnigraphics-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics )
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        prlab

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${jnigraphics-lib}
        lib_opencv
        lib_onnxruntime
        )