@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.agp.app)
}

android {
    compileSdk = 34
    namespace = "com.shapun.apkaabconverter"

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.5"
        applicationId = namespace
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    android.packagingOptions.jniLibs.useLegacyPackaging = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.common)

    implementation(libs.google.guava)
    implementation(libs.google.material)
    implementation(libs.google.protobuf.java)

    implementation(libs.bcprov.jdk15on)
    implementation(libs.android.tools.apksig)
    implementation(libs.android.tools.bundletool)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
