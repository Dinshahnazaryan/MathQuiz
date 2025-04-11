plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = ("com.example.mathquiz")
    compileSdk = 33
    defaultConfig {
        applicationId = ("com.example.mathquiz")
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = ("1.0")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), ("proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.android.gms:play-services-base:18.5.0")
}