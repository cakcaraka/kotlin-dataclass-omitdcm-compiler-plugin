// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.gradle

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

internal const val DEFAULT_OMIT_TO_STRING_ANNOTATION = "com/cakcaraka/omitdcm/annotations/OmitToString"
internal val DEFAULT_OMIT_TO_STRING_ANNOTATION_SET = setOf(DEFAULT_OMIT_TO_STRING_ANNOTATION)

public abstract class OmitDCMPluginExtension @Inject constructor(objects: ObjectFactory) {

  /**
   * Define custom OmitToString marker annotations. The -annotations artifact won't be automatically
   * added to dependencies if you define your own!
   *
   * Note that these must be in the format of a string where packages are delimited by '/' and
   * classes by '.', e.g. "kotlin/Map.Entry"
   */
  public val omitToStringAnnotations: SetProperty<String> =
    objects.setProperty(String::class.java).convention(setOf(DEFAULT_OMIT_TO_STRING_ANNOTATION))

  /** Flag to enable/disable the plugin on this specific compilation. */
  public val enabled: Property<Boolean> =
    objects.property(Boolean::class.javaObjectType).convention(true)
}
