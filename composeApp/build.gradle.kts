import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.fahad.microservices_manager.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Service Deck"
            packageVersion = "1.0.2"

            description = "Microservices Manager"
            vendor = "Service Deck"
            copyright = "© 2024 Service Deck. All rights reserved."

            modules("java.instrument", "java.scripting", "jdk.unsupported")

            linux {
                shortcut = true
                packageName = "servicedeck"
                appCategory = "Development"
                menuGroup = "Development"
            }
            windows {
                shortcut = true
                menu = true
                menuGroup = "Service Deck"
                upgradeUuid = "6724a87c-5c8e-4735-86a4-44b1c3143c72"
                perUserInstall = false // This makes it show up properly for all users in Apps & Features
                if (project.file("icon.ico").exists()) {
                    iconFile.set(project.file("icon.ico"))
                }
            }
            macOS {
                bundleID = "com.fahad.servicedeck"
                if (project.file("icon.icns").exists()) {
                    iconFile.set(project.file("icon.icns"))
                }
            }
        }
    }
}
