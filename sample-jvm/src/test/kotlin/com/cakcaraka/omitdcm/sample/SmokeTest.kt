// Copyright (C) 2026 cakcaraka
// SPDX-License-Identifier: Apache-2.0
package com.cakcaraka.omitdcm.sample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmokeTest {
  @Test
  fun userOmitted() {
    val user = User("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("User[OMITTED](hashCode:${user.hashCode()})")
  }

  @Test
  fun userCustomName() {
    val user = UserCustomName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("CustomName[OMITTED](hashCode:${user.hashCode()})")
  }

  @Test
  fun inheritedUserOmitted() {
    val user = InheritedUser("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("InheritedUser[OMITTED](hashCode:${user.hashCode()})")
  }

  @Test
  fun inheritedUserCustomName() {
    val user = InheritedUserCustomName("Bob", "2815551234")
    assertThat(user.toString()).isEqualTo("BaseCustom[OMITTED](hashCode:${user.hashCode()})")
  }
}
