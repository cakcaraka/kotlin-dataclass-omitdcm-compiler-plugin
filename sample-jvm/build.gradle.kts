import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("com.cakcaraka.omitdcm")
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
    freeCompilerArgs.add("-Xstring-concat=${project.findProperty("string_concat")}")
  }
}

dependencies {
  implementation(project(":omitdcm-annotations"))
  testImplementation(libs.junit)
  testImplementation(libs.truth)
}

configurations.configureEach {
  resolutionStrategy.dependencySubstitution {
    substitute(module("com.cakcaraka.omitdcm:omitdcm-annotations"))
      .using(project(":omitdcm-annotations"))
    substitute(module("com.cakcaraka.omitdcm:omitdcm-annotations-jvm"))
      .using(project(":omitdcm-annotations"))
    substitute(module("com.cakcaraka.omitdcm:omitdcm-compiler-plugin"))
      .using(project(":omitdcm-compiler-plugin"))
  }
}
