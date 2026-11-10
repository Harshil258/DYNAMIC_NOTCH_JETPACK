# Aurora — rules learned from the actual Figma atlas

Source: [Aurora Dynamic Island, page5:2](https://www.figma.com/design/dmQdDCYXcy910vWZLfOqqz/?node-id=5-2). Extracted through Figma REST on9 November2026 after the connected Starter MCP account rejected design reads at its tool limit. Original62 PNG exports, full layer metadata and an index are in [the reference evidence](../reports/aurora-figma/figma-islands.html). This file records observed design facts separately from Android adaptations. Credentials belong in macOS Keychain, never this repository.

## Distinguish presentation families

| Specimen | Figma node | Measured surface | Use |
|---|---|---|---|
| Compact notification |27:1021|189×36|App icon at8,8; trailing20×20 count glyph at161,8. Android uses measured8dp visual insets,20dp icon and count glyph.|
| Compact music |19:979|190×38|Two edge regions; central space is deliberate, not intrinsic badge width.|
| Minimal companion |19:992|38×38|A secondary active region; never substitute for a main notification capsule.|
| Expanded image message |27:992|366×126 R44|103×86 thumbnail at20,20; text starts139,26; separate trailing status region.|
| Five-action call |19:1036|367×166 R42|44.27dp identity row at29,34;49.73dp action row at29,92.27;24dp below actions.|
| Alternate five-action call |19:1035|367×168 R42|Same identity origin;51.73dp controls.|
| Expanded music |19:1033|367×177 R42|Distinct metadata, timeline and transport regions.|

The108×32 red owner Minimal specimen in the earlier package is a separate diagnostic state. The Lab's “Minimal notification” is a collapsed real notification and must use the compact notification recipe. Names must not silently switch geometry families.

## Width and content

1. A compact notification has an intentional capsule width, preferred189dp, reduced only by the safe status-side budget. Keep icon and count at opposite ends. Never derive the entire capsule from the width of “1”. Do not insert private sender/message text to justify the width.
2. Expanded Atlas367@393 widths follow the available window. Apply safe bounds once. Do not scale compact widths, text or action targets with a global canvas ratio.
3. Standard Android messages use one identity block. Repeating the app icon in both a header and a second avatar row creates avoidable height. Actual sender artwork is dynamic; do not replace it with a Figma portrait or screenshot.
4. Expanded notification height follows measured content and the actual actions. One content container owns its edge padding. No invisible header spacer, duplicate shell padding or fixed-height filler. Long text may grow and scroll; it must not be reduced to preserve the specimen height.
5. Preserve reference-specific padding for matching recipes. The canonical active call (Figma 19:1036) features a full, balanced 5-button row (`[Speaker] [Mute] [Camera] [SharePlay] [End Call Red #FA3532]`) measuring 49.73dp circular buttons with exact 15.09dp spacing across a 309dp action frame, with 34dp top inset and 24dp bottom inset within a 166dp tall, 42dp corner radius squircle.
6. The five-control reference is a visual recipe. Real call controls must be filtered through source capabilities. Do not present camera or SharePlay as working carrier-call actions without an actual provider. A three-control source-supported audio call is an Android adaptation, not an exact five-control match.

## Visual and interaction quality

- Compare **surface** bounds, not the PNG rectangle. Figma call19:1036 has367×166 surface bounds but399×198 effect bounds. Its shadow is not content padding. Production native windows remain shadow-free under the overlay rule.
- Image thumbnails, caller identity, type weight, glyph scale, action spacing and corner shape must be reviewed together. A matching outer rectangle alone is insufficient.
- Use48dp minimum actual targets for dismiss, reply, call and other actions. Smaller Figma visuals may sit inside these targets. Actions wrap when necessary rather than squeeze or overflow.
- Keep typography independent of canvas width, use shared theme typography and respect200% font scale. Preserve RTL text direction while retaining physical timelines/routes where appropriate.
- Keep source capabilities, privacy, queue selection, drafts and actual playback state authoritative. A visual improvement must not invent successful actions or real source data.

## Required visual proof

For each changed recipe, retain a before image, original Figma node image, measured reference bounds, after image, and matching Android fixture parameters. Put Figma and Android side by side at the same design-equivalent scale; label dynamic content and supported-action differences. Compare compact, expanded message, three-action audio call, five-action call, music, timer and image-message families. Check narrow/wide windows,100%/200% type and RTL. Distinguish shared-renderer Compose fixtures from actual production accessibility overlays. Record limitations honestly; compilation is not visual acceptance.

This owner-requested correction supersedes the previous intrinsic icon/count notification-width rule in `ISLAND_LAYOUT_RULES.md`. Existing privacy, monetization,48dp target, cutout and lifecycle rules still apply.

Large-text review also requires responsive **groups**: call-summary actions use two columns on narrow windows or above130% text, with48dp targets. Flight gate and ETA wrap as independent regions. Descriptive airplane, mirroring, mobile-data and transit alert text must wrap instead of ellipsizing at200%. These are content-driven Android adaptations, not changes to the original Figma metadata.

## Detailed glyph, font and pager audit — 9 November 2026

- Use original outlined glyphs from `design/figma-exact/manifest.json` at native ink dimensions. Never apply the old blanket20-in24 optical inset to these exports. Thirty-seven semantic glyphs currently use original paths; other authored icons are not certified exact.
- Island typography uses bundled Inter4.1 static Text/Display fonts (OFL), an owner-approved Android substitute for SF Pro. Preserve distinct Light/Regular/Medium/Semibold weights and line metrics. Font substitution prevents a literal pixel-identical typography claim. Source: https://rsms.me/inter/download/.
- Notification pages move vertically. Reply editing owns the gesture; arrivals preserve selected source identity, actions and drafts. Observe drag interaction events rather than a potentially coalesced boolean when recording user ownership.
- Compact family heights are explicit: music38dp, notification/Idle36dp, diagnostic owner32dp; other status families use36.67dp.
- Original image-message width is366dp while ordinary expanded shells use367dp. Record the delta rather than resizing captures to conceal it.
- A60-entry gallery is coverage, not acceptance. Classify direct recipe candidates, family adaptations and states without a verified direct specimen. Airplane-mode19:1039 is not a flight tracker or sports scoreboard; mobile-data19:1041 is not pricing; transit19:1042 is not a movie card; music27:912 is not quick controls.
- Fresh same-content captures and numeric pixel differences belong in `reports/aurora-figma-detail/`. Record APK hashes and measured bounds. Preserve source-backed playback values even if a static Figma scrubber is internally inconsistent.

## Live HotSwan review — 10 November 2026

- Inspect the original rendered PNG as well as the instance metadata. The AirDrop
  export shows one inline “AirDrop from Chi” identity; the audio-transfer export
  shows “Moved to iPhone” without a “Connected” label. Do not render additional
  metadata text that is absent from the actual specimen.
- AirPods' nested battery artwork extends beyond its nominal trailing group.
  Its observed outer arc bounds are approximately57.44dp, with4.418dp stroke,
  centered at319.88/42.68 on the367×86 surface. Group bounds alone do not describe
  the visible ring. Unknown real battery values must still display unknown;
  a75% comparison fixture must supply that value explicitly.
- Inferred delivery, flight and sports cards own their padding and region gaps.
  Measure their complete content instead of retaining an unrelated atlas height.
  Stack identity and status at narrow widths or larger text. Team labels must not
  be clipped into decorative circular badges.
- Debug preview canvas space is not capsule content. Use a contrasting canvas,
  constrain the preview viewport, and retain scrolling for tall accessibility
  content. Debug controls must not fill the remaining screen height.
- Capture state and bounds after transitions settle. Require two equal geometry
  observations, exclude zero-area back-stack semantics, and include the visible
  companion when cropping a split island. Keep untouched device screenshots;
  stale bounds invalidate derived comparison crops even when the full PNG is valid.
- New immutable-token constructor fields may require a HotSwan restart. Computed
  top-level recipe extensions preserve the installed class shape. Adding a getter
  to an already resident object may also require a restart. A reload receipt alone is
  insufficient: inspect post-settle health and the actual rendered result.

## Surface matching and continuous corners

- Actual node19:1034 is the lane-guidance card. The legacy ColorOptions enum must not route its null-source comparison preview to a palette. Its original glyphs,51×39 shield,18sp identity and13sp subtitle are the direct reference; source-backed navigation preserves supplied fields.
- Live node metadata uses cornerSmoothing1.0. Standard circular RoundedCornerShape is not a continuous corner. The shared shape follows Figma-style cubic transitions plus a circular arc; smoothing clamps to the available half-edge budget, retaining circular short capsules. [Figma corner smoothing](https://developers.figma.com/docs/plugins/api/properties/nodes-cornersmoothing/) and [figma-squircle implementation](https://github.com/phamfoo/figma-squircle/blob/main/src/draw.ts) are the sources; the MIT notice is in third_party/figma-squircle-LICENSE.
- Surface comparisons must crop original Figma PNGs using absoluteBoundingBox relative to absoluteRenderBounds, rather than assuming symmetrical shadow margins. Keep the original export and crop coordinates. New captures record wm density; comparison images then use the same design-equivalent scale instead of stretching both images to equal width.
