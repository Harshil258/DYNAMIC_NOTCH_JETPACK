#!/usr/bin/env python3
"""Index original Figma exports and compare user-reported states at design-equivalent scale."""
from pathlib import Path
import json,html
from PIL import Image,ImageDraw,ImageFont
root=Path(__file__).resolve().parents[1];folder=root/'reports/aurora-figma'
manifest=json.loads((folder/'manifest.json').read_text());tree=json.loads((folder/'reference-node-5-2.json').read_text())['nodes']['5:2']['document'];nodes={}
def walk(n):
 nodes[n['id']]=n
 for c in n.get('children',[]):walk(c)
walk(tree)
font=ImageFont.truetype('/System/Library/Fonts/Helvetica.ttc',20)
# Include raw exports and measurable metadata; never hide export effect bounds in measurements.
entries=[]
for n in manifest:
 node=nodes[n['id']];b=n['bounds'];labels=[]
 def texts(x):
  if x.get('characters'):labels.append(x['characters'].replace('\n',' ')[:70])
  for c in x.get('children',[]):texts(c)
 texts(node)
 entries.append('<article><h2>'+html.escape(n['id']+' · '+n['name'])+'</h2><p>'+html.escape(f"{b['width']:.2f} × {b['height']:.2f} · "+' / '.join(labels)[:160])+'</p><img src="'+n['file']+'"></article>')
(folder/'figma-islands.html').write_text('<!doctype html><meta charset="utf-8"><title>Actual Figma island extracts</title><style>body{background:#171b24;color:white;font:15px system-ui;padding:24px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(390px,1fr));gap:20px}article{background:#090b10;border-radius:16px;padding:16px}img{max-width:100%}</style><h1>Actual Figma · 62 island specimens</h1><p>Original node renders and design bounds. Shadows may extend outside these bounds.</p><main>'+''.join(entries)+'</main>')
def specimen(id):
 n=nodes[id];im=Image.open(folder/('figma-'+id.replace(':','-')+'.png')).convert('RGBA');b=n['absoluteBoundingBox'];r=n.get('absoluteRenderBounds',b)
 # Export pixels are rounded independently by Figma: use the observed size / render bounds.
 sx=im.width/r['width'];sy=im.height/r['height'];x=(b['x']-r['x'])*sx;y=(b['y']-r['y'])*sy
 return im.crop((round(x),round(y),round(x+b['width']*sx),round(y+b['height']*sy))).resize((round(b['width']*2),round(b['height']*2)))
# User images are the reported BEFORE state, not newly installed source evidence.
inputs=[('compact','27:1021',str(folder/'reported-compact.png'),(398/922,405/2048,525/922,488/2048),55,36),('message','27:992',str(folder/'reported-message.png'),(78/922,452/2048,844/922,812/2048),336,157),('call','19:1036',str(folder/'reported-call.png'),(78/922,452/2048,844/922,831/2048),336,166)]
for name,id,path,box,w,h in inputs:
 if not Path(path).exists():continue
 before=Image.open(path).convert('RGBA');before=before.crop(tuple(round(v*(before.width if i%2==0 else before.height)) for i,v in enumerate(box))).resize((round(w*2),round(h*2)))
 ref=specimen(id);ref.thumbnail((734,450))
 out=Image.new('RGB',(1580,550),'#1b2230');draw=ImageDraw.Draw(out)
 draw.text((24,24),'Figma '+id+' · reference',font=font,fill='white');draw.text((814,24),'App · user-reported BEFORE',font=font,fill='white')
 out.paste(ref,(24,85),ref);out.paste(before,(814,85),before)
 draw.text((24,500),'Dynamic images/actions differ; compare geometry and hierarchy.',font=font,fill='#bac5d5')
 out.save(folder/(name+'-before-comparison.png'))
print('62-state Figma index and 3 before comparisons written')

# Run after the final device capture has been pulled; each pair is explicitly labelled.
gallery=root/'reports/aurora-v43'
pairs=[('NotificationCompact','27:1021'),('NotificationExpanded','27:992'),('CallExpanded','19:1036'),('FaceTimeCallExpanded','19:1035'),('SharedMediaCallExpanded','19:1037'),('MusicExpanded','19:1033'),('VideoRemoteExpanded','19:1038'),('TimerExpanded','19:1022'),('AirPodsConnected','19:1023'),('VoiceMemoExpanded','19:1024'),('ScreenRecordingExpanded','19:1025'),('ShortcutExpanded','19:1026'),('OngoingCallBanner','19:1027'),('SatelliteConnected','19:1028'),('FindMyAlert','19:1029'),('SilentModeExpanded','19:1030'),('MovedToIPhone','19:1031'),('AirplaneAlert','19:1039'),('ScreenMirroringAlert','19:1040'),('MobileDataAlert','19:1041'),('TransitRouteAlert','19:1042'),('NotificationImage','27:992')]
if (folder/'final-capture.json').exists():
 pages=[]
 for state,id in pairs:
  path=gallery/(state+'-baseline.png')
  if not path.exists():continue
  ref=specimen(id);app=Image.open(path).convert('RGBA')
  # Native gallery exports at2pixels/dp; preserve identical scale for both sides.
  out=Image.new('RGB',(1580,max(440,app.height+130,ref.height+130)),'#1b2230');draw=ImageDraw.Draw(out)
  draw.text((24,24),'Figma '+id+' · original surface',font=font,fill='white')
  draw.text((814,24),'Android · '+state,font=font,fill='white')
  out.paste(ref,(24,85),ref);out.paste(app,(814,85),app)
  filename=state+'-after-comparison.png';out.save(folder/filename)
  note='Dynamic identities/artwork differ. '
  if state=='CallExpanded':note+='Android audio call:3supported actions; Figma specimen:5. Natural height adaptation.'
  elif state=='NotificationExpanded':note+='Text-only Android message compared with image-message reference; no identical plain-message specimen exists.'
  else:note+='Compare surface, content grouping, typography and action spacing; unsupported actions remain hidden.'
  pages.append('<article><h2>'+state+'</h2><p>'+html.escape(note)+'</p><img src="'+filename+'"><details><summary>200% RTL Android</summary><img src="../aurora-v43/'+state+'-large-rtl.png"></details></article>')
 (folder/'comparisons.html').write_text('<!doctype html><meta charset="utf-8"><title>Aurora Figma side-by-side</title><style>body{background:#121826;color:white;font:16px system-ui;padding:24px}article{padding:20px;border-bottom:1px solid #454c60}img{max-width:100%}</style><h1>Original Figma / installed Android</h1><p>Same design-equivalent scale; synthetic shared production renderer on Realme. No production-overlay or real-call acceptance claim.</p><a href="figma-islands.html">All62original Figma specimens</a>'+''.join(pages))
 print(len(pages),'fresh Figma/Android comparisons written')
 if all((gallery/(state+'-baseline.png')).exists() for state in ['NotificationCompact','CallExpanded']):
  board=Image.new('RGB',(1500,950),'#1b2230');d=ImageDraw.Draw(board)
  for x,title in [(20,'FIGMA reference'),(520,'BEFORE · reported app'),(1020,'AFTER · shared renderer')]:d.text((x,20),title,font=font,fill='white')
  for row,(name,id,path,box,w,h) in enumerate(inputs):
   y=85+row*270
   d.text((20,y),name.upper(),font=font,fill='#b8c9e5')
   ref=specimen(id);ref=ref.resize((round(nodes[id]['absoluteBoundingBox']['width']*1.2),round(nodes[id]['absoluteBoundingBox']['height']*1.2)))
   source=Image.open(path).convert('RGBA');before=source.crop(tuple(round(v*(source.width if i%2==0 else source.height)) for i,v in enumerate(box))).resize((round(w*1.2),round(h*1.2)))
   if name=='message':
    source=Image.open(folder/'lab-after-message-actions.png').convert('RGBA');b=(78/922,452/2048,844/922,742/2048)
    after=source.crop(tuple(round(v*(source.width if i%2==0 else source.height)) for i,v in enumerate(b))).resize((round(336*1.2),round(127*1.2)))
   else:
    after=Image.open(gallery/(('NotificationCompact' if name=='compact' else 'CallExpanded')+'-baseline.png')).convert('RGBA');after=after.resize((round(after.width*.6),round(after.height*.6)))
   for x,im in [(20,ref),(520,before),(1020,after)]:board.paste(im,(x,y+36),im)
  d.text((20,910),'Dynamic imagery differs. Call reference has5 actions; Android audio call has3. Window widths393/360 are labelled in the report.',font=font,fill='#b8c9e5')
  board.save(folder/'requested-states-side-by-side.png')
 # Current-source review sheets, never retain an older correction's montage.
 states=json.loads((gallery/'state-order.json').read_text())
 for variant in ['baseline','large-rtl']:
  for page in range(4):
   sheet=Image.new('RGB',(1500,1500),'#1b2230');draw=ImageDraw.Draw(sheet)
   for i,state in enumerate(states[page*15:(page+1)*15]):
    x=i%3*500;y=i//3*300;shot=Image.open(gallery/(state+'-'+variant+'.png')).convert('RGBA');shot.thumbnail((475,250))
    draw.text((x+12,y+8),state,font=font,fill='white');sheet.paste(shot,(x+(500-shot.width)//2,y+38),shot)
   sheet.save(folder/(variant+'-review-'+str(page+1)+'.png'))
