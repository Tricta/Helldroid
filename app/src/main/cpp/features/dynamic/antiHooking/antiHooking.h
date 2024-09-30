#ifndef HELLDROID_ANTIHOOKING_H
#define HELLDROID_ANTIHOOKING_H

#include <stdint.h>
#include <map>
#include <string>
#include <link.h>
#include <linux/fcntl.h>
#include <linux/elf.h>
#include <vector>
#include <android/log.h>

class antiHooking {
public:
    antiHooking(void (*onLibTampered)(const char *name, const char *section, uint32_t old_checksum, uint32_t new_checksum) = 0);
    void execute();
private:
    void calculateChecksum();
    void (*onLibTampered)(const char *name, const char *section, uint32_t old_checksum, uint32_t new_checksum);
};

#endif //HELLDROID_ANTIHOOKING_H