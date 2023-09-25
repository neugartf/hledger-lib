import com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask

plugins {
    kotlin("multiplatform") version "1.9.0"
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
        classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:47dc4517bf")
    }
}

kotlin {
    jvm {
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
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
tasks.register("jsIrBrowserTest")
tasks.register("jsLegacyBrowserTest")

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    println(this)
    this.dependsOn("generateCommonParserSource")
}