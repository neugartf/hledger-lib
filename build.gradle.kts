import com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask

plugins {
    kotlin("multiplatform") version "1.9.0"
    `maven-publish`
    id("com.goncalossilva.resources") version "0.4.0"
}
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/neugartf/hledger-lib")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
group = "com.neugartf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:47dc451")
    }
}

kotlin {
    jvmToolchain(17)
    jvm {
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        nodejs()
    }

    
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated-temp/commonMain/kotlin")
            dependencies {
                api(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                // add antlr-kotlin-runtime
                // otherwise, the generated sources will not compile
                api("com.strumenta.antlr-kotlin:antlr-kotlin-runtime:0ad2c42952")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.goncalossilva:resources:0.4.0")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
}

val generateCommonParserSource by tasks.creating(AntlrKotlinTask::class) {
    val dependencies = project.dependencies



    antlrClasspath = configurations.detachedConfiguration(
        dependencies.create("org.antlr:antlr4:4.7.1"),
        dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:47dc4517bf"),
    )
    maxHeapSize = "64m"
    arguments = listOf("-package", "com.neugartf.antlr.parser")
    source = project.objects
        .sourceDirectorySet("commonAntlr", "commonAntlr")
        .srcDir("src/commonMain/antlr").apply {
            include("*.g4")
        }
    outputDirectory = buildDir.resolve("generated-temp").resolve("commonMain").resolve("kotlin")
}
tasks.filter { it.name.startsWith("compile") }.forEach {
    println(it)
    it.dependsOn("generateCommonParserSource")
}
tasks.filter { it.name.endsWith("sourcesJar")}.forEach {
    println(it)
    it.dependsOn("generateCommonParserSource")
}
tasks.register("jsIrBrowserTest")
tasks.register("jsLegacyBrowserTest")

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    println(this)
    this.dependsOn("generateCommonParserSource")
}