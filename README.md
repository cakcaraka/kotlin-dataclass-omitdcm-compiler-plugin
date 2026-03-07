# omitdcm — Omit Data Class Methods

A Kotlin compiler plugin that omits or modifies the default generated methods of data classes.

Currently supported:

- **`@OmitToString`** — Replaces the generated `toString()` with a redacted version that hides all property names and values. The output uses the compile-time class name (meaning if obfuscation like R8/ProGuard is applied, the obfuscated name will be printed) followed by `[OMITTED]` and the object's `hashCode()`.

More method overrides may be added in the future.

Inspired by [ZacSweers/redacted-compiler-plugin](https://github.com/ZacSweers/redacted-compiler-plugin).

## Usage

Apply `@OmitToString` to any data class whose `toString()` you want to omit.

```kotlin
import com.cakcaraka.omitdcm.annotations.OmitToString

@OmitToString
data class User(val name: String, val phoneNumber: String)
```

When you call `toString()`, property values are hidden:

```
User[OMITTED](1234567)
```

You can also provide a custom display name:

```kotlin
@OmitToString("SensitiveUser")
data class User(val name: String, val phoneNumber: String)
```

```
SensitiveUser[OMITTED](1234567)
```

The number in parentheses is the object's `hashCode()`.

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
  id("com.cakcaraka.omitdcm") version "<version>"
}
```

That's it! The default configuration will automatically add the annotations artifact and wire everything up. Just annotate what you want to omit.

### Configuration

You can configure custom behavior with properties on the `omitdcm` extension.

```kotlin
omitdcm {
  // Define custom annotations. The -annotations artifact won't be automatically added to
  // dependencies if you define your own!
  // Note that these must be in the format of a string where packages are delimited by '/' and
  // classes by '.', e.g. "kotlin/Map.Entry"
  omitToStringAnnotations.add("com/cakcaraka/omitdcm/annotations/OmitToString") // Default

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
