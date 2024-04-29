/*
 * Gradle Build
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.6/userguide/building_java_projects.html in the Gradle documentation.
 */

import java.time.LocalDate

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.adarshr.test-logger") version "4.0.0"
    jacoco
    id("com.github.sakata1222.jacoco-markdown") version "1.4.0"
    pmd
    //id("org.sonarqube") version "4.4.1.3373"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    // implementation(libs.guava)
}

// Apply a specific Java toolchain to ease working on different environments.
// java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(8)
//    }
// }

// application {
//    Define the main class for the application.
//    mainClass = "org.example.App"
// }

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports.csv.required = true
}

tasks.register<Copy>("jacocoTestReportMarkdownAndCopyToDoc") {
    description = "Copies jacoco markdown report on docs directory."
    group = "verification"
    dependsOn(tasks.jacocoTestReportMarkdown)
    from(layout.buildDirectory.file("reports/jacoco/test/jacocoSummary.md"))
    into(layout.buildDirectory.dir("../../docs"))
}

tasks.register<Test>("aTest") {
    description = "Runs the unit test 'ATest'."
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("math.ATest")
    }
}

tasks.register<Test>("runUnitTestGen") {
    description = "Runs the 'UnitTestGen'."
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("math.TestUnitTestGen")
    }
}

tasks.register<Test>("ASCIIMathTeXImgUnitTest") {
    description = "Runs the 'ASCIIMathTeXImgUnitTest'."
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("math.ASCIIMathTeXImgUnitTest")
    }
}

tasks.register<Zip>("reportZip") {
    description = "Makes archive of reports on distib. directory."
    group = "verification"
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.jacocoTestReportMarkdown)
	archiveFileName = "" + LocalDate.now() + "_reports.zip"
	destinationDirectory = layout.buildDirectory.dir("distributions")
	from(layout.buildDirectory.dir("reports"))
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.0.0-rc4"
    rulesMinimumPriority = 5
    ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
}
/*
sonar {
  properties {
    property("sonar.projectKey", "The-Lum_ASCIIMathTeXImg")
    property("sonar.organization", "the-lum")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.coverage.jacoco.xmlReportPaths", "reports/jacoco/test/jacocoTestReport.xml")
  }
}
*/