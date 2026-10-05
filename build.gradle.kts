// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val customBuildBaseDir = file("E:/Program Files/Android/Android Studio/builds/HiperMarketSallingApplication")

layout.buildDirectory.set(customBuildBaseDir.resolve("root"))

subprojects {
    layout.buildDirectory.set(customBuildBaseDir.resolve(name))
}