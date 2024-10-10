#ifndef HELLDROID_ROOTDETECT_H
#define HELLDROID_ROOTDETECT_H

#include <android/log.h>
#include <fcntl.h>

#include "../../../syscalls/HellLibC.h"

class RootDetect{
public:
    bool execute();
private:
    bool detectSuBinaries();
    bool detectMagiskBinaries();
    const char* detectSuBinariesProperties(char *input);
};

#endif HELLDROID_ROOTDETECT_H