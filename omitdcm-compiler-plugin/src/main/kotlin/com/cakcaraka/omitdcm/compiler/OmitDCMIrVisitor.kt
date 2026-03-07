// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.cakcaraka.omitdcm.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.getAllSuperclasses
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class OmitDCMIrVisitor(
  private val pluginContext: IrPluginContext,
  private val omitToStringAnnotations: Set<ClassId>,
) : IrElementTransformerVoidWithContext() {

  override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    if (declaration !is IrSimpleFunction) return super.visitFunctionNew(declaration)
    if (!declaration.isToStringFromAny()) return super.visitFunctionNew(declaration)

    val declarationParent =
      declaration.parentClassOrNull ?: return super.visitFunctionNew(declaration)

    val classIsOmitted = omitToStringAnnotations.any(declarationParent::hasAnnotation)
    val supertypeIsOmitted by unsafeLazy {
      declarationParent.getAllSuperclasses().any { omitToStringAnnotations.any(it::hasAnnotation) }
    }

    if (classIsOmitted || supertypeIsOmitted) {
      val customClassName = findCustomClassName(declarationParent)
      declaration.convertToGeneratedOmittedToString(customClassName)
    }

    return super.visitFunctionNew(declaration)
  }

  private fun IrFunction.isToStringFromAny(): Boolean =
    name == OperatorNameConventions.TO_STRING &&
      parameters.singleOrNull()?.kind == IrParameterKind.DispatchReceiver &&
      returnType.isString()

  private fun IrSimpleFunction.convertToGeneratedOmittedToString(customClassName: String?) {
    val parent = parent as IrClass

    origin = OmitDCMOrigin

    val displayName = customClassName?.takeIf { it.isNotEmpty() } ?: parent.name.asString()

    body =
      DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
        val irConcat = irConcat()
        irConcat.addArgument(irString(displayName + "[OMITTED](hashCode:"))

        val hashCodeFn =
          parent.declarations.filterIsInstance<IrSimpleFunction>().first {
            it.name == OperatorNameConventions.HASH_CODE &&
              it.parameters.none { p -> p.kind == IrParameterKind.Regular }
          }
        irConcat.addArgument(
          irCall(hashCodeFn.symbol, context.irBuiltIns.intType).apply {
            arguments[0] = irGet(this@convertToGeneratedOmittedToString.dispatchReceiverParameter!!)
          }
        )

        irConcat.addArgument(irString(")"))
        +irReturn(irConcat)
      }

    isFakeOverride = false
  }

  /**
   * Finds the custom class name from an [omitToStringAnnotations] annotation on [irClass],
   * falling back to superclasses if the annotation is inherited.
   */
  private fun findCustomClassName(irClass: IrClass): String? {
    extractCustomClassName(irClass)?.let { return it }
    for (superClass in irClass.getAllSuperclasses()) {
      extractCustomClassName(superClass)?.let { return it }
    }
    return null
  }

  private fun extractCustomClassName(irClass: IrClass): String? {
    for (annotation in irClass.annotations) {
      val constructor = annotation.symbol.owner
      val annotationClass = constructor.parent as? IrClass ?: continue
      val annotationFqName = annotationClass.fqNameWhenAvailable ?: continue
      if (omitToStringAnnotations.none { it.asSingleFqName() == annotationFqName }) continue

      val regularParams = constructor.parameters.filter { it.kind == IrParameterKind.Regular }
      val classNameIndex =
        regularParams.indexOfFirst { it.name.asString() == "redactedClassName" }
      if (classNameIndex >= 0) {
        val arg = annotation.arguments[classNameIndex]
        return when (arg) {
          is IrConst -> arg.value as? String
          else -> null
        }
      }
    }
    return null
  }
}
