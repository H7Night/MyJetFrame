# MyJetFrame

Shared Jetpack Compose UI framework for Kenko, MihealthReader, and hackqing.

- `:core:common` — small composable-adjacent helpers.
- `:core:designsystem` — Line theme, tokens, palette system, icons.
- `:core:ui` — domain-agnostic widgets.

Derived in part from Kenko (GPL-3.0); licensed GPL-3.0. See `LICENSE`.
Bundled third-party fonts (Darker Grotesque, Maple Mono) and their OFL-1.1 notices: `core/designsystem/THIRD_PARTY.md`.

Design: `docs/superpowers/specs/2026-10-14-myjetframe-shared-ui-framework-design.md`.

## Modules

| Module | Coordinate | Purpose |
|---|---|---|
| `:core:common` | `com.jhon.myjetframe:core-common` | Lightweight, app-agnostic Compose utilities. |
| `:core:designsystem` | `com.jhon.myjetframe:core-designsystem` | Theme, color schemes, typography, shapes, tokens, icons, window helper. |
| `:core:ui` | `com.jhon.myjetframe:core-ui` | Domain-agnostic widgets. |

`:core:ui` depends on `:core:designsystem` and `:core:common`; `:core:designsystem` depends on `:core:common`.

### Module boundaries

| Module | Allowed | Not allowed |
|---|---|---|
| `:core:designsystem` | Theme, ColorScheme, Typography, Shapes, fonts, icons, design tokens, window helper | Business data, navigation flows |
| `:core:ui` | Generic widgets (Button, Dialog, SettingsGroup, EmptyState, Snackbar, …) | Business ViewModels, database logic |
| `:core:common` | Lightweight tools/types genuinely reused across apps | Orphan utilities, business rules |

## Palette API

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

`MyJetFrameTheme` builds on `MaterialExpressiveTheme`. `Line` uses the framework's Line
light/dark schemes; `Dynamic` uses Monet on API 31+ (Material baseline below 31); `Brand(seed)`
derives a tonal scheme from the seed (approximate, not full Material You). The theme is
visual-only and publishes `LocalMyJetFrameColors`; window/system-bar side effects live in the
separate `MyJetFrameWindowEffects()` helper (no-ops outside an Activity host).

## Widgets (`:core:ui`)

- **Buttons** — `MyJetFrameButton`, `SecondaryMyJetFrameButton`, `TertiaryMyJetFrameButton`, `BackButton`
- **Settings** — `SettingsGroup` (DSL: `SettingsRow`, `SwitchSettingsRow`), `PreferenceSwitchRow`
- **Lists & media** — `StickyHeader`, `EmptyState`, `DigitPicker`, `LazyListScope.endItem()`, `disableScrollConnection()`
- **Dialogs** — `ConfirmDialog`, `SelectionDialog`, `MyJetFrameDatePickerDialog`, `DeleteIconButton`
- **Feedback** — `ErrorSnackbar`
- **Text** — `myJetFrameTextDecorator()`, `myJetFrameTextFieldColor()`

## Helpers (`:core:common`)

- `Modifier.vertical()`, `PHI`
- `PaddingValues` `+` / `-` operators
- `DpRange` value class with `rangeTo` / `contains`
