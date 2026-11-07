# Dynamic Island layout overhaul — November 7–8, 2026

## Original defects and causes

The supplied access-expiry screenshot was produced by a shell fixed at 112dp high
containing one horizontal row: a 38dp crown, a weighted text column, and two
9sp text controls. The text lost width to the controls while the fixed shell kept
unused vertical space. The collapsed notification used a 152dp reference width
regardless of whether its content was just an icon and count; both layout and
copy logic could also put notification bodies in this glanceable surface.

The root shell measured children with a fixed animated width and height. That
coupled text wrapping and content sizing to spring frames. Overlay calibration
values named dp were sent to WindowManager as raw pixels. Native expanded windows
kept a fixed expanded width, and there was no physical cutout placement policy.

## Changes

- `IslandContentViewport` measures expanded content at the destination width and
  lets actual content determine shell height. A bounded scroll viewport handles
  tall content and font scaling. Reference heights are fallback estimates, not
  permanent black rectangles. One shell animation owns size; child size morphing
  is disabled. Spring damping is 0.82; Reduced Motion skips geometry springs.
- Notification count/progress pills use measured text width, 20dp artwork, 8dp gap,
  and matching 12dp edges. Counts are bounded to `99+`. Bodies and sender text are
  absent from compact mode. Compact visual height stays 36.67dp, with a 48dp
  interactive layout region.
- Expiry messages have a header followed by actions. Existing `AppButton` supplies
  readable labels, feedback, semantics and minimum target sizing. Actions stay
  horizontal where they fit and stack for larger text. Generic notification peeks
  similarly put message copy above actions and allow two-line previews.
- Expanded disabled compact click semantics no longer merge the whole card's
  text into a disabled button. Notification action IDs and callbacks are preserved.
- Core music transport controls use four evenly distributed `AppIconButton`
  regions, replacing reference-coordinate offsets and a separately pinned output
  action. Shared icon buttons respect Reduced Motion. Call/timer controls cannot
  shrink below 48dp with width calibration. System-alert text actions and Quick
  Controls use minimum 48dp targets; Quick Controls separates title and utilities.
- `IslandPlacementPolicy` handles public cutout rectangles, status bar clearance,
  edge clamping and display changes. Android 11+ uses full-window metric insets to
  avoid a feedback loop with window-relative overlay insets. Calibration converts
  dp once to physical pixels. Visible native windows wrap actual content; hidden
  windows retain immediate invisible/0-size/non-touchable behavior.

[Android WindowManager reference](https://developer.android.com/reference/android/view/WindowManager)
and [WindowInsets reference](https://developer.android.com/reference/android/view/WindowInsets)
were checked for full-window metrics and inset coordinate behavior. Apple design
skill principles were applied as layout/accessibility guidance, with Android
48dp interaction targets taking precedence over reference SVG dimensions.

## Enforced contract

[ISLAND_LAYOUT_RULES.md](../ISLAND_LAYOUT_RULES.md) is linked as mandatory from
AGENTS.md, UI_UX_RULES.md, ISLAND_BEHAVIOR_RULES.md, and DYNAMIC_ISLAND_RULEBOOK.md.
It explicitly supersedes older fixed-height, universal minimum-width,
single-line-expanded-text and 44dp-target guidance.

Spacing: compact ends **12dp**, inline **8dp**, expanded inset **16dp**, region
gap **12dp**, text gap **4dp**. Expanded maximum width remains
`min(windowWidth - 22dp, 408dp)`. Camera clearance is **4dp**, supplied by measured
hardware bounds rather than an invented blank content slot.

## Evidence and limits

Device fixture screenshots are in [island-layout-2026-11-07](island-layout-2026-11-07).
These render the real composables with synthetic data on the connected Realme;
they are not design mockups and do not claim real call/media behavior.

- [320dp expiry](island-layout-2026-11-07/expiry-320.png)
- [Compact icon/count](island-layout-2026-11-07/compact-count-393.png)
- [320dp, 200% font, RTL expiry](island-layout-2026-11-07/expiry-320-200pct-rtl.png)

Compose Previews for the two reported states cover 320/360/393/430dp and 200% text
in `IslandLayoutPreviews.kt`. `IslandLayoutInstrumentedTest` tests these widths,
100%/200% font, LTR/RTL, action callbacks, target sizes, overlap and six core
expanded activity families. Pure tests cover counts/progress and cutout/rotation
placement. Final command and live-overlay results are recorded below.

Remaining acceptance boundaries: center/pill cameras are JVM fixtures unless
explicitly device-tested; other OEM status/privacy-indicator behavior, real calls,
media delivery, TalkBack traversal, rapid-event frame timing and all Android
versions remain separate compatibility checks. A synthetic fixture screenshot
never substitutes for those checks. No billing/ad flow was initiated.
