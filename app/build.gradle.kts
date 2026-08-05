import AppVersion.getVersionCode
import AppVersion.getVersionName
import org.gradle.language.nativeplatform.internal.Dimensions.applicationVariants
import java.util.Properties
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) localProps.load(localPropsFile.inputStream())

android {
    namespace = AppConfig.NAMESPACE
    compileSdk = AppConfig.COMPILE_SDK

    defaultConfig {
        applicationId = AppConfig.APPLICATION_ID
        minSdk = AppConfig.MIN_SDK
        targetSdk = AppConfig.TARGET_SDK
        versionCode = AppVersion.getVersionCode()
        versionName = AppVersion.getVersionName()
        testInstrumentationRunner = AppConfig.TEST_INSTRUMENTATION_RUNNER
    }

    signingConfigs {
        create(AppConfig.SIGNING_CONFIG_RELEASE) {
            storeFile = file(localProps.getProperty("KEYSTORE_PATH"))
            storePassword = localProps.getProperty("KEYSTORE_PASSWORD")
            keyAlias = localProps.getProperty("KEY_ALIAS")
            keyPassword = localProps.getProperty("KEY_PASSWORD")
        }
    }

    flavorDimensions += Flavors.DIMENSION

    productFlavors {
        create(Flavors.DEV) {
            dimension = Flavors.DIMENSION
            applicationIdSuffix = ".${Flavors.DEV}"
            versionNameSuffix = "-${Flavors.DEV}"
            manifestPlaceholders["appName"] = Flavors.AppNames.DEV
        }
        create(Flavors.PROD) {
            dimension = Flavors.DIMENSION
            manifestPlaceholders["appName"] = Flavors.AppNames.PROD
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(AppConfig.SIGNING_CONFIG_RELEASE)
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

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = false
    }

    resourcePrefix = ""
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.vico.compose.m3)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.compose.foundation.layout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}

afterEvaluate {
    tasks.matching { it.name.startsWith("bundle") && it.name.endsWith("Release") }
        .configureEach {
            doLast {
                val flavorName = name
                    .removePrefix("bundle")
                    .removeSuffix("Release")
                    .replaceFirstChar { it.lowercase() }

                val date = SimpleDateFormat("yyyyMMdd").format(Date())
                val versionName = AppVersion.getVersionName()
                val versionCode = AppVersion.getVersionCode()
                val newName = "pitlane-${flavorName}-release-v${versionName}(${versionCode})-${date}.aab"

                // El AAB está en app/prod/release/, no en app/build/
                val outputDir = project.file("${flavorName}/release")

                println(">>> Buscando en: ${outputDir.absolutePath}")

                if (outputDir.exists()) {
                    outputDir.listFiles()?.forEach { file ->
                        if (file.extension == "aab") {
                            val renamed = File(outputDir, newName)
                            val success = file.renameTo(renamed)
                            println(">>> ✅ ${file.name} → ${renamed.name} (success=$success)")
                        }
                    }
                } else {
                    println(">>> ❌ Directorio no existe: ${outputDir.absolutePath}")
                }
            }
        }
}