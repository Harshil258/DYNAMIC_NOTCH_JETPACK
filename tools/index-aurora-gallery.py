#!/usr/bin/env python3
"""Crop only observed island_surface semantics; retain untouched full-device PNGs."""
import argparse,json,html
from pathlib import Path
from PIL import Image,ImageDraw
from aurora_gallery_semantics import active_gallery
p=argparse.ArgumentParser();p.add_argument('folder');a=p.parse_args();root=Path(__file__).resolve().parents[1];r=Path(a.folder).resolve();manifest=json.loads((r/'manifest.json').read_text());refs=json.loads((root/'reports/aurora-hotswan-2026-11-10/state-review.json').read_text());refs={x['state']:x for x in refs};out=r/'surfaces';out.mkdir(exist_ok=True);items=[]
reference_nodes={};reference_crops={}
def collect_nodes(value):
 if isinstance(value,dict):
  if value.get('id') and value.get('absoluteBoundingBox'):reference_nodes[value['id']]=value
  for child in value.values():collect_nodes(child)
 elif isinstance(value,list):
  for child in value:collect_nodes(child)
collect_nodes(json.loads((root/'reports/aurora-figma/reference-node-5-2.json').read_text()))
def reference_image(ref):
 source=root/'reports/aurora-figma'/f'figma-{ref.replace(":","-")}.png'
 node=reference_nodes.get(ref)
 if not node:return f'<img src="../../aurora-figma/{source.name}">'
 bounds=node['absoluteBoundingBox'];render=node.get('absoluteRenderBounds') or bounds
 im=Image.open(source);scale=im.width/render['width']
 box=tuple(round(v*scale) for v in (bounds['x']-render['x'],bounds['y']-render['y'],bounds['x']-render['x']+bounds['width'],bounds['y']-render['y']+bounds['height']))
 filename=f'reference-{ref.replace(":","-")}.png';im.crop(box).save(out/filename)
 reference_crops[ref]={'source':str(source),'surfaceBounds':bounds,'renderBounds':render,'pixelCrop':box,'exportScale':scale}
 return f'<a href="../../aurora-figma/{source.name}"><img style="width:{bounds["width"]}px" src="surfaces/{filename}" alt="Figma surface, effect margins removed"></a>'
def design_width(tree,box):
 dpi=manifest.get('deviceDensityDpi');profile=manifest.get('requestedProfile',{}).get('width')
 if not dpi or not profile:return None
 nodes={n['id']:n for n in tree['nodes']}
 surfaces=[n for n in tree['nodes'] if n.get('testTag')=='island_surface' and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]]
 n=max(surfaces,key=lambda n:(n['bounds'][2]-n['bounds'][0])*(n['bounds'][3]-n['bounds'][1]))
 body_width=n['bounds'][2]-n['bounds'][0]
 for _ in range(30):
  parent=nodes.get(n.get('parentId'))
  if not parent:break
  parent_width=parent['bounds'][2]-parent['bounds'][0]
  if parent_width>body_width+1:
   density=min(dpi/160,parent_width/profile)
   return round((box[2]-box[0])/density,3)
  n=parent
 raise RuntimeError('Could not establish fixture density from active stage bounds')
for shot in sorted(manifest['captures'],key=lambda x:x['ordinal']):
 path=r/shot['file'];tree=active_gallery(json.loads(path.with_suffix('.semantics.json').read_text()));regions=[n['bounds'] for n in tree['nodes'] if (n.get('testTag')=='island_surface' or n.get('contentDescription','').startswith('Open companion activity')) and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]]
 if not regions:raise RuntimeError('Missing actual surface bounds: '+shot['state'])
 box=(min(b[0] for b in regions),min(b[1] for b in regions),max(b[2] for b in regions),max(b[3] for b in regions))
 if box[2]<=box[0] or box[3]<=box[1]:raise RuntimeError('Empty surface: '+shot['state'])
 im=Image.open(path);im.crop(tuple(round(x) for x in box)).save(out/path.name)
 item=dict(shot,bounds=box,designWidthDp=design_width(tree,box));items.append(item)
for k in range((len(items)+14)//15):
 sheet=Image.new('RGB',(1200,1150),'#20242e');d=ImageDraw.Draw(sheet)
 for i,item in enumerate(items[k*15:(k+1)*15]):
  im=Image.open(out/item['file']).convert('RGBA');im.thumbnail((380,180));x=(i%3)*400;y=(i//3)*230;d.text((x+8,y+8),f"{item['ordinal']}. {item['state']}",fill='white');sheet.paste(im,(x+8,y+35),im)
 sheet.save(r/f'surfaces-{k+1}.jpg')
articles=[]
for item in items:
 name=html.escape(item['state']);ref=refs[name].get('referenceNode');basis='Figma candidate '+ref if ref else 'Inferred family; no direct specimen verified'
 original=reference_image(ref) if ref else '<p>No direct reference</p>'
 width_style=f' style="width:{item["designWidthDp"]}px"' if item['designWidthDp'] else ''
 endpoint = f'<p><a href="{item["bottomFile"]}">Full device: bottom of scrollable content</a></p>' if item.get('bottomFile') else ''
 articles.append(f'<article><h2>{item["ordinal"]}. {name}</h2><p>{basis}; visual acceptance pending.</p><div class="pair"><figure>{original}<figcaption>Figma surface; click for untouched export</figcaption></figure><figure><a href="{item["file"]}"><img{width_style} src="surfaces/{item["file"]}" alt="Observed Android surface"></a><figcaption>Physical-device shared renderer · synthetic fixture · width {item["designWidthDp"] or "not calibrated"}dp</figcaption></figure></div><a href="{item["file"]}">Full device screenshot</a>{endpoint}</article>')
(r/'surface-review.html').write_text('<!doctype html><meta charset="utf-8"><title>Aurora surface review</title><style>body{background:#11151d;color:#eee;font:16px system-ui;margin:24px}article{margin:30px 0;border-top:1px solid #555}.pair{display:grid;grid-template-columns:1fr 1fr;gap:24px}figure{margin:0;padding:12px;background:#252a35}img{width:100%;max-width:100%;height:auto}a{color:#9cf}</style><h1>Actual-device island surface review</h1><p>Surface crops use the live island_surface bounds; full device PNGs are preserved. When device density is recorded, paired images use1CSS pixel per design dp; widths are not stretched to match. Matching outer geometry does not establish pixel parity. Dynamic fixture and font differences remain visible.</p>'+''.join(articles))
(r/'surface-bounds.json').write_text(json.dumps(items,indent=2));print(f'{len(items)} observed surface crops: {r}/surface-review.html')
(r/'reference-surfaces.json').write_text(json.dumps(reference_crops,indent=2))
