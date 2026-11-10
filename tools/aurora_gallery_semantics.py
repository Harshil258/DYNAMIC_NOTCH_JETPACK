"""Select the gallery's Compose root, excluding retained activity compositions."""
import re

def active_gallery(tree):
    nodes = tree.get('nodes', [])
    by_id = {n['id']: n for n in nodes}
    headers = [n for n in nodes if re.fullmatch(r'\d+ / 60 · \w+', n.get('text', ''))]
    if not headers:
        raise RuntimeError('No active gallery header')
    header = max(headers, key=lambda n: n['id'])
    def root_id(node):
        seen = set()
        while node.get('parentId') in by_id and node['id'] not in seen:
            seen.add(node['id'])
            node = by_id[node['parentId']]
        return node['id']
    root = root_id(header)
    selected = [n for n in nodes if root_id(n) == root]
    return dict(tree, nodes=selected, nodeCount=len(selected), galleryRootId=root)
