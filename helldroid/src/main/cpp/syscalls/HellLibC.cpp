#include <android/log.h>
#include "HellLibC.h"

__attribute__((always_inline)) static inline uint32_t crc32(uint8_t *data, size_t size) {
    uint32_t crc = 0xFFFFFFFF;

    for (size_t i = 0; i < size; i++) {
        crc ^= data[i];
        for (size_t j = 0; j < 8; j++) {
            crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
        }
    }

    return ~crc;
}

const char* HellLibC::Helldroid_strstr(const char* mem, const char* key) {
    if (*key == '\0')
        return mem;

    while (*mem != '\0') {
        const char* h = mem;
        const char* n = key;

        while (*n != '\0' && *h == *n) {
            h++;
            n++;
        }

        if (*n == '\0')
            return mem;

        mem++;
    }

    return nullptr;
}

const char* HellLibC::Helldroid_strstr2(const char* mem, const char* key) {
    __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: redirecting function...");
    if (*key == '\0')
        return mem;

    while (*mem != '\0') {
        const char* h = mem;
        const char* n = key;

        while (*n != '\0' && *h == *n) {
            h++;
            n++;
        }

        if (*n == '\0')
            return mem;

        mem++;
    }

    return nullptr;
}