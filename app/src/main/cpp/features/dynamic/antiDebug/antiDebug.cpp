#include "antiDebug.h"
#include "../../../shared/obfuscate.h"

void antiDebug::execute(){
    while(1){
        std::thread JavaThread(&antiDebug::JavaDetect, this);
        std::thread NativeThread(&antiDebug::NativeDetect, this);

        JavaThread.join();
        NativeThread.join();
    }
}

void antiDebug::NativeDetect(){
    int status = Helldroid_openat(O_RDONLY, 0, AY_OBFUSCATE("/proc/self/status"), AT_FDCWD);

    if(checkTracerPID(status)){
        Helldroid_close(AY_OBFUSCATE("Hell"), status);
        __android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: debugging detected!");
        utils::killProcess();
    }

    Helldroid_close(AY_OBFUSCATE("Hell"), status);
}

void antiDebug::JavaDetect(){
    int task = Helldroid_openat(O_RDONLY | O_DIRECTORY, 0, AY_OBFUSCATE("/proc/self/task"), AT_FDCWD);

    struct dirent64 *dirPointer;
    char buffer[512];
    int nread;
    while((nread = Helldroid_getdents64(task, (struct dirent64*) buffer, sizeof(buffer))) > 0){
        for(int dirReads = 0; dirReads < nread;){
            dirPointer = (struct dirent64*)(buffer + dirReads);

            if(dirPointer->d_type == DT_DIR){
                if(!Helldroid_strncmp(dirPointer->d_name, AY_OBFUSCATE("."), 2) || !Helldroid_strncmp(dirPointer->d_name, AY_OBFUSCATE(".."), 3)){
                    dirReads += dirPointer->d_reclen;
                    continue;
                }

                char statusPath[100];
                createTaskFilePath(dirPointer->d_name, statusPath, sizeof(statusPath));

                int taskStatus = Helldroid_openat(O_RDONLY, 0, statusPath, AT_FDCWD);
                if (taskStatus == -1) {
                    dirReads += dirPointer->d_reclen;
                    continue;
                }else if(taskStatus < -1){
                    break;
                }

                char lineBuffer[512];
                while(readLine(taskStatus, lineBuffer, sizeof(lineBuffer)) > 0){
                    //__android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: buffer Hold: %s - %d", lineBuffer, taskStatus);
                    if(HellLibC::Helldroid_strstr(lineBuffer, AY_OBFUSCATE("JDWP"))){
                        __android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: debugging detected!");
                        Helldroid_close(AY_OBFUSCATE("Hell"), taskStatus);
                        Helldroid_close(AY_OBFUSCATE("Hell"), task);
                        utils::killProcess();
                    }
                }

                Helldroid_close(AY_OBFUSCATE("Hell"), taskStatus);
            }

            dirReads += dirPointer->d_reclen;
        }

        sleep(1);
    }

    Helldroid_close(AY_OBFUSCATE("Hell"), task);
}

bool antiDebug::checkTracerPID(int fd) {
    char buffer[512];
    while(readLine(fd, buffer, sizeof(buffer)) > 0){
        if(Helldroid_strncmp(buffer, AY_OBFUSCATE("TracerPid:"), 10) == 0){
            int pid = atoi(buffer + 10);
            //__android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: TracerPid: %d", pid);
            if(pid != 0) {
                return true;
            }
        }
    }

    return false;
}

void antiDebug::createTaskFilePath(const char* num, char* buffer, int sizeOfBuffer){
    if(sizeof(buffer) > sizeOfBuffer){
        return;
    }

    char taskPath[17] = "/proc/self/task/";
    char commPath[6] = "/comm";

    for (int i = 0; taskPath[i] != '\0'; i++) {
        *buffer++ = taskPath[i];
    }

    for (int i = 0; num[i] != '\0'; i++) {
        *buffer++ = num[i];
    }

    for (int i = 0; commPath[i] != '\0'; i++) {
        *buffer++ = commPath[i];
    }

    *buffer = '\0';
}

size_t antiDebug::readLine(int fd, char *buffer, size_t bufferSize){
    size_t i;
    int reader;
    char c;

    for(i = 0; i < bufferSize - 1; i++){
        reader = Helldroid_read(fd, &c, 1);

        if(reader == 0 || c == '\n'){
            break;
        }

        buffer[i] = c;
    }

    buffer[i] = '\0';
    return i;
}
