# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("helldroid")
enable_language(ASM)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        main.cpp

        shared/utils.cpp
        shared/utils.h

        syscalls/HellLibC.cpp
        syscalls/HellLibC.h

        features/dynamic/RootDetect/RootDetect.cpp
        features/dynamic/RootDetect/RootDetect.h
        features/dynamic/AntiFrida/AntiFrida.cpp
        features/dynamic/AntiFrida/AntiFrida.h
        features/dynamic/antiDebug/antiDebug.cpp
        features/dynamic/antiDebug/antiDebug.h
        features/dynamic/antiVirtualDevice/antiVirtualDevice.cpp
        features/dynamic/antiVirtualDevice/antiVirtualDevice.h
        features/dynamic/antiMemDump/antiMemDump.cpp
        features/dynamic/antiMemDump/antiMemDump.h
        features/dynamic/antiHooking/antiHooking.cpp
        features/dynamic/antiHooking/antiHooking.h

        features/static/ARThook/ARThook.cpp
        features/static/ARThook/ARThook.h
)

if(${CMAKE_ANDROID_ARCH_ABI} STREQUAL "arm64-v8a")
    target_sources(${CMAKE_PROJECT_NAME} PRIVATE syscalls/arm64.S)
elseif (${ANDROID_ABI} STREQUAL "x86_64")
    target_sources(${CMAKE_PROJECT_NAME} PRIVATE syscalls/x86_64.S)
endif()

set_source_files_properties(syscalls/x86_64.S PROPERTIES LANGUAGE ASM)

set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
        COMPILE_FLAGS "-nostdlib"
)

set_property(SOURCE syscalls/x86_64.S PROPERTY COMPILE_OPTIONS "-masm=intel")

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log)