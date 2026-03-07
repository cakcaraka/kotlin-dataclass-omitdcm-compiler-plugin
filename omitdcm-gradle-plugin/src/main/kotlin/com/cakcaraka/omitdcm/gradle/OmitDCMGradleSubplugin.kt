// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class OmitDCMGradleSubplugin : KotlinCompilerPluginSupportPlugin {

  override fun apply(target: Project) {
    target.extensions.create("omitdcm", OmitDCMPluginExtension::class.java)
  }

  override fun getCompilerPluginId(): String = "com.cakcaraka.omitdcm.compiler"

  override fun getPluginArtifact(): SubpluginArtifact =
    SubpluginArtifact(
      groupId = COMPILER_PLUGIN_GROUP_ID,
      artifactId = "omitdcm-compiler-plugin",
      version = VERSION,
    )

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    val extension = project.extensions.getByType(OmitDCMPluginExtension::class.java)

    project.dependencies.add(
      kotlinCompilation.defaultSourceSet.implementationConfigurationName,
      "$ANNOTATIONS_GROUP_ID:omitdcm-annotations:$VERSION"
    )

    val enabled = extension.enabled.get()
    val omitToStringPropertyStrategy = extension.omitToStringPropertyStrategy.get()

    return project.provider {
      listOf(
        SubpluginOption(key = "enabled", value = enabled.toString()),
        SubpluginOption(key = "omitToStringPropertyStrategy", value = omitToStringPropertyStrategy),
      )
    }
  }
}
