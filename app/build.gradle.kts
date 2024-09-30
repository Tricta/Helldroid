import java.io.*
import java.nio.file.Files
import java.util.zip.DeflaterOutputStream
import kotlin.experimental.xor

plugins {
    id(libs.plugins.androidApplication.get().pluginId)
    id("hellCrypt")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.1")
    }
}

hellCrypt {
    obfuscationList = setOf("com.sdk.helldroid")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\jotta\\release.jks")
            storePassword = "123456"
            keyPassword = "123456"
            keyAlias = "release"
        }
    }
    namespace = "com.sdk.helldroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sdk.helldroid"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes{
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(project(":hellCrypt"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

/*tasks.register("apkPacker") {
    group = "encryption"
    description = "Compresses and encrypts the APK."

    doLast {
        val apkDir = file("build\\outputs\\apk")
        if (!apkDir.exists()) {
            apkDir.mkdirs()
        }

        val apkFile = file("build\\intermediates\\apk\\release\\app-release.apk")
        val compressedFile = file("build\\intermediates\\apk\\release\\compressed_app-release.apk")
        val encryptedFile = file("build\\outputs\\apk\\app.apk")

        // Compress the APK
        compressFile(apkFile, compressedFile)

        // Encrypt the compressed APK
        val key = "yoki"
        val compressedData = compressedFile.readBytes()
        val encryptedData = xorEncrypt(compressedData, key.toByteArray())

        Files.write(encryptedFile.toPath(), encryptedData)

        compressedFile.delete()

        println("APK compressed and encrypted successfully!")
    }
}

fun compressFile(inputFile: File, compressedFile: File) {
    inputFile.inputStream().use { input ->
        compressedFile.outputStream().use { output ->
            DeflaterOutputStream(output).use { deflater ->
                input.copyTo(deflater)
            }
        }
    }
}

fun xorEncrypt(data: ByteArray, key: ByteArray): ByteArray {
    return ByteArray(data.size).apply {
        for (i in data.indices) {
            this[i] = data[i] xor key[i % key.size]
        }
    }
}*/
