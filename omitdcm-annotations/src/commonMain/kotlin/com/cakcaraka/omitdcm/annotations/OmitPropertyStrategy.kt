// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.annotations

/**
 * Strategy for how property names/values appear in generated `toString()`.
 *
 * - [DEFAULT]: Use the default settings (e.g. from Gradle `omitToStringPropertyStrategy`). If not configured, behaves as [OMIT_ALL].
 * - [REDACT_NAMES]: Property values shown with index as name (e.g. `User(0=Bob, 1=2815551234)`).
 * - [HASH_CODE]: No property names or values; output is `ClassName(hashCode:...)`.
 * - [OMIT_ALL]: Single `-` in the property area (e.g. `User(-)`).
 */
public enum class OmitPropertyStrategy {
    DEFAULT,
    REDACT_NAMES,
    HASH_CODE,
    OMIT_ALL
}
