import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

fun getCommitHash(): String {
    return try {
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

// Function to check if working directory is clean
fun isGitClean(): Boolean {
    return try {
        providers.exec {
            commandLine("git", "status", "--porcelain")
        }.standardOutput.asText.get().isEmpty()
    } catch (e: Exception) {
        false
    }
}

// Function to get current branch name
fun getGitBranch(): String {
    return try {
        providers.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        }.standardOutput.asText.get().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

android {
    namespace = "com.henrythasler.sheetmusic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.henrythasler.sheetmusic"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        val versionMinor = 0
        val versionPatch = 0
        versionName = "$versionCode.$versionMinor.$versionPatch"

        // Make version info available in BuildConfig
        buildConfigField("String", "BRANCH_NAME", "\"${getGitBranch()}\"")
        buildConfigField("String", "COMMIT_HASH", "\"${getCommitHash()}\"")
        buildConfigField("boolean", "GIT_LOCAL_CHANGES", "${!isGitClean()}")
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("String", "BUILD_DATE", "\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++20"
            }
        }

//        ndk {
//            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))
//            abiFilters += listOf("arm64-v8a")
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
//            abiFilters += listOf("x86_64")
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
//        }

        // create specific APKs per target architecture (ABI)
        splits {
            abi {
                isEnable = true
                reset()
                include("armeabi-v7a", "arm64-v8a", "x86_64")
                isUniversalApk = true // Set to true if you want a universal APK
            }
        }
    }

    signingConfigs {
        // keystore is outside the project directory
        // FIXME: check environment variables
        if (file("../../keystore.jks").exists()) {
            if(System.getenv("KEYSTORE_PASSWORD") != null &&
                System.getenv("KEY_ALIAS") != null &&
                System.getenv("KEY_PASSWORD") != null) {
                create("release") {
                    storeFile = file("../keystore.jks")
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                    println("✓ Release signing configured with keystore")
                }
            } else {
                println("⚠ Release signing not configured - missing environment variables")
            }
        } else {
            println("⚠ Release signing not configured - missing keystore")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Only assign signingConfig if it exists
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        debug {
//            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
//    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidsvg)
    implementation(libs.androidx.material3.window.size.class1.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.mockito.core)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)
}