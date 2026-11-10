#!/usr/bin/env python3
"""Validate a physical-device screenshot inventory and build a browsable review folder."""
from pathlib import Path
import csv,json,html,hashlib,sys,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'reports/device-screenshots-2026-11-09'
enum=(ROOT/'app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt').read_text().split('enum class IslandDemoState {',1)[1].split('}',1)[0]
states=re.findall(r'\b[A-Za-z][A-Za-z0-9_]*\b',re.sub(r'//[^\n]*','',enum))
(folder/'state-order.json').write_text(json.dumps(states,indent=2))
cols=['file','state','scenario','viewport','font','rtl','dark','widthPx','heightPx']
rows=[dict(zip(cols,r)) for r in csv.reader((folder/'captured.tsv').open(),delimiter='\t') if len(r)==len(cols)]
expected=set()
for theme in ['dark','light']:
 for state in states:expected.add(f'01-native-device/{theme}/{state}.png')
for scenario in ['long-content','missing-content','no-controls']:
 for state in states:expected.add(f'02-content-and-controls/{scenario}/{state}.png')
for theme in ['dark','light']:
 for w in [320,360,393,430,640]:
  for f in [100,130,150,200]:
   for d in ['ltr','rtl']:
    for state in states:expected.add(f'04-responsive/{theme}/w{w}-font{f}-{d}/{state}.png')
extra=[]
def add(ss,cc):
 for s in ss:
  for c in cc:extra.append((s,c))
add(['NotificationExpanded'],['single-message','single-image','broken-image','multi-actions','reply-editor','inbox','progress','indeterminate','redacted','many-messages','pager-first','pager-last'])
add(['NotificationCompact','NotificationStacked'],[f'count-{i}' for i in [0,1,2,3,9,10,99,100]])
add(['CallExpanded','FaceTimeCallExpanded','SharedMediaCallExpanded','CallCompact','CallAvatars'],['dialing','long-duration','unknown-duration','carrier-controls','no-decline','redacted'])
add(['CallSummaryExpanded'],['missed','unknown-duration'])
add(['MusicCompact','MusicExpanded'],['paused'])
add(['TimerCompact','TimerExpanded','TimerImage'],['paused','expired','long-duration'])
add(['ChargingCompact','ChargingExpanded','LowBatteryCompact','LowBatteryExpanded','BluetoothConnected'],[f'battery-{i}' for i in [0,1,19,20,99,100]])
add(['BluetoothConnected'],['battery-unknown','connecting'])
add(['RingerModeExpanded'],['ringer-normal','ringer-vibrate'])
add(['RingerVolumeCompact','MediaVolumeCompact'],['zero','full'])
add(['PremiumExpiryExpanded'],['expired','long-duration'])
add(['Idle','NotificationCompact','MusicCompact','CallCompact'],['cutout','flat-top'])
kinds={'TIMER':'TimerExpanded','DELIVERY':'DeliveryExpanded','FLIGHT':'FlightExpanded','SPORTS':'SportsExpanded','NAVIGATION':'ColorOptions','VOICE_MEMO':'VoiceMemoExpanded','SCREEN_RECORDING':'ScreenRecordingExpanded','SHORTCUT':'ShortcutExpanded','FOCUS_MODE':'FocusModeExpanded','AIR_DROP':'AirDropActivity','AIRPODS':'AirPodsConnected','SATELLITE':'SatelliteConnected','FIND_MY':'FindMyAlert','MOVED_TO_IPHONE':'MovedToIPhone','VIDEO_REMOTE':'VideoRemoteExpanded','AIRPLANE_ALERT':'AirplaneAlert','SCREEN_MIRRORING_ALERT':'ScreenMirroringAlert','MOBILE_DATA_ALERT':'MobileDataAlert','TRANSIT_ROUTE_ALERT':'TransitRouteAlert'}
for kind,state in kinds.items():add([state],[f'source-{kind}-{v}' for v in ['running','paused','missing']])
add(['Idle'],['hidden'])
expected.update(f'03-family-conditions/{s}/{c}.png' for s,c in extra)
recorded={r['file'] for r in rows};present={str(p.relative_to(folder)) for p in folder.rglob('*.png')}
layeredNames=['three-card-stack','promoted-main-and-merged-child','overflow-25-arrivals','narrow-large-text-rtl','narrow-large-text-redaction','pager-swipe-with-children','emerging-frame','merge-frame']
layeredExpected={f'05-layered-and-motion/{n}.png' for n in layeredNames}
missing=sorted((expected|layeredExpected)-present);unrecorded=sorted(expected-recorded)
bad=[]
for r in rows:
 p=folder/r['file']
 try:
  with Image.open(p) as im:
   if im.size != (int(r['widthPx']),int(r['heightPx'])):bad.append(r['file'])
   im.verify()
 except Exception as e:bad.append(r['file']+':'+str(e))
summary={'expectedMainCaptures':len(expected),'capturedMainFiles':len(expected&present),'manifestEntries':len(rows),'missing':missing,'unrecorded':unrecorded,'invalid':bad,'allStates':len(states),'nativeBaselines':120,'contentAndControls':180,'familyConditions':len(extra),'responsive':4800,'additionalLayeredImages':len(list((folder/'05-layered-and-motion').glob('*.png')))}
for path in layeredExpected & present:
 try:
  with Image.open(folder/path) as im:im.verify()
 except Exception as e:bad.append(path+':'+str(e))
summary['totalDeviceCaptures']=len((expected|layeredExpected)&present)
(folder/'coverage.json').write_text(json.dumps(summary,indent=2))
(folder/'by-state').mkdir(exist_ok=True)
def cards(items,prefix=''):
 return ''.join('<article><a href="'+prefix+html.escape(r['file'])+'"><img loading="lazy" class="'+('native' if r['viewport']=='native' else 'crop')+'" src="'+prefix+html.escape(r['file'])+'"></a><strong>'+html.escape(r['state']+' · '+r['scenario'])+'</strong><p>'+html.escape(f'{r["viewport"]} · font {r["font"]} · {"RTL" if r["rtl"]=="true" else "LTR"} · {"dark" if r["dark"]=="true" else "light"} · {r["widthPx"]}×{r["heightPx"]}px')+'</p></article>' for r in items)
style='<meta charset="utf-8"><style>body{background:#121826;color:#fff;font:15px system-ui;margin:24px}a{color:#9ac1ff}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(330px,1fr));gap:18px}article{background:#20283a;padding:14px;border-radius:12px;overflow:hidden}img{width:100%;display:block;background:#080b11;margin-bottom:10px}.native{height:240px;object-fit:cover;object-position:top}.crop{height:240px;object-fit:contain}p{color:#bac6d9}nav{display:flex;gap:12px;flex-wrap:wrap;margin:24px 0}select,input{font:inherit;padding:8px}</style>'
for state in states:
 own=[r for r in rows if r['state']==state]
 (folder/'by-state'/f'{state}.html').write_text('<!doctype html>'+style+f'<title>{state} device screenshots</title><a href="../index.html">All states</a><h1>{state}</h1><p>{len(own)} captures. Click any image for its original resolution. Native previews show the top of the full phone screenshot; responsive images are renderer crops from the phone.</p><main>'+cards(own,'../')+'</main>')
links=''.join(f'<li><a href="by-state/{s}.html">{s}</a></li>' for s in states)
base=[r for r in rows if r['file'].startswith('01-native-device/')]
(folder/'index.html').write_text('<!doctype html>'+style+'<title>Aurora actual-device screenshot review</title><h1>Aurora · actual-device screenshot review</h1><p>'+str(len(rows))+' captures ·60states · physical Realme RMX3085. Native full-screen baselines below; click a state to review all its conditions.</p><p>Mock sources use the real Compose renderer. Responsive widths/fonts/directions are simulated on the physical phone, not separate hardware. Actual carrier calls, keyguard, IME delivery and accessibility overlay windows are not certified.</p><nav><a href="COVERAGE.md">Coverage and limitations</a><a href="coverage.json">Machine-readable completeness check</a><a href="native-dark-contact-sheet.png">Dark contact sheet</a><a href="native-light-contact-sheet.png">Light contact sheet</a></nav><details><summary>All60 states and their conditions</summary><ul>'+links+'</ul></details><h2>All60 states · native device · dark and light</h2><main>'+cards(base)+'</main>')
motionRows=[{'file':f'05-layered-and-motion/{name}.png','state':'Layered mini islands','scenario':name,'viewport':'device fixture','font':'varies','rtl':'false','dark':'true','widthPx':str(Image.open(folder/f'05-layered-and-motion/{name}.png').width),'heightPx':str(Image.open(folder/f'05-layered-and-motion/{name}.png').height)} for name in layeredNames if (folder/f'05-layered-and-motion/{name}.png').exists()]
with (folder/'index.html').open('a') as index:index.write('<h2>Child islands and selected motion frames</h2><main>'+cards(motionRows)+'</main>')
for theme in ['dark','light']:
 sheet=Image.new('RGB',(2160,3840),'#171b24');d=ImageDraw.Draw(sheet)
 for i,state in enumerate(states):
  p=folder/f'01-native-device/{theme}/{state}.png'
  if not p.exists():continue
  with Image.open(p) as src:im=src.convert('RGB').crop((0,0,src.width,min(src.height,900)))
  im.thumbnail((416,270));x=(i%5)*432;y=(i//5)*320
  d.text((x+8,y+8),state,fill='white');sheet.paste(im,(x+(432-im.width)//2,y+32))
 sheet.save(folder/f'native-{theme}-contact-sheet.png')
text=f'''# Aurora physical-device screenshot review

Open **index.html**, then choose a state to view all its conditions. Original PNGs retain their captured resolution.

| Group | Expected | Captured |
|---|---:|---:|
| Native phone,60states ×dark/light |120|{sum(r['file'].startswith('01-') for r in rows)}|
| Long content, missing content, unavailable controls ×60states |180|{sum(r['file'].startswith('02-') for r in rows)}|
| Family-specific notification/call/media/timer/battery/Bluetooth/ringer/source/cutout/hidden cases |{len(extra)}|{sum(r['file'].startswith('03-') for r in rows)}|
|60states ×5widths ×4font scales ×2directions ×2themes |4800|{sum(r['file'].startswith('04-') for r in rows)}|

Expected main images: **{len(expected)}**. Missing files: **{len(missing)}**. Invalid PNGs: **{len(bad)}**. Unrecorded expected files: **{len(unrecorded)}**.

The original60-state inventory is enumerated automatically, including the separate diagnostic owner Minimal, split notification+music, incoming/group/active/summary calls, all activity cards and generic sports (no cricket). Hidden is an additional explicit condition.

Conditions: widths320/360/393/430/640dp (640 is simulated landscape), font100/130/150/200%, LTR/RTL, dark/light. Full-screen native images are1080×2400 on the Realme with its real density/font. Responsive images are crops of the real renderer executed on that same phone with simulated layout inputs. They are not screenshots from five different devices.

Source conditions include single/image/broken-image/inbox/progress/indeterminate/reply/redacted/multi-action/many-message/first/last notification pages; counts0/1/2/3/9/10/99/100; dialing/unknown/long-duration/carrier-controls/no-decline/private/missed calls; paused media/timers; expired/long timers and entitlement; battery0/1/19/20/99/100 and unknown; Bluetooth connecting; ringer modes and zero/full volume; cutout/flat-top renderer inputs;19live-activity kinds with running/paused/missing source fixtures. All source data is synthetic. A condition input may be visually irrelevant to some recipes; the screenshot preserves what the renderer actually produced.

Additional layered captures cover child stacks, promotion/merge, overflow bursts, vertical paging, large text/RTL, redaction and selected animation frames. Motion captures are selected deterministic frames, not a frame-rate certification.

## Not represented as completed real-system conditions

These captures do not claim real incoming/outgoing carrier calls, real notification intake or RemoteInput submission, actual lockscreen/keyguard, real IME animation, TalkBack traversal, OS permission denial/recovery, real hardware rotation, app-background/lifecycle recovery, production accessibility-overlay window correctness, different OEMs or performance. Those require separate system/device acceptance. Redacted and call screenshots here are mock-source visual states, not genuine private notifications or calls. No phone font, theme, rotation, notification, billing or production preferences were changed.

See captured.tsv for every image's inputs and size, capture-provenance.json for device/build hashes, and coverage.json for the missing-file check. Compilation and screenshot completeness are not pixel-parity verdicts.
'''
(folder/'COVERAGE.md').write_text(text)
print(json.dumps(summary,indent=2))
if missing or unrecorded or bad:sys.exit(1)
