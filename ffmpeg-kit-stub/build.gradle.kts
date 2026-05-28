plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.arthenica.ffmpegkit"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // SmartException - using local stub instead of Maven dependency
    // since com.arthenica:smartexception-java:0.1.1 is not on Maven Central
}
