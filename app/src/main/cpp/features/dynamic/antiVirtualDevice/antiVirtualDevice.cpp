#include "antiVirtualDevice.h"
#include "deviceData.h"

#include "../../../shared/utils.h"
#include "../../../shared/obfuscate.h"

void antiVirtualDevice::execute() {
    antiAVDFiles();
    antiAVDProperties();
}

void antiVirtualDevice::antiAVDFiles() {
    std::vector<const char*> allFiles;

    allFiles.insert(allFiles.end(), std::begin(GENY_FILES), std::end(GENY_FILES));
    allFiles.insert(allFiles.end(), std::begin(PIPES), std::end(PIPES));
    allFiles.insert(allFiles.end(), std::begin(X86_FILES), std::end(X86_FILES));
    allFiles.insert(allFiles.end(), std::begin(NOX_FILES), std::end(NOX_FILES));

    for(const char* Files: allFiles){
        if(checkFiles(Files)){
            utils::killProcess();
        }
    }
}

bool antiVirtualDevice::checkFiles(const char* file){
    int fileDescriptor = Helldroid_openat(O_RDONLY,0, file, AT_FDCWD);

    if (fileDescriptor >= 0) {
        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", file);
        Helldroid_close("Hell", fileDescriptor);
        return true;
    }

    return false;
}

const char* getSystemProperty(char *input) {
    char sdk_ver_str[PROP_VALUE_MAX];
    const char* output;

    if (__system_property_get(input, sdk_ver_str)) {
        output = sdk_ver_str;
    } else {
        output = AY_OBFUSCATE("unknown");
    }

    return output;
}

void antiVirtualDevice::antiAVDProperties(){
    char* prop = AY_OBFUSCATE("ro.build.product");
    const char* product = getSystemProperty(prop);

    for (const auto& _product : products) {
        if (HellLibC::Helldroid_strstr(_product, product)) {
            __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", product);
            utils::killProcess();
        }
    }

    prop = AY_OBFUSCATE("ro.product.manufacturer");
    const char* manufacturer = getSystemProperty(prop);

    for (const auto& _manufacturer: manufacturs) {
        if (HellLibC::Helldroid_strstr(_manufacturer , manufacturer)) {
            __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", manufacturer);
            utils::killProcess();
        }
    }

    prop = AY_OBFUSCATE("ro.bootloader");
    const char* bootloader = getSystemProperty(prop);

    if (HellLibC::Helldroid_strstr(bootloader , "nox")) {
        __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", bootloader);
        utils::killProcess();
    }

    prop = AY_OBFUSCATE("ro.product.device");
    const char* device = getSystemProperty(prop);

    for (const auto& _device: deviceInfo) {
        if (HellLibC::Helldroid_strstr(_device , device)) {
            __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", device);
            utils::killProcess();
        }
    }

    prop = AY_OBFUSCATE("ro.hardware");
    const char* hardware = getSystemProperty(prop);

    for (const auto& _hardware: hardwares) {
        if (HellLibC::Helldroid_strstr(_hardware , hardware)) {
            __android_log_print(ANDROID_LOG_INFO, "Helldroid", "[HELLDROID]: AVD file detected: %s", hardware);
            utils::killProcess();
        }
    }
}