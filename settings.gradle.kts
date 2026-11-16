/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

@file:Suppress("UnstableApiUsage")

// Google-hosted read-only mirror of Maven Central. Listed before mavenCentral()
// so Gradle resolves from it first; mavenCentral() stays as a fallback. Needed
// because Sonatype rate-limits repo.maven.apache.org by IP, which causes 429s
// on shared CI runners. URL is duplicated below because pluginManagement runs
// in an isolated scope that can't see script-level declarations.

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

include(":onlinepayments-sdk")

// Compiles the examples of the Agent Skill (skills/) against the SDK. Not published.
// The module is in the top-level examples/ folder, which the GitHub push leaves out. The folder is missing in
// the GitHub repositories, so the module is only included when it exists.
val skillExamples = file("examples/skill-examples")
if (skillExamples.exists()) {
    include(":skill-examples")
    project(":skill-examples").projectDir = skillExamples
}