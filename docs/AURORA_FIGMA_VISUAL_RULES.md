# Aurora — rules learned from the actual Figma atlas

Source: [Aurora Dynamic Island, page5:2](https://www.figma.com/design/dmQdDCYXcy910vWZLfOqqz/?node-id=5-2). Extracted through Figma REST on9 November2026 after the connected Starter MCP account rejected design reads at its tool limit. Original62 PNG exports, full layer metadata and an index are in [the reference evidence](../reports/aurora-figma/figma-islands.html). This file records observed design facts separately from Android adaptations. Credentials belong in macOS Keychain, never this repository.

## Distinguish presentation families

| Specimen | Figma node | Measured surface | Use |
|---|---|---|---|
| Compact notification |27:1021|189×36|App icon at8,8; trailing20×20 count glyph at161,8. Android uses balanced12dp shell insets,20dp icon and numeric count.|
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
5. Preserve reference-specific padding for matching recipes. The five-action call's34dp top region is a measured design choice, not a universal inset for every call. A standard three-control Android audio call uses24dp top,20dp bottom and a centered control group, with natural measured height. This is a deliberate platform adaptation.
6. Never add fake video/share controls to make three actual call actions resemble a five-action reference. Different supported actions require different composition, not disabled decoration.

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
