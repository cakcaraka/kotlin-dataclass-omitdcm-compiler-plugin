// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.ClassId

public class OmitDCMIrGenerationExtension(
  private val omitToStringAnnotations: Set<ClassId>,
) : IrGenerationExtension {

  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val transformer =
      OmitDCMIrVisitor(
        pluginContext,
        omitToStringAnnotations,
      )
    moduleFragment.transform(transformer, null)
  }
}
