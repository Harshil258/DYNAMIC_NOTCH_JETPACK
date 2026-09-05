---
name: dynamic-island-pixel-perfect
description: Compare and fix Jetpack Compose Dynamic Island states against supplied Figma SVG exports, including exact geometry, typography, colors, artwork, cropping, and device screenshots. Use for pixel-perfect audits, reference-state implementation, visual regression checks, or any request to compare every island one by one.
---

# Dynamic Island Pixel QA

Use the supplied SVG files as the only visual source of truth. Do not use a
rulebook, generated audit, remembered measurements, or a duplicate reference
folder when the requested SVG folder is available. Preserve unrelated
uncommitted work in the repository.

## Workflow

1. Inspect the repository and current diff before editing. Locate the Compose
   island entry point, state enum, state picker, geometry resolver, tests, and
   any existing reference assets.
2. Enumerate every `*.svg` directly inside the requested `Dynamic Island`
   folder. Read each SVG's `<svg>` dimensions and first black body rectangle
   directly from the file. Record filename, body x/y/width/height/radius, and
   the state that should display it.
3. Render reference crops with `scripts/render_reference_islands.py`. Use
   Quick Look on macOS for Figma exports because direct ImageMagick SVG
   rendering can omit embedded `xlink` artwork such as album art. Crop using
   coordinates scaled to the Quick Look render size; never crop a high-density
   render with 1x coordinates.
4. Audit states sequentially. For each state, compare the reference crop and
   the settled device screenshot at the same body origin. Check body bounds,
   radius, internal x/y positions, widths/heights, text content and weight,
   icon artwork, color sampling, opacity, and whitespace. Capture evidence for
   every state, not only the timer.
   Capture the untouched baseline before tapping controls: live waveforms and
   progress animations must be static in that baseline, then may animate only
   after the corresponding control is used.
5. Fix the smallest responsible layer. Prefer Compose/vector code where the
   state must remain interactive. Use a high-resolution `drawable-nodpi`
   reference crop only when exact exported typography/artwork is required;
   keep the whole-island tap behavior and document any inner-control tradeoff.
   Do not leave full-island reference renders in the app resources: keep them
   in the audit directory and retain only small, named artwork crops that the
   Compose implementation actually loads.
   Compose `Canvas` coordinates are pixels, not dp: convert every SVG-derived
   measurement with the active density, or use a tightly cropped sub-asset for
   complex embedded artwork. Re-check the crop bounds so neighboring text or
   transparent padding is not accidentally included.
6. Add or update geometry tests for every nonstandard reference height and run
   the complete unit test suite after the state batch. Reinstall the debug APK,
   revisit each changed state, and re-capture after animations settle.

## Known direct mappings for this project

Use these mappings as a starting point, then verify them against the actual SVG
content. Add a dedicated demo state when two references are variants or when
one reference has no existing state; do not silently map visibly different
artwork to an unrelated label.

| Reference | Expected state/content |
|---|---|
| `Dynamic Island-1.svg` | Timer expanded |
| `Dynamic Island-2.svg` | AirPods connected |
| `Dynamic Island-3.svg` | Voice memo recording |
| `Dynamic Island-4.svg` | Screen recording |
| `Dynamic Island-5.svg` | Shortcut completion |
| `Dynamic Island-6.svg` | Incoming call |
| `Dynamic Island-7.svg` | Satellite connected |
| `Dynamic Island-8.svg` | Find My alert |
| `Dynamic Island-9.svg` | Silent mode |
| `Dynamic Island-10.svg` | Moved to iPhone |
| `Dynamic Island-11.svg` | Music player |
| `Dynamic Island-12.svg` | Turn-by-turn navigation |
| `Dynamic Island-13.svg` / `-14.svg` | FaceTime call variants |
| `Dynamic Island-15.svg` | Shared media/call session |
| `Dynamic Island-16.svg` | Video remote |
| `Dynamic Island-17.svg` | Airplane-mode alert |
| `Dynamic Island-18.svg` | Screen mirroring |
| `Dynamic Island-19.svg` | Mobile-data alert |
| `Dynamic Island-20.svg` | Transit route |
| `Dynamic Island.svg` | AirDrop activity |

`Minimal.svg` is an icon sheet, not one expanded island body. Compare its
individual glyphs only when auditing the minimal-state icon.

## Comparison rules

- Compare the black body crop, not the full SVG canvas or its drop-shadow
  padding. The body may be offset inside the source canvas.
- Preserve source-specific heights. Do not force timer, standard banners, and
  full sheets into one height.
- Treat source text paths as exact artwork. If Compose text differs in kerning,
  baseline, or weight after tuning, use the rendered reference asset for that
  state rather than guessing at font settings.
- Keep width and height independent when the app's device width differs from
  the reference canvas. Do not apply one global scale to hide state-specific
  offsets.
- A control is not complete merely because it is clickable: it must either
  update a local visual state or dispatch to a real destination/action. Keep
  those post-tap changes separate from the untouched reference baseline.
- Keep the reference-device scale unchanged, but allow the inner content scale
  to reduce on narrow windows; a hard minimum tuned only for one phone can
  clip controls in split-screen or small-device layouts.
- Re-query the visible UI after navigation or a failed screenshot. Verify the
  selected state before judging the image.
- Report unresolved states explicitly with the cause and the next safe action.

## Verification

Run, as applicable:

```bash
python3 skills/dynamic-island-pixel-perfect/scripts/render_reference_islands.py \
  "<reference-folder>" --out /tmp/dynamic-island-reference
./gradlew test :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Use a connected device or emulator to capture each state. Keep a compact audit
table with reference filename, app state, body dimensions, visual result,
changed files, and screenshot path. Finish only after every requested SVG has
either passed comparison or been marked blocked with evidence.
