#ifndef HELLDROID_ANTIMEMDUMP_H
#define HELLDROID_ANTIMEMDUMP_H

#include "../../../syscalls/HellLibC.h"

class antiMemDump {
public:
    void execute();
private:
    void antiDump();
    void createTaskFilePath(const char* num, const char* memPath, char* buffer, int sizeOfBuffer);
};


#endif //HELLDROID_ANTIMEMDUMP_H
