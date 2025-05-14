
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.2.2"
        id("com.google.gms.google-services") version "4.4.2"
    }
}

rootProject.name = "MathQuiz"
include(":app")

