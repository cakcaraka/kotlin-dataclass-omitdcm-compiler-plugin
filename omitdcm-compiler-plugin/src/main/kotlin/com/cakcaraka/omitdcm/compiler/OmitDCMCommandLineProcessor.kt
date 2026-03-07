// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_ENABLED =
  CompilerConfigurationKey<Boolean>("Enable/disable OmitToString plugin on the given compilation")

internal val KEY_OMIT_TO_STRING_ANNOTATIONS =
  CompilerConfigurationKey<String>(
    "The OmitToString marker annotations (i.e. com/example/OmitToString) to look for"
  )

public class OmitDCMCommandLineProcessor : CommandLineProcessor {

  internal companion object {
    val OPTION_ENABLED =
      CliOption(
        optionName = "enabled",
        valueDescription = "<true | false>",
        description = KEY_ENABLED.toString(),
        required = false,
        allowMultipleOccurrences = false,
      )

    val OPTION_OMIT_TO_STRING_ANNOTATIONS =
      CliOption(
        optionName = "omitToStringAnnotations",
        valueDescription = "String",
        description = KEY_OMIT_TO_STRING_ANNOTATIONS.toString(),
        required = true,
        allowMultipleOccurrences = false,
      )
  }

  override val pluginId: String = "com.cakcaraka.omitdcm.compiler"

  override val pluginOptions: Collection<AbstractCliOption> =
    listOf(
      OPTION_ENABLED,
      OPTION_OMIT_TO_STRING_ANNOTATIONS,
    )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ): Unit =
    when (option.optionName) {
      "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
      "omitToStringAnnotations" -> configuration.put(KEY_OMIT_TO_STRING_ANNOTATIONS, value)
      else -> error("Unknown plugin option: ${option.optionName}")
    }
}
