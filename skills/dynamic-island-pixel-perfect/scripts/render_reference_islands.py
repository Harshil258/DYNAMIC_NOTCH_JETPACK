#!/usr/bin/env python3
"""Render Figma Dynamic Island SVGs and crop their black body rectangles.

Uses macOS Quick Look for SVG rasterization, then crops in the Quick Look
coordinate space. This avoids the ImageMagick SVG renderer dropping embedded
XLink images from Figma exports.
"""

from __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import tempfile
from pathlib import Path


NUMBER = r"[-+]?(?:\d*\.\d+|\d+\.?)(?:[eE][-+]?\d+)?"


def number(attrs: str, name: str, default: float = 0.0) -> float:
    match = re.search(rf'\b{name}="({NUMBER})"', attrs)
    return float(match.group(1)) if match else default


def body_geometry(source: Path) -> tuple[float, float, float, float, float, float, float]:
    text = source.read_text(encoding="utf-8", errors="ignore")
    canvas = re.search(r'<svg\s+width="([0-9.]+)"\s+height="([0-9.]+)"', text)
    body = re.search(r'<rect\s+([^>]*?\bfill="black"[^>]*)>', text)
    if not canvas or not body:
        raise ValueError(f"Could not find SVG canvas/body geometry: {source}")
    attrs = body.group(1)
    return (
        float(canvas.group(1)),
        float(canvas.group(2)),
        number(attrs, "x"),
        number(attrs, "y"),
        number(attrs, "width"),
        number(attrs, "height"),
        number(attrs, "rx"),
    )


def resource_name(source: Path) -> str:
    name = re.sub(r"[^a-zA-Z0-9]+", "_", source.stem).strip("_").lower()
    return name or "reference"


def render_one(source: Path, out: Path, quicklook_dir: Path, size: int) -> dict[str, object]:
    canvas_w, canvas_h, x, y, body_w, body_h, body_rx = body_geometry(source)
    scale = size / max(canvas_w, canvas_h)
    subprocess.run(
        ["qlmanage", "-t", "-s", str(size), "-o", str(quicklook_dir), str(source)],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    raw = quicklook_dir / f"{source.name}.png"
    if not raw.exists():
        raise FileNotFoundError(f"Quick Look did not produce {raw}")

    converter = shutil.which("convert")
    if converter is None:
        magick = shutil.which("magick")
        if magick is None:
            raise RuntimeError("Need ImageMagick convert or magick for cropping")
        converter_command = [magick, "convert"]
    else:
        converter_command = [converter]

    crop = (
        f"{round(body_w * scale)}x{round(body_h * scale)}"
        f"+{round(x * scale)}+{round(y * scale)}"
    )
    crop_w = round(body_w * scale)
    crop_h = round(body_h * scale)
    crop_rx = round(body_rx * scale, 3)
    output = out / f"{resource_name(source)}.png"
    subprocess.run(
        converter_command
        + [
            str(raw),
            "-crop",
            crop,
            "+repage",
            "-alpha",
            "on",
            "(",
            "-size",
            f"{crop_w}x{crop_h}",
            "xc:none",
            "-fill",
            "white",
            "-draw",
            f"roundrectangle 0,0,{crop_w - 1},{crop_h - 1},{crop_rx},{crop_rx}",
            ")",
            "-compose",
            "CopyOpacity",
            "-composite",
            str(output),
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    return {
        "source": source.name,
        "output": output.name,
        "canvas": [canvas_w, canvas_h],
        "body": {
            "x": x,
            "y": y,
            "width": body_w,
            "height": body_h,
            "rx": body_rx,
        },
        "scale": scale,
        "crop": crop,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("reference_folder", type=Path)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--size", type=int, default=1200)
    args = parser.parse_args()

    if shutil.which("qlmanage") is None:
        raise SystemExit("This renderer requires macOS Quick Look (qlmanage).")
    source_dir = args.reference_folder
    if not source_dir.is_dir():
        raise SystemExit(f"Reference folder does not exist: {source_dir}")
    args.out.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix="dynamic-island-ql-") as temp:
        quicklook_dir = Path(temp)
        manifest = []
        for source in sorted(source_dir.glob("*.svg")):
            manifest.append(render_one(source, args.out, quicklook_dir, args.size))

    (args.out / "manifest.json").write_text(
        json.dumps(manifest, indent=2), encoding="utf-8"
    )
    print(f"Rendered {len(manifest)} SVG bodies to {args.out}")


if __name__ == "__main__":
    main()
