// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val KEY_ENABLED =
  CompilerConfigurationKey<Boolean>("Enable/disable OmitDCMToString plugin on the given compilation")

internal val KEY_OMIT_TO_STRING_PROPERTY_STRATEGY =
  CompilerConfigurationKey<String>(
    "Default OmitPropertyStrategy when the annotation does not specify one: REDACT_NAMES, HASH_CODE, or OMIT_ALL"
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

    val OPTION_OMIT_TO_STRING_PROPERTY_STRATEGY =
      CliOption(
        optionName = "omitToStringPropertyStrategy",
        valueDescription = "<REDACT_NAMES | HASH_CODE | OMIT_ALL>",
        description = KEY_OMIT_TO_STRING_PROPERTY_STRATEGY.toString(),
        required = false,
        allowMultipleOccurrences = false,
      )
  }

  override val pluginId: String = "com.cakcaraka.omitdcm.compiler"

  override val pluginOptions: Collection<AbstractCliOption> =
    listOf(
      OPTION_ENABLED,
      OPTION_OMIT_TO_STRING_PROPERTY_STRATEGY,
    )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ): Unit =
    when (option.optionName) {
      "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
      "omitToStringPropertyStrategy" -> configuration.put(KEY_OMIT_TO_STRING_PROPERTY_STRATEGY, value)
      else -> error("Unknown plugin option: ${option.optionName}")
    }
}
