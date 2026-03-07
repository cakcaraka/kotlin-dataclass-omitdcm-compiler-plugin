pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "kotlin-dataclass-omitdcm"

include(
  ":omitdcm-compiler-plugin",
  ":omitdcm-annotations",
  ":sample-jvm",
)

includeBuild("omitdcm-gradle-plugin") {
  dependencySubstitution {
    substitute(module("com.cakcaraka.omitdcm:omitdcm-gradle-plugin")).using(project(":"))
  }
}
