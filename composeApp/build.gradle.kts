import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun envOrDefault(name: String, defaultValue: String): String {
    return providers.environmentVariable(name).orElse(defaultValue).get()
}

val releaseKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull

val hasReleaseSigning =
    !releaseKeystorePath.isNullOrBlank() &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    kotlin("plugin.serialization") version "2.2.20"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.koin.android) // Android 特有
            implementation(libs.koin.androidx.compose) // Jetpack Compose 支持
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.glance.appwidget) // Jetpack Glance 支持
            implementation(libs.androidx.glance.material3)

        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.core)           // Test core
            implementation(libs.androidx.runner)         // Instrumentation runner
            implementation(libs.androidx.junit.v115)      // JUnit extensions
            implementation(libs.androidx.room.testing.v260)   // Room testing helpers
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.logging)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.navigation.compose)

            implementation(libs.multiplatform.settings)

            implementation(libs.kotlinx.datetime)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        all {
            languageSettings.enableLanguageFeature("PropertyParamAnnotationDefaultTargetMode")
        }
    }
}

android {
    namespace = "com.sky31.gongmultiplatform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.sky31.gongmultiplatform"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "3.1.20251108-beta"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SYSTEM_HOST", "\"${envOrDefault("SYSTEM_HOST", "") }\"")
        buildConfigField("String", "SYSTEM_UPDATE_HOST", "\"${envOrDefault("SYSTEM_UPDATE_HOST", "") }\"")
        buildConfigField("String", "SYSTEM_WEB_HOST", "\"${envOrDefault("SYSTEM_WEB_HOST", "") }\"")
        buildConfigField("int", "SYSTEM_MAX_RETRY_TIMES", envOrDefault("SYSTEM_MAX_RETRY_TIMES", "0"))
        buildConfigField("long", "SYSTEM_RETRY_INTERVAL", "${envOrDefault("SYSTEM_RETRY_INTERVAL", "0")}L")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

