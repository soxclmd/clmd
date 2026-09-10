# SOXCLMD Shared Content Layer

How the website and the Android app share ONE content source on GitHub.

## Principle

```
Edit JSON/PDF/images in the repo  →  commit  →  GitHub Actions validates
and refreshes manifest.json  →  website serves it  →  Android apps detect
the new manifest version  →  download only changed feeds  →  done.
```

## Files

| File | Written by | Read by |
|---|---|---|
| `assets/data/manifest.json` | `tools/build-content.py` (CI) | Android app only |
| `assets/data/news.json` | Content admin | Website + app |
| `assets/data/events.json` | Content admin | Website + app |
| `assets/data/advisories.json` | Content admin | Website + app |
| `assets/data/learning-areas.json` | Content admin | Website + app |
| `assets/data/downloads.json` | Content admin | Website + app |
| `assets/data/site.json` | Content admin | Website + app |
| `assets/data/search-index.json` | Content admin | Website + app |

## manifest.json shape

```json
{
  "content_version": "2026.09.10.0427",
  "last_updated": "2026-09-10",
  "minimum_app_version": "1.0.0",
  "maintenance_mode": false,
  "files": {
    "news":          { "path": "assets/data/news.json",          "sha256": "…", "bytes": 30730 },
    "events":        { "path": "assets/data/events.json",        "sha256": "…", "bytes": 17851 },
    "memoranda":     { "path": "assets/data/advisories.json",    "sha256": "…", "bytes": 18352 },
    "learningAreas": { "path": "assets/data/learning-areas.json","sha256": "…", "bytes": 36237 },
    "resources":     { "path": "assets/data/downloads.json",     "sha256": "…", "bytes": 4610 },
    "site":          { "path": "assets/data/site.json",          "sha256": "…", "bytes": 5066 },
    "searchIndex":   { "path": "assets/data/search-index.json",  "sha256": "…", "bytes": 29132 }
  }
}
```

- `content_version` — timestamp; if it differs from the app's cached value, sync runs.
- `files.*.sha256` — per-feed hash; unchanged feeds are **not** re-downloaded (mobile-data friendly).
- `minimum_app_version` — raise it (e.g. `1.1.0`) when a new app version is REQUIRED; users get a clear update notice.
- `maintenance_mode` — set `true` during migration; app shows a maintenance notice and keeps serving cached content.

The file is regenerated automatically on every push that touches `assets/data/**`
(workflow: `.github/workflows/content-manifest.yml`). You normally never edit it by hand.

## news.json entry (reference)

```json
{
  "id": "alep-2026-09-04",
  "title": "DepEd Region XII Strengthens Arabic Language Instruction…",
  "category": "Madrasah Education",
  "date": "2026-09-04",
  "tags": "ALEP asatidz ALIVE Arabic language",
  "summary": "One-paragraph summary shown on cards.",
  "body": ["Paragraph 1 …", "Paragraph 2 …"],
  "images": ["assets/images/news/alep-1.jpg"],
  "imageAlts": ["Caption"]
}
```

## events.json entry (reference)

```json
{
  "startDate": "2026-09-25",
  "endDate":   "2026-09-27",
  "time":      "optional time note",
  "title":     "Evaluation and Quality Assurance of Interactive Video Lessons",
  "venue":     "Venue to be announced",
  "audience":  "Identified participants",
  "memoNumbers": ["RM No. 045, s. 2026"]
}
```

`memoNumbers` must match a `number` in `advisories.json` for the link to resolve.

## Checklist for adding a new memorandum

1. Put the PDF in `assets/pdf/…` (or use a Google Drive link in `file`).
2. Append to `assets/data/advisories.json`:
   ```json
   { "date": "2026-09-10", "title": "…", "number": "RM No. 055, s. 2026",
     "year": 2026, "file": "assets/pdf/rm2026/RM-055-s2026.pdf",
     "documentType": "Regional Memorandum", "official": true,
     "learningArea": "MAPEH", "program": "…", "driveId": "" }
   ```
3. (Optional) Add a matching entry in `events.json` so it appears on the
   Activities board and Calendar.
4. Commit. CI validates + refreshes the manifest; apps update themselves.
