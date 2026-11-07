# Premium and Settings refinement

## Research and design judgment

Aurora is an Android notification utility. Settings should make preferences and recovery easy to find; Premium should demonstrate the island, explain the offer, and make plan selection clear.

References consulted on 2026-10-31:

- [Android settings patterns](https://developer.android.com/design/ui/mobile/guides/patterns/settings): group preferences into readable lists with labels, supporting text, icons, and appropriate controls.
- [NN/g visual hierarchy](https://www.nngroup.com/articles/visual-hierarchy-ux-definition/): use relative prominence to guide attention in order of importance.
- [NN/g aesthetic-usability effect](https://www.nngroup.com/articles/aesthetic-usability-effect/): visual appeal can affect perceived usability; it does not establish task success.
- [Apple buttons](https://developer.apple.com/design/human-interface-guidelines/buttons), plus the installed apple-design skill's accessibility, layout, typography, color, settings, lists and cross-platform references: distinguish the primary action, support larger text, and group related information. Android controls and the project's 48dp contract take precedence over Apple platform conventions.

This is a design judgment informed by published guidance, not user testing or a measured conversion improvement.

## Implementation

- Settings: remove the horizontal miniature price cards and hardcoded rupee amounts. The selected Settings plan previously did not carry into the purchase screen. One “Explore Aurora Pro” action now leads to live Google Play offers; reward and restore use quieter navigation rows. Paid members get a management action.
- Membership identity and badge stack so larger text does not compete with a trailing badge. Membership gates use EntitlementManager's current snapshot.
- Premium: give each plan a clear title, localized price, billing note, radio semantics and visible selected border/check. Badges wrap using FlowRow. Touch feedback uses the standard ripple.
- Keep the interactive island demonstration and persistent purchase action. The dock stacks price and billing information instead of squeezing both into equal columns. Lifetime identifies a one-time purchase instead of promising subscription cancellation.
- Benefit tiles become a single column below 340dp available width or above 1.3 font scale; normal widths retain two columns. Hero badges wrap.
- Shared buttons allow multiline centered labels, retain minimum 50dp height, show touch feedback, and suppress press scaling with Reduced Motion.
- Aurora Pro purchase buttons use dedicated theme tokens: blue #1D4ED8 and violet #6D28D9 with white labels. Computed WCAG contrast at the endpoints: 6.70:1 and 7.10:1. Other decorative Aurora gradients remain available.

Compact layout: membership → Pro action → secondary rows → preferences. Premium: interactive island → full-width plan cards → recovery/value details, with the selected offer and purchase action pinned. At larger widths, benefit tiles use two columns; pricing remains in a readable vertical list.

## Verification

Final Kotlin compilation and debug APK assembly succeeded. JVM XML reports contain 394 tests with zero failures, errors or skips. git diff --check passed. lintDebug did not finish after more than ten minutes of Kotlin analysis and was stopped; no fresh lint result is claimed. A superseded overlapping validation run was also canceled to reduce memory contention. No connected Android device or configured AVD was available during implementation. On-device light/dark screenshots, 320/360/430dp layouts, 200% font scale and TalkBack remain visual acceptance checks; static responsiveness changes do not establish those checks passed.

Prior unrelated working-tree edits were preserved. No commit or push was performed.
