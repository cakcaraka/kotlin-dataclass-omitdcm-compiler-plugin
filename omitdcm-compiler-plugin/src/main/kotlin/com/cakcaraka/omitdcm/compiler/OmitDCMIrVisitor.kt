// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.cakcaraka.omitdcm.compiler

import com.cakcaraka.omitdcm.annotations.OmitPropertyStrategy
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getAllSuperclasses
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class OmitDCMIrVisitor(
  private val pluginContext: IrPluginContext,
  private val omitToStringAnnotations: Set<ClassId>,
  private val defaultOmitPropertyStrategy: OmitPropertyStrategy,
) : IrElementTransformerVoidWithContext() {

    /**
     * JVM-only: resolve java.lang.Object.getClass and java.lang.Class.getSimpleName()
     * so we can generate receiver.getClass().getSimpleName() for runtime class name.
     * Returns null on non-JVM or if symbols are not available.
     */
    private val jvmSimpleNameSymbols: Pair<IrSimpleFunctionSymbol, IrSimpleFunctionSymbol>? by lazy {
        val objectClass = pluginContext.referenceClass(ClassId.fromString("java/lang/Object")) ?: return@lazy null
        val classClass = pluginContext.referenceClass(ClassId.fromString("java/lang/Class")) ?: return@lazy null
        val getClassFn = objectClass.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .firstOrNull { it.name == Name.identifier("getClass") }
            ?: return@lazy null
        val getSimpleNameFn = classClass.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .firstOrNull { it.name == Name.identifier("getSimpleName") }
            ?: return@lazy null
        getClassFn.symbol to getSimpleNameFn.symbol
    }

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
      val (customClassName, strategy) = findAnnotationParams(declarationParent)
      // Use configured default when: if strategy is DEFAULT then change to OMIT_ALL
      val finalStrategy = if(strategy == OmitPropertyStrategy.DEFAULT) {
        defaultOmitPropertyStrategy
      } else {
        strategy
      }

      val classNameExpression: (IrBuilder).(IrExpression) -> IrExpression = { receiver ->
        if (!customClassName.isNullOrEmpty()) {
          irString(customClassName)
        } else {
          val pair = jvmSimpleNameSymbols
          if (pair == null) {
            irString("")
          } else {
            val (getClassSymbol, getSimpleNameSymbol) = pair
            val getClassCall = irCall(getClassSymbol, getClassSymbol.owner.returnType).apply {
              dispatchReceiver = receiver
            }
            irCall(getSimpleNameSymbol, context.irBuiltIns.stringType).apply {
              dispatchReceiver = getClassCall
            }
          }
        }
      }
      when (finalStrategy) {
        OmitPropertyStrategy.REDACT_NAMES -> {
          val primaryConstructor = declarationParent.primaryConstructor
          if (primaryConstructor != null) {
            declaration.convertToGeneratedRedactNamesToString(classNameExpression, declarationParent, primaryConstructor)
          } else {
            super.visitFunctionNew(declaration)
          }
        }
        OmitPropertyStrategy.HASH_CODE -> declaration.convertToGeneratedHashCodeToString(classNameExpression)
        OmitPropertyStrategy.DEFAULT, OmitPropertyStrategy.OMIT_ALL -> declaration.convertToGeneratedOmitAllToString(classNameExpression)
      }
    }

    return super.visitFunctionNew(declaration)
  }

  private fun IrFunction.isToStringFromAny(): Boolean =
    name == OperatorNameConventions.TO_STRING &&
      parameters.singleOrNull()?.kind == IrParameterKind.DispatchReceiver &&
      returnType.isString()

  private fun IrSimpleFunction.convertToGeneratedHashCodeToString(customClassName: (IrBuilder).(IrExpression) -> IrExpression) {
    val parent = parent as IrClass
    origin = OmitDCMOrigin
    body =
      DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
        val receiver = irGet(this@convertToGeneratedHashCodeToString.dispatchReceiverParameter!!)
        val irConcat = irConcat()
        irConcat.addArgument(customClassName(receiver))
        irConcat.addArgument(irString("(hashCode:"))
        val hashCodeFn =
          parent.declarations.filterIsInstance<IrSimpleFunction>().first {
            it.name == OperatorNameConventions.HASH_CODE &&
              it.parameters.none { p -> p.kind == IrParameterKind.Regular }
          }
        irConcat.addArgument(
          irCall(hashCodeFn.symbol, context.irBuiltIns.intType).apply {
            arguments[0] = irGet(this@convertToGeneratedHashCodeToString.dispatchReceiverParameter!!)
          }
        )
        irConcat.addArgument(irString(")"))
        +irReturn(irConcat)
      }
    isFakeOverride = false
  }

  private fun IrSimpleFunction.convertToGeneratedOmitAllToString(customClassName: (IrBuilder).(IrExpression) -> IrExpression) {
    val parent = parent as IrClass
    origin = OmitDCMOrigin
    body =
      DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
        val receiver = irGet(this@convertToGeneratedOmitAllToString.dispatchReceiverParameter!!)
        val irConcat = irConcat()
        irConcat.addArgument(customClassName(receiver))
        irConcat.addArgument(irString("(-)"))
        +irReturn(irConcat)
      }
    isFakeOverride = false
  }

  /**
   * Finds (customClassName, strategyOrdinal, strategyExplicitInAnnotation) from an [omitToStringAnnotations] annotation.
   * strategyOrdinal: 0 = REDACT_NAMES, 1 = HASH_CODE, 2 = OMIT_ALL, 3 = DEFAULT. Falls back to superclasses if inherited.
   * strategyExplicitInAnnotation is true when the annotation declares the strategy parameter (current or custom annotation).
   */
  private fun findAnnotationParams(irClass: IrClass): Pair<String?, OmitPropertyStrategy> {
    extractAnnotationParams(irClass)?.let { return it }
    for (superClass in irClass.getAllSuperclasses()) {
      extractAnnotationParams(superClass)?.let { return it }
    }
    return Pair(null, OmitPropertyStrategy.DEFAULT) // no annotation found: use configured default (HASH_CODE if not set)
  }

  private fun extractAnnotationParams(irClass: IrClass): Pair<String?, OmitPropertyStrategy>? {
    for (annotation in irClass.annotations) {
      val constructor = annotation.symbol.owner
      val annotationClass = constructor.parent as? IrClass ?: continue
      val annotationFqName = annotationClass.fqNameWhenAvailable ?: continue
      if (omitToStringAnnotations.none { it.asSingleFqName() == annotationFqName }) continue

      val regularParams = constructor.parameters.filter { it.kind == IrParameterKind.Regular }
      val paramNames = regularParams.map { it.name.asString() }
      val classNameIndex = paramNames.indexOf("redactedClassName")
      val strategyIndex = paramNames.indexOf("omitPropertyStrategy")

      var customClassName: String? = null
      var strategy = OmitPropertyStrategy.DEFAULT

      if (classNameIndex >= 0) {
        val arg = annotation.arguments.getOrNull(classNameIndex)
        customClassName = when (arg) {
          is IrConst -> arg.value as? String
          else -> null
        }
      }
      if (strategyIndex >= 0) {
        val arg = annotation.arguments.getOrNull(strategyIndex)
        strategy = when (arg) {
          is IrConst -> if (arg.value is Int) OmitPropertyStrategy.entries.getOrNull(arg.value as Int) ?: OmitPropertyStrategy.DEFAULT else OmitPropertyStrategy.DEFAULT
          is IrGetEnumValue -> OmitPropertyStrategy.entries.find { it.name == arg.symbol.owner.name.asString() } ?: OmitPropertyStrategy.DEFAULT
          else -> OmitPropertyStrategy.DEFAULT
        }
      }
      return Pair(customClassName, strategy)
    }
    return null
  }

  private fun IrSimpleFunction.convertToGeneratedRedactNamesToString(
    customClassName: (IrBuilder).(IrExpression) -> IrExpression,
    declarationParent: IrClass,
    primaryConstructor: IrFunction
  ) {
    val constructorParameters =
        primaryConstructor.parameters
          .filter { it.kind == IrParameterKind.Regular }
          .associateBy { it.name.asString() }

    val properties = mutableListOf<Pair<IrProperty, IrValueParameter>>()

    for (prop in declarationParent.properties) {
      val parameter = constructorParameters[prop.name.asString()] ?: continue
      properties += Pair(prop, parameter)
    }

    origin = OmitDCMOrigin

    body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
      val receiver = irGet(this@convertToGeneratedRedactNamesToString.dispatchReceiverParameter!!)
      val irConcat = irConcat()
      irConcat.addArgument(customClassName(receiver))
      irConcat.addArgument(irString("("))
      properties.forEachIndexed { index, property ->
        if (index > 0) irConcat.addArgument(irString(", "))

        irConcat.addArgument(irString( "$index="))
        val irPropertyValue = irGetField(
          irGet(
            this@convertToGeneratedRedactNamesToString.dispatchReceiverParameter!!
          ),
          property.first.backingField!!
        )

        val param = property.second
        val irPropertyStringValue = if (param.type.isArray() || param.type.isPrimitiveArray()) {
          irCall(
            context.irBuiltIns.dataClassArrayMemberToStringSymbol,
            context.irBuiltIns.stringType,
          )
          .apply { arguments[0] = irPropertyValue }
        } else {
          irPropertyValue
        }

        irConcat.addArgument(irPropertyStringValue)
      }
      irConcat.addArgument(irString(")"))
      +irReturn(irConcat)
    }
    isFakeOverride = false
  }
}
