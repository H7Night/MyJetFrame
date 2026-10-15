# MyJetFrame — Shared UI Framework Design

Date: 2026-10-14

## 1. Purpose

Extract a single shared Jetpack Compose UI framework, **MyJetFrame**, to be used by
three sibling Android apps that currently each own a copy-adapted theme/UI layer:

- **Kenko** — `com.looker.kenko` (workout journal)
- **MihealthReader** — `com.jhon.mihealthreader` (Mi Fitness data reader)
- **hackqing** — `com.hackqing.scale` (to be renamed `com.jhon.hackqing`; body-fat scale)

Kenko already contains the richest design language — the **"Line"** theme (a Linear.app-inspired
design: violet accent on Zinc neutrals, Tulip-style hairline borders, Darker Grotesque + Maple Mono
typography). That design language is the foundation of the framework. Inspiration is also taken from
Android's [nowinandroid](https://github.com/android/nowinandroid) `core:designsystem` /
`core:ui` / `core:common` module convention.

This document specifies the framework project. A later phase migrates the three apps to consume it.

## 2. Goals

- One versioned framework, consumed by all three apps, iterated in place (no publish step).
- Preserve and generalize Kenko's Line theme, typography, shapes, and border tokens.
- Provide a palette system (`Line` default, plus `Dynamic` and `Brand(seed)`) so the apps share
  code and components while each may keep a distinct color identity.
- Provide a reusable, domain-agnostic widget library.
- Keep each app a single `:app` module; consume the framework from module dependencies.

## 3. Non-goals

- No shared domain models, database, networking, or business logic.
- No shared charts or domain-coupled widgets in the initial release. Kenko's `TagSelector`,
  `BodyPartMuscleFilter`, `WeightLineChart`, `SetItem`, `KenkoBottomBar`, and `timer/*` stay per-app.
- No publishing to a remote Maven repository (local composite build only for now).
- No unification of app package names other than hackqing's requested rename.

## 4. Toolchain

Matched to the three apps for dependency compatibility:

| Item | Version |
|---|---|
| Gradle (wrapper) | 8.13 |
| AGP | 8.13.2 |
| Kotlin | 2.2.21 |
| KSP | 2.2.21-2.0.4 (unused unless needed) |
| Compose BOM | 2025.11.00 |
| Material 3 (Expressive) | 1.5.0-alpha08 |
| Java / JVM target | 17 |
| compileSdk / minSdk / targetSdk | 36 / 26 / 36 |

Versions are pinned to Kenko's currently-verified set to minimize migration variables. The Compose
Compiler is applied via the `org.jetbrains.kotlin.plugin.compose` Gradle plugin (version follows
Kotlin 2.2.21) in every module; `buildFeatures { compose = true }` alone is insufficient under
Kotlin 2.x, and no `kotlinCompilerExtensionVersion` is set.

Compose BOM ↔ Material 3: BOM `2025.11.00` maps `androidx.compose.material3:material3` to `1.4.0`
(stable). The framework deliberately overrides `material3` to `1.5.0-alpha08` (Expressive) because
Kenko's theme uses `MaterialExpressiveTheme`. Bump the BOM and the Material 3 override together.

The framework maintains its **own** `gradle/libs.versions.toml` and is the single source of truth
for Compose/Material 3 versions (see 6.6). Apps keep their own catalogs for their own dependencies
but reference framework modules by coordinate string and must not redeclare Compose/Material 3.

## 5. Project layout

Standalone git repository at `C:\Users\user\Abandon\Projects\MyJetFrame`:

```
MyJetFrame/
├── settings.gradle.kts          # include(":core:common"), ":core:designsystem", ":core:ui"
├── build.gradle.kts             # plugin aliases, apply false
├── gradle.properties            # android.useAndroidX=true, nonTransitiveRClass=true
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/…                # Gradle 8.13
├── core/
│   ├── common/                  # :core:common
│   │   └── src/main/kotlin/com/jhon/myjetframe/core/common/
│   ├── designsystem/            # :core:designsystem
│   │   ├── src/main/kotlin/com/jhon/myjetframe/core/designsystem/
│   │   └── src/main/res/font/   # bundled OFL fonts
│   └── ui/                      # :core:ui
│       └── src/main/kotlin/com/jhon/myjetframe/core/ui/
├── docs/superpowers/specs/      # this document
└── README.md
```

Module coordinates:

- `com.jhon.myjetframe:core-common`
- `com.jhon.myjetframe:core-designsystem`
- `com.jhon.myjetframe:core-ui`

All three modules are `com.android.library` with Compose enabled
(`buildFeatures { compose = true }`), namespace = base package of the module.
`:core:ui` depends on `:core:designsystem` and `:core:common`; `:core:designsystem` depends on
`:core:common`.

## 6. `:core:designsystem`

### 6.1 Theming model

```kotlin
enum class ThemeMode { System, Light, Dark }

sealed interface Palette {
    data object Line : Palette
    data object Dynamic : Palette
    data class Brand(val seed: Color) : Palette
}

@Composable
fun MyJetFrameTheme(
    themeMode: ThemeMode = ThemeMode.System,
    palette: Palette = Palette.Line,
    content: @Composable () -> Unit,
)
```

- Built on **`MaterialExpressiveTheme`** (M3 `1.5.0-alpha08`), matching Kenko today; this
  preserves Expressive components and motion for all apps.
- Palette resolves a `ColorScheme`:
  - `Line` → `LineLightColorScheme` / `LineDarkColorScheme` (moved verbatim from Kenko).
  - `Dynamic` → `dynamicLightColorScheme(context)` / `dynamicDarkColorScheme(context)` on API 31+;
    Material baseline (`lightColorScheme()` / `darkColorScheme()`) below 31.
  - `Brand(seed)` → framework tonal derivation from the seed (see 6.2).
- **Typography and Shapes are theme-wide** (palette-independent): the Kenko `Typography` and
  `Shapes` move in unchanged, so the framework carries one design identity while the palette
  swaps colors.
- **The theme performs no window/system-bar side effects.** It only provides the visual system and
  publishes the resolved state for the optional window layer:
  - `data class MyJetFrameColors(val isDark: Boolean, val colorScheme: ColorScheme)`
  - `val LocalMyJetFrameColors: ProvidableCompositionLocal<MyJetFrameColors>`
  - `MyJetFrameTheme` wraps `MaterialExpressiveTheme` inside a
    `CompositionLocalProvider(LocalMyJetFrameColors provides …)`.

  Rationale: `LocalContext.current` is not necessarily an `Activity` (Preview, Dialog, test hosts),
  and window/system-bar behavior also depends on edge-to-edge and system version. Keeping it out of
  the theme avoids overloading the theme with lifecycle/window responsibilities. Windowing lives in
  a separate helper (see 6.5), invoked from the Activity's root layout / window-adaptation layer.

### 6.2 `Brand(seed)` color derivation

`Brand(seed)` produces a full `ColorScheme` without a system-dependency by deriving tonal roles
from the seed using HSL/lightness manipulation (light and dark variants), while retaining the Line
neutral surfaces and Zinc outline tokens. This is intentionally an approximation, not full Material
You HCT fidelity. It provides hackqing a green identity (`Brand(Color(0xFF00695B))` and its dark
counterpart) while keeping the framework's neutral structure.

Resolved decision: we do **not** add the `material-color-utilities` dependency for this.

### 6.3 Design tokens

- **Color.kt** — `LineLightColorScheme`, `LineDarkColorScheme` (verbatim from Kenko), baseline
  schemes, and the `Palette` resolution helpers.
- **Type.kt** — Kenko's tuned `Typography` (11–28sp) plus helpers `Typography.header()`,
  `TextStyle.numbers()`, `Typography.watermark()`, and the `FontFamily.Companion.Numbers`
  extension. Bundled fonts: `darkergrotesque_bold.ttf`, `darkergrotesque_semibold.ttf`
  (display), `maplemono_bold.ttf`, `maplemono_regular.ttf` (body + numbers).
- **Shapes.kt** — Kenko's `Shapes` (6–14dp) and `CornerBasedShape.start()/end()` extensions.
- **Border.kt** — hairline tokens, renamed neutrally: `MyJetFrameBorderWidth` (1.dp),
  `MyJetFrameHairlineWidth` (0.8.dp), and `BorderStroke` getters `HairlineBorder`,
  `HairlineBorderStrong`, `PrimaryBorder`, `OutlineBorder`, `OnSurfaceBorder`,
  `OnSurfaceVariantBorder`. The Kenko-specific `Linear*` / `Kenko*` aliases are dropped; apps
  migrate to the neutral names. Visual result is unchanged.
- **Icons** — the `icon { }` `ImageVector` builder and the Kenko icon set, renamed
  `MyJetFrameIcons`. Material Symbols vector drawables used by the set are copied into the module's
  resources.

### 6.4 Primitives

- `Background(modifier, content)` — a themed surface wrapper (nowinandroid-style convenience).

### 6.5 Window & system-bar layer (separate from the theme)

`Window.kt` in `:core:designsystem` provides the window concerns as a standalone helper so the
theme stays pure:

- `@Composable fun MyJetFrameWindowEffects()` — reads `LocalMyJetFrameColors`, then:
  - Sets the window background to `colorScheme.surface` (avoids a theme flash on transitions).
  - Configures the system bars via `WindowCompat` (`isAppearanceLightStatusBars` /
    `isAppearanceLightNavigationBars` = `!isDark`).
  - Resolves the host Activity via `LocalView.current.context as? Activity` and **no-ops** when it
    is not an Activity (Preview, Dialog, non-Activity host) — no crash.
- `fun configureMyJetFrameWindow(activity: Activity, isDark: Boolean)` — non-composable entry for a
  dedicated window-adaptation layer if an app prefers to call it outside composition.

Edge-to-edge (`enableEdgeToEdge()`) remains the Activity's responsibility and is called alongside
this helper; the framework does not manage the decor-fits policy.

Call site (Activity):

```kotlin
setContent {
    MyJetFrameTheme(themeMode = settings.themeMode, palette = settings.palette) {
        MyJetFrameWindowEffects()
        AppContent()
    }
}
```

Verified behaviors: `@Preview` (no Activity → no-op), Dialog hosts, multi-window/split-screen, and
Activity recreation (effects reapplied via `SideEffect`).

### 6.6 Version centralization

The framework is the single source of truth for the Compose ecosystem versions:

- `:core:designsystem` and `:core:ui` expose the Compose BOM and `material3` with `api(...)` so
  consumers receive them transitively.
- Both modules declare strict version constraints on `material3` so an app that tries to pin a
  different `material3` fails resolution instead of silently overriding the framework.
- Apps are required **not** to redeclare `androidx.compose.*` or `material3`; they rely on the
  framework (enforced by the strict constraints and documented in the migration steps, §10).
- Compatibility note recorded in `libs.versions.toml`: Compose BOM `2025.11.00` maps
  `material3` to `1.4.0` (stable); the framework intentionally overrides it to `1.5.0-alpha08`
  (Expressive). These two facts must be updated together when the BOM is bumped.

## 7. `:core:ui`

Domain-agnostic widgets ported from Kenko and renamed (`Kenko*` → `MyJetFrame*` / neutral):

| Source (Kenko) | Framework |
|---|---|
| `Button.kt` (`KenkoButton`, `SecondaryKenkoButton`, `TertiaryKenkoButton`, `BackButton`) | `MyJetFrameButton`, `SecondaryMyJetFrameButton`, `TertiaryMyJetFrameButton`, `BackButton` |
| `SettingsGroup.kt` (DSL, `SettingsRow`, `SwitchSettingsRow`) | same names |
| `PreferenceSwitchRow.kt` | `PreferenceSwitchRow` |
| `StickyHeader.kt` | `StickyHeader` |
| `EmptyState.kt` | `EmptyState` |
| `ConfirmDialog.kt` | `ConfirmDialog` |
| `SelectionDialog.kt` | `SelectionDialog` |
| `KenkoDatePickerDialog.kt` | `MyJetFrameDatePickerDialog` |
| `DeleteIconButton.kt` | `DeleteIconButton` |
| `Snackbar.kt` (`ErrorSnackbar`) | `ErrorSnackbar` |
| `TextField.kt` (`kenkoTextDecorator`, `kenkoTextFieldColor`) | `myJetFrameTextDecorator`, `myJetFrameTextFieldColor` |
| `DigitPicker.kt` | `DigitPicker` |
| `List.kt` (`endItem()`) | `endItem()` |
| `DisableScrollConnection.kt` | `DisableScrollConnection` |

## 8. `:core:common`

Small, business-agnostic, composable-adjacent helpers.

**Admission rule (strict):** a helper is admitted only if it is generic (no domain/business types)
**and** is reused by more than one app (or is a generic Compose primitive clearly expected to be).
`:core:common` must not become a dumping ground for unowned or one-off code.

Current contents (all generic, app-agnostic Compose utilities ported from Kenko):

- `Modifier.vertical()`, `PHI`
- `PaddingValues` `+` / `-` operators
- `DpRange` value class with `rangeTo` / `contains`

Verification note: as of this design these helpers are referenced only by Kenko (MihealthReader and
hackqing have no references). They are retained in `:core:common` as generic framework primitives;
if any fails the admission rule at migration time, it is inlined back into its app or moved to
`:core:designsystem` rather than left in `:core:common`.

### 8.1 Module boundaries

| Module | Allowed | Not allowed |
|---|---|---|
| `:core:designsystem` | Theme, ColorScheme, Typography, Shapes, fonts, icons, design tokens, window helper | Business data, navigation flows |
| `:core:ui` | Generic widgets (Button, Dialog, SettingsGroup, EmptyState, Snackbar, …) | Business ViewModels, database logic |
| `:core:common` | Lightweight tools/types genuinely reused across apps | Orphan utilities, business rules |

## 9. Consumption — git submodule + composite build

For each app repo:

1. Add the framework as a submodule at the app repo root:
   ```
   git submodule add <MyJetFrame-remote-url> MyJetFrame
   ```
   If no remote URL exists yet, add it from the local path and swap to GitHub later.

2. `settings.gradle.kts`:
   ```kotlin
   includeBuild("MyJetFrame") {
       dependencySubstitution {
           substitute(module("com.jhon.myjetframe:core-designsystem"))
               .using(project(":core:designsystem"))
           substitute(module("com.jhon.myjetframe:core-ui"))
               .using(project(":core:ui"))
           substitute(module("com.jhon.myjetframe:core-common"))
               .using(project(":core:common"))
       }
   }
   ```

3. `app/build.gradle.kts`:
   ```kotlin
   implementation("com.jhon.myjetframe:core-designsystem")
   implementation("com.jhon.myjetframe:core-ui")
   implementation("com.jhon.myjetframe:core-common")
   ```

4. Remove the app's own `androidx.compose.*` and `material3` declarations (and the local
   `compose-bom`). The framework supplies them with strict version constraints, so redeclaring or
   pinning a conflicting version fails the build by design.

The framework is a separate Gradle build; its own version catalog and repositories are isolated
from the app's. Apps keep `RepositoriesMode.FAIL_ON_PROJECT_REPOS` unchanged.

## 10. Migration plan (later phase, per app)

### Kenko
- Delete `ui/theme/*`, `ui/component/Border.kt`, `ui/component/Dp.kt`,
  `ui/component/icons/*`, `ui/component/KenkoIcons.kt`, the generic widgets listed in §7, and
  `ui/extension/*`; import the framework equivalents.
- Replace the `ColorPalette` domain enum with framework `Palette`; update `MainViewModel`,
  `Settings`, the settings screen (`Color style` dialog), `DatastoreSettingsRepo`, and
  `SettingsRepo` accordingly. The DataStore key `color_palette` remains, with `Line` preserved as
  the stored default.
- `MainActivity` calls `MyJetFrameTheme(themeMode, palette) { MyJetFrameWindowEffects(); … }`;
  remove Kenko's in-theme window/system-bar side effects.
- Keep domain-coupled widgets and routes.

### MihealthReader
- Replace `MiHealthTheme` with `MyJetFrameTheme`. Palette: use `Line` as default or `Dynamic` to
  retain Monet (a migration-time choice); the framework supports both.
- Adopt shared widgets incrementally. Keep the hand-rolled Canvas charts and `SportColors` for now.
- Remove the now-unused local `ui/theme/Theme.kt`.

### hackqing
- Rename package `com.hackqing.scale` → `com.jhon.hackqing`: namespace, applicationId, source
  directory, `R` imports, manifest, BLE/service/health/DI classes. The vendored vendor JNI package
  `com.yunmai.haoqing.logic.config` is left unchanged (must match `libconfig.so`).
- Replace `HackqingTheme` with `MyJetFrameTheme(palette = Palette.Brand(green))` (or `Line`).
- Keep the hand-rolled Canvas charts and `TrendMetric.color` palette for now.
- The applicationId change forces uninstall/reinstall and re-grants Health Connect permissions; note
  in the app's verification doc.

## 11. Verification

Framework:
- `./gradlew :core:designsystem:assembleDebug :core:ui:assembleDebug :core:common:assembleDebug`
- `./gradlew :core:designsystem:testDebugUnitTest :core:ui:lintDebug`
- A small `@Preview` set (and/or a Compose UI test) exercising `MyJetFrameTheme` for each palette in
  light/dark, plus key widgets.

Apps (after migration):
- `./gradlew assembleDebug`, existing unit tests, and `lintDebug`.
- The first successful app build confirms composite-build wiring.
- Manual/visual verification that each app's screens render with the chosen palette.

Build/validation order: framework first, then apps.

## 12. Risks & mitigations

| Risk | Mitigation |
|---|---|
| Submodule needs a remote URL | Use a local-path submodule until a GitHub remote exists, then update `.gitmodules`. |
| Bundled fonts (OFL) | Darker Grotesque and Maple Mono are OFL-licensed; keep license notices. |
| M3 Expressive is alpha (`1.5.0-alpha08`) | Chosen deliberately to match Kenko (keeps Expressive theme/motion). Centralized in the framework with a strict constraint; BOM↔M3 mapping documented. |
| Compose BOM ↔ Material 3 override drift | Record the mapping (BOM 2025.11.00 → M3 1.4.0; overridden to 1.5.0-alpha08) in the catalog and bump both together. |
| Window/system-bar behavior across hosts | Theme is side-effect-free; window effects isolated and no-op outside an Activity; verified for Preview/Dialog/multi-window/recreation. |
| Strict version constraints break an app that pins Compose/M3 | Intended enforcement; migration removes app-level Compose/M3 declarations. |
| hackqing package rename is invasive | Reinstall + Health Connect re-auth; documented as a discrete migration task. |
| `Brand(seed)` fidelity | Documented as an approximation, not full Monet. |
| Composite build / catalog version drift | Framework catalog pinned to the same toolchain as the apps; bump together. |
| Package name collisions | Framework uses its own `com.jhon.myjetframe.*` namespace. |
| Kenko is GPL-3.0; copying its code makes MyJetFrame a derivative | License MyJetFrame GPL-3.0 and record provenance in the README; all three apps must be GPL-3.0-compatible. |

## 13. Resolved decisions

1. Consumption: git submodule + composite build (`includeBuild`).
2. Structure: lean multi-module (`:core:designsystem`, `:core:ui`, `:core:common`).
3. Theming: palette system with `Line` default; `Dynamic` and `Brand(seed)` available.
4. Framework namespace: `com.jhon.myjetframe`; hackqing renamed to `com.jhon.hackqing`.
5. Initial scope: design system + generic widgets (+ small common helpers); charts excluded.
6. Theme built on `MaterialExpressiveTheme`; `Brand(seed)` derived in-framework (no extra dep).
7. Version policy: keep Material 3 `1.5.0-alpha08` (match Kenko, fewest migration variables)
   rather than dropping to stable `1.4.0`.
8. Separation of concerns: the theme is visual-only; window/system-bar handling lives in a separate
   `MyJetFrameWindowEffects()` helper invoked from the Activity.
9. Version centralization: the framework owns Compose/Material 3 versions and exposes them via
   `api` + strict constraints; apps must not redeclare them.

## 14. Implementation phasing

This design spans the framework plus three app migrations. It is decomposed so each phase gets its
own implementation plan:

- **Phase 1 (first plan):** create the MyJetFrame repository and the `:core:common`,
  `:core:designsystem`, `:core:ui` modules; port the design system, generic widgets, and helpers;
  add previews/tests; verify with `assembleDebug` / `lintDebug` / unit tests. No app is touched.
- **Phase 2:** migrate Kenko to consume the framework (§10).
- **Phase 3:** migrate MihealthReader to consume the framework (§10).
- **Phase 4:** migrate hackqing to consume the framework, including the `com.hackqing.scale` →
  `com.jhon.hackqing` rename (§10).

Only Phase 1 is planned now; Phases 2–4 follow as separate plans.
