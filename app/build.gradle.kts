@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("android")
    id("com.android.application")
}

android {
    compileSdk = 33
    namespace = "com.shapun.apkaabconverter"

    defaultConfig {
        minSdk = 26
        targetSdk = 33
        versionCode = 3
        versionName = "1.3"
        applicationId = android.namespace
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    android.packagingOptions.jniLibs.useLegacyPackaging = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.protobuf:protobuf-java:3.21.2")
    implementation("com.android.tools.build:apksig:7.4.2")
    implementation("com.android.tools.build:bundletool:1.14.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
