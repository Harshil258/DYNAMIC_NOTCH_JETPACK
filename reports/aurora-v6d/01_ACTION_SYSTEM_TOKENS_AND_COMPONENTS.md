# Aurora V6D Deliverable 01: Action System Tokens and Components

## 1. Architectural Philosophy
The Aurora V6D Action System establishes a unified, semantically typed design language across all 60 Dynamic Island states. Rather than scattering one-off button composables or using generic grey capsules with small, low-contrast 13dp glyphs, all interactive elements now derive from a centralized taxonomy that prioritizes:
1. **Immediate Glanceability**: Users can instantly discern the primary action from secondary or dismiss affordances.
2. **Crisp Iconography**: Icons use 17–20dp optical sizing aligned to vector grid proportions, eliminating microscopic glyphs.
3. **Intent-Based Color**: Vibrant Apple Blue (`#0A84FF`), Capsule Green (`#34C759`), and Capsule Red (`#FA3532`) are reserved for meaningful actions.
4. **Accessible Touch Targets**: Every action guarantees a minimum 48dp hit target while preserving a 36–40dp visual capsule.
5. **Spring Press Feedback**: Subtle physical micro-motion (`Modifier.pressScale`) respecting `LocalReducedMotion`.

---

## 2. Action Taxonomy & Styles (`IslandActionStyle`)

| Style | Purpose | Visual Surface | Typography & Icon | Minimum Touch Target |
|---|---|---|---|---|
| `Primary` | Best next action (e.g. Redial, Reply, Upgrade) | High-contrast tinted capsule (`#0A84FF` / `#2563EB`) | 12.5–13sp SemiBold White + 18dp White glyph | 48dp (visual 38dp) |
| `Positive` | Accept incoming call | Circular `#34C759` / `#37C058` green | 22dp White `AppleGlyph.Phone` | 52dp circle |
| `Destructive` | Decline/End call, Stop recording | Circular or capsule `#FA3532` red | 22dp White `AppleGlyph.EndCall` / `Stop` | 44–52dp |
| `Secondary` | Contextual action (View Ticket, Settings) | Subdued dark glass (`#222B3A`) with 0.5dp white border | 12.5sp White + 18dp Cyan/White glyph | 48dp (visual 36–38dp) |
| `Tertiary` | Quiet dismiss, Done, Maybe later | Quiet neutral (`#1C1C1E`) with subtle border | 12sp Neutral (`#E5E7EB` / `#8E8E93`) | 48dp (visual 38dp) |
| `Toggle` | Hardware state toggle (Mute, Speaker, Video) | Active: White background; Inactive: `#2C2C2E` glass | Active: Black glyph; Inactive: White glyph | 44–48dp circle |
| `Passive` | Non-clickable status/metrics | Neutral `#1E2129` capsule (not clickable) | 12sp Neutral text + glyph | Intrinsic (no hit target) |
| `Overflow` | Secondary options menu affordance | Compact `#2C2C2E` pill | White glyph + concise label | 48dp (visual 36dp) |

---

## 3. Semantic Accents (`IslandSemanticAccent`)

- `Blue`: `#0A84FF` — Default primary system action (Redial, Reply, Send).
- `Green`: `#34C759` — Call accept, positive connectivity, charging confirmed.
- `Red`: `#FA3532` — Call end/decline, stop recording, critical low battery.
- `Cyan`: `#38BDF8` / `IslandColors.CapsuleCyan` — Transit, navigation, tickets, secondary active links.
- `Orange`: `#FB8B28` — Timers, airplane mode, missed call alerts.
- `Neutral`: `#2C2C2E` / `#1C1C1E` — Quiet dismiss, subtle borders.

---

## 4. Centralized Components

### `IslandActionPill`
Located in `app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandActionSystem.kt`.
- Enforces 48dp touch region with `Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)`.
- Enforces visual capsule height (default 38dp, corner radius 19dp).
- Integrated `Modifier.pressScale(interaction, pressedScale = 0.96f)`.
- Exposes `Role.Button` and optional TalkBack `contentDescription`.

### `IslandIconAction`
- Circular or rounded icon-only button for call controls, media transport, and stop recording.
- Supports active toggle states (`isActiveToggle`) with inverted black/white contrast.
- Visual sizes: 44dp (ongoing call controls), 52dp (incoming call answer/decline), 28dp (quick utility buttons).

### `IslandControlRow`
- Horizontal container enforcing standard 8dp spacing and center vertical alignment.

### `IslandActionCapability`
- Eliminates dangerous `queryIntentActivities(...).getOrDefault(true)`.
- Validates real package manager intent resolution for `tel:`, `smsto:`, and `Settings.ACTION_*` with `.getOrDefault(false)`.
- Separates demo preview fixtures (`isDemoPreview = true`) from production runtime capabilities.
