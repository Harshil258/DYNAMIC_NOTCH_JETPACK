# Aurora V6D Deliverable 05: Responsiveness and Accessibility

## 1. Multi-Viewport Responsive Matrix

Every Dynamic Island component was tested and verified across four canonical display widths:

| Viewport Width | Typical Hardware Canvas | Adaptive Layout Adaptation | Action Row Behavior | Result |
|---|---|---|---|---|
| **320dp** | Small compact Android phones | Padding scaled to 12dp; avatar 36dp; compact line-height | 2-button row compresses padding; text maxLines=1 with ellipsis | **PASS** |
| **360dp** | Standard Android handset | Standard tokens: 16dp horizontal padding; 40dp avatar | Primary Redial (1.35f weight) + Done (0.95f weight) | **PASS** |
| **393dp** | Modern flagships (iPhone / Pixel / Galaxy) | Full Figma canonical geometry (367dp capsule, 18dp padding) | Ideal optical spacing, 8dp gap between buttons | **PASS** |
| **430dp** | Large display / Max devices | Max constraints respected; centered horizontal alignment | Spacious action layout with ample touch margins | **PASS** |

---

## 2. Dynamic Type & Font Scaling (1.0x to 2.0x)

- **1.0x (Standard)**: Standard typography tokens (Title 15–16sp, Subtitle 11.5–12sp, Action label 12.5sp).
- **1.3x**: Header and buttons maintain single-line readability without overflowing capsule boundaries.
- **1.5x–2.0x (Accessibility Large)**:
  - Header switches from inline row to stacked layout (`stacked = fontScale > 1.3f`) where needed.
  - Buttons expand vertical padding while preserving 48dp minimum touch bounds.
  - Text softWrap disabled on actions to prevent awkward half-word wrapping; ellipses cleanly when constrained.

---

## 3. Right-to-Left (RTL) & Bi-directional Safety

- **START / END Gravity**: Uses Compose `Alignment.Start` / `Alignment.End` and `Arrangement.SpaceBetween` rather than left/right absolutes.
- **Physical Directions Preserved**:
  - Navigation maneuver arrows (Turn Left, Turn Right) use physical LTR layout providers (`LocalLayoutDirection provides LayoutDirection.Ltr`) to prevent turn directions from flipping in Hebrew/Arabic locales.
  - Chronometers and timers preserve LTR monospace tabular formatting (`textDirection = TextDirection.Ltr`).

---

## 4. Accessibility (TalkBack & WCAG 2.1 AA)

1. **Semantic Roles**:
   - Every action explicitly sets `Role.Button` via Compose semantics.
2. **Accessible Descriptions**:
   - Buttons without visible text (e.g. `IslandIconAction` for call controls, recording stop, and media transport) have descriptive `contentDescription`s ("Accept call", "Decline call", "Toggle speakerphone", "Stop voice memo", "Previous track").
   - Action pills combine action verb and target context (e.g. "Redial Tamia Castillo", "Dismiss call summary").
3. **Touch Target Dimensions**:
   - Minimum interactive dimensions: `defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)` across all tappable surfaces.
   - Touch targets are contained inside the island bounds without expanding touch shields over unrelated screen regions.
4. **Reduced Motion**:
   - `rememberPressScale` inspects `LocalReducedMotion.current`. When enabled, spring physics are replaced with instant snap, preventing visual vestibular triggers.
