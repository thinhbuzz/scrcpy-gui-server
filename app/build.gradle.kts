plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.thinhbuzz.scrcpy.gui.server"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "me.thinhbuzz.scrcpy.gui.server"
        minSdk = 26
        targetSdk = 36
        versionCode = 1001
        versionName = "1.0.1"
    }
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "111111"
            keyAlias = "Server"
            keyPassword = "111111"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
}
