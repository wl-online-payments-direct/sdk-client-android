/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    repositories {
        google()
        // Mirror first, mavenCentral as fallback — see settings.gradle.kts for why.
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2")
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.owasp.dependencycheck") version "12.1.9" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("com.gradleup.nmcp.aggregation").version("1.3.0")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(java.io.FileInputStream(localPropertiesFile))
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("MAVEN_USERNAME") ?: localProperties.getProperty("mavenUsername", "")
        password = System.getenv("MAVEN_PASSWORD") ?: localProperties.getProperty("mavenPassword", "")
        publishingType = "AUTOMATIC"
    }

    // Publish all projects that apply the 'maven-publish' plugin
    publishAllProjectsProbablyBreakingProjectIsolation()
}
