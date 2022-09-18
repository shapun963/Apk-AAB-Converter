@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    namespace = "com.shapun.apkaabconverter"

    defaultConfig {
        minSdk = 26
        targetSdk = 33
        versionCode = 2
        versionName = "1.1"
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
    implementation("com.android.tools.build:bundletool:1.9.1")
    implementation("com.android.tools.build:apksig:7.1.3")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.protobuf:protobuf-java:3.21.2")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
