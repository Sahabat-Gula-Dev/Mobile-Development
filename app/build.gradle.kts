plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.pkm.sahabatgula"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pkm.sahabatgula"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://api-service.sahabatgula.com/\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api-service.sahabatgula.com/\"")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.activity)

    // paging
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.runtime.ktx)

    // card
    implementation(libs.androidx.cardview)

    // cameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // viewpager2
    implementation(libs.androidx.viewpager2)

    // Lifecycle dan Coroutines
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v286)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // for network
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // encryptedSharedPreferences
    implementation(libs.androidx.security.crypto)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.javapoet)

    // auth google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Google Identity Services untuk Credential Manager
    implementation(libs.googleid)
    // firebase
    implementation(libs.firebase.auth.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    // Room Kotlin Extensions & Coroutines support
    implementation(libs.androidx.room.ktx)
    // Annotation Processor pakai KSP (bukan kapt)
    ksp(libs.androidx.room.compiler)
    // (Opsional) Testing Room
    testImplementation(libs.androidx.room.testing)

    // chart
    implementation(libs.mpandroidchart)

    // glide
    implementation(libs.glide)
    ksp(libs.compiler)

}