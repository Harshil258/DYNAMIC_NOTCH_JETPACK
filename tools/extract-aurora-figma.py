import json,subprocess,urllib.request,urllib.parse
from pathlib import Path
p=Path('reports/aurora-figma'); j=json.loads((p/'reference-node-5-2.json').read_text())['nodes']['5:2']['document']
nodes=[]
def walk(n):
 b=n.get('absoluteBoundingBox',{})
 if n.get('type')=='INSTANCE' and n.get('name') in ['Dynamic Island','Dynamic Island / Expanded','Dynamic Island / Compact','Dynamic Island / Minimal']:nodes.append(n)
 elif n.get('type')=='FRAME' and n['name'] in ['Figma Comment','Call Time','Music Player','Connected Device','Lyft'] and b.get('width',0)<400:nodes.append(n)
 for c in n.get('children',[]):walk(c)
walk(j)
key=subprocess.check_output(['security','find-generic-password','-a','aurora-figma','-s','codex.aurora.figma','-w'],text=True).strip()
def api(path):
 with urllib.request.urlopen(urllib.request.Request('https://api.figma.com/v1/'+path,headers={'X-Figma-Token':key}),timeout=60) as r:return json.load(r)
ids=[n['id'] for n in nodes]; manifest=[]
for start in range(0,len(ids),40):
 batch=ids[start:start+40]; res=api('images/dmQdDCYXcy910vWZLfOqqz?'+urllib.parse.urlencode({'ids':','.join(batch),'format':'png','scale':2}))
 for node in nodes[start:start+40]:
  url=res.get('images',{}).get(node['id'])
  if not url:continue
  filename='figma-'+node['id'].replace(':','-')+'.png'
  with urllib.request.urlopen(url,timeout=60) as r:(p/filename).write_bytes(r.read())
  manifest.append({'id':node['id'],'name':node['name'],'bounds':node['absoluteBoundingBox'],'file':filename})
(p/'manifest.json').write_text(json.dumps(manifest,indent=2));print('Exported',len(manifest),'island renders')
