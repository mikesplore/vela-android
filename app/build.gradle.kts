
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)      // <--- 1. Uncomment/Add this
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)                // <--- 2. Move KSP above Hilt
    alias(libs.plugins.hilt)               // <--- 3. Hilt must come after Android/KSP
}

android {
    namespace = "com.template.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.template.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ──── BuildConfig fields ────────────────────────────────────────────
        // Override per build flavor below; consumed in NetworkModule.kt
        buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
    }

    // ──── Build Types ───────────────────────────────────────────────────────
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BASE_URL", "\"https://api.mikesplore.me/\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.mikesplore.me/\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
        }
    }

    // ──── Product Flavors (optional, remove if not needed) ──────────────────
    flavorDimensions += "environment"
    productFlavors {
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"https://staging-api.example.com/\"")
        }
        create("production") {
            dimension = "environment"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ──── Core ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.splashscreen)

    // ──── Lifecycle ─────────────────────────────────────────────────────────
    implementation(libs.bundles.lifecycle)

    // ──── Compose ───────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // ──── Navigation ────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ──── Hilt (DI) ─────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // ──── Networking ────────────────────────────────────────────────────────
    implementation(libs.bundles.networking)
    ksp(libs.moshi.kotlin.codegen)

    // ──── Room ──────────────────────────────────────────────────────────────
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ──── DataStore ─────────────────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ──── Coroutines ────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ──── Testing ───────────────────────────────────────────────────────────
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
