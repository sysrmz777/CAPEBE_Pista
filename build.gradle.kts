// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Evita bloqueos de sincronización de OneDrive sobre app/build al generar APKs en Windows
if (rootDir.absolutePath.contains("OneDrive", ignoreCase = true)) {
    val safeBuildRoot = file("${System.getProperty("user.home")}/.cb/p")
    allprojects {
        layout.buildDirectory.set(file("$safeBuildRoot/${project.name}"))
    }
}
