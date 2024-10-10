#include "ARThook.h"

#define TRAMPOLINE_SPACE_SIZE 4096

static unsigned char *currentTrampolineOff = 0;

#if defined(__x86_64__)
// 48 bf 00 00 00 00 00 00 00 00 ; movabs rdi, 0x0
// ff 77 20 ; push QWORD PTR [rdi + 0x20]
// c3 ; ret
unsigned char trampoline[] = {
        0x00, 0x00, 0x00, 0x00, // code_size_ in OatQuickMethodHeader
        0x48, 0xbf, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0xff, 0x77, 0x20,
        0xc3
};
#elif defined(__aarch64__)
// 60 00 00 58 ; ldr x0, 12
// 10 00 40 F8 ; ldr x16, [x0, #0x00]
// 00 02 1f d6 ; br x16
// 00 00 00 00
// 00 00 00 00 ; 0x0000000000000000
unsigned char trampoline[] = {
        0x00, 0x00, 0x00, 0x00, // code_size_ in OatQuickMethodHeader
        0x60, 0x00, 0x00, 0x58,
        0x10, 0x00, 0x40, 0xf8,
        0x00, 0x02, 0x1f, 0xd6,
        0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00
};
#endif

#define roundUpToPtrSize(v) (v + sizeof(void*) - 1 - ((v + sizeof(void*) - 1) & (sizeof(void*) - 1)))

void* hookARTMethod(JNIEnv* env, jobject targetObject){
    void* hookedARTMethod = nullptr;

    jclass executableClass = env->FindClass("java/lang/reflect/Executable");
    jfieldID artMethodID = env->GetFieldID(executableClass, "artMethod", "J");

    hookedARTMethod = (void*) env->GetLongField(targetObject, artMethodID);

    return hookedARTMethod;
}

static void *allocTrampolineSpace() {
    unsigned char *buffer = (unsigned char*)mmap(NULL, TRAMPOLINE_SPACE_SIZE, PROT_READ | PROT_WRITE | PROT_EXEC,
                              MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    if (buffer == MAP_FAILED) {
        return NULL;
    }
    else {
        return buffer;
    }
}

void* genTrampoline(void *toMethod, void *entrypoint){
    size_t trampolineSize = sizeof(trampoline);
    currentTrampolineOff = (unsigned char*)allocTrampolineSpace();

    unsigned char *targetAddr = currentTrampolineOff;

    memcpy(targetAddr, trampoline,sizeof(trampoline));

#if defined(__x86_64__)
    memcpy(targetAddr + 6, &toMethod, sizeof(void*));
#elif defined(__aarch64__)
    memcpy(targetAddr + 16, &toMethod, sizeof(void*));
#endif

    if(entrypoint == NULL) {
        targetAddr += 4;
    }

    currentTrampolineOff += roundUpToPtrSize(trampolineSize);

    return targetAddr;
}

extern "C" JNIEXPORT void JNICALL ReplaceMethodByObject(JNIEnv* env, jobject /* this */, jobject targetMethod, jobject newMethod){
    void* targetArtMethod;
    void* newArtMethod;

#if defined(__x86_64__)
    trampoline[16] = roundUpToPtrSize(4 * 3 + 2 * 2) + sizeof(void*);
#elif defined(__aarch64__)
    trampoline[9] |= roundUpToPtrSize(4 * 4 + 2 * 2) << 4;
    trampoline[10] |= roundUpToPtrSize(4 * 4 + 2 * 2) >> 4;
#endif

    targetArtMethod = hookARTMethod(env, targetMethod);
    newArtMethod = hookARTMethod(env, newMethod);

    if (targetArtMethod != nullptr && newArtMethod != nullptr) {
        void *newEntrypoint = NULL;
        newEntrypoint = genTrampoline(newArtMethod, NULL);

        if (newEntrypoint) {
            void* newTargetEntryPoint = (char*)targetArtMethod + roundUpToPtrSize(4 * 3 + 2 * 2) + sizeof(void*);
            *((void **)(newTargetEntryPoint)) = newEntrypoint;
            //const char* logTxt = AY_OBFUSCATE("[HELLDROID]: Replaced target method");
            //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "%s", logTxt);
        }
    }else{
        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: FAIL!! Replaced target method");
    }
}

JNIEXPORT void JNICALL registerMethods(JNIEnv* env, jclass /* this */, jclass targetClass) {
    static JNINativeMethod methods[] ={
            {"ReplaceMethodByObject", "(Ljava/lang/Object;Ljava/lang/Object;)V", (void*) ReplaceMethodByObject}
    };

    for(int i = 0; i< sizeof(methods) / sizeof(JNINativeMethod); i++){
        jmethodID methodId = env->GetMethodID(targetClass, methods[i].name, methods[i].signature);

        if(env->ExceptionCheck()){
            env->ExceptionClear();
            continue;
        }

        if(methodId != nullptr){
            //const char* logTxt = AY_OBFUSCATE("[HELLDROID]: Registering native methods");
            //__android_log_print(ANDROID_LOG_INFO, "Helldroid", "%s", logTxt);
            if(env->RegisterNatives(targetClass, &methods[i], 1) != JNI_OK){
                __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: Error on register native methods");
            }
        }
    }
}