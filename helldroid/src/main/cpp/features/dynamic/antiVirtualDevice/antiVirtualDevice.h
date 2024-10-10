#ifndef HELLDROID_ANTIVIRTUALDEVICE_H
#define HELLDROID_ANTIVIRTUALDEVICE_H

#include <stddef.h>
#include <asm-generic/fcntl.h>
#include <linux/fcntl.h>
#include <android/log.h>
#include <vector>
#include <sys/system_properties.h>

#include "../../../syscalls/HellLibC.h"

class antiVirtualDevice {
public:
    void execute();
private:
    void antiAVDFiles();
    bool checkFiles(const char* file);
    void antiAVDProperties();
};

#endif //HELLDROID_ANTIVIRTUALDEVICE_H
