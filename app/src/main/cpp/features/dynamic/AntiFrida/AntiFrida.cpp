#include "AntiFrida.h"
#include "../../../shared/obfuscate.h"

#define MAX_LINE 512
static char keyword[] = "libfrida";

bool AntiFrida::executeGadget() {
    return AntiFridaAgent();
}

void AntiFrida::executeServer() {
    while (1) {
        std::vector<std::thread> threads;
        threads.emplace_back(&AntiFrida::AntiFridaServer, this, 0, 16384);
        threads.emplace_back(&AntiFrida::AntiFridaServer, this, 16385, 32768);
        threads.emplace_back(&AntiFrida::AntiFridaServer, this, 32769, 49152);
        threads.emplace_back(&AntiFrida::AntiFridaServer, this, 49153, 65535);

        for (std::thread& thread : threads) {
            if (thread.joinable()) {
                thread.join();
            }
        }
    }
}

void AntiFrida::AntiFridaServer(int initPort, int FinalPort) {
    struct sockaddr_in lo;
    lo.sin_family = AF_INET;
    lo.sin_addr.s_addr = Helldroid_htonl(INADDR_LOOPBACK);

    for (int i = initPort; i <= FinalPort; i++) {
        int socket = Helldroid_socket(AF_INET, SOCK_STREAM, 0);
        if (socket < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "Helldroid", "[ERROR] Failed to create socket");
            continue;
        }

        lo.sin_port = Helldroid_htons(i);

        if((Helldroid_connect(socket, (const struct sockaddr*) &lo, sizeof(lo))) >= 0){
            //__android_log_print(ANDROID_LOG_WARN, "Helldroid", "[HELLDROID]: port %d opened", i);

            char req[1024];
            Helldroid_buildRequest(&lo, req);
            Helldroid_send(socket, req, sizeof(req), MSG_NOSIGNAL);

            char res[1024];
            if(Helldroid_read(socket, res, sizeof(res)) > 0) {
                //__android_log_print(ANDROID_LOG_WARN, "Helldroid", "[HELLDROID]: %s", res);
                if (HellLibC::Helldroid_strstr(res, "NuMpdyOt8cxBj4a6g04o9qsdJ60=")) {
                    __android_log_print(ANDROID_LOG_WARN, "Helldroid","[HELLDROID]: frida server detected!");
                    Helldroid_close("Hell", socket);

                    utils::killProcess();
                }
            }
        }
        close(socket);
    }
}

bool AntiFrida::AntiFridaAgent() {
    char map[MAX_LINE];
    int fd;
    int num_found = 0;

    while(1) {
        fd = Helldroid_openat(O_RDONLY, 0, AY_OBFUSCATE("/proc/self/maps"), AT_FDCWD);
        if (fd >= 0) {
            while ((readVirtualMem(fd, map, MAX_LINE)) > 0) {
                int scan = scanExecutable(map);

                if (scan == 1) {
                    num_found++;
                }

                if (num_found >= 1) {
                    __android_log_print(ANDROID_LOG_WARN, "Helldroid", "[HELLDROID]: suspect string found in memory!");
                    Helldroid_close(AY_OBFUSCATE("Hell"), fd);
                    return true;
                }
            }

            Helldroid_close(AY_OBFUSCATE("Hell"), fd);
            return false;
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "Helldroid","[HELLDROID]: Error opening /proc/self/maps. That's usually a bad sign.");
            return false;
        }
    }
}

void AntiFrida::Helldroid_buildRequest(const struct sockaddr_in* lo, char* buffer) {
    const char* request_line = AY_OBFUSCATE("GET /ws HTTP/1.1\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: th89hNDPYm3p9sznbYYL8Q==\r\nSec-WebSocket-Version: 13\r\nHost: ");

    for (int i = 0; request_line[i] != '\0'; i++) {
        *buffer++ = request_line[i];
    }

    char* ip_str = inet_ntoa(lo->sin_addr);
    for (int i = 0; ip_str[i] != '\0'; i++) {
        *buffer++ = ip_str[i];
    }

    *buffer++ = ':';

    int port = ntohs(lo->sin_port);
    char port_str[10];
    int port_index = 0;
    do {
        port_str[port_index++] = port % 10 + '0';
        port /= 10;
    } while (port > 0);
    for (int i = port_index - 1; i >= 0; i--) {
        *buffer++ = port_str[i];
    }

    const char* rest = "\r\nUser-Agent: Frida/16.3.3\r\n\r\n";
    for (int i = 0; rest[i] != '\0'; i++) {
        *buffer++ = rest[i];
    }

    *buffer++ = '\0';
}

int AntiFrida::scanExecutable(char* map){
    char buffer[MAX_LINE];
    unsigned long start, end;

    sscanf(map, "%lx-%lx %s", &start, &end, buffer);

   // __android_log_print(ANDROID_LOG_WARN, "Helldroid", "[HELLDROID]: %s", map);
    if (buffer[2] == 'x' && HellLibC::Helldroid_strstr(map, keyword)) {
        return 1;
    } else {
        return 0;
    }
}

int AntiFrida::readVirtualMem(int fd, char* map, unsigned int max_len){
    char buffer;
    ssize_t ret;
    ssize_t bytes_read = 0;

    Helldroid_memset(map, 0, max_len);

    do{
        ret = Helldroid_read(fd, &buffer, 1);

        if (ret != 1) {
            return -1;
        }

        if (buffer == '\n'){
            return bytes_read;
        }

        *(map++) = buffer;
        bytes_read += 1;
    }while(bytes_read < max_len - 1);

    return bytes_read;
}