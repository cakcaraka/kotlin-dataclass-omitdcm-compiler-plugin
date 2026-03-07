// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.sample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {
  @Test
  fun userOmitted() {
    val user = User("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("User(hashCode:${user.hashCode()})")
  }

  @Test
  fun userCustomName() {
    val user = UserCustomName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("CustomName(hashCode:${user.hashCode()})")
  }

  @Test
  fun inheritedUserOmitted() {
    val user = InheritedUser("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("InheritedUser(hashCode:${user.hashCode()})")
  }

  @Test
  fun inheritedUserCustomName() {
    val user = InheritedUserCustomName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("BaseCustom(hashCode:${user.hashCode()})")
  }

  @Test
  fun userRedactNames() {
    val user = UserRedactNames("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("UserRedactNames(0=Bob, 1=2815551234)")
  }

  @Test
  fun userOmitAll() {
    val user = UserOmitAll("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("UserOmitAll(-)")
  }

  // --- Base has strategy; subclass inherits (no annotation) ---

  @Test
  fun subUserInheritsHashCode_fromBase() {
    val user = SubUserInheritsHashCode("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubUserInheritsHashCode(hashCode:${user.hashCode()})")
  }

  @Test
  fun subUserInheritsHashCodeAndName_fromBase() {
    val user = SubUserInheritsHashCodeAndName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("BaseHash(hashCode:${user.hashCode()})")
  }

  @Test
  fun subUserInheritsRedactNames_fromBase() {
    val user = SubUserInheritsRedactNames("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubUserInheritsRedactNames(0=Bob, 1=2815551234)")
  }

  @Test
  fun subUserInheritsOmitAll_fromBase() {
    val user = SubUserInheritsOmitAll("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubUserInheritsOmitAll(-)")
  }

  // --- Base has strategy; subclass overrides with different strategy ---

  @Test
  fun subOverridesToRedact_baseWasOmitAll() {
    val user = SubOverridesToRedact("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubOverridesToRedact(0=Bob, 1=2815551234)")
  }

  @Test
  fun subOverridesToOmitAll_baseWasHashCode() {
    val user = SubOverridesToOmitAll("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubOverridesToOmitAll(-)")
  }

  @Test
  fun subOverridesToHashCode_baseWasRedactNames() {
    val user = SubOverridesToHashCode("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("SubOverridesToHashCode(hashCode:${user.hashCode()})")
  }

  @Test
  fun subOverridesStrategyKeepsName_baseHadNameAndOmitAll() {
    val user = SubOverridesStrategyKeepsName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("BaseLabel(0=Bob, 1=2815551234)")
  }
}
