"""
Explore a reference SVG: cluster it into visually separate pieces, dump each
piece as a standalone SVG, and print a report. Used to audit the Figma exports
before choosing which symbols become app assets.

    python3 tools/explore.py "<file.svg>" <outdir> [pad]
"""

import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(__file__))
import svgkit as sk  # noqa: E402


def main() -> None:
    src = sys.argv[1]
    out = sys.argv[2]
    pad = float(sys.argv[3]) if len(sys.argv) > 3 else 2.0
    os.makedirs(out, exist_ok=True)
    doc = sk.parse(src)
    # Drop the capsule "shell" (and any other full-bleed backdrop) so the
    # symbols painted on top of it can be separated from each other.
    canvas = max(1.0, doc.view_box[2] * doc.view_box[3])
    shapes = [s for s in doc.shapes if s.area < canvas * 0.30]
    groups = sk.cluster(shapes, pad=pad)
    print(f"{src}: viewBox={doc.view_box} shapes={len(doc.shapes)} clusters={len(groups)}")
    for i, g in enumerate(groups):
        b = sk.union_bbox(g)
        w, h = b[2] - b[0], b[3] - b[1]
        fills = sorted({s.fill for s in g if s.fill and s.fill != "none"})
        print(
            f"  [{i:02d}] n={len(g):3d} box=({b[0]:7.2f},{b[1]:7.2f}) "
            f"{w:7.2f}x{h:7.2f} fills={fills[:6]}"
        )
        svg = sk.emit(g, doc.defs, pad=1.0)
        p = os.path.join(out, f"c{i:02d}.svg")
        with open(p, "w") as fh:
            fh.write(svg)
        subprocess.run(
            ["node", "/tmp/svgtools/render.js", p, p.replace(".svg", ".png"), "160"],
            capture_output=True,
        )


if __name__ == "__main__":
    main()
