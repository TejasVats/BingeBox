import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.core)
    implementation("org.openjfx:javafx-base:${libs.versions.javafx.get()}:${javaFxPlatform()}")
    implementation("org.openjfx:javafx-graphics:${libs.versions.javafx.get()}:${javaFxPlatform()}")
    implementation("org.openjfx:javafx-controls:${libs.versions.javafx.get()}:${javaFxPlatform()}")
    implementation("org.openjfx:javafx-media:${libs.versions.javafx.get()}:${javaFxPlatform()}")
    implementation("org.openjfx:javafx-swing:${libs.versions.javafx.get()}:${javaFxPlatform()}")
}

compose.desktop {
    application {
        mainClass = "com.example.bingebox.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.bingebox"
            packageVersion = "1.0.0"
        }
    }
}

fun javaFxPlatform(): String {
    val os = org.gradle.internal.os.OperatingSystem.current()
    val arch = System.getProperty("os.arch")
    return when {
        os.isMacOsX && arch == "aarch64" -> "mac-aarch64"
        os.isMacOsX -> "mac"
        os.isWindows -> "win"
        os.isLinux -> "linux"
        else -> error("Unsupported desktop platform for JavaFX: ${os.name}")
    }
}