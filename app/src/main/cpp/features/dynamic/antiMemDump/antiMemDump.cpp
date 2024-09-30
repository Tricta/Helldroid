#include <linux/inotify.h>
#include <dirent.h>
#include <android/log.h>
#include <string.h>
#include "antiMemDump.h"
#include "../../../shared/utils.h"
#include "../../../shared/obfuscate.h"

void antiMemDump::execute(){
    while(1){
        antiDump();
        sleep(.3);
    }
}

void antiMemDump::antiDump(){
    int fd = Helldroid_inotify_init1(0);

    int n = 0;
    int wd[100];

    wd[n++] = Helldroid_inotify_add_watch(fd, AY_OBFUSCATE("/proc/self/maps"), IN_ACCESS | IN_OPEN);
    wd[n++] = Helldroid_inotify_add_watch(fd, AY_OBFUSCATE("/proc/self/mem"), IN_ACCESS | IN_OPEN);
    wd[n++] = Helldroid_inotify_add_watch(fd, AY_OBFUSCATE("/proc/self/pagemap"), IN_ACCESS | IN_OPEN);

    int task = Helldroid_openat(O_RDONLY | O_DIRECTORY, 0, AY_OBFUSCATE("/proc/self/task"), AT_FDCWD);

    struct dirent64 *dirPointer;
    char buffer[512];
    int nread;
    while((nread = Helldroid_getdents64(task, (struct dirent64*) buffer, sizeof(buffer))) > 0) {
        for (int dirReads = 0; dirReads < nread;) {
            dirPointer = (struct dirent64 *) (buffer + dirReads);

            if (dirPointer->d_type == DT_DIR) {
                if (!Helldroid_strncmp(dirPointer->d_name, AY_OBFUSCATE("."), 2) ||
                    !Helldroid_strncmp(dirPointer->d_name, AY_OBFUSCATE(".."), 3)) {
                    dirReads += dirPointer->d_reclen;
                    continue;
                }
            }

            if (dirPointer->d_type == DT_DIR) {
                char memPath[512], pagemapPath[512];

                createTaskFilePath(memPath, AY_OBFUSCATE("/mem"), dirPointer->d_name, sizeof(memPath));
                createTaskFilePath(memPath, AY_OBFUSCATE("/pagemap"), dirPointer->d_name, sizeof(memPath));

                wd[n++] = Helldroid_inotify_add_watch(fd, memPath, IN_ACCESS | IN_OPEN);
                wd[n++] = Helldroid_inotify_add_watch(fd, pagemapPath, IN_ACCESS | IN_OPEN);
            }

            dirReads += dirPointer->d_reclen;
        }
    }
    Helldroid_close(AY_OBFUSCATE("Hell"), task);

    int memDumpWatch = Helldroid_read(fd, buffer, sizeof(buffer));
    if (memDumpWatch > 0) {
        struct inotify_event *event;
        for (char *ptr = buffer; ptr < buffer + memDumpWatch; ptr += sizeof(struct inotify_event) + event->len) {
            event = (struct inotify_event *) ptr;
            if (event->mask & IN_ACCESS || event->mask & IN_OPEN) {
                __android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: Memory Dump detected!");
                Helldroid_close(AY_OBFUSCATE("Hell"), fd);

                utils::killProcess();
            }
        }
    }

    for (int i = 0; i < n; i++) {
        if (wd[i]) {
            Helldroid_inotify_rm_watch(fd, wd[i]);
        }
    }

    Helldroid_close(AY_OBFUSCATE("Hell"), fd);
}

void antiMemDump::createTaskFilePath(const char* num, const char* memPath, char* buffer, int sizeOfBuffer){
    if(sizeof(buffer) > sizeOfBuffer){
        return;
    }

    char taskPath[17] = "/proc/self/task/";

    for (int i = 0; taskPath[i] != '\0'; i++) {
        *buffer++ = taskPath[i];
    }

    for (int i = 0; num[i] != '\0'; i++) {
        *buffer++ = num[i];
    }

    for (int i = 0; memPath[i] != '\0'; i++) {
        *buffer++ = memPath[i];
    }

    *buffer = '\0';
}