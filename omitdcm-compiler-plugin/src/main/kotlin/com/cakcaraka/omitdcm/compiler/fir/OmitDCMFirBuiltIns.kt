// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.name.ClassId

internal class OmitDCMFirBuiltIns(
  session: FirSession,
  val omitToStringAnnotations: Set<ClassId>,
) : FirExtensionSessionComponent(session) {
  companion object {
    fun getFactory(
      omitToStringAnnotations: Set<ClassId>,
    ) = Factory { session ->
      OmitDCMFirBuiltIns(session, omitToStringAnnotations)
    }
  }
}

internal val FirSession.omitDCMFirBuiltIns: OmitDCMFirBuiltIns by
  FirSession.sessionComponentAccessor()

internal val FirSession.omitToStringAnnotations: Set<ClassId>
  get() = omitDCMFirBuiltIns.omitToStringAnnotations
