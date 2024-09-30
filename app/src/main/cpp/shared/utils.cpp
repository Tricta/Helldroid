#include <android/log.h>
#include "utils.h"

void utils::killProcess(){
    int pid = Helldroid_getpid();
    Helldroid_kill(pid, SIGKILL);
}