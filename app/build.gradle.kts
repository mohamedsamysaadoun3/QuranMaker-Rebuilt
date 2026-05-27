plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "hazem.nurmontage.videoquran"
    compileSdk = 35

    defaultConfig {
        applicationId = "hazem.nurmontage.videoquran"
        minSdk = 24
        targetSdk = 35
        versionCode = 21000106
        versionName = "6.7.1-nurmontage4kb"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.window)

    // Material Design
    implementation(libs.material)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Image Loading - Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // FFmpeg Kit - Video Processing
    implementation(libs.ffmpeg.kit)

    // Google Play Billing
    implementation(libs.billing.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
