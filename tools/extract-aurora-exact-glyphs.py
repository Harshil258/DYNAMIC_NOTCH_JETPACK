from pathlib import Path
import json,subprocess,urllib.request,urllib.parse
p=Path(__file__).resolve().parents[1]/'design/figma-exact';p.mkdir(parents=True,exist_ok=True)
spec={'Speaker':'I19:1036;43:772','Microphone':'I19:1036;43:785','FaceTimeVideo':'I19:1036;43:788','SharePlay':'I19:1036;43:791','EndCall':'I19:1036;43:782','Info':'I19:1036;43:792','AirPods':'I19:1035;73:816','Forward':'I19:1033;1:1029','Backward':'I19:1033;1:1027','Play':'I19:1033;1:1028','AirPlay':'I19:1033;1:1030','Pause':'I19:1022;43:758','Close':'I19:1022;43:757','BellSlash':'I19:1030;43:1094','Undo':'I19:1031;93:769','SkipBack15':'I19:1038;78:857','SkipForward15':'I19:1038;78:859','Airplane':'I19:1039;69:403','Chat':'27:1008'}
spec.update({'Bell':'I19:1007;67:1914','Check':'I19:1008;67:1917','Lock':'I19:1009;67:1920','Unlock':'I19:1010;67:1923','PersonalHotspot':'I19:1011;76:755','Moon':'I19:1012;78:805','Car':'I19:1013;78:816','ScreenMirroring':'I19:1040;93:785','Laptop':'I19:1040;93:797','Cellular':'I19:1041;107:862','TransitTrain':'I19:1042;107:912','Phone':'I19:1027;1:2456','NavigationLeft':'I19:1034;43:1042','NavigationRight':'I19:1034;43:1061','NavigationStraight':'I19:1034;43:1056','FaceId':'I19:1002;67:1905','Waveform':'I19:1033;1:1032'})
# Cached exports are immutable inputs by default; avoid API quota use for an offline rebuild.
import sys
existing = {m['glyph']:m for m in json.loads((p/'manifest.json').read_text())} if (p/'manifest.json').exists() else {}
missing = {name:id for name,id in spec.items() if '--refresh' in sys.argv or not (p/(name+'.svg')).exists()}
images = {}
if missing:
    key=subprocess.check_output(['security','find-generic-password','-a','aurora-figma','-s','codex.aurora.figma','-w'],text=True).strip()
    url='https://api.figma.com/v1/images/dmQdDCYXcy910vWZLfOqqz?'+urllib.parse.urlencode({'ids':','.join(missing.values()),'format':'svg','svg_outline_text':'true','svg_include_id':'true'})
    with urllib.request.urlopen(urllib.request.Request(url,headers={'X-Figma-Token':key}),timeout=60) as r:images=json.load(r)['images']
for name,id in missing.items():
    if not images.get(id):raise RuntimeError('Missing Figma glyph export: '+name)
    with urllib.request.urlopen(images[id],timeout=60) as r:(p/(name+'.svg')).write_bytes(r.read())
for name,id in spec.items():
    existing[name] = dict(existing.get(name,{}), glyph=name,node=id,file=name+'.svg')
(p/'manifest.json').write_text(json.dumps(list(existing.values()),indent=2))
print('Available',len(existing),'exact glyphs;',len(missing),'downloaded; local original-SVG extracts retained')
