// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import com.cakcaraka.omitdcm.BuildConfig.KOTLIN_PLUGIN_ID
import com.cakcaraka.omitdcm.compiler.fir.OmitDCMFirExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.name.ClassId

public class OmitDCMCompilerPluginRegistrar : CompilerPluginRegistrar() {

  override val pluginId: String = KOTLIN_PLUGIN_ID

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    if (configuration[KEY_ENABLED] == false) return

    val omitToStringAnnotations =
      checkNotNull(configuration[KEY_OMIT_TO_STRING_ANNOTATIONS]).splitToSequence(":").mapTo(
        LinkedHashSet()
      ) {
        ClassId.fromString(it)
      }

    FirExtensionRegistrarAdapter.registerExtension(
      OmitDCMFirExtensionRegistrar(omitToStringAnnotations)
    )
    IrGenerationExtension.registerExtension(
      OmitDCMIrGenerationExtension(omitToStringAnnotations)
    )
  }
}
