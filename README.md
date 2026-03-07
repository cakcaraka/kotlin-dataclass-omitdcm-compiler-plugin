# omitdcm — Omit Data Class Methods

A Kotlin compiler plugin that omits or modifies the default generated methods of data classes.

Currently supported:

- **`@OmitDCMToString`** — Replaces the generated `toString()` with an omitted or redacted version. You can choose how properties appear via `omitPropertyStrategy`:
  - **`DEFAULT`** (annotation default) — Use the default settings (e.g. Gradle `omitToStringPropertyStrategy`). If not configured, behaves as `HASH_CODE`.
  - **`HASH_CODE`** — No property names or values; output is `ClassName(hashCode:<hashCode>)`.
  - **`REDACT_NAMES`** — Property values shown with index as name (e.g. `User(0=Bob, 1=2815551234)`).
  - **`OMIT_ALL`** — Single `-` in the property area (e.g. `User(-)`).

More method overrides may be added in the future.

Inspired by [ZacSweers/redacted-compiler-plugin](https://github.com/ZacSweers/redacted-compiler-plugin).

## Usage

Apply `@OmitDCMToString` to any data class whose `toString()` you want to omit.

```kotlin
import com.cakcaraka.omitdcm.annotations.OmitDCMToString

@OmitDCMToString
data class User(val name: String, val phoneNumber: String)
```

When you call `toString()`, property values are hidden:

```
User(hashCode:1234567)
```

You can also provide a custom display name or pick a strategy:

```kotlin
import com.cakcaraka.omitdcm.annotations.OmitDCMToString
import com.cakcaraka.omitdcm.annotations.OmitPropertyStrategy

@OmitDCMToString("SensitiveUser")
data class User(val name: String, val phoneNumber: String)

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.REDACT_NAMES)
data class UserRedacted(val name: String, val phone: String)  // UserRedacted(0=Bob, 1=2815551234)

@OmitDCMToString(omitPropertyStrategy = OmitPropertyStrategy.OMIT_ALL)
data class UserOmitted(val name: String, val phone: String)  // UserOmitted(-)
```

```
SensitiveUser(hashCode:1234567)
```

You can put `@OmitDCMToString` on a base class; subclasses inherit the behavior. If the base has a custom display name, that name is used for the subclass too:

```kotlin
@OmitDCMToString
abstract class BaseUser

data class Admin(val name: String) : BaseUser()

@OmitDCMToString("Sensitive")
abstract class BaseSensitive

data class Secret(val id: Int) : BaseSensitive()
```

```
Admin(hashCode:1234567)      // no custom name on base → uses class name
Sensitive(hashCode:1234567)  // custom name on base → subclass uses it
```

## Installation

### JitPack

Add JitPack repository and apply the gradle plugin.

```gradle
// settings.gradle.kts
pluginManagement {
  repositories {
    maven("https://jitpack.io")
    gradlePluginPortal()
    mavenCentral()
  }
}
```

```gradle
// build.gradle.kts
plugins {
  id("com.cakcaraka.omitdcm") version "<version>"  // tag or commit, e.g. 9028b48fb7
}
```

When using **JitPack**, the plugin adds the annotations dependency with groupId `com.cakcaraka.omitdcm`, but JitPack publishes under `com.github.<user>.<repo>`. Set the groupId so it resolves:

```kotlin
omitdcm {
  annotationsGroupId.set("com.github.cakcaraka.kotlin-dataclass-omitdcm-compiler-plugin")
}
```

Use your actual GitHub user and repo if different. The version is taken from the plugin, so you don't need to set `annotationsVersion`.

Otherwise the default configuration adds the annotations artifact automatically. Just annotate what you want to omit.

### Configuration

You can configure custom behavior with properties on the `omitdcm` extension.

```kotlin
omitdcm {
  // Default strategy when the annotation uses DEFAULT or has no strategy parameter.
  // Use exact enum name: "REDACT_NAMES", "HASH_CODE", or "OMIT_ALL".
  omitToStringPropertyStrategy.set("HASH_CODE")  // Default

  // Define custom annotations. The -annotations artifact won't be automatically added to
  // dependencies if you define your own!
  // Note that these must be in the format of a string where packages are delimited by '/' and
  // classes by '.', e.g. "kotlin/Map.Entry"
  omitToStringAnnotations.add("com/cakcaraka/omitdcm/annotations/OmitDCMToString") // Default

  // Enable/disable the plugin on this specific compilation.
  enabled = true // Default
}
```

## IDE Support

FIR diagnostics can render in the IDE under the following conditions:

1. The K2 Kotlin IDE plugin must be enabled.
2. The IntelliJ `kotlin.k2.only.bundled.compiler.plugins.enabled` registry key must be set to `false`.

## Caveats

- Kotlin compiler plugins are not a stable API! Compiled outputs from this plugin _should_ be stable,
  but usage in newer versions of kotlinc are not guaranteed to be stable.

## License

    Copyright (C) 2026 cakcaraka

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
