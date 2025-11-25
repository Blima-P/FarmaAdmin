import re
import glob

java_files = glob.glob('**/*.java', recursive=True)
print(f'Found {len(java_files)} java files')

for f in java_files:
    try:
        with open(f, 'r', encoding='utf-8') as fh:
            content = fh.read()
    except Exception as e:
        print('skip', f, e)
        continue
    lines = content.splitlines()
    import_lines = []
    for i, line in enumerate(lines):
        if line.strip().startswith('import ') and 'static' not in line:
            import_lines.append((i, line))
    if not import_lines:
        continue
    to_remove = set()
    seen = set()
    body = '\n'.join([l for idx,l in enumerate(lines) if idx not in [it[0] for it in import_lines]])
    for idx, il in import_lines:
        txt = il.strip()
        if txt in seen:
            to_remove.add(idx)
            continue
        seen.add(txt)
        m = re.match(r'import\s+([\w\.\*]+);', txt)
        if not m:
            continue
        qual = m.group(1)
        if qual.endswith('.*'):
            continue
        simple = qual.split('.')[-1]
        # check usage in body
        if re.search(r'\b' + re.escape(simple) + r'\b', body) is None:
            to_remove.add(idx)
    if not to_remove:
        continue
    new_lines = [l for i,l in enumerate(lines) if i not in to_remove]
    with open(f, 'w', encoding='utf-8') as fh:
        fh.write('\n'.join(new_lines))
    print('Patched', f, 'removed', len(to_remove))
print('done')
