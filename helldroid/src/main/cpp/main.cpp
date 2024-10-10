#include <jni.h>
#include <thread>
#include <android/native_activity.h>
#include <dlfcn.h>

#include "features/dynamic/RootDetect/RootDetect.h"
#include "features/dynamic/AntiFrida/AntiFrida.h"
#include "shared/utils.h"
#include "features/dynamic/antiDebug/antiDebug.h"
#include "features/dynamic/antiVirtualDevice/antiVirtualDevice.h"
#include "features/dynamic/antiMemDump/antiMemDump.h"
#include "features/dynamic/antiHooking/antiHooking.h"

#include "features/static/ARThook/ARThook.h"

void helldroid(antiHooking AntiHooking){
    antiVirtualDevice AntiVirtualDevice;
    std::thread antiVirtualDeviceThread(&antiVirtualDevice::execute, &AntiVirtualDevice);
    antiVirtualDeviceThread.detach();

    antiDebug antidebug;
    std::thread antiDebugThread(&antiDebug::execute, &antidebug);
    antiDebugThread.detach();

    AntiFrida antiFrida;
    antiFrida.executeGadget();

    antiMemDump AntiMemDump;
    std::thread AntiMemDumpThread(&antiMemDump::execute, &AntiMemDump);
    AntiMemDumpThread.detach();

    RootDetect rootDetect;
    std::future<bool> DetectRoot = std::async(std::launch::async, &RootDetect::execute, &rootDetect);
    if (DetectRoot.get()) {
        utils::killProcess();
    }

    std::thread antiFridaThread(&AntiFrida::executeServer, &antiFrida);
    antiFridaThread.detach();

    std::thread AntiHookingThread(&antiHooking::execute, &AntiHooking);
    AntiHookingThread.detach();
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    antiHooking AntiHooking;
    JNIEnv* env;
    if(vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK){
        return -1;
    }

    jclass helldroidClass = env->FindClass("com/lib/helldroid/Helldroid");
    if(!helldroidClass){
        return JNI_ERR;
    }

    JNINativeMethod hellRegisterMethod = {
        "nativeRegister", "(Ljava/lang/Class;)V",  (void*) registerMethods
    };

    if(env->RegisterNatives(helldroidClass, &hellRegisterMethod, 1) != JNI_OK){
        return JNI_ERR;
    }

    helldroid(AntiHooking);

    return JNI_VERSION_1_6;
}