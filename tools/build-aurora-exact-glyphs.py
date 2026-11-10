#!/usr/bin/env python3
"""Generate exact outlined Figma glyphs with no optical inset or handwritten substitute."""
from pathlib import Path
import json,sys,xml.etree.ElementTree as ET
sys.path.insert(0,str(Path(__file__).parent))
import svgkit
root=Path(__file__).resolve().parents[1];folder=root/'design/figma-exact'
manifest=json.loads((folder/'manifest.json').read_text());out=['package ai.emots.kishan_dynamic.ui.icons','','/** Generated from authenticated Figma outline exports. Run tools/build-aurora-exact-glyphs.py. */','object FigmaExactGlyphCatalog {']
for m in manifest:
 p=folder/m['file'];doc=svgkit.parse(str(p));r=ET.parse(p).getroot();v=[float(x) for x in r.get('viewBox').split()];paths=svgkit.bake(doc.shapes,canvas=24,optical=24,box=(v[0],v[1],v[0]+v[2],v[1]+v[3]))
 out.append(f'    // Figma {m["node"]}; native ink viewport {v[2]} × {v[3]}.')
 out.append(f'    val {m["glyph"]} = IslandVector("figma_{m["glyph"]}", listOf(')
 for p in paths:
  if p.fill=='none' and not p.stroke:continue
  out.append('        IslandPath(d = '+json.dumps(p.d)+f', stroked = {str(bool(p.stroke)).lower()}, strokeWidth = {p.stroke_width}f, evenOdd = {str(bool(p.fill_rule)).lower()}, alpha = {p.opacity}f),')
 out.append('    ))')
 m['nativeWidth']=v[2];m['nativeHeight']=v[3]
out.append('}')
(root/'app/src/main/java/ai/emots/kishan_dynamic/ui/icons/FigmaExactGlyphCatalog.kt').write_text('\n'.join(out)+'\n')
(folder/'manifest.json').write_text(json.dumps(manifest,indent=2))
print('Generated',len(manifest),'exact glyphs')
