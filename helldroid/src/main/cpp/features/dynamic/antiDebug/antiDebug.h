#ifndef ANTIDEBUG_H
#define ANTIDEBUG_H

#include <dirent.h>
#include <sys/types.h>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <thread>

#include "../../../syscalls/HellLibC.h"
#include "../../../shared/utils.h"

class antiDebug {
public:
    void execute();
private:
    void NativeDetect();
    void JavaDetect();
    bool checkTracerPID(int fd);

    void createTaskFilePath(const char* num, char* buffer, int sizeOfBuffer);
    size_t readLine(int fd, char *buf, size_t bufSize);
};

#endif // ANTIDEBUG_H