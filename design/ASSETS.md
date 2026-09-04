# Visual Assets

Everything the app draws — icons, the Dynamic Island symbols, and the launcher
mark — is derived from the iOS 17 Dynamic Island Figma export supplied as the
project's design reference. Nothing in the UI uses Material icon fonts,
template artwork, or text characters standing in for glyphs.

```
design/
├── reference/
│   ├── ios17-dynamic-island/          primary Figma export (unmodified)
│   │   ├── Dynamic Island/            21 activity artboards + Minimal.svg glyph sheet
│   │   ├── Components.svg             expanded-sheet layout blueprint
│   │   ├── *Presentation.svg          compact / minimal / expanded presentations
│   │   └── iPhone 15 Pro*.svg         device frames (island placement + geometry)
│   └── ios17-dynamic-island-alt/      second export, duplicates removed
├── icons/                             72 extracted / authored symbols, 24 x 24 grid
│   ├── call|media|status|system/*.svg source of truth, one file per symbol
│   ├── index.svg                      contact sheet of the whole set
│   └── manifest.json                  machine-readable inventory
└── brand/
    ├── app-icon.svg                   launcher master (108pt adaptive canvas)
    └── app-icon-512.png               store render
```

## How the set was produced

`tools/svgkit.py` is a small dependency-free SVG reader written for these
exports. It flattens a document into shapes, converts primitives to path data,
computes real bounding boxes (beziers and arcs are sampled), and clusters
shapes that touch. That is what makes it possible to pull individual symbols
out of an artboard: a single `Dynamic Island-13.svg` is not one image, it is an
avatar, an info button, and five action buttons, each of which is a backing
circle plus a glyph.

`tools/extract_icons.py` drives that library from a manifest. For every symbol
it locates the cluster, drops the backing circle/pill, bakes every transform
into the path data (so the output has clean, transform-free geometry) and fits
the result onto a shared 24 x 24 grid at a per-symbol optical size — the same
trick SF Symbols use to keep glyphs of different aspect ratios looking equally
weighted. It writes the SVG source of truth **and** the Compose path data in
`IslandVectorCatalog.kt`, so the two can never drift.

```
python3 tools/extract_icons.py     # icons + Compose catalogue + index sheet
node    tools/build_launcher.js    # launcher bitmaps from design/brand/app-icon.svg
```

## Using them

```kotlin
AppleIcon(glyph = AppleGlyph.AirPlay, tint = Color.White, size = 24.dp)

// inside an existing Canvas
drawAppleGlyph(AppleGlyph.Bell, tint = Color.White, side = 18f)
```

`AppleGlyph` is the semantic layer; `IslandVectorCatalog` holds the geometry;
`IslandVector`/`IslandPath` render it. Icons are pure vectors, so they stay
sharp at any size and take a tint without needing per-colour assets.

## Inventory

42 symbols carry geometry harvested straight from the reference
artboards; 30 are authored on the same grid because the reference
sheets contain no equivalent. Sources are relative to
`design/reference/ios17-dynamic-island/`.

### Media

| Symbol | Purpose | Origin |
|---|---|---|
| `play` | Music player transport – play triangle | `Dynamic Island-11.svg` · cluster 8 |
| `pause` | Timer activity – pause bars | `Dynamic Island-1.svg` · cluster 0 |
| `backward` | Music player transport – rewind | `Dynamic Island-11.svg` · cluster 7 |
| `forward` | Music player transport – fast forward | `Dynamic Island-11.svg` · cluster 9 |
| `skip_back_15` | Remote video – back 15 seconds | `Dynamic Island-16.svg` · cluster 7 |
| `skip_forward_15` | Remote video – forward 15 seconds | `Dynamic Island-16.svg` · cluster 9 |
| `airplay_audio` | Music player – AirPlay routing | `Dynamic Island-11.svg` · cluster 10 |
| `equalizer` | Now-playing animated equalizer bars | `Dynamic Island-11.svg` · cluster 2 |
| `waveform` | Minimal sheet – audio waveform | `Minimal.svg` · cluster 20 |
| `waveform_recording` | Minimal sheet – live recording trace | `Minimal.svg` · cluster 10 |
| `speaker` | Call controls – speaker / audio route | `Dynamic Island-14.svg` · cluster 4 |
| `airpods` | Call controls – AirPods audio route | `Dynamic Island-13.svg` · cluster 4 |
| `music_note` | SF music.note | authored on the 24pt grid |
| `speaker_slash` | SF speaker.slash.fill – ringer muted | authored on the 24pt grid |

### Calls

| Symbol | Purpose | Origin |
|---|---|---|
| `phone` | Minimal sheet – phone handset | `Minimal.svg` · cluster 2 |
| `phone_down` | Call controls – end call | `Dynamic Island-13.svg` · cluster 8 |
| `microphone` | Call controls – microphone | `Dynamic Island-13.svg` · cluster 5 |
| `video` | Call controls – FaceTime video | `Dynamic Island-13.svg` · cluster 6 |
| `shareplay` | Call controls – SharePlay | `Dynamic Island-13.svg` · cluster 7 |
| `info` | FaceTime header – info button | `Dynamic Island-13.svg` · cluster 2 |
| `chat` | Satellite activity – message button | `Dynamic Island-7.svg` · cluster 1 |
| `close` | Shared session – dismiss | `Dynamic Island-15.svg` · cluster 7 |

### Status & live activities

| Symbol | Purpose | Origin |
|---|---|---|
| `bell` | Minimal sheet – notification bell | `Minimal.svg` · cluster 12 |
| `bell_slash` | Silent mode activity – bell slash | `Dynamic Island-9.svg` · cluster 1 |
| `timer` | Minimal sheet – timer dial | `Minimal.svg` · cluster 4 |
| `record` | Minimal sheet – recording dot | `Minimal.svg` · cluster 11 |
| `stop_circle` | Screen recording – stop control | `Dynamic Island-4.svg` · cluster 0 |
| `lock` | Minimal sheet – locked | `Minimal.svg` · cluster 14 |
| `unlock` | Minimal sheet – unlocked | `Minimal.svg` · cluster 15 |
| `link` | Minimal sheet – link / handoff | `Minimal.svg` · cluster 16 |
| `moon` | Minimal sheet – Focus | `Minimal.svg` · cluster 17 |
| `car` | Minimal sheet – driving Focus | `Minimal.svg` · cluster 18 |
| `face_id` | Minimal sheet – Face ID | `Minimal.svg` · cluster 7 |
| `airdrop` | Minimal sheet – AirDrop | `Minimal.svg` · cluster 1 |
| `shortcut` | Shortcut activity – stacked shortcut mark | `Dynamic Island-5.svg` · cluster 0 |
| `undo` | Moved-to-iPhone activity – undo | `Dynamic Island-10.svg` · cluster 1 |
| `satellite` | Satellite / Find My beacon, in the language of the reference locator glyph | authored on the 24pt grid |
| `check` | SF-style checkmark | authored on the 24pt grid |
| `battery` | SF battery.100 proportions | authored on the 24pt grid |
| `bolt` | SF bolt.fill – charging | authored on the 24pt grid |
| `location` | SF location.north.fill | authored on the 24pt grid |

### System & navigation

| Symbol | Purpose | Origin |
|---|---|---|
| `airplane` | Airplane-mode alert | `Dynamic Island-17.svg` · cluster 0 |
| `screen_mirroring` | Screen mirroring alert | `Dynamic Island-18.svg` · cluster 0 |
| `laptop` | Screen mirroring – target device | `Dynamic Island-18.svg` · cluster 2 |
| `personal_hotspot` | Mobile-data alert – antenna | `Dynamic Island-19.svg` · cluster 1 |
| `transit_train` | Transit route – train | `Dynamic Island-20.svg` · cluster 0 |
| `navigation_left` | Turn-by-turn – turn left | `Dynamic Island-12.svg` · cluster 2 |
| `navigation_right` | Turn-by-turn – turn right | `Dynamic Island-12.svg` · cluster 3 |
| `navigation_straight` | Turn-by-turn – continue straight | `Dynamic Island-12.svg` · cluster 0 |
| `notch` | App mark – Dynamic Island capsule at the reference 126 x 36.67 ratio (r = 18.335) with the TrueDepth camera and Face ID sensor punched out | authored on the 24pt grid |
| `chevron_left` | iOS navigation chevron | authored on the 24pt grid |
| `chevron_right` | iOS navigation chevron | authored on the 24pt grid |
| `wifi` | SF wifi – three arcs and a dot | authored on the 24pt grid |
| `bluetooth` | Bluetooth rune | authored on the 24pt grid |
| `torch` | SF flashlight.on.fill | authored on the 24pt grid |
| `globe` | SF globe | authored on the 24pt grid |
| `search` | SF magnifyingglass | authored on the 24pt grid |
| `settings` | SF gearshape.fill | authored on the 24pt grid |
| `power` | SF power | authored on the 24pt grid |
| `shield` | SF shield.fill | authored on the 24pt grid |
| `rotate` | SF arrow.clockwise | authored on the 24pt grid |
| `reset` | SF arrow.counterclockwise | authored on the 24pt grid |
| `expand` | SF arrow.up.left.and.arrow.down.right | authored on the 24pt grid |
| `history` | SF clock.arrow.circlepath | authored on the 24pt grid |
| `star` | SF star.fill | authored on the 24pt grid |
| `sparkles` | SF sparkles | authored on the 24pt grid |
| `crown` | Pro badge – crown | authored on the 24pt grid |
| `heart` | SF heart.fill | authored on the 24pt grid |
| `palette` | Appearance – palette with colour wells | authored on the 24pt grid |
| `camera` | SF camera.fill | authored on the 24pt grid |
| `controls` | SF slider.horizontal.3 | authored on the 24pt grid |
| `maps` | Maps live activity – folded map | authored on the 24pt grid |

## Colour

`ui/theme/IslandColors.kt` is the single colour source for every island
component. It holds two groups: the iOS 17 dark-mode system palette (the
semantic accents and the systemGray ramp) and values sampled straight out of
the export — capsule fills, the timer sweep, the equalizer gradient, the type
ramp, the glass button fills, and the iOS 17 spectrum wallpaper pulled from the
raster embedded in `iPhone 15 Pro.svg`.

The components previously carried around a hundred loose hex literals, a mix of
iOS values and Tailwind-palette ones (`#10B981`, `#F59E0B`, `#3B82F6`,
`#8B5CF6`, `#E879F9`, `#38BDF8` …) plus a neon indigo/magenta stage backdrop.
Those are gone: every one now resolves through a token, the stage is neutral
graphite with a dimmed wallpaper bloom, and the only literals left are genuine
third-party brand colours (a WhatsApp notification really is `#25D366`) and
black/white alphas. `res/values/colors.xml` mirrors the same tokens for XML
consumers; the template `purple_*` / `teal_*` resources were deleted.

## Launcher mark

The app icon is the Dynamic Island itself: a black capsule at the reference
`126 x 36.67` ratio with a corner radius of exactly half its height, the
TrueDepth lens and Face ID emitter punched in, floating on a wash sampled from
the iOS 17 wallpaper in the device-frame exports. It ships as an adaptive icon
(`ic_launcher_background` / `ic_launcher_foreground` / `ic_launcher_monochrome`
for themed icons) with WebP fallbacks for pre-API-26 launchers.

The previous mark — the stock Android robot on the green grid, plus a
purple/teal capsule drawable — has been removed along with the template
`purple_*` / `teal_*` colour resources.
