import com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask

plugins {
    kotlin("multiplatform") version "1.9.0"
    `maven-publish`
    signing
    id("com.goncalossilva.resources") version "0.4.0"
}
publishing {
    repositories {
        maven {
            name = "mavenRepositorySnapshots"
            url = uri("https://maven.droplet.neugartf.com/snapshots")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}


signing {
    useGpgCmd()
    sign(configurations.archives.get())
    sign(publishing.publications["kotlinMultiplatform"])
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
        binaries.library()
    }

    
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated-temp/commonMain/kotlin")
            dependencies {
                api(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.0")
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
    outputDirectory = layout.buildDirectory.dir("generated-temp/commonMain/kotlin").get().asFile
}
tasks.filter { it.name.startsWith("compile") }.forEach {
    it.dependsOn("generateCommonParserSource")
}
tasks.filter { it.name.endsWith("sourcesJar", true)}.forEach {
    println(it)
    it.dependsOn("generateCommonParserSource")
}
tasks.register("jsIrBrowserTest")
tasks.register("jsLegacyBrowserTest")

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    this.dependsOn("generateCommonParserSource")
}