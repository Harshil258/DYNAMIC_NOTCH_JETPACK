# Dynamic Island layout rules

Mandatory for shell, compact content, expanded cards, overlay positioning, previews,
and tests. This contract supersedes reference-only fixed heights, single-line-only
expanded copy, 44dp controls, and a universal 126dp minimum in older SVG guidance.
References are visual evidence, not Android hardware measurements.

## One owner per dimension

- `AppTheme.island` owns island geometry; `AppTheme.spacing` owns general spacing;
  `AppTheme.typography` owns text. No per-device padding or compensating offsets.
- The shell owns clipping, background, morph, compact edge insets, and width limits.
  Content owns its own expanded padding and intrinsic height. Never pad both owners
  for the same edge. Do not give expanded content a fixed shell-height dependency.
- Measure expanded content at the **destination width**, not each spring frame's
  width. `IslandContentViewport` bounds the viewport to the current display and
  measures the content through a scroll container. Text therefore wraps once per
  width/content/font change; tall content scrolls instead of clipping.
- Expanded height comes from measured content. SVG heights are first-frame
  estimates only. The shell owns the sole size animation; avoid nested size morphs.

## Compact / minimal

- Visual body height: 36.67dp. Interactive layout target: at least 48dp, including
  the compact body/companion. Transparent vertical hit clearance is intentional;
  it is not content padding. Hidden windows remain invisible and non-touchable.
- Notification width = 12dp leading + 20dp icon + 8dp gap + measured label width
  + 12dp trailing, bounded by the current status-side budget. Both ends match.
  Never preserve a 152dp reference width around a 60dp icon/count group.
- Compact notifications show only bounded unseen count (`0`…`99+`) or genuine
  ongoing progress. No message body, sender, or permanent notification title.
- Compact content uses the same 12dp shell insets and 8dp content gap. Components
  must not add another inset inside the shell. The space between distinct active
  regions must express real content; no fake companion for idle notifications.
- Width calibration must not introduce dead space in the icon/count notification
  pill. It still applies to the idle notch and other calibrated presentations.

## Expanded / actions

- Outer width: at most current window width minus 22dp, capped at 408dp.
- Standard content padding: 16dp; region gap: 12dp; inline gap: 8dp; text gap: 4dp.
  The access card uses a 32dp icon and 24dp card corners.
- Put descriptive content above action groups. Do not place multiple text buttons
  beside title/subtitle in a single cramped row.
- Access buttons reuse `AppButton`, with readable theme typography, at least 48dp
  targets, ripple, semantic button role, and one primary treatment. At under 240dp
  content width or font scale above 1.3, stack actions vertically.
- Expanded titles/descriptions may wrap. Ellipsis is reserved for genuinely bounded
  glanceable regions; shrinking text or truncating a message to preserve buttons
  is not an acceptable fallback. Preserve source IDs, actions, drafts and pager ownership.

## Hardware / native window

- Read public WindowInsets status bar insets and DisplayCutout bounding rectangles.
  Never assume a centered camera, fixed camera width, or camera offset from a model name.
- Convert saved calibration dp to pixels **once** at WindowManager, not by treating
  saved dp as raw pixels. Re-evaluate on insets, layout and configuration changes.
- Expanded controls sit below status bar/cutouts. A compact content surface that
  intersects a physical cutout moves below that cutout with 4dp clearance. A left
  punch-hole that does not intersect a centered pill adds no blank camera region.
- Native pill shells are shadow-free: a blur halo clipped by the tightly wrapped
  native window must never appear as a rectangular patch.
- Clamp the native window to display edges. Visible windows wrap the measured
  shell instead of retaining expanded-width transparent touch rectangles. Decorations
  must not add layout width/height. OEM status-icon bounds are not public: actual
  status/privacy-indicator compatibility remains a device acceptance test.

## Motion / verification

- Shared geometry spring: damping 0.82, stiffness 380 (fast setting may raise stiffness).
  Keep content enter/exit coordinated with the shell. Reduced Motion disables
  geometry springs and press scaling. Do not animate private notification copy.
- Pure tests cover count/progress and cutout placement (left/center/pill/rotation).
- Compose tests must check 320/360/393/430dp, 100%/200% fonts, LTR/RTL, action reachability,
  no action overlap, no giant compact remainder, and multiple expanded families.
- Before/after screenshots must distinguish actual overlay from synthetic Compose
  fixtures. Compilation does not establish visual or device compatibility.
