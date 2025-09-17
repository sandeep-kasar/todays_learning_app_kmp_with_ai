import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinX.serialization.plugin)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {

        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(libs.coroutines)
            implementation(libs.bundles.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            implementation(libs.napier)
            implementation(libs.navigation)
            implementation(libs.bundles.kmpPalette)
            implementation(libs.datastore.preferences)
            implementation(libs.bundles.coil)
            implementation(libs.kotlinX.serializationJson)
            implementation(libs.compose.calender)
        }

        androidMain.dependencies {
            implementation(libs.ktor.android)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.androidX.activity)
        }

        iosMain.dependencies {
            implementation(libs.ktor.darwin)
        }

        desktopMain.dependencies {
            implementation(libs.ktor.java)
            implementation(libs.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.todays.learning"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.annotation.jvm)
    implementation(libs.androidx.ui.tooling.preview.android)
}

compose.desktop {
    application {
        mainClass = "com.todays.learning.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.todays.learning"
            packageVersion = "1.0.0"
        }
    }
}
