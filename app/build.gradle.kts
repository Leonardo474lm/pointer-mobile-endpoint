
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
   // id("kolin-kapt")
  //  id("com.google.protobuf")version "0.9.4"
}

android {
    namespace = "com.example.scan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.scan"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    //todo:add
    composeOptions {
        kotlinCompilerExtensionVersion =
            "1.5.14"//
    }
    buildFeatures {
        compose = true // Habilitar Jetpack Compose
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.recaptcha)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

   implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.material:material:1.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.1")

    //scan fingers
    implementation("androidx.biometric:biometric:1.1.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.0-beta01")
    implementation("androidx.camera:camera-camera2:1.4.0-beta01")
    implementation("androidx.camera:camera-lifecycle:1.4.0-beta01")
    implementation("androidx.camera:camera-view:1.4.0-beta01")
    implementation("androidx.camera:camera-extensions:1.4.1")
    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:object-detection:17.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    //retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

}

