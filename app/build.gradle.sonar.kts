/*
 * Gradle Build
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.6/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    //id("com.adarshr.test-logger") version "4.0.0"
    jacoco
    //id("com.github.sakata1222.jacoco-markdown") version "1.4.0"
    id("org.sonarqube") version "4.4.1.3373"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    // implementation(libs.guava)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.test {
    testLogging.showStandardStreams = false
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports.csv.required = true
}

sonar {
  properties {
    property("sonar.projectKey", "The-Lum_ASCIIMathTeXImg")
    property("sonar.organization", "the-lum")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.coverage.jacoco.xmlReportPaths", "reports/jacoco/test/jacocoTestReport.xml")
  }
}
