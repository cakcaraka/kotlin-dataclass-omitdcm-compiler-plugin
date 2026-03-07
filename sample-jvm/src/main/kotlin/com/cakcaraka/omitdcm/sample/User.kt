// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.sample

import com.cakcaraka.omitdcm.annotations.OmitToString

@OmitToString
data class User(val name: String, val phoneNumber: String)

@OmitToString("CustomName")
data class UserCustomName(val name: String, val phoneNumber: String)

@OmitToString
abstract class BaseUser

data class InheritedUser(val name: String, val phoneNumber: String) : BaseUser()

@OmitToString("BaseCustom")
abstract class BaseUserCustomName

data class InheritedUserCustomName(val name: String, val phoneNumber: String) : BaseUserCustomName()
