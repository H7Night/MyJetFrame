# MyJetFrame Phase 1 — Framework Scaffold & Port Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the standalone `MyJetFrame` Android library repo with `:core:common`, `:core:designsystem`, and `:core:ui` modules, porting Kenko's Line theme, design tokens, and generic widgets into a reusable, palette-based framework.

**Architecture:** A new Gradle build at `C:\Users\user\Abandon\Projects\MyJetFrame` with three `com.android.library` + Compose modules under `core/`. `:core:ui` depends on `:core:designsystem`, which depends on `:core:common`. The theme is built on `MaterialExpressiveTheme` with a `Palette` sealed interface (`Line` default, `Dynamic`, `Brand(seed)`). No app is modified in this phase.

**Tech Stack:** Gradle 8.13, AGP 8.13.2, Kotlin 2.2.21, Compose BOM 2025.11.00, Material3 1.5.0-alpha08 (Expressive), Java 17, compileSdk 36 / minSdk 26 / targetSdk 36. JUnit4 + Robolectric + Compose UI test.

**Spec:** `docs/superpowers/specs/2026-10-14-myjetframe-shared-ui-framework-design.md`

## Global Constraints

- Repo root: `C:\Users\user\Abandon\Projects\MyJetFrame`. OS is Windows; shell is pwsh 7. Use `./gradlew` (resolves to `gradlew.bat`).
- Modules and coordinates (verbatim):
  - `:core:common` → `com.jhon.myjetframe:core-common` → namespace `com.jhon.myjetframe.core.common`
  - `:core:designsystem` → `com.jhon.myjetframe:core-designsystem` → namespace `com.jhon.myjetframe.core.designsystem`
  - `:core:ui` → `com.jhon.myjetframe:core-ui` → namespace `com.jhon.myjetframe.core.ui`
- Toolchain (verbatim): Gradle 8.13; AGP 8.13.2; Kotlin 2.2.21; Compose BOM 2025.11.00; Material3 1.5.0-alpha08; Java/JVM 17; compileSdk 36; minSdk 26; targetSdk 36.
- No Maven publishing. No app repos are edited in Phase 1.
- Version centralization: the framework is the single source of truth for Compose/Material 3. Expose the Compose BOM and `material3` via `api(...)` and add a strict version constraint on `material3` so consumers cannot override the framework version. Apps redeclare neither `androidx.compose.*` nor `material3`.
- Compose Compiler is applied via the `org.jetbrains.kotlin.plugin.compose` plugin (alias `kotlin-compose`, version = Kotlin 2.2.21) in every module. Do NOT rely on `buildFeatures { compose = true }` alone; do NOT set `kotlinCompilerExtensionVersion`.
- Compose BOM `2025.11.00` maps `material3` to `1.4.0` (stable); the framework deliberately overrides it to `1.5.0-alpha08` (Expressive). Bump both together.
- Separation of concerns: `MyJetFrameTheme` performs no window/system-bar side effects. Window concerns live in a separate `MyJetFrameWindowEffects()` helper, called from the Activity, and no-op outside an Activity.
- Kenko is GPL-3.0; MyJetFrame is a derivative → license MyJetFrame GPL-3.0.
- Naming: framework symbols use `MyJetFrame*` or neutral names. No `Kenko*` / `Linear*` identifiers remain in the framework.
- Module boundaries: `:core:designsystem` = theme/tokens/icons/window helper only; `:core:ui` = generic widgets only; `:core:common` = generic, business-agnostic helpers genuinely reused across apps only. No business data, navigation, ViewModels, or DB logic in any of them. Do not add orphan utilities to `:core:common` (admission rule: generic AND multi-app).
- Source of truth for ported files: `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\...` (package `com.looker.kenko`).
- Every task ends with a build/verification step and a commit.

## Review Focus

1. **Theme + window effects with no Activity context** (e.g. `@Preview`, unit test, or a non-Activity host) — `MyJetFrameTheme` must not crash (it is side-effect-free), and `MyJetFrameWindowEffects()` must no-op when `context as? Activity` is null.
2. **`Brand(seed)` with an extreme seed** (near-white or near-black) — the derived scheme must still produce a legible `primary`/`onPrimary` pair.
3. **`Palette.Dynamic` on API < 31** — must fall back to the Material baseline scheme without calling the dynamic-color APIs.
4. **Unthemed/standalone component use** — widgets must not depend on a `Scaffold`/`SnackbarHost` being present to compile or render (e.g. `ErrorSnackbar` renders when hosted; `EmptyState` fills its parent).
5. **Missing bundled font/resources** — a missing font must fail the build, not silently fall back at runtime (resources are referenced via `R.font.*`).

---

### Task 1: Scaffold repo, modules, and toolchain

**Files:**
- Create: `MyJetFrame/.gitignore`
- Create: `MyJetFrame/README.md`
- Create: `MyJetFrame/LICENSE`
- Create: `MyJetFrame/settings.gradle.kts`
- Create: `MyJetFrame/build.gradle.kts`
- Create: `MyJetFrame/gradle.properties`
- Create: `MyJetFrame/gradle/libs.versions.toml`
- Create: `MyJetFrame/gradle/wrapper/gradle-wrapper.properties` (+ `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` via `gradle wrapper`)
- Create: `MyJetFrame/core/common/build.gradle.kts`
- Create: `MyJetFrame/core/designsystem/build.gradle.kts`
- Create: `MyJetFrame/core/ui/build.gradle.kts`
- Create: `MyJetFrame/core/*/src/main/AndroidManifest.xml` (empty `<manifest/>`)

**Interfaces:**
- Consumes: nothing.
- Produces: a buildable 3-module Gradle project; `libs.plugins.android.library`, `libs.plugins.kotlin.android`, `libs.plugins.kotlin.compose`; library aliases `androidx-core-ktx`, `androidx-activity-compose`, `androidx-lifecycle-runtime-ktx`, `androidx-compose-bom`, `androidx-ui`, `androidx-ui-graphics`, `androidx-ui-tooling`, `androidx-ui-tooling-preview`, `androidx-material3`, `androidx-material-icons-core`, `androidx-ui-test-junit4`, `androidx-ui-test-manifest`, `junit`, `robolectric`, `androidx-test-core`, `androidx-test-ext-junit`, `kotlinx-coroutines-test`.

- [ ] **Step 1: Init the git repo**

Run (from `C:\Users\user\Abandon\Projects`):
```powershell
New-Item -ItemType Directory -Path "MyJetFrame" -Force
git -C "MyJetFrame" init -b main
```

- [ ] **Step 2: Write `.gitignore`**

```gitignore
*.iml
.gradle/
.kotlin/
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

- [ ] **Step 3: Write `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 4: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.13.2"
kotlin = "2.2.21"
coreKtx = "1.17.0"
activityCompose = "1.11.0"
lifecycle = "2.9.4"
# BOM 2025.11.00 maps androidx.compose.material3:material3 -> 1.4.0 (stable).
# The framework intentionally overrides material3 to the Expressive alpha below to match Kenko.
# Bump composeBom and material3 together.
composeBom = "2025.11.00"
material3 = "1.5.0-alpha08"
junit = "4.13.2"
robolectric = "4.14.1"
androidxTestCore = "1.6.1"
androidxTestExtJunit = "1.2.1"
coroutines = "1.10.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "material3" }
androidx-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "androidxTestCore" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExtJunit" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 5: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MyJetFrame"
include(":core:common")
include(":core:designsystem")
include(":core:ui")
```

- [ ] **Step 6: Write root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

- [ ] **Step 7: Write `:core:common/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jhon.myjetframe.core.common"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
```

- [ ] **Step 8: Write `:core:designsystem/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jhon.myjetframe.core.designsystem"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

dependencies {
    // Version centralization: expose Compose/Material3 to consumers so apps do not redeclare them.
    api(project(":core:common"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.material3)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.core)

    constraints {
        // Strict: an app pinning a different material3 fails resolution instead of overriding.
        api("androidx.compose.material3:material3") {
            version { strictly(libs.versions.material3.get()) }
        }
    }

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

- [ ] **Step 9: Write `:core:ui/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jhon.myjetframe.core.ui"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_17 }
}

dependencies {
    api(project(":core:designsystem"))
    api(project(":core:common"))
    api(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    constraints {
        api("androidx.compose.material3:material3") {
            version { strictly(libs.versions.material3.get()) }
        }
    }

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

- [ ] **Step 10: Write empty manifests**

Create `core/common/src/main/AndroidManifest.xml`, `core/designsystem/src/main/AndroidManifest.xml`, `core/ui/src/main/AndroidManifest.xml`, each containing:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **Step 11: Add `LICENSE` (GPL-3.0) and `README.md`**

Download the canonical GPL-3.0 text into `LICENSE` (or copy `C:\Users\user\Abandon\Projects\Kenko\LICENSE`). `README.md`:
```markdown
# MyJetFrame

Shared Jetpack Compose UI framework for Kenko, MihealthReader, and hackqing.

- `:core:common` — small composable-adjacent helpers.
- `:core:designsystem` — Line theme, tokens, palette system, icons.
- `:core:ui` — domain-agnostic widgets.

Derived in part from Kenko (GPL-3.0); licensed GPL-3.0. See `LICENSE`.

Design: `docs/superpowers/specs/2026-10-14-myjetframe-shared-ui-framework-design.md`.
```

- [ ] **Step 12: Generate the Gradle wrapper (8.13)**

Copy the wrapper from Kenko, then pin the distribution:
```powershell
Copy-Item "C:\Users\user\Abandon\Projects\Kenko\gradlew" -Destination "MyJetFrame\gradlew"
Copy-Item "C:\Users\user\Abandon\Projects\Kenko\gradlew.bat" -Destination "MyJetFrame\gradlew.bat"
New-Item -ItemType Directory -Path "MyJetFrame\gradle\wrapper" -Force
Copy-Item "C:\Users\user\Abandon\Projects\Kenko\gradle\wrapper\gradle-wrapper.jar" -Destination "MyJetFrame\gradle\wrapper\gradle-wrapper.jar"
Copy-Item "C:\Users\user\Abandon\Projects\Kenko\gradle\wrapper\gradle-wrapper.properties" -Destination "MyJetFrame\gradle\wrapper\gradle-wrapper.properties"
```
Then verify `MyJetFrame\gradle\wrapper\gradle-wrapper.properties` contains `gradle-8.13-bin.zip` (Kenko uses 8.13).

- [ ] **Step 13: Verify the project builds**

Run (in `MyJetFrame`):
```powershell
./gradlew projects
./gradlew assembleDebug
```
Expected: `projects` lists `:core:common`, `:core:designsystem`, `:core:ui`; `assembleDebug` ends with `BUILD SUCCESSFUL`.

- [ ] **Step 14: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "chore: scaffold MyJetFrame framework with core:common, core:designsystem, core:ui"
```

---

### Task 2: `:core:common` helpers (boundary-enforced)

This module holds exactly the three generic helpers below. Do not add anything else; the admission
rule (generic AND multi-app) applies. These are ported as-is from Kenko.

**Files:**
- Create: `core/common/src/main/kotlin/com/jhon/myjetframe/core/common/Dp.kt`
- Create: `core/common/src/main/kotlin/com/jhon/myjetframe/core/common/Modifier.kt`
- Create: `core/common/src/main/kotlin/com/jhon/myjetframe/core/common/PaddingValues.kt`
- Test: `core/common/src/test/kotlin/com/jhon/myjetframe/core/common/DpTest.kt`
- Test: `core/common/src/test/kotlin/com/jhon/myjetframe/core/common/PaddingValuesTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `class DpRange` (value class over `ClosedFloatingPointRange<Float>`), `operator Dp.rangeTo(Dp): DpRange`, `operator DpRange.contains(Dp): Boolean`.
  - `val PHI: Float` (= Kenko's exact value `16F / 10F`, i.e. `1.6f` — do NOT use `1.618f`), `fun Modifier.vertical(towardsRight: Boolean = true): Modifier`.
  - `operator fun PaddingValues.plus(other: PaddingValues): PaddingValues`, `operator fun PaddingValues.minus(other: PaddingValues): PaddingValues`.
  - Module deps (`:core:common`): add `androidx.compose.foundation:foundation` (version from the BOM) so `PaddingValues` resolves, and expose `androidx.ui` + `foundation` with `api(...)` (the module's public API uses `Modifier`/`PaddingValues`), plus `api(platform(libs.androidx.compose.bom))`. Do not use `implementation(...)` for `ui`/`foundation`.

- [ ] **Step 1: Port `DpRange` from Kenko**

Copy `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\component\Dp.kt` to `core/common/src/main/kotlin/com/jhon/myjetframe/core/common/Dp.kt`. Change `package com.looker.kenko.ui.component` to `package com.jhon.myjetframe.core.common`. If the copied file references other Kenko symbols, adjust imports to compose-only equivalents. Keep the `DpRange` value class and `rangeTo`/`contains` operators.

- [ ] **Step 2: Write the failing test for `DpRange`**

```kotlin
package com.jhon.myjetframe.core.common

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DpTest {
    @Test
    fun contains_isInclusive_ofBounds() {
        val range = 4.dp..10.dp
        assertTrue(range.contains(4.dp))
        assertTrue(range.contains(10.dp))
        assertTrue(range.contains(7.dp))
    }

    @Test
    fun contains_isFalse_outsideBounds() {
        val range = 4.dp..10.dp
        assertFalse(range.contains(3.dp))
        assertFalse(range.contains(11.dp))
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :core:common:testDebugUnitTest --tests "com.jhon.myjetframe.core.common.DpTest"`
Expected: FAIL (compilation error until `Dp.kt` symbols exist) or PASS if `DpRange` already works — if it PASSes immediately, proceed; the test pins behavior regardless.

- [ ] **Step 4: Implement `DpRange` minimally (if not already present)**

```kotlin
package com.jhon.myjetframe.core.common

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@JvmInline
value class DpRange(private val fl: ClosedFloatingPointRange<Float>) {
    operator fun contains(dp: Dp): Boolean = fl.contains(dp.value)
}

operator fun Dp.rangeTo(other: Dp): DpRange = DpRange(value..other.value)
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :core:common:testDebugUnitTest --tests "com.jhon.myjetframe.core.common.DpTest"`
Expected: PASS.

- [ ] **Step 6: Port `Modifier` extensions**

Copy `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\extension\Modifier.kt` to `core/common/src/main/kotlin/com/jhon/myjetframe/core/common/Modifier.kt`. Change the package to `com.jhon.myjetframe.core.common`. Keep `Modifier.vertical()` and `PHI` exactly as in Kenko — `PHI` is `const val PHI = 16F / 10F` (= `1.6f`); reproduce that literal, do not substitute `1.618f`. If `Modifier.vertical()` references Kenko-only symbols, replace with compose equivalents.

- [ ] **Step 7: Write the failing test for `PaddingValues` operators**

```kotlin
package com.jhon.myjetframe.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PaddingValuesTest {
    @Test
    fun plus_addsEachEdge() {
        val a = PaddingValues(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
        val b = PaddingValues(start = 2.dp, top = 1.dp, end = 3.dp, bottom = 5.dp)
        val result = a + b
        assertEquals(6.dp, result.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(9.dp, result.calculateTopPadding())
        assertEquals(7.dp, result.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(13.dp, result.calculateBottomPadding())
    }

    @Test
    fun minus_subtractsEachEdge() {
        val a = PaddingValues(start = 6.dp, top = 9.dp, end = 7.dp, bottom = 13.dp)
        val b = PaddingValues(start = 2.dp, top = 1.dp, end = 3.dp, bottom = 5.dp)
        val result = a - b
        assertEquals(4.dp, result.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(8.dp, result.calculateTopPadding())
        assertEquals(4.dp, result.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
        assertEquals(8.dp, result.calculateBottomPadding())
    }
}
```

- [ ] **Step 8: Run the test to verify it fails**

Run: `./gradlew :core:common:testDebugUnitTest --tests "com.jhon.myjetframe.core.common.PaddingValuesTest"`
Expected: FAIL (unresolved `plus`/`minus`).

- [ ] **Step 9: Implement `PaddingValues` operators**

Port from `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\extension\PaddingValues.kt`, package `com.jhon.myjetframe.core.common`. If Kenko's file is insufficient, implement:
```kotlin
package com.jhon.myjetframe.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateLeftPadding(layoutDirection) + other.calculateLeftPadding(layoutDirection)
        override fun calculateTopPadding() = this@plus.calculateTopPadding() + other.calculateTopPadding()
        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@plus.calculateRightPadding(layoutDirection) + other.calculateRightPadding(layoutDirection)
        override fun calculateBottomPadding() = this@plus.calculateBottomPadding() + other.calculateBottomPadding()
    }

operator fun PaddingValues.minus(other: PaddingValues): PaddingValues =
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@minus.calculateLeftPadding(layoutDirection) - other.calculateLeftPadding(layoutDirection)
        override fun calculateTopPadding() = this@minus.calculateTopPadding() - other.calculateTopPadding()
        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@minus.calculateRightPadding(layoutDirection) - other.calculateRightPadding(layoutDirection)
        override fun calculateBottomPadding() = this@minus.calculateBottomPadding() - other.calculateBottomPadding()
    }
```

- [ ] **Step 10: Run the tests to verify they pass**

Run: `./gradlew :core:common:testDebugUnitTest`
Expected: PASS.

- [ ] **Step 11: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:common): add DpRange, Modifier, and PaddingValues helpers"
```

---

### Task 3: `:core:designsystem` — Color + Palette system

**Files:**
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Color.kt`
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Palette.kt`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/theme/PaletteTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `val LineLightColorScheme: ColorScheme`, `val LineDarkColorScheme: ColorScheme`
  - `enum class ThemeMode { System, Light, Dark }`
  - `sealed interface Palette { data object Line; data object Dynamic; data class Brand(val seed: Color) }`
  - `fun brandColorScheme(seed: Color, dark: Boolean): ColorScheme`
  - `fun resolveColorScheme(palette: Palette, dark: Boolean, context: Context): ColorScheme`

- [ ] **Step 1: Port the Line color schemes**

Create `Color.kt` with Kenko's exact schemes (`package com.jhon.myjetframe.core.designsystem.theme`):
```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LineLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF5E6AD2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EAFB),
    onPrimaryContainer = Color(0xFF2F3764),
    secondary = Color(0xFF6B7280),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2F3F5),
    onSecondaryContainer = Color(0xFF363A43),
    tertiary = Color(0xFF6B7280),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8F9FA),
    onTertiaryContainer = Color(0xFF363A43),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0E0E10),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0E0E10),
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFB8BDC4),
    outlineVariant = Color(0xFFD0D5DD),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF0E0E10),
    inverseOnSurface = Color(0xFFF7F8F8),
    inversePrimary = Color(0xFF8B92FF),
    surfaceDim = Color(0xFFD9DCE0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F9FA),
    surfaceContainer = Color(0xFFF2F3F5),
    surfaceContainerHigh = Color(0xFFE8EAED),
    surfaceContainerHighest = Color(0xFFD9DCE0),
)

val LineDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF7C83FD),
    onPrimary = Color(0xFF080A0E),
    primaryContainer = Color(0xFF1A1D3A),
    onPrimaryContainer = Color(0xFFC2C8FD),
    secondary = Color(0xFF8A8F98),
    onSecondary = Color(0xFF080A0E),
    secondaryContainer = Color(0xFF1E2025),
    onSecondaryContainer = Color(0xFFD0D4DA),
    tertiary = Color(0xFF8A8F98),
    onTertiary = Color(0xFF080A0E),
    tertiaryContainer = Color(0xFF1E2025),
    onTertiaryContainer = Color(0xFFD0D4DA),
    error = Color(0xFFF87171),
    onError = Color(0xFF080A0E),
    errorContainer = Color(0xFF3A1F24),
    onErrorContainer = Color(0xFFFECACA),
    background = Color(0xFF080A0E),
    onBackground = Color(0xFFF7F8F8),
    surface = Color(0xFF080A0E),
    onSurface = Color(0xFFF7F8F8),
    surfaceVariant = Color(0xFF12151D),
    onSurfaceVariant = Color(0xFF8A8F98),
    outline = Color(0xFF333845),
    outlineVariant = Color(0xFF2A2E38),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFF7F8F8),
    inverseOnSurface = Color(0xFF0E0E10),
    inversePrimary = Color(0xFF5E6AD2),
    surfaceDim = Color(0xFF23252B),
    surfaceBright = Color(0xFF1A1E2A),
    surfaceContainerLowest = Color(0xFF06080C),
    surfaceContainerLow = Color(0xFF0E1015),
    surfaceContainer = Color(0xFF12151D),
    surfaceContainerHigh = Color(0xFF1A1E2A),
    surfaceContainerHighest = Color(0xFF23252B),
)
```

- [ ] **Step 2: Write the failing test for palette resolution and Brand derivation**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PaletteTest {
    private fun hue(c: Color): Float {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(c.toArgb(), hsl)
        return hsl[0]
    }

    @Test
    fun line_resolvesToLineSchemes() {
        val ctx = RuntimeEnvironment.getApplication()
        assertEquals(LineLightColorScheme, resolveColorScheme(Palette.Line, dark = false, context = ctx))
        assertEquals(LineDarkColorScheme, resolveColorScheme(Palette.Line, dark = true, context = ctx))
    }

    @Test
    fun brand_primaryHueMatchesSeed() {
        val seed = Color(0xFF00695B)
        val scheme = brandColorScheme(seed, dark = false)
        assertTrue(kotlin.math.abs(hue(scheme.primary) - hue(seed)) < 20f)
    }

    @Test
    fun brand_extremeSeed_keepsReadableContrast() {
        val scheme = brandColorScheme(Color.White, dark = false)
        val ratio = ColorUtils.calculateContrast(
            scheme.onPrimary.toArgb(),
            scheme.primary.toArgb(),
        )
        assertTrue("contrast ratio=$ratio", ratio >= 3.0)
    }

    @Test
    @Config(sdk = [30])
    fun dynamic_belowApi31_usesBaselineScheme() {
        val ctx = RuntimeEnvironment.getApplication()
        assertEquals(
            lightColorScheme(),
            resolveColorScheme(Palette.Dynamic, dark = false, context = ctx),
        )
        assertEquals(
            darkColorScheme(),
            resolveColorScheme(Palette.Dynamic, dark = true, context = ctx),
        )
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.PaletteTest"`
Expected: FAIL (unresolved `Palette`, `resolveColorScheme`, `brandColorScheme`).

- [ ] **Step 4: Implement `Palette.kt`**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.abs
import kotlin.math.min

enum class ThemeMode { System, Light, Dark }

sealed interface Palette {
    data object Line : Palette
    data object Dynamic : Palette
    data class Brand(val seed: Color) : Palette
}

fun brandColorScheme(seed: Color, dark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    val h = hsl[0]
    val s = hsl[1]
    fun tone(lightness: Float, sat: Float = s): Color =
        Color(ColorUtils.HSLToColor(floatArrayOf(h, sat, lightness)))
    return if (dark) {
        LineDarkColorScheme.copy(
            primary = tone(0.78f, min(s, 0.45f)),
            onPrimary = tone(0.12f, min(s, 0.25f)),
            primaryContainer = tone(0.30f, min(s, 0.35f)),
            onPrimaryContainer = tone(0.90f, min(s, 0.40f)),
            inversePrimary = tone(0.55f, min(s, 0.45f)),
            secondary = tone(0.70f, min(s, 0.25f)),
            tertiary = tone(0.70f, min(s, 0.25f)),
        )
    } else {
        LineLightColorScheme.copy(
            primary = tone(0.42f, min(s, 0.55f)),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = tone(0.92f, min(s, 0.35f)),
            onPrimaryContainer = tone(0.25f, min(s, 0.45f)),
            inversePrimary = tone(0.72f, min(s, 0.45f)),
            secondary = tone(0.45f, min(s, 0.25f)),
            tertiary = tone(0.45f, min(s, 0.25f)),
        )
    }
}

fun resolveColorScheme(palette: Palette, dark: Boolean, context: Context): ColorScheme =
    when (palette) {
        Palette.Line -> if (dark) LineDarkColorScheme else LineLightColorScheme
        Palette.Dynamic ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (dark) darkColorScheme() else lightColorScheme()
            }
        is Palette.Brand -> brandColorScheme(palette.seed, dark)
    }
```
Note: remove the unused `abs`/`min` imports not used; keep `min`. `abs` is unused here — delete it.

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.PaletteTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:designsystem): add Line color schemes and palette system"
```

---

### Task 4: `:core:designsystem` — Typography + fonts

**Files:**
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Type.kt`
- Copy: `core/designsystem/src/main/res/font/darkergrotesque_bold.ttf`
- Copy: `core/designsystem/src/main/res/font/darkergrotesque_semibold.ttf`
- Copy: `core/designsystem/src/main/res/font/maplemono_bold.ttf`
- Copy: `core/designsystem/src/main/res/font/maplemono_regular.ttf`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/theme/TypeTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `val Typography: Typography` (framework typography), `fun Typography.header()`, `fun TextStyle.numbers()`, `fun Typography.watermark()`, `val FontFamily.Companion.Numbers`.

- [ ] **Step 1: Copy the font files**

```powershell
New-Item -ItemType Directory -Path "MyJetFrame\core\designsystem\src\main\res\font" -Force
Copy-Item "C:\Users\user\Abandon\Projects\Kenko\app\src\main\res\font\*.ttf" -Destination "MyJetFrame\core\designsystem\src\main\res\font\"
```
Confirm the four `.ttf` files listed above exist there.

- [ ] **Step 2: Port `Type.kt`**

Create `Type.kt` with Kenko's content, package `com.jhon.myjetframe.core.designsystem.theme`, and add `import com.jhon.myjetframe.core.designsystem.R`. Reproduce Kenko's `Type.kt` verbatim otherwise (the `Numbers`/`displayFont`/`bodyFont` families, `header()`, `numbers()`, `watermark()`, `baseline`, and the full `Typography` copy).

- [ ] **Step 3: Write the failing test for typography tokens**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {
    @Test
    fun header_is28sp() {
        assertEquals(28.sp, Typography.header().fontSize)
    }

    @Test
    fun watermark_is40sp() {
        assertEquals(40.sp, Typography.watermark().fontSize)
    }

    @Test
    fun displayLarge_isBold() {
        assertEquals(FontWeight.Bold, Typography.displayLarge.fontWeight)
    }

    @Test
    fun numbers_usesMonoFamily() {
        val style = Typography.bodyLarge.numbers()
        assertEquals(Typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Numbers).fontFamily, style.fontFamily)
    }
}
```

- [ ] **Step 4: Run the test to verify it fails**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.TypeTest"`
Expected: FAIL until `Type.kt` compiles and values match.

- [ ] **Step 5: Fix `Type.kt` until the test passes**

Adjust font sizes/weights to the values asserted (Kenko's already match: displayLarge 28sp Bold; header 28sp; watermark 40sp).

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.TypeTest"`
Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:designsystem): add typography and bundled fonts"
```

---

### Task 5: `:core:designsystem` — Shapes + Border tokens

**Files:**
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Shapes.kt`
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Border.kt`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/theme/ShapesTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `val Shapes: Shapes`, `fun CornerBasedShape.start(...)`, `fun CornerBasedShape.end(...)`; `val MyJetFrameBorderWidth: Dp` (1.dp), `val MyJetFrameHairlineWidth: Dp` (0.8.dp), and `BorderStroke` getters `HairlineBorder`, `HairlineBorderStrong`, `PrimaryBorder`, `OutlineBorder`, `OnSurfaceBorder`, `OnSurfaceVariantBorder`.

- [ ] **Step 1: Port `Shapes.kt`**

Create `Shapes.kt` (package `com.jhon.myjetframe.core.designsystem.theme`) with Kenko's exact `Shapes` (6/8/10/12/14.dp) and the `CornerBasedShape.start()/end()` extensions.

- [ ] **Step 2: Port `Border.kt` with neutral names**

Create `Border.kt` (package `com.jhon.myjetframe.core.designsystem.theme`):
```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val MyJetFrameBorderWidth: Dp = 1.dp
val MyJetFrameHairlineWidth: Dp = 0.8.dp

val HairlineBorder: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.outline)

val HairlineBorderStrong: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.outlineVariant)

val PrimaryBorder: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.primary)

val OutlineBorder: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.outline)

val OnSurfaceBorder: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.outline)

val OnSurfaceVariantBorder: BorderStroke @Composable
    get() = BorderStroke(MyJetFrameBorderWidth, MaterialTheme.colorScheme.outline)
```

- [ ] **Step 3: Write the failing test for shape values**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ShapesTest {
    @Test
    fun shapes_matchTokenScale() {
        assertEquals(RoundedCornerShape(6.dp), Shapes.extraSmall)
        assertEquals(RoundedCornerShape(8.dp), Shapes.small)
        assertEquals(RoundedCornerShape(10.dp), Shapes.medium)
        assertEquals(RoundedCornerShape(12.dp), Shapes.large)
        assertEquals(RoundedCornerShape(14.dp), Shapes.extraLarge)
    }

    @Test
    fun borderWidths_areHairline() {
        assertEquals(1.dp, MyJetFrameBorderWidth)
        assertEquals(0.8.dp, MyJetFrameHairlineWidth)
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.ShapesTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:designsystem): add shapes and neutral border tokens"
```

---

### Task 6: `:core:designsystem` — `MyJetFrameTheme` + `Background`

**Files:**
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/theme/Theme.kt`
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/Window.kt`
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/Background.kt`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/theme/ThemeTest.kt`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/WindowTest.kt`

**Interfaces:**
- Consumes: `Palette`, `ThemeMode`, `resolveColorScheme`, `LineLightColorScheme`, `LineDarkColorScheme`, `Typography`, `Shapes`.
- Produces:
  - `data class MyJetFrameColors(val isDark: Boolean, val colorScheme: ColorScheme)`
  - `val LocalMyJetFrameColors: ProvidableCompositionLocal<MyJetFrameColors>`
  - `@Composable fun MyJetFrameTheme(themeMode: ThemeMode = ThemeMode.System, palette: Palette = Palette.Line, content: @Composable () -> Unit)` — visual only, no window side effects
  - `@Composable fun MyJetFrameWindowEffects()` — no-op outside an Activity
  - `fun configureMyJetFrameWindow(activity: Activity, isDark: Boolean)`
  - `@Composable fun Background(modifier: Modifier = Modifier, content: @Composable () -> Unit)`

- [ ] **Step 1: Write the failing test (theme renders outside an Activity context)**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun theme_light_publishesNonDarkLineScheme() {
        var captured: MyJetFrameColors? = null
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                captured = LocalMyJetFrameColors.current
                Text("x")
            }
        }
        compose.waitForIdle()
        val colors = requireNotNull(captured)
        assertEquals(false, colors.isDark)
        assertEquals(LineLightColorScheme.primary, colors.colorScheme.primary)
    }

    @Test
    fun theme_dark_publishesDarkScheme() {
        var captured: MyJetFrameColors? = null
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Dark, palette = Palette.Line) {
                captured = LocalMyJetFrameColors.current
            }
        }
        compose.waitForIdle()
        val colors = requireNotNull(captured)
        assertTrue(colors.isDark)
        assertEquals(LineDarkColorScheme.primary, colors.colorScheme.primary)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.ThemeTest"`
Expected: FAIL (unresolved `MyJetFrameTheme`). If the Robolectric compose environment itself fails to initialize, note it and fall back to verifying via `assembleDebug` + `@Preview`.

- [ ] **Step 3: Implement `Theme.kt`**

```kotlin
package com.jhon.myjetframe.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

data class MyJetFrameColors(val isDark: Boolean, val colorScheme: ColorScheme)

val LocalMyJetFrameColors: ProvidableCompositionLocal<MyJetFrameColors> =
    staticCompositionLocalOf { MyJetFrameColors(false, LineLightColorScheme) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyJetFrameTheme(
    themeMode: ThemeMode = ThemeMode.System,
    palette: Palette = Palette.Line,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = remember(themeMode, systemDark) {
        when (themeMode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    }
    val context = LocalContext.current
    val colorScheme = remember(palette, isDark, context) {
        resolveColorScheme(palette, isDark, context)
    }
    CompositionLocalProvider(
        LocalMyJetFrameColors provides MyJetFrameColors(isDark, colorScheme),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
```

- [ ] **Step 4: Implement `Background.kt`**

```kotlin
package com.jhon.myjetframe.core.designsystem

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Background(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        content = content,
    )
}
```

- [ ] **Step 5: Write the failing window-effects test**

```kotlin
package com.jhon.myjetframe.core.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import com.jhon.myjetframe.core.designsystem.theme.Palette
import com.jhon.myjetframe.core.designsystem.theme.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WindowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun windowEffects_lightTheme_setsLightSystemBars() {
        compose.setContent {
            MyJetFrameTheme(themeMode = ThemeMode.Light, palette = Palette.Line) {
                MyJetFrameWindowEffects()
            }
        }
        compose.waitForIdle()
        val controller = WindowCompat.getInsetsController(
            compose.activity.window,
            compose.activity.window.decorView,
        )
        assertTrue(controller.isAppearanceLightStatusBars)
        assertTrue(controller.isAppearanceLightNavigationBars)
    }
}
```
Note: `createAndroidComposeRule<ComponentActivity>()` needs `androidx.activity:activity-compose` (present). It provides `compose.activity` for a real read-back assertion. If `WindowInsetsControllerCompat.isAppearanceLightStatusBars` does not reflect the value under this Robolectric SDK, report it as a concern rather than weakening the test to "no crash" silently.

- [ ] **Step 6: Implement `Window.kt`**

```kotlin
package com.jhon.myjetframe.core.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.jhon.myjetframe.core.designsystem.theme.LocalMyJetFrameColors

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MyJetFrameWindowEffects() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val colors = LocalMyJetFrameColors.current
    SideEffect {
        val activity = view.context.findActivity() ?: return@SideEffect
        activity.window.setBackgroundDrawable(
            ColorDrawable(colors.colorScheme.surface.toArgb()),
        )
        with(WindowCompat.getInsetsController(activity.window, view)) {
            isAppearanceLightStatusBars = !colors.isDark
            isAppearanceLightNavigationBars = !colors.isDark
        }
    }
}

fun configureMyJetFrameWindow(activity: Activity, isDark: Boolean) {
    with(WindowCompat.getInsetsController(activity.window, activity.window.decorView)) {
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
    }
}
```

- [ ] **Step 7: Run the design-system tests to verify they pass**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.theme.ThemeTest" --tests "com.jhon.myjetframe.core.designsystem.WindowTest"`
Expected: PASS. (If Robolectric compose is not viable in this repo, remove the compose tests and instead run `./gradlew :core:designsystem:assembleDebug` plus a `@Preview` for each palette; document the fallback.)

- [ ] **Step 8: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:designsystem): add MyJetFrameTheme, window effects, and Background"
```

---

### Task 7: `:core:designsystem` — Icons

**Files:**
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/icon/Icon.kt`
- Create: `core/designsystem/src/main/kotlin/com/jhon/myjetframe/core/designsystem/icon/MyJetFrameIcons.kt`
- Copy: referenced vector drawables from Kenko `app/src/main/res/drawable*/` into `core/designsystem/src/main/res/drawable*/`
- Test: `core/designsystem/src/test/kotlin/com/jhon/myjetframe/core/designsystem/icon/IconTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `fun icon(name: String, viewPort: Float = 24f, size: Dp = 24.dp, autoMirror: Boolean = false, block: IconPathBuilder.() -> Unit): ImageVector`; `object MyJetFrameIcons` (ported icon set).

- [ ] **Step 1: Port the icon builder**

Copy `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\component\icons\Helper.kt` to `core/designsystem/.../icon/Icon.kt`; change package to `com.jhon.myjetframe.core.designsystem.icon`. Keep the `icon(...)` `ImageVector` builder as-is. Do the same for `icons\Stack.kt`.

- [ ] **Step 2: Port the icon set + drawables**

Copy `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\theme\KenkoIcons.kt` to `.../icon/MyJetFrameIcons.kt`; rename `object KenkoIcons` → `object MyJetFrameIcons` and fix the package. Read the file, then copy every referenced vector drawable from `C:\Users\user\Abandon\Projects\Kenko\app\src\main\res\drawable*\` into `core/designsystem/src/main/res/drawable*/` preserving density buckets. Add `import com.jhon.myjetframe.core.designsystem.R` where drawables are referenced.

- [ ] **Step 3: Write the failing test**

```kotlin
package com.jhon.myjetframe.core.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class IconTest {
    @Test
    fun builder_setsDefaultSize() {
        val v: ImageVector = icon("test") {}
        assertEquals(24.dp, v.defaultWidth)
        assertEquals(24.dp, v.defaultHeight)
    }

    @Test
    fun iconSet_isNonEmpty() {
        val icons: List<ImageVector> = MyJetFrameIcons.all
        org.junit.Assert.assertTrue(icons.isNotEmpty())
    }
}
```
Add an `all: List<ImageVector>` aggregate property to `MyJetFrameIcons` if it does not already expose one.

- [ ] **Step 4: Run the test to verify it fails, then passes**

Run: `./gradlew :core:designsystem:testDebugUnitTest --tests "com.jhon.myjetframe.core.designsystem.icon.IconTest"`
Expected: FAIL first (unresolved), then PASS after adding `all`.

- [ ] **Step 5: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:designsystem): port icon builder and icon set"
```

---

### Task 8: `:core:ui` — Buttons

**Files:**
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/Button.kt`
- Test: `core/ui/src/test/kotlin/com/jhon/myjetframe/core/ui/ButtonTest.kt`

**Interfaces:**
- Consumes: `com.jhon.myjetframe.core.designsystem.theme.*` (`PrimaryBorder`, `OutlineBorder`, `Typography`, tokens), `MyJetFrameIcons`.
- Produces: `@Composable fun MyJetFrameButton(...)`, `SecondaryMyJetFrameButton(...)`, `TertiaryMyJetFrameButton(...)`, `BackButton(...)` (same parameters as the Kenko originals).

- [ ] **Step 1: Port `Button.kt`**

Copy `C:\Users\user\Abandon\Projects\Kenko\app\src\main\kotlin\com\looker\kenko\ui\component\Button.kt` to `core/ui/.../Button.kt`. Change package to `com.jhon.myjetframe.core.ui`. Apply these renames mechanically:
- `KenkoButton` → `MyJetFrameButton`
- `SecondaryKenkoButton` → `SecondaryMyJetFrameButton`
- `TertiaryKenkoButton` → `TertiaryMyJetFrameButton`
- Border tokens: `KenkoBorder/LinearBorder/PrimaryBorder/...` → framework names (`HairlineBorder`, `PrimaryBorder`, `OutlineBorder`, …).
- `KenkoIcons` → `MyJetFrameIcons`; imports `com.looker.kenko.*` → `com.jhon.myjetframe.*`.
- Remove any Kenko-domain imports.

- [ ] **Step 2: Write the failing test**

```kotlin
package com.jhon.myjetframe.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ButtonTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun button_isDisplayed_andClickable() {
        var clicked = false
        compose.setContent {
            MyJetFrameTheme {
                MyJetFrameButton(onClick = { clicked = true }) { Text("Go") }
            }
        }
        compose.onNodeWithText("Go").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }
}
```

- [ ] **Step 3: Run the test to verify it fails, then passes**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "com.jhon.myjetframe.core.ui.ButtonTest"`
Expected: FAIL before the port compiles; PASS after. (If Robolectric compose is not viable, fall back to `./gradlew :core:ui:assembleDebug` + a `@Preview`.)

- [ ] **Step 4: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:ui): port button composables"
```

---

### Task 9: `:core:ui` — Settings widgets

**Files:**
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/SettingsGroup.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/PreferenceSwitchRow.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/StickyHeader.kt`
- Test: `core/ui/src/test/kotlin/com/jhon/myjetframe/core/ui/SettingsGroupTest.kt`

**Interfaces:**
- Consumes: framework tokens/widgets.
- Produces: `SettingsGroup(...)`, `SettingsRow(...)`, `SwitchSettingsRow(...)`, `PreferenceSwitchRow(...)`, and `StickyHeader(name, sequence, countLabel, isCollapsed, onCollapseToggle, actions)`.
- **Boundary rule:** `:core:ui` is domain-agnostic. It must NOT ship app-specific copy or workout-domain concepts. `StickyHeader` is therefore parameterized: the caller supplies `countLabel: String?` (e.g. "3 sets") instead of the widget rendering Kenko's `label_sets_count` string. No `strings.xml` is added to `:core:ui`.

- [ ] **Step 1: Port the files**

Copy from Kenko and rename packages/imports exactly as in Task 8:
- `ui\component\SettingsGroup.kt` → `core/ui/.../SettingsGroup.kt`
- `ui\component\PreferenceSwitchRow.kt` → `core/ui/.../PreferenceSwitchRow.kt`
- `ui\component\StickyHeader.kt` → `core/ui/.../StickyHeader.kt`
Replace `com.looker.kenko.*` → `com.jhon.myjetframe.*`; border tokens → framework names; `KenkoIcons` → `MyJetFrameIcons`; drop Kenko-domain imports.

**StickyHeader de-coupling (do this, don't ship app copy):** in the ported `StickyHeader.kt`,
- Replace the `setCount: Int` parameter with `countLabel: String? = null`.
- Replace `text = stringResource(R.string.label_sets_count, normalizeInt(setCount))` with `text = countLabel`, shown only when `countLabel != null`.
- Remove the `normalizeInt` helper (now unused) and the `androidx.compose.ui.res.stringResource` import.
- Do NOT create any `res/values/strings.xml` (or `values-zh`) in `:core:ui`.

- [ ] **Step 2: Write the failing test**

```kotlin
package com.jhon.myjetframe.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsGroupTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun preferenceSwitchRow_showsTitle() {
        compose.setContent {
            MyJetFrameTheme {
                PreferenceSwitchRow(title = "Dark mode", checked = true, onCheckedChange = {})
            }
        }
        compose.onNodeWithText("Dark mode").assertIsDisplayed()
    }
}
```
If `PreferenceSwitchRow`'s parameter names differ, match the ported signature.

- [ ] **Step 3: Run the test to verify it fails, then passes**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "com.jhon.myjetframe.core.ui.SettingsGroupTest"`
Expected: FAIL before port, PASS after.

- [ ] **Step 4: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:ui): port settings group, switch row, and sticky header"
```

---

### Task 10: `:core:ui` — Dialogs

**Files:**
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/ConfirmDialog.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/SelectionDialog.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/DatePickerDialog.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/DeleteIconButton.kt`
- Test: `core/ui/src/test/kotlin/com/jhon/myjetframe/core/ui/ConfirmDialogTest.kt`

**Interfaces:**
- Consumes: framework tokens/widgets.
- Produces: `ConfirmDialog(...)`, `SelectionDialog<T>(...)`, `MyJetFrameDatePickerDialog(...)` (exposes `java.time.LocalDate`), `DeleteIconButton(...)`.

- [ ] **Step 1: Port `ConfirmDialog`, `SelectionDialog`, `DeleteIconButton`**

Copy from Kenko (`ui\component\ConfirmDialog.kt`, `SelectionDialog.kt`, `DeleteIconButton.kt`) with the Task 8 package/import/token renames; drop domain imports.

- [ ] **Step 2: Port the date picker using `java.time.LocalDate`**

Copy Kenko's `ui\component\KenkoDatePickerDialog.kt` to `DatePickerDialog.kt`, rename to `MyJetFrameDatePickerDialog`, package `com.jhon.myjetframe.core.ui`. **Replace `kotlinx.datetime.LocalDate` with `java.time.LocalDate`** in all signatures and conversions (available at minSdk 26), so the framework does not depend on kotlinx-datetime.

- [ ] **Step 3: Write the failing test**

```kotlin
package com.jhon.myjetframe.core.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onAllNodesWithText
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfirmDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun confirmDialog_showsText_andConfirms() {
        var confirmed = false
        compose.setContent {
            MyJetFrameTheme {
                ConfirmDialog(
                    show = true,
                    onDismissRequest = {},
                    onConfirm = { confirmed = true },
                    title = "Delete?",
                    text = "This cannot be undone.",
                )
            }
        }
        compose.onNodeWithText("Delete?").assertIsDisplayed()
        compose.onAllNodesWithText("OK")[0].performClick()
        assertTrue(confirmed)
    }
}
```
Match the actual ported `ConfirmDialog` parameter names (adjust `show`, `confirmText`, etc. to the ported signature).

- [ ] **Step 4: Run the test to verify it fails, then passes**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "com.jhon.myjetframe.core.ui.ConfirmDialogTest"`
Expected: FAIL before port, PASS after.

- [ ] **Step 5: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:ui): port dialogs and date picker (java.time)"
```

---

### Task 11: `:core:ui` — Misc widgets

**Files:**
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/EmptyState.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/Snackbar.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/TextField.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/DigitPicker.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/List.kt`
- Create: `core/ui/src/main/kotlin/com/jhon/myjetframe/core/ui/DisableScrollConnection.kt`
- Test: `core/ui/src/test/kotlin/com/jhon/myjetframe/core/ui/EmptyStateTest.kt`

**Interfaces:**
- Consumes: framework tokens/widgets.
- Produces: `EmptyState(...)`, `ErrorSnackbar(...)`, `myJetFrameTextDecorator`, `myJetFrameTextFieldColor`, `DigitPicker(...)`, `LazyListScope.endItem(...)`, `DisableScrollConnection(...)`.

- [ ] **Step 1: Port the files**

Copy from Kenko, rename packages/imports/tokens as in Task 8:
- `ui\component\EmptyState.kt` → `EmptyState.kt`
- `ui\component\Snackbar.kt` → `Snackbar.kt` (`ErrorSnackbar`)
- `ui\component\TextField.kt` → `TextField.kt` (`kenkoTextDecorator`→`myJetFrameTextDecorator`, `kenkoTextFieldColor`→`myJetFrameTextFieldColor`)
- `ui\component\DigitPicker.kt` → `DigitPicker.kt`
- `ui\component\List.kt` → `List.kt` (`endItem()`)
- `ui\component\DisableScrollConnection.kt` → `DisableScrollConnection.kt`

- [ ] **Step 2: Write the failing test**

```kotlin
package com.jhon.myjetframe.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.jhon.myjetframe.core.designsystem.theme.MyJetFrameTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmptyStateTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun emptyState_showsTitle() {
        compose.setContent {
            MyJetFrameTheme { EmptyState(title = "Nothing here") }
        }
        compose.onNodeWithText("Nothing here").assertIsDisplayed()
    }
}
```
Match `EmptyState`'s actual ported parameter names.

- [ ] **Step 3: Run the test to verify it fails, then passes**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "com.jhon.myjetframe.core.ui.EmptyStateTest"`
Expected: FAIL before port, PASS after.

- [ ] **Step 4: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "feat(core:ui): port empty state, snackbar, text field, digit picker, list, scroll helpers"
```

---

### Task 12: Final verification

**Files:**
- Modify: `MyJetFrame/README.md` (add module/widget inventory)

**Interfaces:**
- Consumes: all previous tasks.
- Produces: a verified, documented Phase 1 framework.

- [ ] **Step 1: Full build**

Run (in `MyJetFrame`):
```powershell
./gradlew clean assembleDebug assembleRelease
```
Expected: `BUILD SUCCESSFUL` for all three modules.

- [ ] **Step 2: Lint**

Run:
```powershell
./gradlew :core:common:lintDebug :core:designsystem:lintDebug :core:ui:lintDebug
```
Expected: no errors (warnings acceptable; fix any errors).

- [ ] **Step 3: All unit tests**

Run:
```powershell
./gradlew testDebugUnitTest
```
Expected: all tests PASS. Record any tests that had to fall back to `@Preview` + `assembleDebug` in the README.

- [ ] **Step 4: Verify no `Kenko`/`Linear` identifiers remain**

Run:
```powershell
Get-ChildItem -Path "MyJetFrame\core" -Recurse -Include *.kt | Select-String -Pattern "Kenko|looker\.|Linear(Border|BorderStrong)?" -CaseSensitive
```
Expected: no matches in public API (references inside comments explaining provenance are acceptable; rename or remove them).

- [ ] **Step 5: Verify module boundaries**

Run:
```powershell
Get-ChildItem -Path "MyJetFrame\core\common\src" -Recurse -Include *.kt | ForEach-Object { $_.FullName }
```
Expected: only `Dp.kt`, `Modifier.kt`, `PaddingValues.kt` (plus their tests). Anything else violates the `:core:common` admission rule — remove or relocate it.

- [ ] **Step 6: Update README module inventory**

Append the widget list from spec §6–§8 (including the §8.1 module-boundary table) and the palette API (`MyJetFrameTheme`, `ThemeMode`, `Palette`).

- [ ] **Step 7: Commit**

```powershell
git -C "MyJetFrame" add -A
git -C "MyJetFrame" commit -m "docs: document MyJetFrame modules, palette API, and widget inventory"
```

---

## Self-Review

**1. Spec coverage**
- §4 Toolchain → Task 1 (versions, wrapper). ✓
- §5 Project layout → Task 1. ✓
- §6.1 Theming model → Tasks 3, 6. ✓
- §6.2 Brand derivation → Task 3. ✓
- §6.3 Tokens (Color/Type/Shapes/Border/Icons) → Tasks 3–7. ✓
- §6.4 Primitives (`Background`) → Task 6. ✓
- §6.5 Window/system-bar layer → Task 6 (`Window.kt`, `MyJetFrameWindowEffects`). ✓
- §6.6 Version centralization (`api` + strict `material3` constraint) → Task 1 Steps 8–9. ✓
- §7 `:core:ui` widget inventory → Tasks 8–11. ✓
- §8 `:core:common` + §8.1 boundaries → Task 2 (contents) and Task 12 Step 5 (enforcement). ✓
- §9 Consumption → out of scope for Phase 1 (Phase 2–4); spec §14 confirms. ✓
- §10 Migration → Phases 2–4, not this plan. ✓
- §11 Verification → Task 12. ✓
- §12 Risks licensing → Task 1 Step 11 (LICENSE). ✓

**2. Placeholder scan:** No "TBD"/"TODO"; porting steps name exact source and target paths and enumerate renames; code steps include real code.

**3. Type consistency:** `MyJetFrameTheme(themeMode, palette, content)`, `Palette`/`ThemeMode`, `resolveColorScheme(palette, dark, context)`, `brandColorScheme(seed, dark)`, `LineLightColorScheme`/`LineDarkColorScheme`, `MyJetFrameIcons.all`, `MyJetFrameButton` — used consistently across Tasks 1–12.

**4. Review Focus coverage:**
- (1) No-Activity host → Task 6: `MyJetFrameTheme` is side-effect-free; `MyJetFrameWindowEffects()` guarded with `as? Activity` and tested under Robolectric.
- (2) Extreme Brand seed → Task 3 `brand_extremeSeed_keepsReadableContrast` (WCAG contrast ratio ≥ 3.0 via `ColorUtils.calculateContrast`).
- (3) Dynamic < API 31 fallback → Task 3 `dynamic_belowApi31_usesBaselineScheme` (`@Config(sdk = [30])`).
- (4) Standalone component render → Tasks 8–11 render widgets inside only `MyJetFrameTheme` (no Scaffold).
- (5) Missing fonts → Task 4 references fonts via `R.font.*`; `assembleDebug` in Task 12 fails if absent.

## Execution Handoff

Plan saved to `docs/superpowers/plans/2026-10-14-myjetframe-phase1-framework.md`.
