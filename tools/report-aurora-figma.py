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
pairs=[
    ('Idle', '27:1050', 'Idle minimal capsule 127x36 matching Figma 27:1050'),
    ('Minimal', '19:992', 'Minimal companion circle 38x38 matching Figma 19:992 / owner minimal 108x32'),
    ('MusicCompact', '19:979', 'Compact music 190x38 matching Figma 19:979'),
    ('MusicExpanded', '19:1033', 'Expanded music player Heat Waves 367x177 matching Figma 19:1033'),
    ('CallCompact', '19:991', 'Compact ongoing call 155x38 matching Figma 19:991'),
    ('CallExpanded', '19:1036', 'Expanded active audio call with full balanced 5-button row matching Figma 19:1036'),
    ('CallSummaryExpanded', '19:1036', 'Call summary card 367x112 derivation from call family'),
    ('FaceTimeCallExpanded', '19:1035', 'FaceTime audio call with 5-button row matching Figma 19:1035'),
    ('SharedMediaCallExpanded', '19:1037', 'Shared media call SharePlay matching Figma 19:1037'),
    ('OngoingCallBanner', '19:1027', 'Incoming / ongoing call banner 367x86 matching Figma 19:1027'),
    ('NotificationCompact', '27:1021', 'Compact notification 189x36 matching Figma 27:1021'),
    ('NotificationStacked', '27:1021', 'Stacked multi-notification 189x36 matching Figma 27:1021'),
    ('NotificationWithMusicCompact', '19:979', 'Split pill: music 190x38 + notification companion bubble'),
    ('NotificationExpanded', '27:992', 'Expanded text message hugging content tightly without vertical dead space'),
    ('SilentModeCompact', '19:991', 'Compact silent indicator matching compact status family'),
    ('SilentModeExpanded', '19:1030', 'Expanded silent mode alert 367x86 matching Figma 19:1030'),
    ('RingerModeExpanded', '19:1030', 'Expanded ringer mode alert 367x86 matching Figma 19:1030'),
    ('VibrateModeCompact', '19:979', 'Compact vibrate indicator 172x36.67'),
    ('NormalModeCompact', '19:979', 'Compact normal ringer indicator 172x36.67'),
    ('RingerVolumeCompact', '19:979', 'Compact ringer volume slider 172x36.67'),
    ('MediaVolumeCompact', '19:979', 'Compact media volume slider 172x36.67'),
    ('BluetoothConnected', '27:682', 'Compact connected device 189x36 matching Figma 27:682'),
    ('BluetoothConnecting', '27:682', 'Compact connected device connecting status'),
    ('ActionControlExpanded', '27:912', 'Quick action control toggles matching expanded controls family'),
    ('PremiumExpiryExpanded', '19:1030', 'Premium access expiry alert 367x112 derivation'),
    ('ChargingCompact', '19:991', 'Compact charging indicator 132x36.67'),
    ('ChargingExpanded', '19:1023', 'Expanded charging alert 367x86 matching alert family'),
    ('LowBatteryCompact', '19:991', 'Compact low battery indicator 132x36.67'),
    ('LowBatteryExpanded', '19:1023', 'Expanded low battery alert 367x86 matching alert family'),
    ('TimerCompact', '19:979', 'Compact timer countdown 222x36.67'),
    ('TimerExpanded', '19:1022', 'Expanded timer 3:35 matching Figma 19:1022 with 52dp buttons'),
    ('DeliveryCompact', '27:1080', 'Compact delivery indicator matching Figma 27:1080 Lyft'),
    ('DeliveryExpanded', '27:1101', 'Expanded delivery tracking card 367x144'),
    ('FlightCompact', '27:1080', 'Compact flight status indicator 160x36.67'),
    ('FlightExpanded', '19:1039', 'Expanded flight live activity card 367x144'),
    ('SportsCompact', '27:1080', 'Compact sports match score indicator 156x36.67'),
    ('SportsExpanded', '19:1039', 'Expanded sports live activity card 367x144'),
    ('NavigationCompact', '19:1019', 'Compact turn-by-turn navigation indicator 156x36.67'),
    ('VoiceMemoExpanded', '19:1024', 'Expanded voice memo 00:06 matching Figma 19:1024'),
    ('ScreenRecordingExpanded', '19:1025', 'Expanded screen recording 00:06 matching Figma 19:1025'),
    ('ShortcutExpanded', '19:1026', 'Expanded shortcut 100% matching Figma 19:1026'),
    ('FocusModeExpanded', '19:1030', 'Expanded focus mode alert 367x86 matching alert family'),
    ('AirPodsConnected', '19:1023', 'Expanded AirPods connected 367x86 matching Figma 19:1023'),
    ('SatelliteConnected', '19:1028', 'Expanded satellite connected 367x86 matching Figma 19:1028'),
    ('FindMyAlert', '19:1029', 'Expanded Find My alert 367x86 matching Figma 19:1029'),
    ('MovedToIPhone', '19:1031', 'Expanded moved to iPhone 367x86 matching Figma 19:1031'),
    ('VideoRemoteExpanded', '19:1038', 'Expanded video remote Samsung TV 367x177 matching Figma 19:1038'),
    ('AirDropActivity', '19:1021', 'Expanded AirDrop from Chi 367x86 matching Figma 19:1021'),
    ('TimerImage', '19:1022', 'Timer contextual alert derivation with preview'),
    ('NotificationImage', '27:992', 'Expanded image message 366x126 matching Figma 27:992'),
    ('CallAvatars', '19:1027', 'Group call avatar alert derivation 367x86'),
    ('TransportActivity', '27:1101', 'Transport activity card 367x148 matching Figma 27:1101'),
    ('FlightTrackerExpanded', '19:1039', 'Expanded flight tracker live activity 367x144'),
    ('SubscriptionPricing', '19:1041', 'Subscription pricing alert card 367x162 derivation'),
    ('MovieCard', '19:1042', 'Movie preview card 367x142 derivation'),
    ('ColorOptions', '19:1034', 'Turn-by-turn navigation / direction 367x185.4 matching Figma 19:1034'),
    ('AirplaneAlert', '19:1039', 'Turn off airplane mode alert 367x148 matching Figma 19:1039'),
    ('ScreenMirroringAlert', '19:1040', 'Screen mirroring MacBook Pro 367x144 matching Figma 19:1040'),
    ('MobileDataAlert', '19:1041', 'Mobile data turned off alert 367x162 matching Figma 19:1041'),
    ('TransitRouteAlert', '19:1042', 'Transit route Prague station 367x142 matching Figma 19:1042')
]
# Historical captures are retained as evidence, never labelled fresh merely because a file exists.
direct = {'Idle','MusicCompact','MusicExpanded','CallExpanded','FaceTimeCallExpanded','SharedMediaCallExpanded','NotificationCompact','NotificationStacked','SilentModeExpanded','TimerExpanded','VoiceMemoExpanded','ScreenRecordingExpanded','ShortcutExpanded','AirPodsConnected','SatelliteConnected','FindMyAlert','MovedToIPhone','VideoRemoteExpanded','AirDropActivity','NotificationImage','AirplaneAlert','ScreenMirroringAlert','MobileDataAlert','TransitRouteAlert'}
family = {'OngoingCallBanner','CallSummaryExpanded','NotificationWithMusicCompact','NotificationExpanded','RingerModeExpanded','PremiumExpiryExpanded','ChargingExpanded','LowBatteryExpanded','FocusModeExpanded','TimerImage','CallAvatars'}
pages=[]
for state,nodeid,_ in pairs:
 path=gallery/(state+'-baseline.png')
 if not path.exists(): continue
 classification = 'DIRECT RECIPE CANDIDATE' if state in direct else 'FAMILY ADAPTATION' if state in family else 'NO DIRECT SPECIMEN VERIFIED'
 note = 'Historical capture; not current APK acceptance. '+classification+'.'
 if state in direct or state in family:
  ref=specimen(nodeid);app=Image.open(path).convert('RGBA')
  out=Image.new('RGB',(1580,max(440,app.height+130,ref.height+130)),'#1b2230');draw=ImageDraw.Draw(out)
  draw.text((24,24),'Figma '+nodeid+' · '+classification,font=font,fill='white');draw.text((814,24),'HISTORICAL Android · '+state,font=font,fill='white')
  out.paste(ref,(24,85),ref);out.paste(app,(814,85),app)
  filename=state+'-after-comparison.png';out.save(folder/filename)
  visual='<img src="'+filename+'">'
 else:
  visual='<img src="../aurora-v43/'+state+'-baseline.png">'
 pages.append('<article><h2>'+state+'</h2><p>'+note+'</p>'+visual+'</article>')
(folder/'comparisons.html').write_text('<!doctype html><meta charset="utf-8"><title>60-state mapping audit</title><style>body{background:#121826;color:white;font:16px system-ui;padding:24px}article{padding:20px;border-bottom:1px solid #454c60}img{max-width:100%}</style><h1>60-state mapping audit — historical captures</h1><p>Supersedes the unsupported all60 exact-match claim. A similar rectangle or unrelated source node is not pixel parity. No exact-match verdict is issued here.</p><a href="../aurora-figma-detail/comparisons.html">Fresh same-content device pixel audit</a>'+''.join(pages))
print(len(pages),'classified historical articles; no fresh/pixel-perfect claim')
