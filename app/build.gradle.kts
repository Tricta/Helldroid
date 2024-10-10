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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(project(":hellCrypt"))
    implementation(project(":helldroid"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

tasks.register("writeProguardRule") {
    val proguardFile = file("proguard-rules.pro")

    val mainApp = "com.sdk.helldroid"

    doLast {
        val ruleToAdd = """
            -keep, allowoptimization public class com.lib.hellcrypt.hellcrypt {
                public static void init();
            }
            
            -keep, allowoptimization public class com.lib.hellcrypt.Stub {
                public static final com.lib.hellcrypt.Stub instance;
                public java.lang.String hellYoki(java.lang.String);
            }
            
            -keep, allowoptimization public class com.lib.hellcrypt.hellDecrypt {
                public static java.lang.String hellYoki(byte[]);
            }
            
            -keep class sun.misc.Unsafe {
                public native void putObject(java.lang.Object, long, java.lang.Object);
                public long objectFieldOffset(java.lang.reflect.Field);
            }
            
            -keep, allowoptimization class $mainApp.MainActivity {
                public native void ReplaceMethodByObject(java.lang.Object, java.lang.Object);
            }
            
            -keepclasseswithmembers, allowoptimization class com.lib.helldroid.Helldroid {
                native <methods>;
            }
            
            -keepclassmembers, allowoptimization class com.lib.helldroid.Helldroid {
                public static void LoadLibrary(android.content.Context, java.lang.Class);
                public static void registerNativeMethods(java.lang.Class);
                public static void createDummyFile(android.content.Context);
                public static void checkMainTrace();
            }
            
            -dontoptimize
            -allowaccessmodification
            -dontskipnonpubliclibraryclasses
            -dontskipnonpubliclibraryclassmembers
        """.trimIndent()

        if (!proguardFile.exists()) {
            proguardFile.createNewFile()
        }

        proguardFile.appendText("\n$ruleToAdd\n")
        println("ProGuard rule written to proguard-rules.pro")
    }
}

tasks.register("modifyManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")

    doLast {
        if (!manifestFile.exists()) {
            println("AndroidManifest.xml file does not exist.")
            return@doLast
        }

        val manifestLines = manifestFile.readLines().toMutableList()

        val permissionLine = """<uses-permission android:name="android.permission.INTERNET" />"""
        val extractNativeLibsLine = """android:extractNativeLibs="true""""
        val allowBackupFalseLine = """android:allowBackup="false""""
        val providerLine = """<provider android:name="androidx.startup.InitializationProvider" android:authorities="${'$'}{applicationId}.androidx-startup" tools:node="remove" />"""
        val receiverLine = """<receiver android:name="androidx.profileinstaller.ProfileInstallReceiver" android:permission="android.permission.DUMP" tools:node="remove" />"""

        var permissionExists = false
        var extractNativeLibsExists = false
        var allowBackupExists = false
        var providerExists = false
        var receiverExists = false

        val updatedLines = manifestLines.map { line ->
            var newLine = line
            if (line.contains("android:allowBackup=")) {
                allowBackupExists = true
                if (line.contains("android:allowBackup=\"true\"")) {
                    newLine = line.replace("android:allowBackup=\"true\"", allowBackupFalseLine)
                }
            }
            if (line.contains("android:extractNativeLibs=")) {
                extractNativeLibsExists = true
                if (line.contains("android:extractNativeLibs=\"false\"")) {
                    newLine = line.replace("android:extractNativeLibs=\"false\"", extractNativeLibsLine)
                }
            }
            if (line.contains(permissionLine)) permissionExists = true
            if (line.contains(providerLine)) providerExists = true
            if (line.contains(receiverLine)) receiverExists = true
            newLine
        }.toMutableList()

        println(updatedLines)

        val applicationIndex = updatedLines.indexOfFirst { it.contains("<application") }
        if (!permissionExists && applicationIndex != -1) {
            updatedLines.add(applicationIndex, permissionLine)
        }

        if (!extractNativeLibsExists) {
            updatedLines.replaceAll { line ->
                if (line.contains("<application") && !line.contains(extractNativeLibsLine)) {
                    line + " " + extractNativeLibsLine
                } else {
                    line
                }
            }
        }

        if (!allowBackupExists) {
            updatedLines.replaceAll { line ->
                if (line.contains("<application") && !line.contains(allowBackupFalseLine)) {
                    line + " " + allowBackupFalseLine
                } else {
                    line
                }
            }
        }

        val removeIndex = updatedLines.indexOfFirst { it.contains("</application>") }
        if (!providerExists && removeIndex != -1) {
            updatedLines.add(removeIndex, providerLine)
        }

        if (!receiverExists && removeIndex != -1) {
            updatedLines.add(removeIndex, receiverLine)
        }

        manifestFile.writeText(updatedLines.joinToString("\n"))
        println("AndroidManifest.xml has been updated.")
    }
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
