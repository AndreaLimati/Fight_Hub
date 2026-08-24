plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fighthub"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.fighthub"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewfinder.core)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.runtime.saved.instance.state)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.5.0"))
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.9")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.9")
}

configurations.configureEach {
    resolutionStrategy {
        force("androidx.test.espresso:espresso-core:3.6.1")
        force("androidx.test.espresso:espresso-base:3.6.1")
        force("androidx.test:runner:1.6.2")
        force("androidx.test:monitor:1.7.1")
        force("androidx.test:rules:1.6.1")
        force("androidx.test:core:1.6.1")
        force("androidx.test.ext:junit:1.2.1")
    }
}