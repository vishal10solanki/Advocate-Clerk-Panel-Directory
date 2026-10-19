import csv
import json
import base64
import os

# 1. Parse CSV
with open("app/src/main/assets/directory_seed.csv", "r", encoding="utf-8") as f:
    reader = csv.reader(f)
    header = next(reader)
    rows = list(reader)

clerk_map = {}
clerks_list = []
clerk_counter = 1

for r in rows:
    c_name = r[6].strip()
    c_mob1 = r[7].strip()
    c_mob2 = r[8].strip() if len(r) > 8 else ""
    if c_name:
        key = c_name.lower()
        if key not in clerk_map:
            clerk_obj = {
                "id": clerk_counter,
                "name": c_name,
                "primaryMobile": c_mob1,
                "secondaryMobile": c_mob2
            }
            clerk_map[key] = clerk_obj
            clerks_list.append(clerk_obj)
            clerk_counter += 1
        else:
            if not clerk_map[key]["primaryMobile"] and c_mob1:
                clerk_map[key]["primaryMobile"] = c_mob1
            if not clerk_map[key]["secondaryMobile"] and c_mob2:
                clerk_map[key]["secondaryMobile"] = c_mob2

advocates_list = []
for idx, r in enumerate(rows, 1):
    sr = int(r[0].strip()) if r[0].strip().isdigit() else idx
    adv_name = r[1].strip()
    adv_code = r[2].strip()
    panel = r[3].strip()
    adv_mob1 = r[4].strip()
    adv_mob2 = r[5].strip()
    c_name = r[6].strip()
    
    clerk_ids = []
    if c_name and c_name.lower() in clerk_map:
        clerk_ids.append(clerk_map[c_name.lower()]["id"])

    advocates_list.append({
        "id": idx,
        "srNo": sr,
        "name": adv_name,
        "code": adv_code,
        "panel": panel,
        "primaryMobile": adv_mob1,
        "secondaryMobile": adv_mob2,
        "clerkIds": clerk_ids
    })

seed_json = json.dumps({"advocates": advocates_list, "clerks": clerks_list}, ensure_ascii=False)

manifest_obj = {
    "name": "Advocate & Clerk Directory",
    "short_name": "Adv Directory",
    "description": "High Court Advocate and Clerk Directory with fast search, Call & WhatsApp actions.",
    "start_url": "./",
    "scope": "./",
    "display": "standalone",
    "orientation": "portrait",
    "background_color": "#0f172a",
    "theme_color": "#1e3a8a",
    "icons": [
        {
            "src": "data:image/svg+xml;base64," + base64.b64encode(b'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <rect width="512" height="512" rx="120" fill="#1e3a8a"/>
  <path d="M256 80 L390 180 L360 200 L270 135 L270 380 L350 380 L350 410 L162 410 L162 380 L242 380 L242 135 L152 200 L122 180 Z" fill="#ffffff"/>
  <circle cx="137" cy="270" r="45" fill="#f59e0b"/>
  <circle cx="375" cy="270" r="45" fill="#f59e0b"/>
</svg>''').decode('utf-8'),
            "sizes": "192x192 512x512",
            "type": "image/svg+xml",
            "purpose": "any maskable"
        }
    ]
}
manifest_b64 = base64.b64encode(json.dumps(manifest_obj).encode('utf-8')).decode('utf-8')

print("Data & manifest prepared.")
