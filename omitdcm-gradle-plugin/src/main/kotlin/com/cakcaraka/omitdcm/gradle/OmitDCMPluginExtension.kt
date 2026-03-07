// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class OmitDCMPluginExtension @Inject constructor(objects: ObjectFactory) {

  /** Flag to enable/disable the plugin on this specific compilation. */
  public val enabled: Property<Boolean> =
    objects.property(Boolean::class.javaObjectType).convention(true)

  /**
   * Default strategy used when the annotation has [OmitPropertyStrategy.DEFAULT] or no strategy parameter.
   * Use the exact enum name: "REDACT_NAMES", "HASH_CODE", or "OMIT_ALL". Convention: "OMIT_ALL".
   */
  public val omitToStringPropertyStrategy: Property<String> =
    objects.property(String::class.java).convention("OMIT_ALL")
}
