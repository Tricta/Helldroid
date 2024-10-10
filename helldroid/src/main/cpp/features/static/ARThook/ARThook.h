#ifndef HELLDROID_ARTHOOK_H
#define HELLDROID_ARTHOOK_H

#include <jni.h>
#include <android/log.h>
#include <sys/mman.h>
#include <string.h>

extern "C" JNIEXPORT void JNICALL ReplaceMethodByObject(JNIEnv* env, jobject /* this */, jobject targetMethod, jobject newMethod);
JNIEXPORT void JNICALL registerMethods(JNIEnv* env, jclass /* this */, jclass targetClass);

#endif //HELLDROID_ARTHOOK_H
