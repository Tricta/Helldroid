#ifndef UTILS_H
#define UTILS_H

#include <jni.h>
#include <unistd.h>
#include <android/native_activity.h>

#include "../syscalls/HellLibC.h"

class utils {
public:
    void closeAPK(JNIEnv* env, jobject obj, const char* msg);
    void popupAPK(JNIEnv* env, ANativeActivity* activity, const char* msg);
    static void killProcess();
};

#endif UTILS_H