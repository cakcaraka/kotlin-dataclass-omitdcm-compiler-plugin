// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOriginImpl

internal val OmitDCMOrigin: IrDeclarationOrigin =
  IrDeclarationOriginImpl("GENERATED_OMITDCM_CLASS_MEMBER")
