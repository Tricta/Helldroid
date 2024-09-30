// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(libs.plugins.androidApplication.get().pluginId) apply false
    id(libs.plugins.androidLibrary.get().pluginId) apply false
}