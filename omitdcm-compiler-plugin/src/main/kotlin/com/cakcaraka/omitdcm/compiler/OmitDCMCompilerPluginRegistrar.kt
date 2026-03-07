// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import com.cakcaraka.omitdcm.BuildConfig.KOTLIN_PLUGIN_ID
import com.cakcaraka.omitdcm.annotations.OmitPropertyStrategy
import com.cakcaraka.omitdcm.compiler.fir.OmitDCMFirExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.name.ClassId

internal const val DEFAULT_OMIT_TO_STRING_ANNOTATION = "com/cakcaraka/omitdcm/annotations/OmitDCMToString"

public class OmitDCMCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val pluginId: String = KOTLIN_PLUGIN_ID

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) return

        val omitToStringAnnotations =
            DEFAULT_OMIT_TO_STRING_ANNOTATION.splitToSequence(":").mapTo(
                LinkedHashSet()
            ) {
                ClassId.fromString(it)
            }

        val defaultOmitPropertyStrategy =
            parseOmitToStringPropertyStrategy(configuration[KEY_OMIT_TO_STRING_PROPERTY_STRATEGY])

        FirExtensionRegistrarAdapter.registerExtension(
            OmitDCMFirExtensionRegistrar(omitToStringAnnotations)
        )
        IrGenerationExtension.registerExtension(
            OmitDCMIrGenerationExtension(omitToStringAnnotations, defaultOmitPropertyStrategy)
        )
    }

    /**
     * Parses config string to [OmitPropertyStrategy] for use as the configured default.
     * When not set (null/empty), returns [OmitPropertyStrategy.DEFAULT].
     */
    private fun parseOmitToStringPropertyStrategy(value: String?): OmitPropertyStrategy {
        val trimmed = value?.trim() ?: return OmitPropertyStrategy.DEFAULT
        return OmitPropertyStrategy.entries.find { it.name.equals(trimmed, ignoreCase = true) }
            ?: OmitPropertyStrategy.DEFAULT
    }
}
