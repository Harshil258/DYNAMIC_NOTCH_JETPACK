#!/usr/bin/env python3
"""Build review indexes from real device measurements; never invent missing evidence."""
from pathlib import Path
import csv
import re
root=Path(__file__).resolve().parents[1]
report=root/'reports'
folder=report/'aurora-v43'
rows=list(csv.DictReader((root/'AURORA_V4_3_CODEX_IMPLEMENTATION_PACKAGE/AURORA_60_STATE_GEOMETRY_TARGETS.csv').open()))
observations=list(csv.DictReader((folder/'measurements.csv').open())) if (folder/'measurements.csv').exists() else []
fresh_capture=(report/'aurora-figma/final-capture.json').exists()
geometry_source=(root/'app/src/main/java/ai/emots/kishan_dynamic/ui/components/IslandFamilyGeometry.kt').read_text()
families=dict(re.findall(r'IslandDemoState\.(\w+) to IslandGeometryTarget\(IslandGeometryFamily\.(\w+)',geometry_source))
measured={r['state']:r for r in observations if r['viewport']=='393' and r['font_scale']=='1.0'}
gallery='# V4.3 60-state visual gallery\n\nINTERIM EVIDENCE: these captures predate the final visual corrections. Final recapture is blocked by the disconnected phone. These are synthetic shared-renderer Compose fixtures on Realme Android13.\nThey do not prove60 platform integrations or production accessibility-overlay behavior.\nThe image names are unique per source enum. Radius is the renderer target, not a fitted pixel measurement.\n\n| State | Expected | Observed dp | Evidence | Review status |\n|---|---|---|---|---|\n'
with (report/'AURORA_V43_STATE_AUDIT.csv').open('w') as f:
 writer=csv.writer(f)
 writer.writerow(['state','expected_geometry','observed_width','observed_height','radius_target','width_error','height_error','family','renderer','fixture','actions','test','screenshot','status'])
 for r in rows:
  n=r['EnumState'];o=measured.get(n,{})
  image=folder/f'{n}-baseline.png'
  exists=image.exists();status=('CAPTURED — current shared renderer; synthetic fixture' if fresh_capture else 'INTERIM capture — predates final corrections; recapture pending') if exists else 'NOT RUN'
  shot=f'aurora-v43/{n}-baseline.png' if exists else ''
  writer.writerow([n,r['FamilyGeometryReference'],o.get('width',''),o.get('height',''),o.get('radius',''),float(o['width'])-float(o['target_width']) if o else '',float(o['height'])-float(o['target_height']) if o else '',families.get(n,''),'app/src/main/java/ai/emots/kishan_dynamic/ui/components/DynamicIslandPill.kt','synthetic demo','no external dispatch','AuroraV43GalleryTest.captureEveryStateAndResponsiveBounds',shot,status])
  obs=f"{float(o['width']):.2f} × {float(o['height']):.2f}" if o else 'NOT RUN'
  gallery+=f"| {n} | {r['FamilyGeometryReference']} | {obs} | {'[PNG]('+shot+')' if exists else 'pending'} | {status} |\n"
if fresh_capture:
 gallery=gallery.replace('INTERIM EVIDENCE: these captures predate the final visual corrections. Final recapture is blocked by the disconnected phone.', 'Fresh device capture:1200 configurations,60 states. Figma comparison corrections are recorded in../docs/AURORA_FIGMA_VISUAL_RULES.md.')
(report/'AURORA_V43_60_STATE_VISUAL_GALLERY.md').write_text(gallery)
(folder/'state-order.json').write_text(__import__('json').dumps([r['EnumState'] for r in rows]))
print(f'{len(rows)} states indexed; {len(measured)} measured baseline states')
try:
 from PIL import Image,ImageDraw
 images=[(r['EnumState'],folder/f"{r['EnumState']}-baseline.png") for r in rows]
 if all(p.exists() for _,p in images):
  atlas=Image.new('RGB',(1400,((len(images)+3)//4)*250),'#121826');draw=ImageDraw.Draw(atlas)
  for i,(name,p) in enumerate(images):
   im=Image.open(p).convert('RGBA');im.thumbnail((330,210))
   x=(i%4)*350;y=(i//4)*250
   draw.text((x+10,y+8),name,fill='white');atlas.paste(im,(x+(350-im.width)//2,y+30),im)
  atlas.save(folder/'all-60-atlas.jpg',quality=92)
except ImportError:print('Pillow unavailable; PNG index remains usable')

import html
(folder/'gallery.html').write_text('<!doctype html><meta charset="utf-8"><title>Aurora V4.3 gallery</title><style>body{background:#121826;color:white;font:16px system-ui;padding:24px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(370px,1fr));gap:20px}article{background:#080b12;padding:16px;border-radius:16px}img{max-width:100%;height:auto}small{color:#bfc8d8}</style><h1>Aurora V4.3 · 60 source states</h1><p>INTERIM captures before final corrections · recapture pending phone connection · synthetic fixtures · not production-overlay proof.</p><details><summary>Original expanded reference atlas</summary><img src="../../docs/reference_design/rendered/Expanded.svg.png"></details><details><summary>Baseline Playground</summary><img src="before-playground.png"></details><main>'+''.join('<article><h2>'+html.escape(r['EnumState'])+'</h2><small>'+html.escape(r['FamilyGeometryReference'])+'</small><p><img src="'+r['EnumState']+'-baseline.png" alt="'+r['EnumState']+' baseline"></p><details><summary>200% RTL</summary><img src="'+r['EnumState']+'-large-rtl.png"></details></article>' for r in rows)+'</main>')

if fresh_capture:
 p=folder/'gallery.html';p.write_text(p.read_text().replace('INTERIM captures before final corrections · recapture pending phone connection', 'Fresh1200-configuration device capture · Figma comparison corrections'))

# Related unit evidence is kept separate from full scenario acceptance.
import xml.etree.ElementTree as ET
cases={}
for result in (root/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'):
    suite=ET.parse(result).getroot()
    for case in suite.findall('testcase'):
        cases[case.attrib['classname']+'.'+case.attrib['name']]=case.find('failure') is None and case.find('error') is None and case.find('skipped') is None
mapping={1:'test_A01',2:'test_A02',3:'test_A03',4:'test_A08',5:'test_A13',6:'test_A17',7:'test_A16',8:'test_A21',9:'test_A22',10:'test_A06',12:'test_A40',13:'oldQueueIsNotPendingOverflow',14:'promotionSelectsExactlyOneExistingPagerIdentity',15:'expiringSiblingDoesNotUnpauseHeldChildOrCancelAndroid',16:'replyAndCollapsePreserveQueueAndNeverEmitAndroidCancellation',18:'callRestoresSelectedReaderWithoutExtendingChildDeadline',23:'stalePromotionAndReusedKeyCannotSelectRemovedSource',24:'privacyProjectionNeverExposesSensitiveText',29:'zeroHeightOrConstrainedScreenCollapsesChildPresentationWithoutDanglingOverflow',37:'test_A11',38:'burstBoundedAtCanonicalCapacityWithNoDuplicateKeys',39:'layoutObservationCannotReplaceAnotherSelectedFixture',41:'test_A05',49:'test_C05',53:'replyAndCollapsePreserveQueueAndNeverEmitAndroidCancellation',54:'sourceAndActionIdentityRetainDraftOnlyInsideMonotonicWindow',58:'test_A23',59:'slowRenderReceivesFullLifetimeAndAcknowledgementIsIdempotent',60:'deliberateReaderHasNoShortExpiryAfterNinetySeconds',62:'unlockedOtpAndFinanceRespectSensitivePreviewPreference',63:'test_A19',64:'zeroHeightOrConstrainedScreenCollapsesChildPresentationWithoutDanglingOverflow'}
evidence='\n\n## Related JVM evidence by scenario\n\nPassing a related regression below does not establish the full device/visual scenario.\n\n| Scenario | Related executed regression | Result |\n|---|---|---|\n'
for n in range(1,65):
    matches=[(name,passed) for name,passed in cases.items() if mapping.get(n) and mapping[n] in name]
    evidence+=f'| {n} | '+ ('; '.join(name for name,_ in matches) if matches else 'No direct mapped JVM test')+' | '+('PASS (JVM scope only)' if matches and all(p for _,p in matches) else 'NOT RUN / requires separate evidence')+' |\n'
behavior=report/'AURORA_V43_UX_BEHAVIOR_AND_PERSONAS.md'
if behavior.exists():
    original=behavior.read_text().split('\n\n## Related JVM evidence by scenario')[0]
    behavior.write_text(original+evidence)

# Full scenarios remain separate from related passing unit assertions.
if behavior.exists():
    content=behavior.read_text()
    for n in range(1,65):
        related=any(mapping.get(n) and mapping[n] in name and passed for name,passed in cases.items())
        status=('BLOCKED — production service/device access unavailable' if n in (25,26,30,40) else
                'PARTIAL — related JVM regression passes; full scenario remains unverified' if related else
                'NOT RUN — full scenario requires separate execution')
        content=re.sub(r'(?m)^(\| '+str(n)+r' \|[^\n]*\| )(?:NOT RUN — see test log mapping; no inferred PASS|PARTIAL — related JVM regression passes; full scenario remains unverified|BLOCKED — production service/device access unavailable|NOT RUN — full scenario requires separate execution)( \|)$',lambda m:m.group(1)+status+m.group(2),content)
    behavior.write_text(content)
