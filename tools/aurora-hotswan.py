#!/usr/bin/env python3
"""Use the installed HotSwan IDE bridge for this checkout; never performs an APK build/install."""
import argparse,json,urllib.request
from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('action',choices=['status','logs','start','stop','reload','health','semantics','inspector','devices']);p.add_argument('files',nargs='*');a=p.parse_args()
port=json.loads((root/'.hotswan/bridge.json').read_text())['port']
route={'status':'status','logs':'logs?count=15','start':'start','stop':'stop','reload':'reload','health':'reload-health','semantics':'semantics','inspector':'inspector','devices':'connected-devices'}[a.action]
body=None
if a.action in ['start','stop']:body=b'{}'
if a.action=='reload':
 if not a.files:p.error('reload requires explicit changed source files')
 body=json.dumps({'files':[str((root/f).resolve()) for f in a.files]}).encode()
req=urllib.request.Request(f'http://127.0.0.1:{port}/api/{route}',data=body,headers={'Accept':'application/json','Content-Type':'application/json'})
with urllib.request.urlopen(req,timeout=120 if body else 15) as response:print(response.read().decode())
