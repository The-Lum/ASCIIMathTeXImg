/*
 * Gradle Settings
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.6/userguide/multi_project_builds.html in the Gradle documentation.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    //id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "ASCIIMathTeXImg"
include("app")
