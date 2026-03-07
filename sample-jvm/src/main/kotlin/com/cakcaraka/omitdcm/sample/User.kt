// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.sample

import com.cakcaraka.omitdcm.annotations.OmitDCMToString
import com.cakcaraka.omitdcm.annotations.OmitPropertyStrategy

@OmitDCMToString
data class User(val name: String, val phoneNumber: String)

@OmitDCMToString("CustomName")
data class UserCustomName(val name: String, val phoneNumber: String)

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
data class UserRedactNames(val name: String, val phoneNumber: String)

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
data class UserOmitAll(val name: String, val phoneNumber: String)

@OmitDCMToString
abstract class BaseUser

data class InheritedUser(val name: String, val phoneNumber: String) : BaseUser()

@OmitDCMToString("BaseCustom")
abstract class BaseUserCustomName

data class InheritedUserCustomName(val name: String, val phoneNumber: String) : BaseUserCustomName()

// --- Base has strategy; subclass inherits (no annotation) ---

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.HASH_CODE)
abstract class BaseUserHashCode

data class SubUserInheritsHashCode(val name: String, val phoneNumber: String) : BaseUserHashCode()

@OmitDCMToString(redactedClassName = "BaseHash", omitPropertyStrategy = OmitPropertyStrategy.HASH_CODE)
abstract class BaseUserHashCodeCustomName

data class SubUserInheritsHashCodeAndName(val name: String, val phoneNumber: String) : BaseUserHashCodeCustomName()

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
abstract class BaseUserRedactNames

data class SubUserInheritsRedactNames(val name: String, val phoneNumber: String) : BaseUserRedactNames()

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
abstract class BaseUserOmitAllStrategy

data class SubUserInheritsOmitAll(val name: String, val phoneNumber: String) : BaseUserOmitAllStrategy()

// --- Base has strategy; subclass overrides with different strategy ---

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
abstract class BaseOmitAll

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
data class SubOverridesToRedact(val name: String, val phoneNumber: String) : BaseOmitAll()

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.HASH_CODE)
abstract class BaseHashCode

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
data class SubOverridesToOmitAll(val name: String, val phoneNumber: String) : BaseHashCode()

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
abstract class BaseRedactNames

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.HASH_CODE)
data class SubOverridesToHashCode(val name: String, val phoneNumber: String) : BaseRedactNames()

// --- Base has strategy + custom name; subclass overrides strategy only ---

@OmitDCMToString(redactedClassName = "BaseLabel", omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
abstract class BaseWithNameAndOmitAll

@OmitDCMToString(redactedClassName = "BaseLabel", omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
data class SubOverridesStrategyKeepsName(val name: String, val phoneNumber: String) : BaseWithNameAndOmitAll()
