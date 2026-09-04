"""
svgkit — a dependency-free SVG reader used to mine the iOS 17 Dynamic Island
Figma exports for individual, reusable vector assets.

It is intentionally small and tailored to Figma's export dialect:

* flat documents made of <path>, <rect>, <circle>, <ellipse>, <line>
* absolute coordinates, occasional `transform="translate(...)"` / matrix
* <defs> holding linearGradient / radialGradient / filter / clipPath

Capabilities
------------
* parse a document into a flat list of `Shape`s (geometry + paint + bbox)
* convert primitives (rect/circle/ellipse/line) into path data
* compute tight bounding boxes by flattening beziers and arcs
* cluster shapes into visually-separate "icons" so a single artboard SVG can be
  split into its individual symbols
* re-emit any subset of shapes as a standalone, normalised SVG
"""

from __future__ import annotations

import math
import re
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from typing import Iterable

SVG_NS = "http://www.w3.org/2000/svg"
NS = {"svg": SVG_NS}

_NUM = re.compile(r"[-+]?(?:\d*\.\d+|\d+\.?)(?:[eE][-+]?\d+)?")
_CMD = re.compile(r"([MmLlHhVvCcSsQqTtAaZz])")


# --------------------------------------------------------------------------
# affine transforms (a, b, c, d, e, f)
# --------------------------------------------------------------------------
Matrix = tuple[float, float, float, float, float, float]
IDENTITY: Matrix = (1.0, 0.0, 0.0, 1.0, 0.0, 0.0)


def mat_mul(m: Matrix, n: Matrix) -> Matrix:
    a1, b1, c1, d1, e1, f1 = m
    a2, b2, c2, d2, e2, f2 = n
    return (
        a1 * a2 + c1 * b2,
        b1 * a2 + d1 * b2,
        a1 * c2 + c1 * d2,
        b1 * c2 + d1 * d2,
        a1 * e2 + c1 * f2 + e1,
        b1 * e2 + d1 * f2 + f1,
    )


def mat_apply(m: Matrix, x: float, y: float) -> tuple[float, float]:
    a, b, c, d, e, f = m
    return (a * x + c * y + e, b * x + d * y + f)


def parse_transform(value: str | None) -> Matrix:
    if not value:
        return IDENTITY
    out = IDENTITY
    for name, args in re.findall(r"(\w+)\s*\(([^)]*)\)", value):
        n = [float(v) for v in _NUM.findall(args)]
        if name == "translate":
            m = (1.0, 0.0, 0.0, 1.0, n[0], n[1] if len(n) > 1 else 0.0)
        elif name == "scale":
            sx = n[0]
            sy = n[1] if len(n) > 1 else sx
            m = (sx, 0.0, 0.0, sy, 0.0, 0.0)
        elif name == "matrix":
            m = (n[0], n[1], n[2], n[3], n[4], n[5])
        elif name == "rotate":
            r = math.radians(n[0])
            cos, sin = math.cos(r), math.sin(r)
            m = (cos, sin, -sin, cos, 0.0, 0.0)
            if len(n) == 3:
                m = mat_mul((1.0, 0, 0, 1.0, n[1], n[2]), m)
                m = mat_mul(m, (1.0, 0, 0, 1.0, -n[1], -n[2]))
        else:
            continue
        out = mat_mul(out, m)
    return out


# --------------------------------------------------------------------------
# path data
# --------------------------------------------------------------------------
def tokenize_path(d: str) -> list[tuple[str, list[float]]]:
    parts = [p for p in _CMD.split(d) if p.strip()]
    out: list[tuple[str, list[float]]] = []
    i = 0
    while i < len(parts):
        cmd = parts[i]
        if not _CMD.fullmatch(cmd):
            i += 1
            continue
        args: list[float] = []
        if i + 1 < len(parts) and not _CMD.fullmatch(parts[i + 1]):
            args = [float(v) for v in _NUM.findall(parts[i + 1])]
            i += 1
        out.append((cmd, args))
        i += 1
    return out


def _arc_points(x0, y0, rx, ry, phi, large, sweep, x1, y1, steps=24):
    """Endpoint -> centre parameterisation, sampled."""
    if rx == 0 or ry == 0:
        return [(x1, y1)]
    phi = math.radians(phi)
    cos_p, sin_p = math.cos(phi), math.sin(phi)
    dx2, dy2 = (x0 - x1) / 2.0, (y0 - y1) / 2.0
    x1p = cos_p * dx2 + sin_p * dy2
    y1p = -sin_p * dx2 + cos_p * dy2
    rx, ry = abs(rx), abs(ry)
    lam = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
    if lam > 1:
        s = math.sqrt(lam)
        rx, ry = rx * s, ry * s
    num = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p
    den = rx * rx * y1p * y1p + ry * ry * x1p * x1p
    co = math.sqrt(max(0.0, num / den)) if den else 0.0
    if large == sweep:
        co = -co
    cxp = co * rx * y1p / ry
    cyp = -co * ry * x1p / rx
    cx = cos_p * cxp - sin_p * cyp + (x0 + x1) / 2.0
    cy = sin_p * cxp + cos_p * cyp + (y0 + y1) / 2.0

    def angle(ux, uy, vx, vy):
        dot = ux * vx + uy * vy
        n = math.hypot(ux, uy) * math.hypot(vx, vy)
        if n == 0:
            return 0.0
        a = math.acos(max(-1.0, min(1.0, dot / n)))
        return -a if ux * vy - uy * vx < 0 else a

    th1 = angle(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry)
    dth = angle((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
    if not sweep and dth > 0:
        dth -= 2 * math.pi
    elif sweep and dth < 0:
        dth += 2 * math.pi
    pts = []
    for i in range(1, steps + 1):
        t = th1 + dth * i / steps
        px = cos_p * rx * math.cos(t) - sin_p * ry * math.sin(t) + cx
        py = sin_p * rx * math.cos(t) + cos_p * ry * math.sin(t) + cy
        pts.append((px, py))
    return pts


def flatten_path(d: str, steps: int = 16) -> list[tuple[float, float]]:
    """Sample a path into points (good enough for bounding boxes)."""
    pts: list[tuple[float, float]] = []
    cx = cy = sx = sy = 0.0
    prev_ctrl: tuple[float, float] | None = None
    prev_qctrl: tuple[float, float] | None = None
    for cmd, a in tokenize_path(d):
        up = cmd.upper()
        rel = cmd.islower()

        def pt(i):
            x, y = a[i], a[i + 1]
            return (cx + x, cy + y) if rel else (x, y)

        if up == "M":
            for i in range(0, len(a) - 1, 2):
                p = pt(i)
                if i == 0:
                    sx, sy = p
                pts.append(p)
                cx, cy = p
            prev_ctrl = prev_qctrl = None
        elif up == "L":
            for i in range(0, len(a) - 1, 2):
                p = pt(i)
                pts.append(p)
                cx, cy = p
            prev_ctrl = prev_qctrl = None
        elif up == "H":
            for v in a:
                cx = cx + v if rel else v
                pts.append((cx, cy))
            prev_ctrl = prev_qctrl = None
        elif up == "V":
            for v in a:
                cy = cy + v if rel else v
                pts.append((cx, cy))
            prev_ctrl = prev_qctrl = None
        elif up in ("C", "S"):
            stride = 6 if up == "C" else 4
            for i in range(0, len(a) - stride + 1, stride):
                if up == "C":
                    c1 = pt(i)
                    c2 = pt(i + 2)
                    end = pt(i + 4)
                else:
                    c1 = (2 * cx - prev_ctrl[0], 2 * cy - prev_ctrl[1]) if prev_ctrl else (cx, cy)
                    c2 = pt(i)
                    end = pt(i + 2)
                for s in range(1, steps + 1):
                    t = s / steps
                    mt = 1 - t
                    x = mt**3 * cx + 3 * mt * mt * t * c1[0] + 3 * mt * t * t * c2[0] + t**3 * end[0]
                    y = mt**3 * cy + 3 * mt * mt * t * c1[1] + 3 * mt * t * t * c2[1] + t**3 * end[1]
                    pts.append((x, y))
                prev_ctrl = c2
                cx, cy = end
            prev_qctrl = None
        elif up in ("Q", "T"):
            stride = 4 if up == "Q" else 2
            for i in range(0, len(a) - stride + 1, stride):
                if up == "Q":
                    c1 = pt(i)
                    end = pt(i + 2)
                else:
                    c1 = (2 * cx - prev_qctrl[0], 2 * cy - prev_qctrl[1]) if prev_qctrl else (cx, cy)
                    end = pt(i)
                for s in range(1, steps + 1):
                    t = s / steps
                    mt = 1 - t
                    x = mt * mt * cx + 2 * mt * t * c1[0] + t * t * end[0]
                    y = mt * mt * cy + 2 * mt * t * c1[1] + t * t * end[1]
                    pts.append((x, y))
                prev_qctrl = c1
                cx, cy = end
            prev_ctrl = None
        elif up == "A":
            for i in range(0, len(a) - 6, 7):
                rx, ry, rot, large, sweep = a[i], a[i + 1], a[i + 2], int(a[i + 3]), int(a[i + 4])
                end = (cx + a[i + 5], cy + a[i + 6]) if rel else (a[i + 5], a[i + 6])
                pts.extend(_arc_points(cx, cy, rx, ry, rot, large, sweep, end[0], end[1], steps))
                cx, cy = end
            prev_ctrl = prev_qctrl = None
        elif up == "Z":
            pts.append((sx, sy))
            cx, cy = sx, sy
            prev_ctrl = prev_qctrl = None
    return pts


# --------------------------------------------------------------------------
# shapes
# --------------------------------------------------------------------------
def fmt(v: float) -> str:
    s = f"{v:.3f}".rstrip("0").rstrip(".")
    return "0" if s in ("", "-0") else s


def transform_path_data(d: str, m: Matrix) -> str:
    """Bake an affine matrix into path data so the result needs no transform.

    Only translation + uniform scale is supported for arc radii, which is all
    the Figma exports (and our normalisation step) ever use.
    """
    a, b, c, dd, e, f = m
    sx = math.hypot(a, b)
    sy = math.hypot(c, dd)
    out: list[str] = []
    cx = cy = sx0 = sy0 = 0.0

    def abs_pt(x, y, rel):
        return (cx + x, cy + y) if rel else (x, y)

    for cmd, args in tokenize_path(d):
        up = cmd.upper()
        rel = cmd.islower()
        if up == "Z":
            out.append("Z")
            cx, cy = sx0, sy0
            continue
        pts: list[tuple[float, float]] = []
        if up in ("M", "L", "T"):
            for i in range(0, len(args) - 1, 2):
                p = abs_pt(args[i], args[i + 1], rel)
                pts.append(p)
                cx, cy = p
                if up == "M" and i == 0:
                    sx0, sy0 = p
            letter = up
        elif up == "H":
            for v in args:
                cx = cx + v if rel else v
                pts.append((cx, cy))
            letter = "L"
        elif up == "V":
            for v in args:
                cy = cy + v if rel else v
                pts.append((cx, cy))
            letter = "L"
        elif up in ("C", "S", "Q"):
            stride = {"C": 6, "S": 4, "Q": 4}[up]
            for i in range(0, len(args) - stride + 1, stride):
                chunk = [abs_pt(args[i + k], args[i + k + 1], rel) for k in range(0, stride, 2)]
                pts.extend(chunk)
                cx, cy = chunk[-1]
            letter = up
        elif up == "A":
            for i in range(0, len(args) - 6, 7):
                rx, ry = args[i] * sx, args[i + 1] * sy
                rot, large, sweep = args[i + 2], int(args[i + 3]), int(args[i + 4])
                end = abs_pt(args[i + 5], args[i + 6], rel)
                tp = mat_apply(m, *end)
                out.append(
                    f"A{fmt(rx)} {fmt(ry)} {fmt(rot)} {large} {sweep} {fmt(tp[0])} {fmt(tp[1])}"
                )
                cx, cy = end
            continue
        else:
            continue
        tps = [mat_apply(m, x, y) for x, y in pts]
        out.append(letter + " ".join(f"{fmt(x)} {fmt(y)}" for x, y in tps))
    return "".join(out)


@dataclass
class Shape:
    index: int
    tag: str
    d: str
    fill: str | None
    stroke: str | None
    stroke_width: float
    opacity: float
    fill_rule: str | None
    transform: Matrix
    attrs: dict = field(default_factory=dict)
    bbox: tuple[float, float, float, float] = (0, 0, 0, 0)

    @property
    def width(self) -> float:
        return self.bbox[2] - self.bbox[0]

    @property
    def height(self) -> float:
        return self.bbox[3] - self.bbox[1]

    @property
    def area(self) -> float:
        return self.width * self.height

    @property
    def center(self) -> tuple[float, float]:
        return ((self.bbox[0] + self.bbox[2]) / 2, (self.bbox[1] + self.bbox[3]) / 2)


def _f(attrs: dict, key: str, default: float = 0.0) -> float:
    try:
        return float(attrs.get(key, default))
    except (TypeError, ValueError):
        return default


def primitive_to_path(tag: str, at: dict) -> str:
    if tag == "rect":
        x, y = _f(at, "x"), _f(at, "y")
        w, h = _f(at, "width"), _f(at, "height")
        rx = _f(at, "rx", _f(at, "ry"))
        ry = _f(at, "ry", rx)
        rx, ry = min(rx, w / 2), min(ry, h / 2)
        if rx <= 0 or ry <= 0:
            return f"M{x} {y}H{x + w}V{y + h}H{x}Z"
        return (
            f"M{x + rx} {y}H{x + w - rx}A{rx} {ry} 0 0 1 {x + w} {y + ry}"
            f"V{y + h - ry}A{rx} {ry} 0 0 1 {x + w - rx} {y + h}"
            f"H{x + rx}A{rx} {ry} 0 0 1 {x} {y + h - ry}"
            f"V{y + ry}A{rx} {ry} 0 0 1 {x + rx} {y}Z"
        )
    if tag in ("circle", "ellipse"):
        cx, cy = _f(at, "cx"), _f(at, "cy")
        if tag == "circle":
            rx = ry = _f(at, "r")
        else:
            rx, ry = _f(at, "rx"), _f(at, "ry")
        return (
            f"M{cx - rx} {cy}A{rx} {ry} 0 1 0 {cx + rx} {cy}"
            f"A{rx} {ry} 0 1 0 {cx - rx} {cy}Z"
        )
    if tag == "line":
        return f"M{_f(at, 'x1')} {_f(at, 'y1')}L{_f(at, 'x2')} {_f(at, 'y2')}"
    if tag == "polygon" or tag == "polyline":
        n = [float(v) for v in _NUM.findall(at.get("points", ""))]
        if len(n) < 4:
            return ""
        d = f"M{n[0]} {n[1]}" + "".join(f"L{n[i]} {n[i + 1]}" for i in range(2, len(n) - 1, 2))
        return d + ("Z" if tag == "polygon" else "")
    return ""


def local_tag(el) -> str:
    return el.tag.split("}")[-1]


@dataclass
class Document:
    path: str
    width: float
    height: float
    view_box: tuple[float, float, float, float]
    shapes: list[Shape]
    defs: dict[str, str]

    def bbox(self) -> tuple[float, float, float, float]:
        return union_bbox(self.shapes)


def union_bbox(shapes: Iterable[Shape]) -> tuple[float, float, float, float]:
    boxes = [s.bbox for s in shapes if s.width or s.height]
    if not boxes:
        return (0, 0, 0, 0)
    return (
        min(b[0] for b in boxes),
        min(b[1] for b in boxes),
        max(b[2] for b in boxes),
        max(b[3] for b in boxes),
    )


DRAWABLE = {"path", "rect", "circle", "ellipse", "line", "polygon", "polyline"}
SKIP_CONTAINERS = {"defs", "clipPath", "mask", "filter", "marker", "symbol"}


def parse(path: str) -> Document:
    tree = ET.parse(path)
    root = tree.getroot()
    vb = root.get("viewBox")
    if vb:
        n = [float(v) for v in _NUM.findall(vb)]
        view_box = (n[0], n[1], n[2], n[3])
    else:
        view_box = (0.0, 0.0, _f(root.attrib, "width"), _f(root.attrib, "height"))

    defs: dict[str, str] = {}
    for el in root.iter():
        if local_tag(el) == "defs":
            for child in el:
                cid = child.get("id")
                if cid:
                    defs[cid] = ET.tostring(child, encoding="unicode")

    shapes: list[Shape] = []
    counter = [0]

    def walk(el, matrix: Matrix, opacity: float):
        for child in el:
            tag = local_tag(child)
            if tag in SKIP_CONTAINERS:
                continue
            m = mat_mul(matrix, parse_transform(child.get("transform")))
            o = opacity * float(child.get("opacity", 1) or 1)
            if tag == "g":
                walk(child, m, o)
                continue
            if tag not in DRAWABLE:
                continue
            at = dict(child.attrib)
            d = at.get("d", "") if tag == "path" else primitive_to_path(tag, at)
            if not d:
                continue
            pts = [mat_apply(m, x, y) for x, y in flatten_path(d)]
            if not pts:
                continue
            xs = [p[0] for p in pts]
            ys = [p[1] for p in pts]
            sw = _f(at, "stroke-width", 0.0)
            pad = sw / 2 if at.get("stroke") else 0.0
            shapes.append(
                Shape(
                    index=counter[0],
                    tag=tag,
                    d=d,
                    fill=at.get("fill"),
                    stroke=at.get("stroke"),
                    stroke_width=sw,
                    opacity=o,
                    fill_rule=at.get("fill-rule"),
                    transform=m,
                    attrs=at,
                    bbox=(min(xs) - pad, min(ys) - pad, max(xs) + pad, max(ys) + pad),
                )
            )
            counter[0] += 1

    walk(root, IDENTITY, 1.0)
    return Document(
        path=path,
        width=_f(root.attrib, "width", view_box[2]),
        height=_f(root.attrib, "height", view_box[3]),
        view_box=view_box,
        shapes=shapes,
        defs=defs,
    )


# --------------------------------------------------------------------------
# clustering
# --------------------------------------------------------------------------
def _expand(b, pad):
    return (b[0] - pad, b[1] - pad, b[2] + pad, b[3] + pad)


def _overlaps(a, b) -> bool:
    return not (a[2] < b[0] or b[2] < a[0] or a[3] < b[1] or b[3] < a[1])


def cluster(shapes: list[Shape], pad: float = 2.0) -> list[list[Shape]]:
    """Union-find grouping of shapes whose (padded) boxes touch."""
    parent = list(range(len(shapes)))

    def find(i):
        while parent[i] != i:
            parent[i] = parent[parent[i]]
            i = parent[i]
        return i

    def union(i, j):
        ri, rj = find(i), find(j)
        if ri != rj:
            parent[rj] = ri

    boxes = [_expand(s.bbox, pad) for s in shapes]
    for i in range(len(shapes)):
        for j in range(i + 1, len(shapes)):
            if _overlaps(boxes[i], boxes[j]):
                union(i, j)

    groups: dict[int, list[Shape]] = {}
    for i, s in enumerate(shapes):
        groups.setdefault(find(i), []).append(s)
    out = list(groups.values())
    out.sort(key=lambda g: (round(union_bbox(g)[1] / 10), union_bbox(g)[0]))
    return out


# --------------------------------------------------------------------------
# emitting
# --------------------------------------------------------------------------
def _shape_xml(s: Shape, dx: float, dy: float, scale: float) -> str:
    a, b, c, d, e, f = s.transform
    m = mat_mul((scale, 0, 0, scale, dx * scale, dy * scale), (a, b, c, d, e, f))
    bits = [f'd="{s.d}"']
    tr = f'matrix({m[0]:.6g} {m[1]:.6g} {m[2]:.6g} {m[3]:.6g} {m[4]:.6g} {m[5]:.6g})'
    if m != IDENTITY:
        bits.append(f'transform="{tr}"')
    bits.append(f'fill="{s.fill or "none"}"')
    if s.fill_rule:
        bits.append(f'fill-rule="{s.fill_rule}"')
        bits.append(f'clip-rule="{s.fill_rule}"')
    if s.stroke:
        bits.append(f'stroke="{s.stroke}"')
        if s.stroke_width:
            bits.append(f'stroke-width="{s.stroke_width:g}"')
        for k in ("stroke-linecap", "stroke-linejoin", "stroke-miterlimit"):
            if k in s.attrs:
                bits.append(f'{k}="{s.attrs[k]}"')
    if s.opacity != 1.0:
        bits.append(f'opacity="{s.opacity:g}"')
    return "<path " + " ".join(bits) + "/>"


_URL = re.compile(r"url\(#([^)]+)\)")


def emit(
    shapes: list[Shape],
    defs: dict[str, str],
    box: tuple[float, float, float, float] | None = None,
    pad: float = 0.0,
    size: float | None = None,
) -> str:
    """Render a subset of shapes as a standalone, origin-normalised SVG."""
    b = box or union_bbox(shapes)
    x0, y0, x1, y1 = b[0] - pad, b[1] - pad, b[2] + pad, b[3] + pad
    w, h = x1 - x0, y1 - y0
    scale = 1.0
    if size:
        scale = size / max(w, h) if max(w, h) else 1.0
        # centre the smaller axis inside a square canvas
        off_x = (max(w, h) - w) / 2
        off_y = (max(w, h) - h) / 2
        dx, dy = -x0 + off_x, -y0 + off_y
        vw = vh = size
    else:
        dx, dy = -x0, -y0
        vw, vh = w, h

    used: set[str] = set()
    for s in shapes:
        for value in list(s.attrs.values()):
            for ref in _URL.findall(str(value)):
                used.add(ref)
    # gradients can reference other gradients via href
    frontier = list(used)
    while frontier:
        key = frontier.pop()
        body = defs.get(key, "")
        for ref in re.findall(r'href="#([^"]+)"', body):
            if ref not in used:
                used.add(ref)
                frontier.append(ref)

    defs_xml = "".join(defs[k] for k in used if k in defs)
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{vw:g}" height="{vh:g}" '
        f'viewBox="0 0 {vw:g} {vh:g}" fill="none">'
    ]
    if defs_xml:
        parts.append(f"<defs>{defs_xml}</defs>")
    for s in shapes:
        parts.append(_shape_xml(s, dx, dy, scale))
    parts.append("</svg>")
    return "\n".join(parts).replace(f' xmlns:ns0="{SVG_NS}"', "").replace("ns0:", "")


# --------------------------------------------------------------------------
# normalisation: fit a group of shapes onto a square icon canvas
# --------------------------------------------------------------------------
@dataclass
class BakedPath:
    d: str
    fill: str | None
    stroke: str | None
    stroke_width: float
    opacity: float
    fill_rule: str | None


def bake(
    shapes: list[Shape],
    canvas: float = 24.0,
    optical: float = 20.0,
    box: tuple[float, float, float, float] | None = None,
) -> list[BakedPath]:
    """Scale/centre shapes onto `canvas`, baking all transforms into the data.

    `optical` is the size of the longest edge of the artwork inside the canvas,
    which is how SF Symbols keep glyphs of different aspect ratios looking the
    same weight next to each other.
    """
    b = box or union_bbox(shapes)
    w, h = b[2] - b[0], b[3] - b[1]
    longest = max(w, h) or 1.0
    s = optical / longest
    tx = -b[0] * s + (canvas - w * s) / 2
    ty = -b[1] * s + (canvas - h * s) / 2
    fit: Matrix = (s, 0.0, 0.0, s, tx, ty)
    out = []
    for sh in shapes:
        m = mat_mul(fit, sh.transform)
        out.append(
            BakedPath(
                d=transform_path_data(sh.d, m),
                fill=sh.fill,
                stroke=sh.stroke,
                stroke_width=sh.stroke_width * s,
                opacity=sh.opacity,
                fill_rule=sh.fill_rule,
            )
        )
    return out


def baked_svg(paths: list[BakedPath], canvas: float = 24.0, mono: str | None = None) -> str:
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{canvas:g}" height="{canvas:g}" '
        f'viewBox="0 0 {canvas:g} {canvas:g}" fill="none">'
    ]
    for p in paths:
        bits = [f'd="{p.d}"']
        fill = p.fill
        stroke = p.stroke
        if mono:
            if fill and fill != "none":
                fill = mono
            if stroke and stroke != "none":
                stroke = mono
        bits.append(f'fill="{fill or "none"}"')
        if p.fill_rule:
            bits.append(f'fill-rule="{p.fill_rule}"')
            bits.append(f'clip-rule="{p.fill_rule}"')
        if stroke and stroke != "none":
            bits.append(f'stroke="{stroke}"')
            bits.append(f'stroke-width="{p.stroke_width:.3f}"')
            bits.append('stroke-linecap="round"')
            bits.append('stroke-linejoin="round"')
        if p.opacity != 1.0:
            bits.append(f'opacity="{p.opacity:g}"')
        parts.append("  <path " + " ".join(bits) + "/>")
    parts.append("</svg>")
    return "\n".join(parts)
