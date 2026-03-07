// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.compiler.fir

import org.jetbrains.kotlin.descriptors.isEnumEntry
import org.jetbrains.kotlin.descriptors.isObject
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.processAllDeclarations
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.declarations.utils.isExtension
import org.jetbrains.kotlin.fir.declarations.utils.isExternal
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isInlineOrValue
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.isString
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.util.OperatorNameConventions

public class OmitDCMFirExtensionRegistrar(
  private val omitToStringAnnotations: Set<ClassId>,
) : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +OmitDCMFirBuiltIns.getFactory(omitToStringAnnotations)
    +::FirOmitDCMCheckers
  }
}

internal class FirOmitDCMCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers =
    object : DeclarationCheckers() {
      override val classCheckers: Set<FirClassChecker>
        get() = setOf(FirOmitDCMDeclarationChecker)
    }
}

internal object FirOmitDCMDeclarationChecker : FirClassChecker(MppCheckerKind.Common) {

  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val classOmitAnnotations =
      context.session.omitToStringAnnotations.mapNotNull { classId ->
        declaration.getAnnotationByClassId(classId, context.session)?.let { it to classId }
      }
    val classIsOmitted = classOmitAnnotations.isNotEmpty()

    if (!classIsOmitted) return

    val omitName = classOmitAnnotations.first().second.shortClassName.asString()

    var customToStringFunction: FirNamedFunctionSymbol? = null
    declaration.processAllDeclarations(context.session) { symbol ->
      if (
        symbol is FirNamedFunctionSymbol &&
          symbol.isToStringFromAny(context.session) &&
          symbol.origin == FirDeclarationOrigin.Source
      ) {
        customToStringFunction = symbol
      }
    }

    if (customToStringFunction != null) {
      reporter.reportOn(
        customToStringFunction.source,
        OmitDCMDiagnostics.OMITDCM_ERROR,
        "@$omitName is only supported on data or value classes that do *not* have a custom toString() function. Please remove the function or remove the @$omitName annotation.",
      )
      return
    }
    if (
      declaration.isInstantiableEnum ||
        declaration.isEnumClass ||
        declaration.classKind.isEnumEntry
    ) {
      reporter.reportOn(
        declaration.source,
        OmitDCMDiagnostics.OMITDCM_ERROR,
        "@$omitName does not support enum classes or entries!",
      )
      return
    }
    if (declaration.isFinal && !(declaration.status.isData || declaration.isInlineOrValue)) {
      reporter.reportOn(
        declaration.source,
        OmitDCMDiagnostics.OMITDCM_ERROR,
        "@$omitName is only supported on data or value classes!",
      )
      return
    }
    if (declaration.classKind.isObject) {
      reporter.reportOn(
        classOmitAnnotations.first().first.source,
        OmitDCMDiagnostics.OMITDCM_ERROR,
        "@$omitName is useless on object classes.",
      )
      return
    }
  }

  private fun FirNamedFunctionSymbol.isToStringFromAny(session: FirSession): Boolean =
    name == OperatorNameConventions.TO_STRING &&
      dispatchReceiverType != null &&
      !isExtension &&
      valueParameterSymbols.isEmpty() &&
      resolvedReturnType.fullyExpandedType(session).isString

  private val FirClass.isInstantiableEnum: Boolean
    get() = isEnumClass && !isExpect && !isExternal
}
