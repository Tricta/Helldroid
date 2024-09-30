#include <fcntl.h>
#include "../../../syscalls/HellLibC.h"
#include "antiHooking.h"
#include "../../../shared/utils.h"
#include "../../../shared/obfuscate.h"

#define Elf_Ehdr Elf64_Ehdr
#define Elf_Shdr Elf64_Shdr
#define Elf_Sym  Elf64_Sym

std::map<std::string, std::map<std::string, uint32_t>> checksums;
std::map<std::string, std::map<std::string, uint32_t>> last_checksums;

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

antiHooking::antiHooking(void (*callback)(const char *name, const char *section, uint32_t old_checksum, uint32_t new_checksum)) : onLibTampered(callback){
    dl_iterate_phdr([](struct dl_phdr_info *info, size_t size, void *data) -> int {
        //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: %s", info->dlpi_name);

        const char *moduleName = info->dlpi_name;
        const char *subString = ".so";
        bool foundSo = false;

        while (*moduleName != '\0') {
            const char *h = moduleName;
            const char *n = subString;

            while (*n != '\0' && *h == *n) {
                h++;
                n++;
            }

            if (*n == '\0') {
                foundSo = true;
                break;
            }

            moduleName++;
        }

        moduleName = info->dlpi_name;
        subString = "helldroid";
        bool foundLib = false;

        while (*moduleName != '\0') {
            const char *h = moduleName;
            const char *n = subString;

            while (*n != '\0' && *h == *n) {
                h++;
                n++;
            }

            if (*n == '\0') {
                foundLib = true;
                break;
            }

            moduleName++;
        }

        if (!foundSo || !foundLib) {
            return 0;
        }

        int fd = Helldroid_openat(O_RDONLY,0, info->dlpi_name, AT_FDCWD);

        if (fd < 0) {
            return 0;
        }

        Elf_Ehdr ehdr;
        Helldroid_read(fd, &ehdr, sizeof(Elf_Ehdr));

        Elf_Shdr shdr;
        Helldroid_lseek(fd, ehdr.e_shoff + ehdr.e_shstrndx * sizeof(Elf_Shdr), SEEK_SET);
        Helldroid_read(fd, &shdr, sizeof(Elf_Shdr));

        char* shstrtab = new char[shdr.sh_size];
        Helldroid_lseek(fd, shdr.sh_offset, SEEK_SET);
        Helldroid_read(fd, shstrtab, shdr.sh_size);

        Helldroid_lseek(fd, ehdr.e_shoff, SEEK_SET);
        for(int i = 0; i < ehdr.e_shnum; i++){
            Helldroid_read(fd, &shdr, sizeof(Elf_Shdr));
            const char *name = shstrtab + shdr.sh_name;

            if(shdr.sh_type == SHT_PROGBITS){
                if ((shdr.sh_flags & (SHF_EXECINSTR | SHF_ALLOC)) == (SHF_EXECINSTR | SHF_ALLOC)) {
                    char *tmp = new char[shdr.sh_size];
                    Helldroid_lseek(fd, shdr.sh_addr, SEEK_SET);
                    Helldroid_read(fd, tmp, shdr.sh_size);

                    uint32_t checksum = ((std::map<std::string, std::map<std::string, uint32_t>> *) data)->operator[](info->dlpi_name)[name] = crc32((uint8_t *) tmp, shdr.sh_size);

                    //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "AntiLibPatch::AntiLibPatch this->m_checksums[%s][%s]: 0x%08X", info->dlpi_name, name, checksum);
                    delete[] tmp;
                }
            }
        }

        delete[] shstrtab;
        Helldroid_close("Hell", fd);
        return 0;
    }, &checksums);
}

void antiHooking::execute() {
    while(true){
        calculateChecksum();

        int seconds = 1;

        struct timespec {
            uint64_t tv_sec;
            uint64_t tv_nsec;
        } req;

        req.tv_sec = seconds;
        req.tv_nsec = 0;

#if defined(__x86_64__)
        __asm__ volatile (
                "mov $35, %%rax\n"
                "mov %0, %%rdi\n"
                "mov $0, %%rsi\n"
                "syscall\n"
                :
                : "r"(&req)
                : "rax", "rdi", "rsi"
                );
#elif defined(__aarch64__)
        long ret;
        __asm__ volatile (
                "mov x8, #101\n"
                "mov x0, %1\n"
                "mov x1, %0\n"
                "svc #0\n"
                "mov %0, x0\n"
                : "=r"(ret)
                : "r"(&req)
                : "x0", "x1", "x8"
                );
#endif
    }
}

void antiHooking::calculateChecksum() {
    std::vector<dl_phdr_info> infos;
    dl_iterate_phdr([](struct dl_phdr_info *info, size_t size, void *data) -> int {
        ((std::vector<dl_phdr_info> *) data)->push_back(*info);
        return 0;
    }, &infos);

    for (auto info : infos) {
        const char *moduleName = info.dlpi_name;
        const char *subString = ".so";
        bool foundSo = false;

        while (*moduleName != '\0') {
            const char *h = moduleName;
            const char *n = subString;

            while (*n != '\0' && *h == *n) {
                h++;
                n++;
            }

            if (*n == '\0') {
                foundSo = true;
                break;
            }

            moduleName++;
        }

        moduleName = info.dlpi_name;
        subString = "helldroid";
        bool foundLib = false;

        while (*moduleName != '\0') {
            const char *h = moduleName;
            const char *n = subString;

            while (*n != '\0' && *h == *n) {
                h++;
                n++;
            }

            if (*n == '\0') {
                foundLib = true;
                break;
            }

            moduleName++;
        }

        if (!foundSo || !foundLib) {
            continue;
        }

        int fd = Helldroid_openat(O_RDONLY,0, info.dlpi_name, AT_FDCWD);

        if (fd < 0) {
            continue;
        }

        Elf_Ehdr ehdr;
        Helldroid_read(fd, &ehdr, sizeof(Elf_Ehdr));

        Elf_Shdr shdr;
        Helldroid_lseek(fd, ehdr.e_shoff + ehdr.e_shstrndx * sizeof(Elf_Shdr), SEEK_SET);
        Helldroid_read(fd, &shdr, sizeof(Elf_Shdr));

        char* shstrtab = new char[shdr.sh_size];
        Helldroid_lseek(fd, shdr.sh_offset, SEEK_SET);
        Helldroid_read(fd, shstrtab, shdr.sh_size);

        Helldroid_lseek(fd, ehdr.e_shoff, SEEK_SET);
        for(int i = 0; i < ehdr.e_shnum; i++) {
            Helldroid_read(fd, &shdr, sizeof(Elf_Shdr));
            const char *name = shstrtab + shdr.sh_name;

            if (shdr.sh_type == SHT_PROGBITS) {
                if ((shdr.sh_flags & (SHF_EXECINSTR | SHF_ALLOC)) == (SHF_EXECINSTR | SHF_ALLOC)) {
                    if (!strcmp(name, ".plt")) {
                        continue;
                    }

                    uint32_t checksum = crc32((uint8_t *) info.dlpi_addr + shdr.sh_addr, shdr.sh_size);

                    uint32_t& lastChecksum = checksums.at(info.dlpi_name).at(name);

                    //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: [%s] - 0x%08X - 0x%08X", info.dlpi_name, lastChecksum, checksum);
                    if (lastChecksum != checksum) {
                        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: [%s] - 0x%08X - 0x%08X", info.dlpi_name, lastChecksum, checksum);
                        if (this->onLibTampered) {
                            if (last_checksums.find(info.dlpi_name) == last_checksums.end() || last_checksums[info.dlpi_name][name] != checksum) {
                                last_checksums[info.dlpi_name][name] = checksum;
                                this->onLibTampered(info.dlpi_name, name, checksums[info.dlpi_name][name], checksum);
                            }
                        }

#if defined(__x86_64__)
                        __asm__ volatile (
                                "mov $62, %%rax\n"
                                "mov $-1, %%rdi\n"
                                "mov $9, %%rsi\n"
                                "syscall\n"
                                :
                                :
                                : "rax", "rdi", "rsi"
                                );
#elif defined(__aarch64__)
                        __asm__ volatile (
                                "mov x8, #129\n"
                                "mov x0, #-1\n"
                                "mov x1, #9\n"
                                "svc 0\n"
                                :
                                :
                                : "x0", "x1", "x8"
                            );
#endif

                    }
                }
            }
        }

        delete[] shstrtab;
        Helldroid_close("Hell", fd);
    }
}