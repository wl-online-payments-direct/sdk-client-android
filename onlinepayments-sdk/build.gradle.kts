/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.bundling.Zip
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("signing")
    id("maven-publish")
    id("org.sonarqube")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.onlinepayments.sdk.client.android"
    compileSdk = 34
    compileSdkVersion = "android-34"

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    lint {
        abortOnError = false
        targetSdk = 34
        lintConfig = file("lint.xml")
    }

    testOptions {
        targetSdk = 34
        unitTests.isIncludeAndroidResources = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    implementation("com.google.android.gms:play-services-wallet:19.4.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.17")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.mockito:mockito-core:5.16.1")
    //noinspection GradleDependency We need this until we upgrade to support SDK 35
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val decodedSigningKey = localProperties
    .getProperty("signingKey", "")
    .replace("\\n", "\n")

val POM_GROUP_ID: String by project
val POM_ARTIFACT_ID: String by project
val POM_VERSION: String by project
val POM_NAME: String by project
val POM_URL: String by project
val POM_DESCRIPTION: String by project
val POM_ORGANIZATION_NAME: String by project
val POM_ORGANIZATION_URL: String by project
val POM_LICENSE_NAME: String by project
val POM_LICENSE_URL: String by project
val POM_DEVELOPER_NAME: String by project
val POM_DEVELOPER_EMAIL: String by project
val POM_DEVELOPER_ORGANIZATION: String by project
val POM_DEVELOPER_ORGANIZATION_URL: String by project
val POM_ISSUE_MANAGEMENT_SYSTEM: String by project
val POM_ISSUE_MANAGEMENT_URL: String by project
val POM_SCM_CONNECTION: String by project
val POM_SCM_DEVELOPER_CONNECTION: String by project
val POM_SCM_URL: String by project

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = POM_GROUP_ID
                artifactId = POM_ARTIFACT_ID
                version = POM_VERSION

                pom {
                    name.set(POM_NAME)
                    description.set(POM_DESCRIPTION)
                    url.set(POM_URL)

                    organization {
                        name.set(POM_ORGANIZATION_NAME)
                        url.set(POM_ORGANIZATION_URL)
                    }

                    licenses {
                        license {
                            name.set(POM_LICENSE_NAME)
                            url.set(POM_LICENSE_URL)
                        }
                    }

                    developers {
                        developer {
                            name.set(POM_DEVELOPER_NAME)
                            email.set(POM_DEVELOPER_EMAIL)
                            organization.set(POM_DEVELOPER_ORGANIZATION)
                            organizationUrl.set(POM_DEVELOPER_ORGANIZATION_URL)
                        }
                    }

                    issueManagement {
                        system.set(POM_ISSUE_MANAGEMENT_SYSTEM)
                        url.set(POM_ISSUE_MANAGEMENT_URL)
                    }

                    scm {
                        connection.set(POM_SCM_CONNECTION)
                        developerConnection.set(POM_SCM_DEVELOPER_CONNECTION)
                        url.set(POM_SCM_URL)
                    }
                }
            }
        }

        repositories {
            maven {
                name = "central"
                url = uri("https://central.sonatype.com/api/v1/publisher/upload")
            }

            maven {
                name = "stagingDir"
                url = layout.buildDirectory.dir("staging").get().asFile.toURI()
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            decodedSigningKey,
            localProperties.getProperty("signingPassword", System.getenv("SIGNING_PASSWORD"))
        )
        sign(publishing.publications["release"])
    }
}

tasks.register<Zip>("prepareForRelease") {
    group = "publishing"
    description = "Builds, signs, and bundles artifacts for manual upload to OSSRH/Central"
    dependsOn("publishReleasePublicationToStagingDirRepository")
    from(layout.buildDirectory.dir("staging"))
    archiveFileName.set("${project.name}-${POM_VERSION}.zip")
    destinationDirectory.set(layout.buildDirectory)
}
