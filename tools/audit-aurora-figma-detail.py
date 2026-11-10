#!/usr/bin/env python3
"""Compare fresh same-content device crops with Figma at 2 pixels/design unit.
Never resize Android to fit a reference: geometric differences remain visible.
"""
from pathlib import Path
import json, html, hashlib
import numpy as np
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'reports/aurora-figma-detail'; REF=ROOT/'reports/aurora-figma'
nodes={}
def walk(n):
 nodes[n['id']]=n
 for c in n.get('children',[]): walk(c)
walk(json.loads((REF/'reference-node-5-2.json').read_text())['nodes']['5:2']['document'])
pairs={'NotificationExpanded':'27:992','NotificationImage':'27:992','NotificationCompact':'27:1021','MusicCompact':'19:979','MusicExpanded':'19:1033','CallExpanded':'19:1036','FaceTimeCallExpanded':'19:1035','SharedMediaCallExpanded':'19:1037','SilentModeExpanded':'19:1030','TimerExpanded':'19:1022','CallAvatars':'19:1027'}
rows=[]; articles=[]
for state,nodeid in pairs.items():
 n=nodes[nodeid]; b=n['absoluteBoundingBox']; r=n.get('absoluteRenderBounds',b)
 ref=Image.open(REF/f"figma-{nodeid.replace(':','-')}.png").convert('RGBA');sx=ref.width/r['width'];sy=ref.height/r['height']
 x=(b['x']-r['x'])*sx;y=(b['y']-r['y'])*sy
 ref=ref.crop((round(x),round(y),round(x+b['width']*sx),round(y+b['height']*sy))).resize((round(b['width']*2),round(b['height']*2)),Image.Resampling.LANCZOS)
 black=Image.new('RGBA',ref.size,'black');black.alpha_composite(ref);ref=black.convert('RGB')
 app=Image.open(OUT/'device'/f'{state}.png').convert('RGB')
 w=max(ref.width,app.width);h=max(ref.height,app.height)
 canvases=[]
 for im in (ref,app):
  c=Image.new('RGB',(w,h));c.paste(im,(0,0));canvases.append(c)
 a,z=[np.asarray(c).astype(float) for c in canvases];diff=np.abs(a-z);mask=(a.max(2)>40)|(z.max(2)>40)
 fm=a.max(2)>40;am=z.max(2)>40
 row={'state':state,'figmaNode':nodeid,'figmaDp':[b['width'],b['height']],'androidDp':[app.width/2,app.height/2],'widthDeltaDp':app.width/2-b['width'],'heightDeltaDp':app.height/2-b['height'],'rgbMAE':float(diff.mean()),'foregroundMAE':float(diff[mask].mean()),'foregroundIoU':float((fm&am).sum()/max(1,(fm|am).sum())),'status':'REVIEW_REQUIRED'}
 rows.append(row)
 heat=Image.fromarray(np.clip(diff*3,0,255).astype('uint8'))
 board=Image.new('RGB',(w*3+48,h+64),'#171b24');d=ImageDraw.Draw(board)
 for i,(title,im) in enumerate(zip(('Figma '+nodeid,'Android '+state,'Absolute RGB difference ×3'),(*canvases,heat))):
  d.text((i*(w+16)+8,10),title,fill='white');board.paste(im,(i*(w+16)+8,48))
 board.save(OUT/f'{state}-diff.png')
 articles.append(f'<article><h2>{state}</h2><p>Size delta {row["widthDeltaDp"]:.2f} × {row["heightDeltaDp"]:.2f} dp; foreground MAE {row["foregroundMAE"]:.2f}/255; foreground IoU {row["foregroundIoU"]:.3f}. Review required.</p><img src="{state}-diff.png"></article>')
(OUT/'pixel-metrics.json').write_text(json.dumps(rows,indent=2))
(OUT/'comparisons.html').write_text('<!doctype html><meta charset="utf-8"><title>Measured device / Figma audit</title><style>body{background:#111827;color:white;font:16px system-ui;padding:24px}img{max-width:100%}article{padding:20px 0;border-bottom:1px solid #555}</style><h1>Measured device / Figma audit</h1><p>Native Android surface crops at 2 pixels/dp, reference surface bounds excluding shadow. Android is never resized to hide size differences. Black canvas padding preserves mismatched bounds. Metrics quantify differences; they do not certify exact parity. Inter is an approved font substitute. Playback progress/state and source-supported controls may differ. Production accessibility overlay acceptance is separate.</p>'+''.join(articles))
print(json.dumps(rows,indent=2))
