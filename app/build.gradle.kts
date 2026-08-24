import AppVersion.getVersionCode
import AppVersion.getVersionName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

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
        versionCode = getVersionCode()
        versionName = getVersionName()
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

    // android auto
    implementation(libs.androidx.car.app)
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
                val renamedPrefix = "pitlane-${flavorName}-release-"
                val originalFileName = "app-${flavorName}-release.aab"

                // El AAB puede terminar en dos lugares distintos según cómo se dispare el
                // build: Android Studio ("Generate Signed Bundle") lo deja en
                // app/<flavor>/release/, mientras que la CLI de Gradle usa la ubicación
                // estándar de AGP (build/outputs/bundle/). Buscamos en ambas y nos
                // quedamos con la más reciente, para no agarrar un .aab viejo por error.
                val candidateDirs = listOf(
                    project.file("${flavorName}/release"),
                    project.layout.buildDirectory.dir("outputs/bundle/${flavorName}Release").get().asFile
                )
                val original = candidateDirs
                    .map { File(it, originalFileName) }
                    .filter { it.exists() }
                    .maxByOrNull { it.lastModified() }

                if (original != null) {
                    val outputDir = original.parentFile
                    println(">>> AAB encontrado en: ${original.absolutePath}")

                    // Limpiamos copias renombradas de builds anteriores: si no lo
                    // hiciéramos, quedan .aab viejos con otro versionCode dando vueltas
                    // en la carpeta y es fácil subir el equivocado a Play Console.
                    outputDir.listFiles()?.forEach { file ->
                        if (file.extension == "aab" && file.name.startsWith(renamedPrefix)) {
                            file.delete()
                        }
                    }

                    val renamed = File(outputDir, newName)
                    // Copiamos (no movemos) el original: AGP genera un task interno
                    // (BundleIdeModelProducerTask) que espera encontrarlo con su nombre
                    // original después de este task, así que no podemos hacerlo desaparecer.
                    original.copyTo(renamed, overwrite = true)
                    println(">>> ✅ Copia creada: ${original.name} → ${renamed.name}")
                } else {
                    println(">>> ❌ No se encontró $originalFileName en ninguna ubicación conocida")
                }
            }
        }
}