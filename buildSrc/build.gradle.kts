import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-gradle-plugin`
    signing
    kotlin("jvm") version "2.0.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.android.tools.build:gradle-api:8.4.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    implementation("com.android.tools.build:gradle:8.4.1")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.8.10")
    implementation("org.javassist:javassist:3.27.0-GA")
}

gradlePlugin {
    plugins.create("hellCrypt") {
        id = "hellCrypt"
        displayName = "hellCrypt"
        description = "Code Obfuscation for Helldroid"
        implementationClass = "hellCrypt"
    }
}