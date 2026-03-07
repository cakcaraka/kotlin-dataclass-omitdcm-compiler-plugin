// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * An annotation to indicate that a data class's `toString()` should be omitted/redacted.
 * When applied to a class, the generated `toString()` will output
 * `ClassName[OMITTED](hashCode)` instead of showing property values.
 *
 * @param redactedClassName Optional custom class name to use in the generated `toString()`. When provided,
 *   the output will be `customName[OMITTED](hashCode)` instead of the actual class simple name.
 */
@Retention(BINARY) @Target(CLASS) public annotation class OmitToString(val redactedClassName: String = "")
