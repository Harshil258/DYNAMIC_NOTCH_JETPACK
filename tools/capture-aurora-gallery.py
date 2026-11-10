#!/usr/bin/env python3
"""Capture each real-device gallery state, verified by live Compose semantics.
The gallery is a synthetic demonstration using the production renderer, not a
production overlay or proof of carrier/media integration. No APK is installed.
"""
import argparse,json,re,subprocess,time,urllib.request
from pathlib import Path
from aurora_gallery_semantics import active_gallery
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--out',required=True);p.add_argument('--width',type=int,choices=[320,360,393,430]);p.add_argument('--font-scale',type=int,choices=[100,200]);p.add_argument('--rtl',action='store_true');p.add_argument('--scroll-endpoints',action='store_true');p.add_argument('--resume',action='store_true');p.add_argument('--states',nargs='+');p.add_argument('--expected-apk-sha');a=p.parse_args()
root=Path(__file__).resolve().parents[1];out=Path(a.out).resolve();out.mkdir(parents=True,exist_ok=True)
port=json.loads((root/'.hotswan/bridge.json').read_text())['port'];base=f'http://127.0.0.1:{port}/api/'
def api(route,body=None):
 data=None if body is None else json.dumps(body).encode()
 result=json.load(urllib.request.urlopen(urllib.request.Request(base+route,data=data,headers={'Content-Type':'application/json'}),timeout=15))
 return active_gallery(result) if route=='semantics' else result
def state():
 tree=api('semantics')
 for n in tree.get('nodes',[]):
  m=re.fullmatch(r'(\d+) / 60 · (\w+)',n.get('text',''))
  if m:return int(m[1]),m[2],tree
 raise RuntimeError('Open the 60-state Gallery; no state header found')
def click_next(tree):
 n=next(n for n in tree['nodes'] if n.get('contentDescription')=='Next state')
 try:
  r=api('input/click',{'nodeId':n['id']})
  if r.get('success') is False:raise RuntimeError(str(r))
 except Exception:
  # Bounds are observed afresh for this actual device, never hardcoded.
  l,t,r,b=n['bounds'];subprocess.run(['adb','-s',a.serial,'shell','input','tap',str(round((l+r)/2)),str(round((t+b)/2))],check=True,timeout=10)
def advance(tree,name):
 click_next(tree)
 deadline=time.monotonic()+12
 while time.monotonic()<deadline:
  time.sleep(.15)
  if state()[1]!=name:return
 raise RuntimeError(f'Next state did not settle after {name}')
def surface_descendants(tree):
 nodes={n['id']:n for n in tree['nodes']}
 surfaces={n['id'] for n in tree['nodes'] if n.get('testTag')=='island_surface' and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]}
 result=[]
 for n in tree['nodes']:
  cur=n
  for _ in range(50):
   if cur['id'] in surfaces:
    if n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]:result.append(n)
    break
   if cur.get('parentId') not in nodes:break
   cur=nodes[cur['parentId']]
 return result

def text_positions(tree):
 return sorted((n.get('text',''),tuple(n['bounds'])) for n in surface_descendants(tree) if n.get('text'))

def capture_scroll_end(entry,tree):
 after=tree;pages=[]
 for page in range(1,31):
  candidates=[n for n in surface_descendants(after) if 'ScrollBy' in n.get('actions',[])]
  if not candidates:break
  # First descendant is the shell viewport, never its nested message pager.
  n=candidates[0];before=text_positions(after)
  distance=max(1,round((n['bounds'][3]-n['bounds'][1])*.7))
  result=api('input/scroll',{'nodeId':n['id'],'direction':'down','distancePx':distance})
  if result.get('success') is False:raise RuntimeError('Scroll failed: '+str(result))
  time.sleep(.3);_,name,after=state();deadline=time.monotonic()+8
  while True:
   if name!=entry['state']:raise RuntimeError('State changed while scrolling')
   positions=text_positions(after);time.sleep(.15);_,name,newtree=state();after=newtree
   if positions==text_positions(after):break
   if time.monotonic()>deadline:raise RuntimeError('Scroll did not settle')
  if before==text_positions(after):break
  file=entry['file'].replace('.png',f'-scroll-{page:02d}.png')
  (out/file).write_bytes(subprocess.check_output(['adb','-s',a.serial,'exec-out','screencap','-p'],timeout=15))
  (out/file.replace('.png','.semantics.json')).write_text(json.dumps(after,indent=2))
  pages.append(file)
 else:raise RuntimeError('Content exceeded30 scroll captures; coverage incomplete')
 if pages:
  entry['scrollFiles']=pages;entry['bottomFile']=pages[-1]
  entry['scrollMethod']='semantics';entry['scrollTarget']='outer content viewport';entry['scrollOverlapPercent']=30
 candidates=[v for v in surface_descendants(after) if 'ScrollBy' in v.get('actions',[])]
 if candidates:
  result=api('input/scroll',{'nodeId':candidates[0]['id'],'direction':'up','distancePx':50000})
  if result.get('success') is False:raise RuntimeError('Could not restore scroll')
 time.sleep(.2)

def choose_text(text):
 tree=api('semantics')
 n=next(n for n in tree['nodes'] if n.get('text')==text and 'OnClick' in n.get('actions',[]) and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1])
 result=api('input/click',{'nodeId':n['id']})
 if result.get('success') is False:raise RuntimeError(str(result))
 time.sleep(.7)
if a.width:choose_text(str(a.width))
if a.font_scale:
 texts={n.get('text') for n in api('semantics')['nodes']}
 desired=f'{a.font_scale}%'
 if desired not in texts:choose_text('100%' if a.font_scale==200 else '200%')
if a.rtl:
 texts={n.get('text') for n in api('semantics')['nodes']}
 if 'RTL' not in texts:choose_text('LTR')
else:
 texts={n.get('text') for n in api('semantics')['nodes']}
 if 'LTR' not in texts:choose_text('RTL')
seen={};manifest={'serial':a.serial,'surface':'physical-device debug gallery, synthetic data, shared renderer','accepted':False,'geometrySampling':'settled-two-equal-frames','scrollEndpointsRequested':a.scroll_endpoints,'requestedProfile':{'width':a.width,'fontPercent':a.font_scale,'rtl':a.rtl},'captures':[]}
density_output=subprocess.check_output(['adb','-s',a.serial,'shell','wm','density'],text=True,timeout=10)
density_values=re.findall(r'(?:Physical|Override) density: (\d+)',density_output)
if not density_values:raise RuntimeError('Could not verify physical device density')
manifest['deviceDensityDpi']=int(density_values[-1])
manifest['appPid']=subprocess.check_output(['adb','-s',a.serial,'shell','pidof','com.island.dynamicnotch'],text=True,timeout=10).strip()
apk_path=subprocess.check_output(['adb','-s',a.serial,'shell','pm','path','com.island.dynamicnotch'],text=True,timeout=10).strip().splitlines()[0].removeprefix('package:')
manifest['installedApkSha256']=subprocess.check_output(['adb','-s',a.serial,'shell','sha256sum',apk_path],text=True,timeout=20).split()[0]
if a.expected_apk_sha and manifest['installedApkSha256']!=a.expected_apk_sha:raise RuntimeError('Installed APK does not match the required visual baseline')
targets=set(a.states) if a.states else None
target_count=len(targets) if targets else 60
manifest['requestedStates']=sorted(targets) if targets else 'all60'
if a.resume:
 previous=json.loads((out/'manifest.json').read_text())
 if previous['requestedProfile']!=manifest['requestedProfile'] or previous['serial']!=a.serial or previous.get('requestedStates','all60')!=manifest['requestedStates']:raise RuntimeError('Resume profile or requested states differs')
 if previous.get('appPid')!=manifest['appPid'] or previous.get('installedApkSha256')!=manifest['installedApkSha256']:raise RuntimeError('Process or installed baseline changed; use a fresh capture folder')
 manifest=previous;seen={s['state']:s['ordinal'] for s in manifest['captures']}
 for _ in range(60):
  _,name,tree=state()
  if name not in seen:break
  click_next(tree);time.sleep(.7)
 else:raise RuntimeError('No uncaptured state available')
for _ in range(60):
 idx,name,tree=state()
 if targets and name not in targets:
  advance(tree,name);continue
 if name in seen:raise RuntimeError(f'Duplicate state before complete coverage: {name}')
 time.sleep(.6)
 idx2,name2,tree=state()
 if (idx,name)!=(idx2,name2):raise RuntimeError('State changed during settling')
 # Retain settled geometry, not the earlier header frame of an AnimatedContent transition.
 deadline=time.monotonic()+8
 while True:
  bounds=[n['bounds'] for n in tree['nodes'] if n.get('testTag')=='island_surface' and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]]
  time.sleep(.15)
  idx3,name3,newtree=state()
  newbounds=[n['bounds'] for n in newtree['nodes'] if n.get('testTag')=='island_surface' and n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1]]
  if (idx3,name3)!=(idx,name):raise RuntimeError('State changed while verifying geometry')
  tree=newtree
  if bounds and bounds==newbounds:break
  if time.monotonic()>deadline:raise RuntimeError('Surface did not settle: '+name)
 if subprocess.check_output(['adb','-s',a.serial,'shell','pidof','com.island.dynamicnotch'],text=True,timeout=10).strip()!=manifest['appPid']:raise RuntimeError('App process changed; visual revision must be revalidated before resuming')
 path=out/f'{idx:02d}-{name}.png'
 image=subprocess.check_output(['adb','-s',a.serial,'exec-out','screencap','-p'],timeout=15)
 if not image.startswith(b'\x89PNG\r\n\x1a\n'):raise RuntimeError('Invalid screenshot')
 path.write_bytes(image);(out/f'{idx:02d}-{name}.semantics.json').write_text(json.dumps(tree,indent=2))
 entry={'state':name,'ordinal':idx,'file':path.name,'visualReview':'pending','capturedAtEpoch':time.time()}
 if a.scroll_endpoints:capture_scroll_end(entry,tree)
 seen[name]=idx;manifest['captures'].append(entry)
 (out/'manifest.json').write_text(json.dumps(manifest,indent=2))
 print(f'{len(seen)}/{target_count} {name}',flush=True)
 if len(seen)==target_count:break
 advance(tree,name)
assert len(seen)==target_count, f'Expected {target_count} states; captured {len(seen)}'
(out/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Aurora device gallery</title><style>body{background:#111;color:white;font:16px system-ui}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:20px}img{width:100%}</style><h1>60 actual-device gallery captures</h1><p>Synthetic demonstrations using the shared renderer. Visual acceptance pending.</p><main>'+''.join(f'<article><h2>{r["ordinal"]}. {r["state"]}</h2><a href="{r["file"]}"><img loading="lazy" src="{r["file"]}"></a>'+ (f'<a href="{r["bottomFile"]}">Scrolled endpoint</a>' if r.get('bottomFile') else '')+'</article>' for r in sorted(manifest['captures'],key=lambda x:x['ordinal']))+'</main>')
index=out/'index.html'
index.write_text(index.read_text().replace('60 actual-device gallery captures', f'{len(seen)} actual-device gallery captures')+'<h2>Overlapping scroll captures</h2>'+''.join('<p>'+r['state']+': '+ ' · '.join(f'<a href="{f}">View {i+1}</a>' for i,f in enumerate(r.get('scrollFiles',[])))+'</p>' for r in manifest['captures'] if r.get('scrollFiles')))
print(index)
