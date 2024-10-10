#ifndef HELLLIBC_H
#define HELLLIBC_H

#include <sys/types.h>

class HellLibC {
public:
    static const char* Helldroid_strstr(const char* mem, const char* key);
    static const char* Helldroid_strstr2(const char* mem, const char* key);
};

extern "C" int Helldroid_openat(int flags, mode_t mode, const char* pathname, int dirfd);
extern "C" int Helldroid_close(const char* code, int fd);

extern "C" void Helldroid_memset(void* memSpace, int byte, size_t firstBytes);
extern "C" int Helldroid_read(int fd, void* buffer, size_t size);

extern "C" int Helldroid_socket(int domain, int type, int protocol);
extern "C" int Helldroid_htons(uint16_t hostshort);
extern "C" int Helldroid_htonl(uint32_t hostshort);
extern "C" int Helldroid_connect(int sockfd, const struct sockaddr *addr, socklen_t addrlen);
extern "C" int Helldroid_send(int sockfd, const void *buf, size_t len, int flags);

extern "C" int Helldroid_strcmp(const char* str1, const char* str2);
extern "C" int Helldroid_strncmp(const char *string1, const char *string2, size_t count);
extern "C" int Helldroid_getdents64(int fd, struct dirent64* dirent, unsigned int count);

extern "C" int Helldroid_inotify_init1(int flags);
extern "C" int Helldroid_inotify_add_watch(int fd, const char* path, unsigned int mask);
extern "C" int Helldroid_inotify_rm_watch(int fd, char wd);

extern "C" int Helldroid_getpid();
extern "C" int Helldroid_kill(int pid, int sig);

extern "C" int Helldroid_lseek(int fd, off_t offset, int whence);
#endif // HELLLIBC_H