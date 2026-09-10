#!/usr/bin/env python3
"""
SOXCLMD content manifest generator + validator.

Regenerates assets/data/manifest.json with SHA-256 hashes of every content
file the SOXCLMD Android app consumes. Run automatically by GitHub Actions
on every push that changes content (see .github/workflows/content-manifest.yml),
or manually:  python3 tools/build-content.py

The Android app downloads this manifest first; if the content_version changes,
it re-downloads ONLY the files whose hash changed. This is what makes GitHub
the single source of truth for both the website and the app.
"""
import hashlib, json, os, sys, datetime

BASE = os.path.join(os.path.dirname(__file__), '..')
DATA = os.path.join(BASE, 'assets', 'data')

# every content feed the app consumes (key -> file)
FILES = {
    'news':          'news.json',
    'events':        'events.json',
    'memoranda':     'advisories.json',
    'learningAreas': 'learning-areas.json',
    'resources':     'downloads.json',
    'site':          'site.json',
    'searchIndex':   'search-index.json',
}

def sha256(path):
    h = hashlib.sha256()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(1 << 16), b''):
            h.update(chunk)
    return h.hexdigest()

def main():
    files, problems = {}, []
    for key, name in FILES.items():
        path = os.path.join(DATA, name)
        if not os.path.exists(path):
            problems.append(f'missing: assets/data/{name}')
            continue
        try:
            json.load(open(path, encoding='utf-8'))
        except Exception as e:
            problems.append(f'invalid JSON in assets/data/{name}: {e}')
            continue
        files[key] = dict(path=f'assets/data/{name}', sha256=sha256(path),
                          bytes=os.path.getsize(path))
    if problems:
        print('CONTENT VALIDATION FAILED:')
        for p in problems: print(' -', p)
        sys.exit(1)

    today = datetime.date.today().isoformat()
    stamp = datetime.datetime.now().strftime('%Y.%m.%d.%H%M')
    manifest = dict(
        content_version=stamp,
        last_updated=today,
        minimum_app_version='1.0.0',
        maintenance_mode=False,
        files=files)
    out = os.path.join(DATA, 'manifest.json')
    if os.path.exists(out):
        old = json.load(open(out, encoding='utf-8'))
        if old.get('files') == files:  # nothing changed -> do not rewrite
            print('manifest unchanged: ' + old.get('content_version', ''))
            for k, v in files.items():
                print(f"  {k:14s} {v['sha256'][:12]}… {v['bytes']:>9,} B  {v['path']}")
            return
        # keep human-set fields stable across regenerations
        manifest['minimum_app_version'] = old.get('minimum_app_version', '1.0.0')
        manifest['maintenance_mode'] = old.get('maintenance_mode', False)
    with open(out, 'w', encoding='utf-8') as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)
        f.write('\n')
    print(('manifest regenerated: ' if changed else 'manifest unchanged: ') + stamp)
    for k, v in files.items():
        print(f"  {k:14s} {v['sha256'][:12]}… {v['bytes']:>9,} B  {v['path']}")

if __name__ == '__main__':
    main()
