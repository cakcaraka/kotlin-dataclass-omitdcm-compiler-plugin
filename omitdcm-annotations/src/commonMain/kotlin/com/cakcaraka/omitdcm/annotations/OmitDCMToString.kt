// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.annotations

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that a data class's `toString()` should be omitted/redacted.
 *
 * @param redactedClassName Optional custom class name to use in the generated `toString()`.
 * @param omitPropertyStrategy
 *   [OmitPropertyStrategy.DEFAULT]: use default settings (e.g. Gradle); if not set, behaves as [OMIT_ALL].
 *   [OmitPropertyStrategy.REDACT_NAMES]: property values with index (e.g. `User(0=Bob, 1=...)`).
 *   [OmitPropertyStrategy.HASH_CODE]: no properties; output is `ClassName(hashCode:...)`.
 *   [OmitPropertyStrategy.OMIT_ALL]: single `-` (e.g. `User(-)`).
 */
@Retention(SOURCE)
@Target(CLASS)
public annotation class OmitDCMToString(
  val redactedClassName: String = "",
  val omitPropertyStrategy: OmitPropertyStrategy = OmitPropertyStrategy.DEFAULT
)
