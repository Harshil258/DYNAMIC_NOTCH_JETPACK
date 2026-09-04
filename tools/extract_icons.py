"""
Build the app's icon set from the iOS 17 Dynamic Island Figma exports.

For every entry in `MANIFEST` this script either

  * `harvest`s real vector geometry out of a reference SVG (the symbol is
    located by clustering the artboard, the backing circle/pill is dropped, and
    the remaining paths are baked onto a 24x24 grid), or
  * `draw`s a symbol that the reference sheets do not contain, authored on the
    same 24x24 grid with the same optical weight so the set stays coherent.

Outputs (both are committed):

  design/icons/<category>/<name>.svg        source-of-truth vector assets
  app/.../ui/icons/IslandVectorCatalog.kt   generated Compose catalogue

Run:  python3 tools/extract_icons.py
"""

from __future__ import annotations

import json
import math
import os
import sys
from dataclasses import dataclass, field

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import svgkit as sk  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REF = os.path.join(ROOT, "design", "reference", "ios17-dynamic-island")
ICONS_OUT = os.path.join(ROOT, "design", "icons")
KOTLIN_OUT = os.path.join(
    ROOT,
    "app/src/main/java/ai/emots/kishan_dynamic/ui/icons/IslandVectorCatalog.kt",
)

CANVAS = 24.0


def island(n: str = "") -> str:
    return os.path.join(REF, "Dynamic Island", f"Dynamic Island{n}.svg")


MINIMAL = os.path.join(REF, "Dynamic Island", "Minimal.svg")


# ---------------------------------------------------------------------------
# helpers for the hand-authored symbols
# ---------------------------------------------------------------------------
def n(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return "0" if s in ("", "-0") else s


def poly(points, close=True) -> str:
    d = "M" + " ".join(f"{n(x)} {n(y)}" for x, y in points[:1])
    d += "".join(f"L{n(x)} {n(y)}" for x, y in points[1:])
    return d + ("Z" if close else "")


def star_path(cx, cy, outer, inner, points=5, rotation=-90.0) -> str:
    pts = []
    for i in range(points * 2):
        r = outer if i % 2 == 0 else inner
        a = math.radians(rotation + i * 180.0 / points)
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return poly(pts)


def gear_path(cx, cy, outer, inner, hole, teeth=8) -> str:
    """A rounded SF-style gear: alternating outer/inner arcs plus a centre hole."""
    step = 2 * math.pi / teeth
    tooth = step * 0.50          # angular width of the tooth crest
    gap = step * 0.10            # shoulder between crest and valley
    d = ""
    for i in range(teeth):
        a = i * step
        a0, a1 = a - tooth / 2, a + tooth / 2
        b0, b1 = a1 + gap, a + step - tooth / 2 - gap
        o0 = (cx + outer * math.cos(a0), cy + outer * math.sin(a0))
        o1 = (cx + outer * math.cos(a1), cy + outer * math.sin(a1))
        i0 = (cx + inner * math.cos(b0), cy + inner * math.sin(b0))
        i1 = (cx + inner * math.cos(b1), cy + inner * math.sin(b1))
        d += ("M" if i == 0 else "L") + f"{n(o0[0])} {n(o0[1])}"
        d += f"A{n(outer)} {n(outer)} 0 0 1 {n(o1[0])} {n(o1[1])}"
        d += f"L{n(i0[0])} {n(i0[1])}"
        d += f"A{n(inner)} {n(inner)} 0 0 1 {n(i1[0])} {n(i1[1])}"
    d += "Z"
    d += (
        f"M{n(cx - hole)} {n(cy)}"
        f"A{n(hole)} {n(hole)} 0 1 0 {n(cx + hole)} {n(cy)}"
        f"A{n(hole)} {n(hole)} 0 1 0 {n(cx - hole)} {n(cy)}Z"
    )
    return d


def rounded_rect(x, y, w, h, r) -> str:
    r = min(r, w / 2, h / 2)
    return (
        f"M{n(x + r)} {n(y)}H{n(x + w - r)}A{n(r)} {n(r)} 0 0 1 {n(x + w)} {n(y + r)}"
        f"V{n(y + h - r)}A{n(r)} {n(r)} 0 0 1 {n(x + w - r)} {n(y + h)}"
        f"H{n(x + r)}A{n(r)} {n(r)} 0 0 1 {n(x)} {n(y + h - r)}"
        f"V{n(y + r)}A{n(r)} {n(r)} 0 0 1 {n(x + r)} {n(y)}Z"
    )


def circle(cx, cy, r) -> str:
    return (
        f"M{n(cx - r)} {n(cy)}A{n(r)} {n(r)} 0 1 0 {n(cx + r)} {n(cy)}"
        f"A{n(r)} {n(r)} 0 1 0 {n(cx - r)} {n(cy)}Z"
    )


# ---------------------------------------------------------------------------
# manifest
# ---------------------------------------------------------------------------
@dataclass
class Stroke:
    d: str
    width: float = 1.9
    cap: str = "round"


@dataclass
class Icon:
    name: str
    category: str
    note: str
    # harvested
    src: str | None = None
    cluster: int | None = None
    keep: tuple[int, ...] | None = None  # indices inside the cluster to keep
    drop_backing: bool = False
    optical: float = 20.0
    # authored
    fills: list[str] = field(default_factory=list)
    strokes: list[Stroke] = field(default_factory=list)
    even_odd: bool = False


def H(name, category, src, cluster, note, optical=20.0, drop_backing=True, keep=None):
    return Icon(
        name=name,
        category=category,
        note=note,
        src=src,
        cluster=cluster,
        keep=keep,
        drop_backing=drop_backing,
        optical=optical,
    )


def D(name, category, note, fills=(), strokes=(), even_odd=False):
    return Icon(
        name=name,
        category=category,
        note=note,
        fills=list(fills),
        strokes=list(strokes),
        even_odd=even_odd,
    )


MANIFEST: list[Icon] = [
    # ---------------------------------------------------------------- media
    H("play", "media", island("-11"), 8, "Music player transport – play triangle", 17),
    H("pause", "media", island("-1"), 0, "Timer activity – pause bars", 15),
    H("backward", "media", island("-11"), 7, "Music player transport – rewind", 19),
    H("forward", "media", island("-11"), 9, "Music player transport – fast forward", 19),
    H("skip_back_15", "media", island("-16"), 7, "Remote video – back 15 seconds", 20),
    H("skip_forward_15", "media", island("-16"), 9, "Remote video – forward 15 seconds", 20),
    H("airplay_audio", "media", island("-11"), 10, "Music player – AirPlay routing", 19),
    H("equalizer", "media", island("-11"), 2, "Now-playing animated equalizer bars", 18),
    H("waveform", "media", MINIMAL, 20, "Minimal sheet – audio waveform", 18),
    H("waveform_recording", "media", MINIMAL, 10, "Minimal sheet – live recording trace", 20),
    H("speaker", "media", island("-14"), 4, "Call controls – speaker / audio route", 18),
    H("airpods", "media", island("-13"), 4, "Call controls – AirPods audio route", 18),
    # ----------------------------------------------------------------- call
    H("phone", "call", MINIMAL, 2, "Minimal sheet – phone handset", 17),
    H("phone_down", "call", island("-13"), 8, "Call controls – end call", 18),
    H("microphone", "call", island("-13"), 5, "Call controls – microphone", 17),
    H("video", "call", island("-13"), 6, "Call controls – FaceTime video", 18),
    H("shareplay", "call", island("-13"), 7, "Call controls – SharePlay", 18),
    H("info", "call", island("-13"), 2, "FaceTime header – info button", 18, drop_backing=False),
    H("chat", "call", island("-7"), 1, "Satellite activity – message button", 16),
    H("close", "call", island("-15"), 7, "Shared session – dismiss", 15),
    # --------------------------------------------------------------- status
    H("bell", "status", MINIMAL, 12, "Minimal sheet – notification bell", 18),
    H("bell_slash", "status", island("-9"), 1, "Silent mode activity – bell slash", 19),
    H("timer", "status", MINIMAL, 4, "Minimal sheet – timer dial", 18),
    H("record", "status", MINIMAL, 11, "Minimal sheet – recording dot", 12),
    H("stop_circle", "status", island("-4"), 0, "Screen recording – stop control", 20, drop_backing=False),
    H("lock", "status", MINIMAL, 14, "Minimal sheet – locked", 17),
    H("unlock", "status", MINIMAL, 15, "Minimal sheet – unlocked", 17),
    H("link", "status", MINIMAL, 16, "Minimal sheet – link / handoff", 17),
    H("moon", "status", MINIMAL, 17, "Minimal sheet – Focus", 16),
    H("car", "status", MINIMAL, 18, "Minimal sheet – driving Focus", 18),
    H("face_id", "status", MINIMAL, 7, "Minimal sheet – Face ID", 18),
    H("airdrop", "status", MINIMAL, 1, "Minimal sheet – AirDrop", 18),
    H("shortcut", "status", island("-5"), 0, "Shortcut activity – stacked shortcut mark", 18, drop_backing=False),
    H("undo", "status", island("-10"), 1, "Moved-to-iPhone activity – undo", 16),
    # --------------------------------------------------------------- system
    H("airplane", "system", island("-17"), 0, "Airplane-mode alert", 19),
    H("screen_mirroring", "system", island("-18"), 0, "Screen mirroring alert", 18),
    H("laptop", "system", island("-18"), 2, "Screen mirroring – target device", 18),
    H("personal_hotspot", "system", island("-19"), 1, "Mobile-data alert – antenna", 18),
    H("transit_train", "system", island("-20"), 0, "Transit route – train", 18),
    H("navigation_left", "system", island("-12"), 2, "Turn-by-turn – turn left", 19),
    H("navigation_right", "system", island("-12"), 3, "Turn-by-turn – turn right", 19),
    H("navigation_straight", "system", island("-12"), 0, "Turn-by-turn – continue straight", 19),
    # ------------------------------------------------- authored on the grid
    D(
        "satellite",
        "status",
        "Satellite / Find My beacon, in the language of the reference locator glyph",
        fills=[circle(12, 12, 2.7)],
        strokes=[
            Stroke("M8.3 8.4A5.1 5.1 0 0 0 8.3 15.6", 1.85),
            Stroke("M5.1 5.9A9 9 0 0 0 5.1 18.1", 1.85),
            Stroke("M15.7 8.4A5.1 5.1 0 0 1 15.7 15.6", 1.85),
            Stroke("M18.9 5.9A9 9 0 0 1 18.9 18.1", 1.85),
        ],
    ),
    D(
        "notch",
        "system",
        "App mark – Dynamic Island capsule at the reference 126 x 36.67 ratio "
        "(r = 18.335) with the TrueDepth camera and Face ID sensor punched out",
        fills=[
            rounded_rect(1.7, 8.7, 20.6, 6.6, 3.3),
            circle(17.9, 12.0, 1.85),
            circle(6.6, 12.0, 1.15),
        ],
        even_odd=True,
    ),
    D(
        "chevron_left",
        "system",
        "iOS navigation chevron",
        strokes=[Stroke("M14.6 4.8L7.6 12L14.6 19.2", 2.1)],
    ),
    D(
        "chevron_right",
        "system",
        "iOS navigation chevron",
        strokes=[Stroke("M9.4 4.8L16.4 12L9.4 19.2", 2.1)],
    ),
    D(
        "check",
        "status",
        "SF-style checkmark",
        strokes=[Stroke("M4.6 12.7L9.5 17.6L19.4 6.6", 2.3)],
    ),
    D(
        "battery",
        "status",
        "SF battery.100 proportions",
        fills=[
            rounded_rect(3.5, 8.9, 11.2, 6.2, 1.7),
            rounded_rect(20.9, 10.2, 1.7, 3.6, 0.85),
        ],
        strokes=[Stroke(rounded_rect(1.7, 7.2, 17.7, 9.6, 3.3), 1.5)],
    ),
    D(
        "bolt",
        "status",
        "SF bolt.fill – charging",
        fills=[poly([(14.6, 1.8), (5.9, 13.4), (10.6, 13.4), (9.4, 22.2), (18.1, 10.4), (13.4, 10.4)])],
    ),
    D(
        "wifi",
        "system",
        "SF wifi – three arcs and a dot",
        strokes=[
            Stroke("M2.4 9.1C7.7 4.3 16.3 4.3 21.6 9.1", 2.0),
            Stroke("M5.9 12.9C9.3 9.9 14.7 9.9 18.1 12.9", 2.0),
            Stroke("M9.4 16.6C10.9 15.3 13.1 15.3 14.6 16.6", 2.0),
        ],
        fills=[circle(12, 19.7, 1.5)],
    ),
    D(
        "bluetooth",
        "system",
        "Bluetooth rune",
        strokes=[Stroke("M12 2.6V21.4M12 2.6L17.6 7.6L6.4 16.4M12 21.4L17.6 16.4L6.4 7.6", 2.0)],
    ),
    D(
        "torch",
        "system",
        "SF flashlight.on.fill",
        fills=[
            rounded_rect(6.9, 2.2, 10.2, 4.6, 1.7),
            "M8.7 8.4H15.3V19.8C15.3 21 14.3 22 13.1 22H10.9C9.7 22 8.7 21 8.7 19.8V8.4Z",
        ],
    ),
    D(
        "location",
        "status",
        "SF location.north.fill",
        fills=[
            "M20.9 3.1L4.4 10.2C3.4 10.6 3.5 12 4.6 12.3L10.6 13.9L12.2 19.9"
            "C12.5 21 13.9 21.1 14.3 20.1L21.4 3.6C21.6 3.1 21.4 2.9 20.9 3.1Z"
        ],
    ),
    D(
        "globe",
        "system",
        "SF globe",
        strokes=[
            Stroke(circle(12, 12, 9.3), 1.8),
            Stroke("M2.9 12H21.1", 1.6),
            Stroke("M12 2.7C14.7 5.3 16 8.6 16 12C16 15.4 14.7 18.7 12 21.3", 1.6),
            Stroke("M12 2.7C9.3 5.3 8 8.6 8 12C8 15.4 9.3 18.7 12 21.3", 1.6),
        ],
    ),
    D(
        "search",
        "system",
        "SF magnifyingglass",
        strokes=[
            Stroke(circle(10.6, 10.6, 6.7), 2.0),
            Stroke("M15.6 15.6L21.2 21.2", 2.2),
        ],
    ),
    D(
        "settings",
        "system",
        "SF gearshape.fill",
        fills=[gear_path(12, 12, 10.3, 7.9, 3.3)],
        even_odd=True,
    ),
    D(
        "power",
        "system",
        "SF power",
        strokes=[
            Stroke("M12 2.6V11.6", 2.2),
            Stroke(
                "M7 5.7C4.4 7.4 2.7 10.4 2.7 13.8C2.7 19 6.9 21.4 12 21.4"
                "C17.1 21.4 21.3 19 21.3 13.8C21.3 10.4 19.6 7.4 17 5.7",
                2.0,
            ),
        ],
    ),
    D(
        "shield",
        "system",
        "SF shield.fill",
        fills=[
            "M12 2.1L4.4 5C3.9 5.2 3.6 5.7 3.6 6.2V11.6C3.6 16.7 6.9 20.4 11.5 22.2"
            "C11.8 22.3 12.2 22.3 12.5 22.2C17.1 20.4 20.4 16.7 20.4 11.6V6.2"
            "C20.4 5.7 20.1 5.2 19.6 5L12 2.1Z"
        ],
    ),
    D(
        "rotate",
        "system",
        "SF arrow.clockwise",
        strokes=[
            Stroke(
                "M20.6 12C20.6 16.7 16.7 20.6 12 20.6C7.3 20.6 3.4 16.7 3.4 12"
                "C3.4 7.3 7.3 3.4 12 3.4C14.9 3.4 17.5 4.9 19.1 7.2",
                2.0,
            )
        ],
        fills=[poly([(20.6, 2.4), (20.6, 8.6), (14.6, 8.0)])],
    ),
    D(
        "reset",
        "system",
        "SF arrow.counterclockwise",
        strokes=[
            Stroke(
                "M3.4 12C3.4 16.7 7.3 20.6 12 20.6C16.7 20.6 20.6 16.7 20.6 12"
                "C20.6 7.3 16.7 3.4 12 3.4C9.1 3.4 6.5 4.9 4.9 7.2",
                2.0,
            )
        ],
        fills=[poly([(3.4, 2.4), (3.4, 8.6), (9.4, 8.0)])],
    ),
    D(
        "expand",
        "system",
        "SF arrow.up.left.and.arrow.down.right",
        strokes=[
            Stroke("M10.1 13.9L3.6 20.4M15.1 3.6H20.4V8.9M8.9 20.4H3.6V15.1M13.9 10.1L20.4 3.6", 2.0),
        ],
    ),
    D(
        "history",
        "system",
        "SF clock.arrow.circlepath",
        strokes=[
            Stroke(circle(12, 12, 9.3), 1.8),
            Stroke("M12 6.6V12.3L15.9 14.7", 1.9),
        ],
    ),
    D("star", "system", "SF star.fill", fills=[star_path(12, 12.2, 10.1, 4.5)]),
    D(
        "sparkles",
        "system",
        "SF sparkles",
        fills=[
            "M13.1 2.4L14.6 6.9C14.8 7.5 15.3 8 15.9 8.2L20.4 9.7L15.9 11.2"
            "C15.3 11.4 14.8 11.9 14.6 12.5L13.1 17L11.6 12.5C11.4 11.9 10.9 11.4 10.3 11.2"
            "L5.8 9.7L10.3 8.2C10.9 8 11.4 7.5 11.6 6.9L13.1 2.4Z",
            "M5.6 14.2L6.3 16.1C6.4 16.4 6.6 16.6 6.9 16.7L8.8 17.4L6.9 18.1"
            "C6.6 18.2 6.4 18.4 6.3 18.7L5.6 20.6L4.9 18.7C4.8 18.4 4.6 18.2 4.3 18.1"
            "L2.4 17.4L4.3 16.7C4.6 16.6 4.8 16.4 4.9 16.1L5.6 14.2Z",
        ],
    ),
    D(
        "crown",
        "system",
        "Pro badge – crown",
        fills=[
            "M2.5 7.4C3.2 7.4 3.8 8 3.8 8.7C3.8 9.1 3.6 9.5 3.3 9.8L4.9 17.4H19.1L20.7 9.8"
            "C20.4 9.5 20.2 9.1 20.2 8.7C20.2 8 20.8 7.4 21.5 7.4C22.2 7.4 22.8 8 22.8 8.7"
            "C22.8 9.4 22.2 10 21.5 10L17.8 13.2L13.3 6.7C13.6 6.4 13.8 6 13.8 5.6"
            "C13.8 4.6 13 3.8 12 3.8C11 3.8 10.2 4.6 10.2 5.6C10.2 6 10.4 6.4 10.7 6.7"
            "L6.2 13.2L2.5 10C1.8 10 1.2 9.4 1.2 8.7C1.2 8 1.8 7.4 2.5 7.4Z",
            rounded_rect(4.9, 18.4, 14.2, 2.4, 1.2),
        ],
    ),
    D(
        "heart",
        "system",
        "SF heart.fill",
        fills=[
            "M12 21.1C11.7 21.1 11.3 21 11 20.7C5.5 16.4 2.2 13 2.2 8.9"
            "C2.2 5.7 4.6 3.4 7.5 3.4C9.4 3.4 11 4.4 12 6C13 4.4 14.6 3.4 16.5 3.4"
            "C19.4 3.4 21.8 5.7 21.8 8.9C21.8 13 18.5 16.4 13 20.7C12.7 21 12.3 21.1 12 21.1Z"
        ],
    ),
    D(
        "palette",
        "system",
        "Appearance – palette with colour wells",
        fills=[
            "M12 2.4C6.6 2.4 2.2 6.7 2.2 12C2.2 17.3 6.6 21.6 12 21.6"
            "C13.4 21.6 14.3 20.7 14.3 19.6C14.3 19.1 14.1 18.6 13.7 18.3"
            "C13.4 17.9 13.2 17.5 13.2 17C13.2 15.9 14.1 15 15.2 15H17.4"
            "C20.1 15 22 13 22 10.4C22 6 17.5 2.4 12 2.4Z",
            circle(7.1, 11.6, 1.6),
            circle(9.8, 7.4, 1.6),
            circle(14.5, 7.2, 1.6),
            circle(17.6, 10.9, 1.6),
        ],
        even_odd=True,
    ),
    D(
        "camera",
        "system",
        "SF camera.fill",
        fills=[
            "M9.4 3.4H14.6L15.9 5.8H19.2C20.7 5.8 21.9 7 21.9 8.5V17.5C21.9 19 20.7 20.2 19.2 20.2"
            "H4.8C3.3 20.2 2.1 19 2.1 17.5V8.5C2.1 7 3.3 5.8 4.8 5.8H8.1L9.4 3.4Z",
            circle(12, 13.1, 3.9),
        ],
        even_odd=True,
    ),
    D(
        "controls",
        "system",
        "SF slider.horizontal.3",
        strokes=[
            Stroke("M2.6 7H21.4", 1.9),
            Stroke("M2.6 12H21.4", 1.9),
            Stroke("M2.6 17H21.4", 1.9),
        ],
        fills=[circle(8.2, 7, 2.6), circle(15.4, 12, 2.6), circle(6.6, 17, 2.6)],
    ),
    D(
        "music_note",
        "media",
        "SF music.note",
        fills=[
            "M20 2.6L9.4 5.1C8.8 5.3 8.4 5.8 8.4 6.4V16.4H10.4V8.1L18.6 6.2V13.7H20.6V3.4"
            "C20.6 2.9 20.4 2.5 20 2.6Z",
            circle(6.4, 17.4, 3.2),
            circle(16.6, 15.1, 3.2),
        ],
    ),
    D(
        "speaker_slash",
        "media",
        "SF speaker.slash.fill – ringer muted",
        fills=[
            "M12.6 3.6L7.6 7.9H4.1C3.3 7.9 2.6 8.6 2.6 9.4V14.6C2.6 15.4 3.3 16.1 4.1 16.1"
            "H7.6L12.6 20.4C13.2 20.9 13.9 20.6 13.9 19.8V4.2C13.9 3.4 13.2 3.1 12.6 3.6Z",
        ],
        strokes=[Stroke("M17.1 9.6L21.9 14.4M21.9 9.6L17.1 14.4", 1.9)],
    ),
    D(
        "maps",
        "system",
        "Maps live activity – folded map",
        fills=[
            "M8.9 3.2L3.6 5.1C3.1 5.3 2.8 5.8 2.8 6.3V20C2.8 20.7 3.4 21.1 4 20.9L8.9 19.1V3.2Z",
            "M10.4 3.2V19.1L15.1 20.8V4.9L10.4 3.2Z",
            "M16.6 4.9V20.8L21 19.1C21.5 18.9 21.8 18.4 21.8 17.9V4.2C21.8 3.5 21.2 3.1 20.6 3.3L16.6 4.9Z",
        ],
    ),
]


# ---------------------------------------------------------------------------
# harvesting
# ---------------------------------------------------------------------------
def harvest(icon: Icon) -> list[sk.BakedPath]:
    doc = sk.parse(icon.src)
    canvas_area = max(1.0, doc.view_box[2] * doc.view_box[3])
    shapes = [s for s in doc.shapes if s.area < canvas_area * 0.30]
    groups = sk.cluster(shapes, pad=1.5)
    if icon.cluster is None or icon.cluster >= len(groups):
        raise SystemExit(f"{icon.name}: cluster {icon.cluster} missing in {icon.src}")
    group = list(groups[icon.cluster])
    if icon.keep is not None:
        group = [group[i] for i in icon.keep]
    elif icon.drop_backing and len(group) > 1:
        biggest = max(group, key=lambda s: s.area)
        group = [s for s in group if s is not biggest]
    if not group:
        raise SystemExit(f"{icon.name}: nothing left after filtering")
    return sk.bake(group, canvas=CANVAS, optical=icon.optical)


def authored(icon: Icon) -> list[sk.BakedPath]:
    out = []
    if icon.even_odd and icon.fills:
        # a single compound path so the inner contours punch real holes
        out.append(
            sk.BakedPath(
                d="".join(icon.fills),
                fill="#FFFFFF",
                stroke=None,
                stroke_width=0.0,
                opacity=1.0,
                fill_rule="evenodd",
            )
        )
    else:
        for d in icon.fills:
            out.append(
                sk.BakedPath(
                    d=d, fill="#FFFFFF", stroke=None, stroke_width=0.0,
                    opacity=1.0, fill_rule=None,
                )
            )
    for st in icon.strokes:
        out.append(
            sk.BakedPath(
                d=st.d, fill=None, stroke="#FFFFFF", stroke_width=st.width,
                opacity=1.0, fill_rule=None,
            )
        )
    return out


# ---------------------------------------------------------------------------
# kotlin emission
# ---------------------------------------------------------------------------
def kotlin_name(name: str) -> str:
    return "".join(part.capitalize() for part in name.split("_"))


def emit_kotlin(entries: list[tuple[Icon, list[sk.BakedPath]]]) -> str:
    lines = [
        "package ai.emots.kishan_dynamic.ui.icons",
        "",
        "/**",
        " * GENERATED FILE — do not edit by hand.",
        " *",
        " * Produced by `python3 tools/extract_icons.py` from the iOS 17 Dynamic Island",
        " * Figma exports in `design/reference/`. Every symbol is defined on the same",
        " * 24 x 24 grid with matching optical weight, so icons stay balanced next to",
        " * each other exactly like SF Symbols do.",
        " *",
        " * The source-of-truth SVGs live in `design/icons/<category>/<name>.svg`.",
        " */",
        "internal object IslandVectorCatalog {",
        "",
        f"    const val VIEWPORT: Float = {CANVAS}f",
        "",
    ]
    for icon, paths in entries:
        var = kotlin_name(icon.name)
        origin = (
            f"{os.path.relpath(icon.src, ROOT)} · cluster {icon.cluster}"
            if icon.src
            else "authored on the 24pt grid"
        )
        lines.append(f"    /** {icon.note}. Source: {origin}. */")
        lines.append(f"    val {var}: IslandVector = IslandVector(")
        lines.append(f'        name = "{icon.name}",')
        lines.append("        paths = listOf(")
        for p in paths:
            args = [f'd = "{p.d}"']
            if p.stroke and p.stroke != "none":
                args.append("stroked = true")
                args.append(f"strokeWidth = {p.stroke_width:.3f}f")
            if p.fill_rule and p.fill_rule.replace("-", "") == "evenodd":
                args.append("evenOdd = true")
            if p.opacity != 1.0:
                args.append(f"alpha = {p.opacity:.3f}f")
            lines.append("            IslandPath(" + ", ".join(args) + "),")
        lines.append("        ),")
        lines.append("    )")
        lines.append("")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def emit_index(entries) -> str:
    """A single contact sheet so the whole set can be reviewed at a glance."""
    cols = 8
    cell = 64
    label = 16
    rows = (len(entries) + cols - 1) // cols
    w = cols * cell
    h = rows * (cell + label)
    out = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" '
        f'viewBox="0 0 {w} {h}" fill="none">',
        f'<rect width="{w}" height="{h}" fill="#0B0B0D"/>',
    ]
    for i, (icon, paths) in enumerate(entries):
        cx = (i % cols) * cell
        cy = (i // cols) * (cell + label)
        scale = 32.0 / CANVAS
        off = (cell - 32.0) / 2
        out.append(f'<g transform="translate({cx + off:.2f} {cy + off:.2f}) scale({scale:.4f})">')
        for pth in paths:
            bits = [f'd="{pth.d}"']
            if pth.stroke:
                bits.append('fill="none" stroke="#FFFFFF"')
                bits.append(f'stroke-width="{pth.stroke_width:.3f}"')
                bits.append('stroke-linecap="round" stroke-linejoin="round"')
            else:
                bits.append('fill="#FFFFFF"')
                if pth.fill_rule:
                    bits.append('fill-rule="evenodd" clip-rule="evenodd"')
            out.append("  <path " + " ".join(bits) + "/>")
        out.append("</g>")
        out.append(
            f'<text x="{cx + cell / 2:.1f}" y="{cy + cell + 10:.1f}" fill="#7A7A82" '
            f'font-family="SF Pro Text, Helvetica, Arial, sans-serif" font-size="7" '
            f'text-anchor="middle">{icon.name}</text>'
        )
    out.append("</svg>")
    return "\n".join(out)


def main() -> None:
    names = [i.name for i in MANIFEST]
    dupes = {x for x in names if names.count(x) > 1}
    if dupes:
        raise SystemExit(f"duplicate icon names: {dupes}")

    entries: list[tuple[Icon, list[sk.BakedPath]]] = []
    for icon in MANIFEST:
        paths = harvest(icon) if icon.src else authored(icon)
        entries.append((icon, paths))
        out_dir = os.path.join(ICONS_OUT, icon.category)
        os.makedirs(out_dir, exist_ok=True)
        svg = sk.baked_svg(paths, canvas=CANVAS, mono="#FFFFFF")
        header = (
            f"<!-- {icon.note}\n"
            f"     source: "
            + (
                f"{os.path.relpath(icon.src, ROOT)} (cluster {icon.cluster})"
                if icon.src
                else "authored on the shared 24pt grid"
            )
            + "\n     generated by tools/extract_icons.py — do not edit by hand -->\n"
        )
        with open(os.path.join(out_dir, f"{icon.name}.svg"), "w") as fh:
            fh.write(header + svg + "\n")

    with open(os.path.join(ICONS_OUT, "index.svg"), "w") as fh:
        fh.write(emit_index(entries) + "\n")

    manifest = [
        {
            "name": i.name,
            "category": i.category,
            "note": i.note,
            "source": os.path.relpath(i.src, ROOT) if i.src else None,
            "cluster": i.cluster,
            "contours": len(p),
        }
        for i, p in entries
    ]
    with open(os.path.join(ICONS_OUT, "manifest.json"), "w") as fh:
        json.dump(manifest, fh, indent=2)
        fh.write("\n")

    os.makedirs(os.path.dirname(KOTLIN_OUT), exist_ok=True)
    with open(KOTLIN_OUT, "w") as fh:
        fh.write(emit_kotlin(entries))

    by_cat: dict[str, int] = {}
    for icon, _ in entries:
        by_cat[icon.category] = by_cat.get(icon.category, 0) + 1
    print(f"wrote {len(entries)} icons -> design/icons ({by_cat})")
    print(f"wrote {os.path.relpath(KOTLIN_OUT, ROOT)}")


if __name__ == "__main__":
    main()
