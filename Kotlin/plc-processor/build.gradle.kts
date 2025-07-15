plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.lestere.ksp"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin symbol processor
    implementation(libs.google.devetools.symbol.processing.api)

    // Kotlinx datetime
    implementation(libs.kotlinx.datetime)

    // Kotlin official reflects
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)

    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}