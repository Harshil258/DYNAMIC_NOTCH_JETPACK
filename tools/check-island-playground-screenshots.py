#!/usr/bin/env python3
"""Pixel regression for deterministic Compose fixtures. Standard library only.
Never captures or compares real notification content. Baselines change only with
--update-baselines after visual review; normal runs fail on missing/drifting images.
"""
import argparse, datetime, json, struct, subprocess, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FRAMES = ['expiry_320', 'compact_count_393', 'expiry_320_200pct_rtl']

def png_pixels(data):
    if data[:8] != b'\x89PNG\r\n\x1a\n': raise ValueError('Not a PNG capture')
    offset, compressed, header = 8, bytearray(), None
    while offset < len(data):
        length = struct.unpack('>I', data[offset:offset+4])[0]
        kind, payload = data[offset+4:offset+8], data[offset+8:offset+8+length]
        if kind == b'IHDR': header = struct.unpack('>IIBBBBB', payload)
        if kind == b'IDAT': compressed.extend(payload)
        offset += length + 12
    width, height, depth, color, compression, filtering, interlace = header
    if depth != 8 or color not in (2, 6) or interlace != 0 or width*height > 10_000_000:
        raise ValueError('Expected non-interlaced 8-bit RGB/RGBA Compose capture')
    channels = 4 if color == 6 else 3
    stride, raw = width*channels, zlib.decompress(compressed)
    previous, pixels, pos = bytearray(stride), bytearray(), 0
    for _ in range(height):
        filter_type = raw[pos]; pos += 1
        row = bytearray(raw[pos:pos+stride]); pos += stride
        for i in range(stride):
            left = row[i-channels] if i >= channels else 0
            above = previous[i]
            corner = previous[i-channels] if i >= channels else 0
            if filter_type == 1: predictor = left
            elif filter_type == 2: predictor = above
            elif filter_type == 3: predictor = (left+above)//2
            elif filter_type == 4:
                p = left+above-corner
                distances = (abs(p-left), abs(p-above), abs(p-corner))
                predictor = (left, above, corner)[distances.index(min(distances))]
            elif filter_type == 0: predictor = 0
            else: raise ValueError('Invalid PNG filter')
            row[i] = (row[i]+predictor) & 255
        for i in range(0, stride, channels): pixels.extend(row[i:i+3])
        previous = row
    return width, height, pixels

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--serial', required=True)
    parser.add_argument('--update-baselines', action='store_true')
    args = parser.parse_args()
    baselines = ROOT/'docs/design/playground-goldens'
    actuals = ROOT/'build/reports/island-playground/screenshots'
    baselines.mkdir(parents=True, exist_ok=True); actuals.mkdir(parents=True, exist_ok=True)
    results = []
    for frame in FRAMES:
        result = subprocess.run(['adb','-s',args.serial,'exec-out','run-as','com.island.dynamicnotch','cat',f'files/island-layout-qa/{frame}.png'],capture_output=True,timeout=15)
        if result.returncode: raise RuntimeError('Fixture capture missing: '+frame)
        actual = result.stdout
        (actuals/(frame+'.png')).write_bytes(actual)
        baseline = baselines/(frame+'.png')
        if args.update_baselines: baseline.write_bytes(actual)
        if not baseline.exists():
            results.append({'frame':frame,'status':'baseline_missing'}); continue
        a, b = png_pixels(actual), png_pixels(baseline.read_bytes())
        if a[:2] != b[:2]:
            results.append({'frame':frame,'status':'failed','reason':'dimensions_changed'}); continue
        deltas = [abs(x-y) for x,y in zip(a[2],b[2])]
        mean = sum(deltas)/max(1,len(deltas))
        changed = sum(max(deltas[i:i+3]) > 8 for i in range(0,len(deltas),3))/(a[0]*a[1])
        results.append({'frame':frame,'status':'passed' if mean <= .25 and changed <= .005 else 'failed',
                        'mean_rgb_delta':round(mean,6),'changed_pixel_fraction':round(changed,6)})
    report={'generated_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),
            'baseline_update':args.update_baselines,'frames':results,
            'status':'passed' if all(r['status']=='passed' for r in results) else 'failed'}
    (actuals.parent/'visual-results.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))
    return 0 if report['status']=='passed' else 1
if __name__ == '__main__': raise SystemExit(main())
