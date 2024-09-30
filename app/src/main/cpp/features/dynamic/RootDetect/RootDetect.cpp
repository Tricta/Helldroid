#include "RootDetect.h"
#include "../../../shared/obfuscate.h"

#include <stdio.h>
#include <unistd.h>

#include <fcntl.h>
#include <dirent.h>
#include <syscall.h>

#include <asm/unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/inotify.h>
#include <vector>
#include <sys/system_properties.h>

static const char *suBinaries[] = {
        AY_OBFUSCATE("/data/local/su"),
        AY_OBFUSCATE("/data/local/bin/su"),
        AY_OBFUSCATE("/data/local/xbin/su"),
        AY_OBFUSCATE("/sbin/su"),
        AY_OBFUSCATE("/su/bin/su"),
        AY_OBFUSCATE("/system/bin/su"),
        AY_OBFUSCATE("/system/bin/.ext/su"),
        AY_OBFUSCATE("/system/bin/failsafe/su"),
        AY_OBFUSCATE("/system/sd/xbin/su"),
        AY_OBFUSCATE("/system/usr/we-need-root/su"),
        AY_OBFUSCATE("/system/xbin/su"),
        AY_OBFUSCATE("/cache/su"),
        AY_OBFUSCATE("/data/su"),
        AY_OBFUSCATE("/dev/su)")
};

static const char *magiskMountsPath[] = {
       AY_OBFUSCATE("magisk"),
       AY_OBFUSCATE("core/mirror"),
       AY_OBFUSCATE("core/img")
};

bool RootDetect::execute() {
    return detectSuBinaries() | detectMagiskBinaries();
}

bool RootDetect::detectSuBinaries() {
    for (const char* su: suBinaries){
        //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: su binary opened: %s", su);
        int fileDescriptor = Helldroid_openat(O_RDONLY,0, su, AT_FDCWD);
        //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: fd root - %d", fileDescriptor);

        if (fileDescriptor >= 0) {
            __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: su binary file detected: %s", su);
            Helldroid_close(AY_OBFUSCATE("Hell"), fileDescriptor);
            return true;
        }
    }

    char* prop = AY_OBFUSCATE("ro.debuggable");
    const char* debuggable = detectSuBinariesProperties(prop);

    if (Helldroid_strcmp("1", debuggable) == 0) {
        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: su binary propertie detected: ro.debuggable - 1");
        return true;
    }

    const char* secure = detectSuBinariesProperties("ro.secure");

    if (Helldroid_strcmp("0", secure) == 0) {
        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: su binary propertie detected: ro.secure - 0");
        return true;
    }

    return false;
}

const char* RootDetect::detectSuBinariesProperties(char *input) {
    char sdk_ver_str[PROP_VALUE_MAX];
    const char* output;

    if (__system_property_get(input, sdk_ver_str)) {
        output = sdk_ver_str;
    } else {
        output = AY_OBFUSCATE("unknown");
    }

    return output;
}

bool RootDetect::detectMagiskBinaries(){
    int fd = Helldroid_openat(O_RDONLY,0, AY_OBFUSCATE("/proc/self/mounts"), AT_FDCWD);
    //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: Magisk binary fd: %d", fd);
    if(fd != -1){
        char buf[512];

        while(Helldroid_read(fd, buf, sizeof(buf)) > 0){
            for(const char* magiskMount: magiskMountsPath){
                if(HellLibC::Helldroid_strstr(buf, magiskMount)){
                    __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: Magisk binary detected: /proc/self/mounts/%s", magiskMount);
                    Helldroid_close(AY_OBFUSCATE("Hell"), fd);
                    return true;
                }
            }
        }
    }

    Helldroid_close(AY_OBFUSCATE("Hell"), fd);
    return false;
}