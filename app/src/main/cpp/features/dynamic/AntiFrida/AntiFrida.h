#ifndef HELLDROID_ANTIFRIDA_H
#define HELLDROID_ANTIFRIDA_H

#include <android/log.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <string.h>

#include <sys/socket.h>
#include <linux/in.h>
#include <sys/types.h>
#include <arpa/inet.h>

#include <thread>
#include <future>
#include <vector>
#include <jni.h>

#include "../../../syscalls/HellLibC.h"
#include "../../../shared/utils.h"

class AntiFrida {
public:
    void executeServer();
    bool executeGadget();
private:
    bool AntiFridaAgent();
    void AntiFridaServer(int initPort, int FinalPort);

    int readVirtualMem(int fd, char* map, unsigned int max_len);
    int scanExecutable(char* map);
    void Helldroid_buildRequest(const struct sockaddr_in* lo, char* buffer);
};

#endif HELLDROID_ANTIFRIDA_H