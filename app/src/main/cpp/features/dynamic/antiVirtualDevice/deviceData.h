#include "../../../shared/obfuscate.h"

static const char *GENY_FILES[] = {
        AY_OBFUSCATE("/dev/socket/genyd"),
        AY_OBFUSCATE("/dev/socket/baseband_genyd")
};

static const char *PIPES[] = {
        AY_OBFUSCATE("/dev/socket/qemud"),
        AY_OBFUSCATE("/dev/qemu_pipe")
};

static const char *X86_FILES[] = {
        AY_OBFUSCATE("ueventd.android_x86.rc"),
        AY_OBFUSCATE("x86.prop"),
        AY_OBFUSCATE("ueventd.ttVM_x86.rc"),
        AY_OBFUSCATE("init.ttVM_x86.rc"),
        AY_OBFUSCATE("fstab.ttVM_x86"),
        AY_OBFUSCATE("fstab.vbox86"),
        AY_OBFUSCATE("init.vbox86.rc"),
        AY_OBFUSCATE("ueventd.vbox86.rc")
};

static const char *NOX_FILES[] = {
        AY_OBFUSCATE("fstab.nox"),
        AY_OBFUSCATE("init.nox.rc"),
        AY_OBFUSCATE("ueventd.nox.rc")
};

static const char *products[] = {
        AY_OBFUSCATE("sdk"),
        AY_OBFUSCATE("andy"),
        AY_OBFUSCATE("google_sdk"),
        AY_OBFUSCATE("droid4x"),
        AY_OBFUSCATE("nox"),
        AY_OBFUSCATE("sdk_x86"),
        AY_OBFUSCATE("vbox86p"),
        AY_OBFUSCATE("emu64x")
};

static const char *manufacturs[] = {
        AY_OBFUSCATE("sdk"),
        AY_OBFUSCATE("andy"),
        AY_OBFUSCATE("google_sdk"),
        AY_OBFUSCATE("droid4x"),
        AY_OBFUSCATE("nox"),
        AY_OBFUSCATE("sdk_x86"),
        AY_OBFUSCATE("vbox86p")
};

static const char *deviceInfo[] = {
        AY_OBFUSCATE("generic"),
        AY_OBFUSCATE("andy"),
        AY_OBFUSCATE("droid4x"),
        AY_OBFUSCATE("nox"),
        AY_OBFUSCATE("generic_x86_64"),
        AY_OBFUSCATE("vbox86p"),
        AY_OBFUSCATE("emu64x")
};

static const char *hardwares[] = {
        AY_OBFUSCATE("goldfish"),
        AY_OBFUSCATE("vbox86"),
        AY_OBFUSCATE("nox"),
        AY_OBFUSCATE("VM_x86"),
        AY_OBFUSCATE("intel"),
        AY_OBFUSCATE("amd"),
        AY_OBFUSCATE("x86")
};