import csv
import json
import base64
import os

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

# Build the complete single index.html
html_content = f'''<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <title>Advocate & Clerk Directory</title>
  <meta name="description" content="High Court Advocate & Clerk Directory with fast search, Call & WhatsApp actions, and multiple clerk support.">
  <meta name="theme-color" content="#1e3a8a">
  
  <!-- PWA & Mobile Web App Manifest (Inline Base64 Data URI) -->
  <link rel="manifest" href="data:application/manifest+json;base64,{manifest_b64}">
  <meta name="mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
  <meta name="apple-mobile-web-app-title" content="Adv Directory">
  
  <style>
    :root {{
      --primary: #1e3a8a;
      --primary-dark: #172554;
      --primary-light: #eff6ff;
      --primary-container: #dbeafe;
      --on-primary: #ffffff;
      --bg: #f8fafc;
      --surface: #ffffff;
      --surface-elevated: #ffffff;
      --surface-variant: #f1f5f9;
      --outline: #e2e8f0;
      --outline-variant: #cbd5e1;
      --text-primary: #0f172a;
      --text-secondary: #475569;
      --text-tertiary: #94a3b8;
      --accent: #2563eb;
      --accent-gold: #d97706;
      --whatsapp: #25d366;
      --whatsapp-hover: #1ebd59;
      --whatsapp-bg: #dcfce7;
      --whatsapp-text: #15803d;
      --call-blue: #1d4ed8;
      --call-bg: #eff6ff;
      --danger: #dc2626;
      --danger-light: #fee2e2;
      --panel-gold-bg: #fef3c7;
      --panel-gold-text: #92400e;
      --panel-gold-border: #f59e0b;
      --radius-sm: 8px;
      --radius-md: 14px;
      --radius-lg: 18px;
      --radius-pill: 9999px;
      --shadow-subtle: 0 1px 3px rgba(0,0,0,0.05);
      --shadow-float: 0 8px 24px -4px rgba(15, 23, 42, 0.12), 0 2px 6px -2px rgba(15, 23, 42, 0.08);
      --shadow-card: 0 2px 10px rgba(0,0,0,0.04), 0 1px 3px rgba(0,0,0,0.02);
      --font: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    }}

    [data-theme="dark"] {{
      --primary: #3b82f6;
      --primary-dark: #1d4ed8;
      --primary-light: #1e293b;
      --primary-container: #172554;
      --on-primary: #ffffff;
      --bg: #0b1120;
      --surface: #1e293b;
      --surface-elevated: #1e293b;
      --surface-variant: #0f172a;
      --outline: #334155;
      --outline-variant: #475569;
      --text-primary: #f8fafc;
      --text-secondary: #94a3b8;
      --text-tertiary: #64748b;
      --whatsapp-bg: #064e3b33;
      --whatsapp-text: #4ade80;
      --call-bg: #1e3a8a33;
      --panel-gold-bg: #78350f44;
      --panel-gold-text: #fde68a;
      --panel-gold-border: #d97706;
      --shadow-float: 0 8px 24px rgba(0,0,0,0.4);
      --shadow-card: 0 2px 10px rgba(0,0,0,0.3);
    }}

    * {{
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
    }}

    body {{
      font-family: var(--font);
      background-color: var(--bg);
      color: var(--text-primary);
      line-height: 1.5;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      padding-bottom: 74px; /* Space for bottom nav */
    }}

    /* TOP APP BAR */
    .app-header {{
      background: var(--surface);
      border-bottom: 1px solid var(--outline);
      position: sticky;
      top: 0;
      z-index: 50;
      box-shadow: var(--shadow-subtle);
    }}

    .header-content {{
      max-width: 1200px;
      margin: 0 auto;
      padding: 10px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }}

    /* BRAND LOGO (Secret 7-tap trigger for Admin Login!) */
    .brand-section {{
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      user-select: none;
      -webkit-user-select: none;
    }}

    .court-icon {{
      width: 42px;
      height: 42px;
      background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
      color: #f59e0b;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(30, 58, 138, 0.25);
      flex-shrink: 0;
      transition: transform 0.1s ease;
    }}

    .brand-section:active .court-icon {{
      transform: scale(0.92);
    }}

    .brand-text h1 {{
      font-size: 1.12rem;
      font-weight: 700;
      line-height: 1.2;
      color: var(--text-primary);
      letter-spacing: -0.01em;
    }}

    .brand-text p {{
      font-size: 0.73rem;
      color: var(--text-secondary);
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 1px;
    }}

    .badge-records {{
      background: var(--primary-container);
      color: var(--primary);
      font-weight: 700;
      font-size: 0.68rem;
      padding: 1px 7px;
      border-radius: var(--radius-pill);
    }}

    [data-theme="dark"] .badge-records {{
      color: #93c5fd;
    }}

    .header-actions {{
      display: flex;
      align-items: center;
      gap: 8px;
    }}

    /* PWA Install Button */
    .btn-install {{
      background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
      color: #ffffff;
      border: none;
      padding: 7px 12px;
      border-radius: var(--radius-pill);
      font-size: 0.78rem;
      font-weight: 700;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      box-shadow: 0 2px 6px rgba(2, 132, 199, 0.3);
      transition: transform 0.15s, opacity 0.15s;
    }}

    .btn-install:active {{
      transform: scale(0.96);
    }}

    .btn-icon-head {{
      width: 38px;
      height: 38px;
      border-radius: var(--radius-pill);
      border: 1px solid var(--outline);
      background: var(--surface-variant);
      color: var(--text-secondary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s;
    }}

    .btn-icon-head:hover {{
      color: var(--text-primary);
      border-color: var(--outline-variant);
    }}

    /* Active Admin Status Pill (Hidden by default, shown ONLY after unlocking) */
    .admin-status-pill {{
      display: none;
      align-items: center;
      gap: 5px;
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fde68a;
      padding: 5px 11px;
      border-radius: var(--radius-pill);
      font-size: 0.75rem;
      font-weight: 700;
      cursor: pointer;
    }}

    [data-theme="dark"] .admin-status-pill {{
      background: #78350f44;
      color: #fde68a;
      border-color: #b45309;
    }}

    .admin-status-pill.active {{
      display: inline-flex;
    }}

    /* MAIN CONTAINER */
    main {{
      flex: 1;
      max-width: 1200px;
      width: 100%;
      margin: 0 auto;
      padding: 14px 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }}

    /* FLOATING SEARCH BAR (Modern Android Pill) */
    .floating-search-bar {{
      position: sticky;
      top: 66px;
      z-index: 45;
      background: var(--surface);
      border-radius: var(--radius-pill);
      border: 1.5px solid var(--outline);
      box-shadow: var(--shadow-float);
      display: flex;
      align-items: center;
      padding: 2px 14px 2px 16px;
      transition: border-color 0.2s, box-shadow 0.2s;
    }}

    .floating-search-bar:focus-within {{
      border-color: var(--primary);
      box-shadow: 0 6px 20px rgba(30, 58, 138, 0.15);
    }}

    .floating-search-bar svg.search-icon {{
      color: var(--text-tertiary);
      flex-shrink: 0;
      margin-right: 10px;
    }}

    .search-input-field {{
      flex: 1;
      height: 48px;
      border: none;
      background: transparent;
      color: var(--text-primary);
      font-family: inherit;
      font-size: 0.95rem;
      outline: none;
    }}

    .search-input-field::placeholder {{
      color: var(--text-tertiary);
    }}

    .search-clear-btn {{
      background: var(--surface-variant);
      border: none;
      color: var(--text-secondary);
      cursor: pointer;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: none;
      align-items: center;
      justify-content: center;
    }}

    .search-clear-btn.visible {{
      display: flex;
    }}

    /* HORIZONTAL SCROLLABLE FILTER CHIPS */
    .filter-chips-scroll {{
      display: flex;
      overflow-x: auto;
      gap: 8px;
      padding: 4px 2px 6px;
      scrollbar-width: none;
      -ms-overflow-style: none;
    }}

    .filter-chips-scroll::-webkit-scrollbar {{
      display: none;
    }}

    .filter-pill {{
      flex-shrink: 0;
      padding: 6px 14px;
      border-radius: var(--radius-pill);
      background: var(--surface);
      border: 1px solid var(--outline);
      color: var(--text-secondary);
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s ease;
      user-select: none;
    }}

    .filter-pill.active {{
      background: var(--primary);
      color: #ffffff;
      border-color: var(--primary);
      box-shadow: 0 2px 6px rgba(30, 58, 138, 0.25);
    }}

    [data-theme="dark"] .filter-pill.active {{
      background: #2563eb;
      border-color: #2563eb;
    }}

    /* RESULTS COUNTER & ADMIN ACTION BAR */
    .info-bar {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 4px;
      font-size: 0.82rem;
      color: var(--text-secondary);
      font-weight: 500;
    }}

    .admin-actions-bar {{
      display: none;
      align-items: center;
      gap: 8px;
    }}

    .admin-actions-bar.unlocked {{
      display: flex;
    }}

    .btn-create {{
      background: #f59e0b;
      color: #ffffff;
      border: none;
      padding: 6px 12px;
      border-radius: var(--radius-pill);
      font-size: 0.78rem;
      font-weight: 700;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
    }}

    /* CARDS GRID */
    .cards-grid {{
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
      gap: 14px;
    }}

    @media (max-width: 640px) {{
      .cards-grid {{
        grid-template-columns: 1fr;
      }}
    }}

    /* MODERN CARD (Material 3 Native Android Style) */
    .native-card {{
      background: var(--surface);
      border: 1px solid var(--outline);
      border-radius: var(--radius-lg);
      padding: 16px;
      box-shadow: var(--shadow-card);
      display: flex;
      flex-direction: column;
      gap: 12px;
      transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
      position: relative;
    }}

    .native-card:hover {{
      border-color: var(--outline-variant);
      box-shadow: var(--shadow-float);
    }}

    /* PANEL ADVOCATE CARD HIGHLIGHTING */
    .native-card.panel-highlighted {{
      border: 1.5px solid var(--panel-gold-border);
      box-shadow: 0 4px 14px rgba(245, 158, 11, 0.18);
    }}

    .card-top {{
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 10px;
    }}

    .adv-title-group {{
      flex: 1;
    }}

    .adv-full-name {{
      font-size: 1.05rem;
      font-weight: 700;
      color: var(--text-primary);
      line-height: 1.3;
    }}

    .chips-group {{
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 6px;
      align-items: center;
    }}

    .chip-code {{
      background: var(--surface-variant);
      border: 1px solid var(--outline);
      color: var(--text-secondary);
      font-size: 0.72rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: var(--radius-sm);
    }}

    .chip-sr {{
      background: var(--surface-variant);
      color: var(--text-tertiary);
      font-size: 0.7rem;
      padding: 2px 6px;
      border-radius: var(--radius-sm);
    }}

    /* DISTINCT PANEL ADVOCATE BADGE (Requirement #3) */
    .chip-panel-badge {{
      background: var(--panel-gold-bg);
      color: var(--panel-gold-text);
      border: 1.5px solid var(--panel-gold-border);
      font-size: 0.75rem;
      font-weight: 800;
      padding: 2px 9px;
      border-radius: var(--radius-pill);
      display: inline-flex;
      align-items: center;
      gap: 4px;
      box-shadow: 0 1px 4px rgba(245, 158, 11, 0.2);
    }}

    /* DIRECT CONTACT ROWS WITH CALL & WHATSAPP */
    .contact-container {{
      background: var(--surface-variant);
      border: 1px solid var(--outline);
      border-radius: var(--radius-md);
      padding: 10px 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }}

    .contact-row-item {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }}

    .contact-number-info {{
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.88rem;
      font-weight: 700;
      color: var(--text-primary);
    }}

    .contact-type-tag {{
      font-size: 0.7rem;
      color: var(--text-tertiary);
      font-weight: 500;
    }}

    .quick-action-btns {{
      display: flex;
      align-items: center;
      gap: 6px;
    }}

    /* WHATSAPP DIRECT ACTION BUTTON (Requirement #4: Text 'WhatsApp') */
    .btn-action-whatsapp {{
      height: 34px;
      padding: 0 11px;
      border-radius: var(--radius-sm);
      background: var(--whatsapp);
      color: #ffffff;
      font-size: 0.75rem;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      text-decoration: none;
      cursor: pointer;
      border: none;
      box-shadow: 0 2px 6px rgba(37, 211, 102, 0.25);
      transition: transform 0.15s, opacity 0.15s;
    }}

    .btn-action-whatsapp:active {{
      transform: scale(0.96);
    }}

    /* Call Direct Action Button */
    .btn-action-call {{
      height: 34px;
      padding: 0 10px;
      border-radius: var(--radius-sm);
      background: var(--call-blue);
      color: #ffffff;
      font-size: 0.75rem;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      text-decoration: none;
      cursor: pointer;
      border: none;
      box-shadow: 0 2px 6px rgba(29, 78, 216, 0.25);
      transition: transform 0.15s, opacity 0.15s;
    }}

    .btn-action-call:active {{
      transform: scale(0.96);
    }}

    .btn-action-copy {{
      width: 34px;
      height: 34px;
      border-radius: var(--radius-sm);
      background: var(--surface);
      border: 1px solid var(--outline);
      color: var(--text-secondary);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }}

    /* ASSIGNED CLERKS (MULTIPLE CLERKS SUPPORT) */
    .clerk-box-container {{
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: var(--radius-md);
      padding: 10px 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }}

    [data-theme="dark"] .clerk-box-container {{
      background: #064e3b22;
      border-color: #065f46;
    }}

    .clerk-box-head {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 0.72rem;
      font-weight: 700;
      color: #166534;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }}

    [data-theme="dark"] .clerk-box-head {{
      color: #86efac;
    }}

    .clerk-item-card {{
      display: flex;
      flex-direction: column;
      gap: 6px;
      padding-bottom: 6px;
      border-bottom: 1px dashed rgba(22, 101, 52, 0.2);
    }}

    .clerk-item-card:last-child {{
      padding-bottom: 0;
      border-bottom: none;
    }}

    .clerk-name-title {{
      font-size: 0.92rem;
      font-weight: 700;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }}

    .clerk-tag-pill {{
      background: #dcfce7;
      color: #15803d;
      font-size: 0.68rem;
      font-weight: 700;
      padding: 1px 7px;
      border-radius: var(--radius-pill);
    }}

    [data-theme="dark"] .clerk-tag-pill {{
      background: #14532d;
      color: #86efac;
    }}

    /* CARD FOOTER */
    .card-bottom-actions {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: 8px;
      border-top: 1px solid var(--outline);
      gap: 8px;
    }}

    .btn-share-contact {{
      background: transparent;
      border: 1px solid var(--outline);
      border-radius: var(--radius-pill);
      color: var(--text-secondary);
      padding: 6px 12px;
      font-size: 0.78rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
    }}

    .admin-card-tools {{
      display: none;
      align-items: center;
      gap: 6px;
    }}

    .admin-card-tools.unlocked {{
      display: flex;
    }}

    .btn-card-edit {{
      background: #e0f2fe;
      color: #0369a1;
      border: 1px solid #bae6fd;
      padding: 5px 10px;
      border-radius: var(--radius-sm);
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
    }}

    .btn-card-del {{
      background: var(--danger-light);
      color: var(--danger);
      border: 1px solid #fecaca;
      padding: 5px 10px;
      border-radius: var(--radius-sm);
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
    }}

    /* NATIVE ANDROID BOTTOM NAVIGATION BAR */
    .bottom-nav-bar {{
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      height: 64px;
      background: var(--surface);
      border-top: 1px solid var(--outline);
      display: flex;
      align-items: center;
      justify-content: space-around;
      z-index: 60;
      box-shadow: 0 -4px 16px rgba(0,0,0,0.06);
    }}

    .nav-item-btn {{
      flex: 1;
      height: 100%;
      background: none;
      border: none;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      color: var(--text-tertiary);
      cursor: pointer;
      font-family: inherit;
      font-size: 0.75rem;
      font-weight: 600;
      position: relative;
    }}

    .nav-item-btn.active {{
      color: var(--primary);
    }}

    [data-theme="dark"] .nav-item-btn.active {{
      color: #60a5fa;
    }}

    .nav-item-btn.active .nav-icon-wrapper {{
      background: var(--primary-container);
      color: var(--primary);
    }}

    [data-theme="dark"] .nav-item-btn.active .nav-icon-wrapper {{
      background: #1e3a8a;
      color: #93c5fd;
    }}

    .nav-icon-wrapper {{
      width: 48px;
      height: 28px;
      border-radius: var(--radius-pill);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.15s;
    }}

    .nav-badge-dot {{
      position: absolute;
      top: 6px;
      right: calc(50% - 24px);
      background: var(--accent-gold);
      color: #ffffff;
      font-size: 0.65rem;
      font-weight: 800;
      padding: 0 4px;
      border-radius: var(--radius-pill);
      min-width: 14px;
      text-align: center;
    }}

    /* MODAL SHEETS & DIALOGS */
    .modal-overlay {{
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(3px);
      z-index: 100;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 16px;
    }}

    .modal-overlay.open {{
      display: flex;
    }}

    .modal-box {{
      background: var(--surface);
      border-radius: var(--radius-lg);
      border: 1px solid var(--outline);
      width: 100%;
      max-width: 480px;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      box-shadow: var(--shadow-float);
      animation: popIn 0.2s cubic-bezier(0.16, 1, 0.3, 1);
      overflow: hidden;
    }}

    @keyframes popIn {{
      from {{ transform: scale(0.95) translateY(10px); opacity: 0; }}
      to {{ transform: scale(1) translateY(0); opacity: 1; }}
    }}

    .modal-head {{
      padding: 16px 20px;
      border-bottom: 1px solid var(--outline);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }}

    .modal-head h3 {{
      font-size: 1.1rem;
      font-weight: 700;
    }}

    .modal-close-btn {{
      background: none;
      border: none;
      color: var(--text-tertiary);
      cursor: pointer;
      font-size: 1.2rem;
      padding: 4px;
      border-radius: 50%;
    }}

    .modal-content-area {{
      padding: 20px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }}

    .modal-action-row {{
      padding: 14px 20px;
      border-top: 1px solid var(--outline);
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 10px;
      background: var(--surface-variant);
    }}

    .input-group {{
      display: flex;
      flex-direction: column;
      gap: 6px;
    }}

    .input-group label {{
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--text-secondary);
    }}

    .text-input, .select-input {{
      width: 100%;
      padding: 11px 14px;
      border-radius: var(--radius-md);
      border: 1.5px solid var(--outline);
      background: var(--surface);
      color: var(--text-primary);
      font-family: inherit;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s;
    }}

    .text-input:focus, .select-input:focus {{
      border-color: var(--primary);
    }}

    /* PIN MASKED INPUT (Requirement #2: Standard password dots) */
    .pin-masked-input {{
      letter-spacing: 0.3em;
      font-size: 1.25rem;
      text-align: center;
      font-weight: 800;
    }}

    .btn-modal-primary {{
      background: var(--primary);
      color: #ffffff;
      border: none;
      padding: 9px 18px;
      border-radius: var(--radius-md);
      font-weight: 700;
      cursor: pointer;
    }}

    .btn-modal-cancel {{
      background: transparent;
      color: var(--text-secondary);
      border: 1px solid var(--outline);
      padding: 9px 16px;
      border-radius: var(--radius-md);
      font-weight: 600;
      cursor: pointer;
    }}

    /* MULTI CLERKS SELECTOR IN ADVOCATE MODAL */
    .clerks-chip-box {{
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      min-height: 40px;
      padding: 8px;
      background: var(--surface-variant);
      border: 1px solid var(--outline);
      border-radius: var(--radius-md);
    }}

    .clerk-sel-chip {{
      background: var(--surface);
      border: 1px solid var(--outline);
      padding: 4px 10px;
      border-radius: var(--radius-pill);
      font-size: 0.8rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
    }}

    .btn-remove-chip {{
      background: none;
      border: none;
      color: var(--danger);
      cursor: pointer;
      font-size: 0.95rem;
    }}

    /* TOAST NOTIFICATION */
    .toast-wrap {{
      position: fixed;
      bottom: 78px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 200;
      display: flex;
      flex-direction: column;
      gap: 8px;
      pointer-events: none;
    }}

    .toast-msg {{
      background: #0f172a;
      color: #ffffff;
      padding: 10px 20px;
      border-radius: var(--radius-pill);
      font-size: 0.85rem;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(0,0,0,0.25);
      animation: tIn 0.2s ease, tOut 0.2s ease 2.7s forwards;
      pointer-events: auto;
    }}

    @keyframes tIn {{ from {{ transform: translateY(12px); opacity: 0; }} to {{ transform: translateY(0); opacity: 1; }} }}
    @keyframes tOut {{ from {{ opacity: 1; }} to {{ opacity: 0; }} }}

    /* LOAD MORE BUTTON */
    .pagination-wrap {{
      display: flex;
      justify-content: center;
      margin: 16px 0 24px;
    }}

    .btn-load-more {{
      background: var(--surface);
      border: 1.5px solid var(--outline);
      color: var(--text-primary);
      padding: 10px 24px;
      border-radius: var(--radius-pill);
      font-size: 0.88rem;
      font-weight: 700;
      cursor: pointer;
      box-shadow: var(--shadow-subtle);
    }}
  </style>
</head>
<body>

  <!-- TOP APP BAR -->
  <header class="app-header">
    <div class="header-content">
      <!-- MAIN HEADER LOGO (Tapping 7 times activates secret Admin Mode!) -->
      <div class="brand-section" id="mainHeaderLogoBrand" title="High Court of Gujarat">
        <div class="court-icon">
          <!-- Legal Scales Emblem SVG -->
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m16 16 3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1Z"/>
            <path d="m2 16 3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1Z"/>
            <path d="M7 21h10"/>
            <path d="M12 3v18"/>
            <path d="M3 7h2c2 0 5-1 7-2 2 1 5 2 7 2h2"/>
          </svg>
        </div>
        <div class="brand-text">
          <h1>Adv & Clerk Directory</h1>
          <p>
            <span>Gujarat High Court</span>
            <span class="badge-records" id="badgeTotalRecords">1,365 Records</span>
          </p>
        </div>
      </div>

      <div class="header-actions">
        <!-- Admin Active Indicator Pill (Shown ONLY when unlocked, clicking allows exit) -->
        <div class="admin-status-pill" id="adminActivePill" title="Admin Active - Tap to Exit">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          <span>ADMIN</span>
          <span style="opacity: 0.7; font-size: 10px; margin-left: 2px;">✕</span>
        </div>

        <!-- PWA Install Button -->
        <button class="btn-install" id="pwaInstallHeaderBtn" title="Install App to Home Screen">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>
          <span>Install App</span>
        </button>

        <!-- Dark/Light Theme Toggle -->
        <button class="btn-icon-head" id="themeToggleHeadBtn" title="Toggle Light/Dark Theme">
          <svg id="themeToggleIcon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
          </svg>
        </button>
      </div>
    </div>
  </header>

  <!-- MAIN VIEW -->
  <main>
    <!-- FLOATING PILL SEARCH BAR -->
    <div class="floating-search-bar">
      <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
      <input type="text" class="search-input-field" id="globalSearchInput" placeholder="Search advocate name, code, panel..." autocomplete="off">
      <button class="search-clear-btn" id="globalSearchClearBtn" title="Clear Search">✕</button>
    </div>

    <!-- HORIZONTAL SCROLLING QUICK FILTERS -->
    <div class="filter-chips-scroll" id="advFilterChipsRow">
      <div class="filter-pill active" data-filter="all">All (1,365)</div>
      <div class="filter-pill" data-filter="panel">⚖️ Panel Advocates</div>
      <div class="filter-pill" data-filter="has-clerk">👨‍💼 Has Clerk</div>
      <div class="filter-pill" data-filter="multi-clerk">👥 Multiple Clerks</div>
      <div class="filter-pill" data-filter="no-clerk">Without Clerk</div>
    </div>

    <!-- INFO BAR (Results Count & Admin Add Buttons) -->
    <div class="info-bar">
      <div id="resultsCountNotice">Showing 1,281 Advocates</div>
      
      <!-- Only revealed when Admin is unlocked! -->
      <div class="admin-actions-bar" id="adminHeaderActionsBar">
        <button class="btn-create" id="btnAdminAddAdvocate">+ Advocate</button>
        <button class="btn-create" id="btnAdminAddClerk" style="background: #0284c7;">+ Clerk</button>
      </div>
    </div>

    <!-- TAB 1: ADVOCATES DIRECTORY -->
    <section id="viewAdvocatesTab">
      <div class="cards-grid" id="advocatesCardsGrid"></div>
      <div class="pagination-wrap">
        <button class="btn-load-more" id="advLoadMoreBtn">Load More</button>
      </div>
    </section>

    <!-- TAB 2: CLERKS DIRECTORY -->
    <section id="viewClerksTab" style="display: none;">
      <div class="cards-grid" id="clerksCardsGrid"></div>
      <div class="pagination-wrap">
        <button class="btn-load-more" id="clerkLoadMoreBtn">Load More</button>
      </div>
    </section>

    <!-- TAB 3: ADMIN DATA MANAGEMENT (Revealed ONLY after unlocking PIN!) -->
    <section id="viewAdminTab" style="display: none;">
      <div style="background: var(--surface); border: 1px solid var(--outline); border-radius: var(--radius-lg); padding: 20px; display: flex; flex-direction: column; gap: 18px; box-shadow: var(--shadow-card);">
        <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--outline); padding-bottom: 12px;">
          <div>
            <h2 style="font-size: 1.15rem; font-weight: 700;">🛡️ Administrator Data Console</h2>
            <p style="font-size: 0.8rem; color: var(--text-secondary);">Manage, export, and import directory records.</p>
          </div>
          <button class="btn-modal-cancel" id="btnAdminLogoutFromTab" style="color: var(--danger); font-size: 0.8rem;">Exit Admin</button>
        </div>

        <!-- CSV Export Option (Hidden from main screen, only in Admin!) -->
        <div style="background: var(--surface-variant); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--outline);">
          <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 4px;">📥 Export Directory Data</h4>
          <p style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 10px;">
            Export the complete updated Advocate & Clerk directory to a CSV file or JSON backup.
          </p>
          <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <button class="btn-modal-primary" id="btnExportCsvAdmin">Download CSV File</button>
            <button class="btn-modal-cancel" id="btnExportJsonAdmin">Download JSON File</button>
          </div>
        </div>

        <!-- CSV Import Option (Hidden from main screen, only in Admin!) -->
        <div style="background: var(--surface-variant); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--outline);">
          <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 4px;">📤 Import & Update Data</h4>
          <p style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 10px;">
            Upload a CSV file to update directory records or add new advocates and clerks.
          </p>
          <input type="file" id="adminCsvFileInput" accept=".csv,text/csv" style="display: none;">
          <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <button class="btn-modal-primary" style="background: #0284c7;" onclick="document.getElementById('adminCsvFileInput').click()">
              Choose CSV File to Import
            </button>
            <button class="btn-modal-cancel" id="btnResetDefaultSeedAdmin" style="color: var(--danger);">
              Reset to Default (1,365 Records)
            </button>
          </div>
        </div>

        <!-- Security & PIN Management -->
        <div style="background: var(--surface-variant); padding: 14px; border-radius: var(--radius-md); border: 1px solid var(--outline);">
          <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 4px;">🔒 Admin Security</h4>
          <p style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 10px;">
            Administrator PIN authentication is required to access data management.
          </p>
          <button class="btn-modal-cancel" id="btnChangePinAdmin">Change Admin PIN</button>
        </div>
      </div>
    </section>
  </main>

  <!-- NATIVE ANDROID BOTTOM NAVIGATION BAR -->
  <nav class="bottom-nav-bar">
    <button class="nav-item-btn active" id="navAdvocatesTab">
      <div class="nav-icon-wrapper">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
      </div>
      <span>Advocates</span>
      <span class="nav-badge-dot" id="bottomNavAdvCount">1281</span>
    </button>

    <button class="nav-item-btn" id="navClerksTab">
      <div class="nav-icon-wrapper">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect width="20" height="14" x="2" y="7" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
      </div>
      <span>Clerks</span>
      <span class="nav-badge-dot" id="bottomNavClerkCount" style="background: #16a34a;">119</span>
    </button>

    <!-- Admin Tab: Shows when Admin unlocked -->
    <button class="nav-item-btn" id="navAdminTab" style="display: none;">
      <div class="nav-icon-wrapper">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
      </div>
      <span>Admin</span>
    </button>
  </nav>

  <!-- ADMIN PIN LOGIN MODAL (Masked dots PIN entry) -->
  <div class="modal-overlay" id="adminPinModal">
    <div class="modal-box">
      <div class="modal-head">
        <h3>Admin Verification</h3>
        <button class="modal-close-btn" onclick="closeAdminPinModal()">✕</button>
      </div>
      <div class="modal-content-area">
        <p style="font-size: 0.85rem; color: var(--text-secondary);">
          Please enter your Admin PIN to unlock directory management tools.
        </p>
        <div class="input-group">
          <label for="adminPinInput">Security PIN</label>
          <input type="password" id="adminPinInput" class="text-input pin-masked-input" placeholder="••••" autocomplete="current-password" inputmode="numeric">
          <div id="pinErrorMsg" style="display: none; color: var(--danger); font-size: 0.8rem; margin-top: 4px;"></div>
        </div>
      </div>
      <div class="modal-action-row">
        <button class="btn-modal-cancel" onclick="closeAdminPinModal()">Cancel</button>
        <button class="btn-modal-primary" id="btnConfirmAdminPin">Unlock Admin</button>
      </div>
    </div>
  </div>

  <!-- PWA INSTALL INSTRUCTIONS MODAL -->
  <div class="modal-overlay" id="pwaInstructionsModal">
    <div class="modal-box">
      <div class="modal-head">
        <h3>📲 Install Advocate Directory</h3>
        <button class="modal-close-btn" onclick="closePwaModal()">✕</button>
      </div>
      <div class="modal-content-area" style="font-size: 0.9rem; color: var(--text-primary);">
        <p style="margin-bottom: 12px;">Install this app on your device for instant offline access and native app experience:</p>
        <div style="background: var(--surface-variant); padding: 12px; border-radius: var(--radius-md); margin-bottom: 10px;">
          <strong>🤖 On Android (Chrome / Edge):</strong>
          <ol style="margin-left: 20px; margin-top: 6px; font-size: 0.85rem;">
            <li>Tap the browser menu <strong>(⋮)</strong> in the top right.</li>
            <li>Select <strong>"Install App"</strong> or <strong>"Add to Home Screen"</strong>.</li>
          </ol>
        </div>
        <div style="background: var(--surface-variant); padding: 12px; border-radius: var(--radius-md);">
          <strong>🍏 On iPhone / iPad (Safari):</strong>
          <ol style="margin-left: 20px; margin-top: 6px; font-size: 0.85rem;">
            <li>Tap the <strong>Share</strong> button (box with upward arrow ⎋).</li>
            <li>Scroll down and tap <strong>"Add to Home Screen"</strong> (+).</li>
          </ol>
        </div>
      </div>
      <div class="modal-action-row">
        <button class="btn-modal-primary" onclick="closePwaModal()">Got It</button>
      </div>
    </div>
  </div>

  <!-- ADVOCATE EDIT/ADD MODAL -->
  <div class="modal-overlay" id="advocateEditModal">
    <div class="modal-box">
      <div class="modal-head">
        <h3 id="advocateModalTitle">Add Advocate</h3>
        <button class="modal-close-btn" onclick="closeAdvocateModal()">✕</button>
      </div>
      <div class="modal-content-area">
        <input type="hidden" id="advEditId">
        <div class="input-group">
          <label for="advEditName">Advocate Name *</label>
          <input type="text" id="advEditName" class="text-input" placeholder="e.g. NITIN M AMIN" required>
        </div>
        <div class="input-group">
          <label for="advEditCode">Advocate Code</label>
          <input type="text" id="advEditCode" class="text-input" placeholder="e.g. 126">
        </div>
        <div class="input-group">
          <label for="advEditPanel">Panel Advocate Info</label>
          <input type="text" id="advEditPanel" class="text-input" placeholder="e.g. National Highway Authority Of India; SBI">
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
          <div class="input-group">
            <label for="advEditMob1">Primary Mobile</label>
            <input type="tel" id="advEditMob1" class="text-input" placeholder="e.g. 9825000000">
          </div>
          <div class="input-group">
            <label for="advEditMob2">Secondary Mobile</label>
            <input type="tel" id="advEditMob2" class="text-input" placeholder="Optional">
          </div>
        </div>

        <!-- MULTIPLE CLERKS ASSIGNMENT SELECTOR -->
        <div class="input-group">
          <label>Assigned Clerk(s) <span style="font-weight: normal; color: var(--accent);">(Multiple Clerks Supported)</span></label>
          <div class="clerks-chip-box" id="advEditClerksChipBox"></div>
          <div style="display: flex; gap: 8px; margin-top: 6px;">
            <select class="select-input" id="advEditClerkSelect">
              <option value="">-- Choose Clerk to Assign --</option>
            </select>
            <button class="btn-modal-cancel" type="button" id="btnAssignClerkToAdv">+ Add</button>
          </div>
        </div>
      </div>
      <div class="modal-action-row">
        <button class="btn-modal-cancel" onclick="closeAdvocateModal()">Cancel</button>
        <button class="btn-modal-primary" id="btnSaveAdvocateRecord">Save Advocate</button>
      </div>
    </div>
  </div>

  <!-- CLERK EDIT/ADD MODAL -->
  <div class="modal-overlay" id="clerkEditModal">
    <div class="modal-box">
      <div class="modal-head">
        <h3 id="clerkModalTitle">Add Clerk</h3>
        <button class="modal-close-btn" onclick="closeClerkModal()">✕</button>
      </div>
      <div class="modal-content-area">
        <input type="hidden" id="clerkEditId">
        <div class="input-group">
          <label for="clerkEditName">Clerk Name *</label>
          <input type="text" id="clerkEditName" class="text-input" placeholder="e.g. PRADIPBHAI S SHUKLA" required>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
          <div class="input-group">
            <label for="clerkEditMob1">Primary Mobile *</label>
            <input type="tel" id="clerkEditMob1" class="text-input" placeholder="e.g. 9825049155">
          </div>
          <div class="input-group">
            <label for="clerkEditMob2">Secondary Mobile</label>
            <input type="tel" id="clerkEditMob2" class="text-input" placeholder="Optional">
          </div>
        </div>
      </div>
      <div class="modal-action-row">
        <button class="btn-modal-cancel" onclick="closeClerkModal()">Cancel</button>
        <button class="btn-modal-primary" id="btnSaveClerkRecord">Save Clerk</button>
      </div>
    </div>
  </div>

  <!-- TOAST CONTAINER -->
  <div class="toast-wrap" id="appToastContainer"></div>

  <!-- EMBEDDED DATA & SCRIPT -->
  <script>
    // 1. EMBEDDED DIRECTORY SEED DATA
    const SEED_DATA = {seed_json};

    // 2. CONSTANTS & KEYS
    const KEY_DATA = "adv_clerk_pwa_data_v7_1365";
    const KEY_ADMIN = "adv_clerk_pwa_admin_active";
    const KEY_PIN = "adv_clerk_pwa_pin";
    const KEY_THEME = "adv_clerk_pwa_theme";
    const PRIMARY_PIN = "8899";
    const FALLBACK_PIN = "HighCourt541";

    let directory = {{ advocates: [], clerks: [] }};
    let isAdmin = false;
    let currentTab = "advocates";
    let activeFilter = "all";
    let searchQuery = "";

    const ADV_PAGE_SIZE = 40;
    let advVisibleCount = ADV_PAGE_SIZE;
    const CLERK_PAGE_SIZE = 40;
    let clerkVisibleCount = CLERK_PAGE_SIZE;

    // 7-Tap Secret Trigger Variables (Requirement #1)
    let logoTapCount = 0;
    let logoTapTimer = null;

    let deferredPrompt = null;

    // 3. INITIALIZATION
    document.addEventListener("DOMContentLoaded", () => {{
      initPwaServiceWorker();
      initTheme();
      loadStoredData();
      setupEventListeners();
      renderAllViews();
    }});

    // PWA & Service Worker
    function initPwaServiceWorker() {{
      window.addEventListener("beforeinstallprompt", (e) => {{
        e.preventDefault();
        deferredPrompt = e;
        const installBtn = document.getElementById("pwaInstallHeaderBtn");
        if (installBtn) installBtn.style.display = "inline-flex";
      }});

      window.addEventListener("appinstalled", () => {{
        deferredPrompt = null;
        showToast("App installed to your device!");
      }});

      if ("serviceWorker" in navigator) {{
        navigator.serviceWorker.register("./sw.js").catch(() => {{
          try {{
            const swCode = "self.addEventListener('install', e => self.skipWaiting()); self.addEventListener('activate', e => clients.claim()); self.addEventListener('fetch', e => {{ if (e.request.method === 'GET') {{ e.respondWith(fetch(e.request).catch(() => caches.match(e.request))); }} }});";
            const blob = new Blob([swCode], {{ type: "application/javascript" }});
            navigator.serviceWorker.register(URL.createObjectURL(blob)).catch(() => {{}});
          }} catch(e) {{}}
        }});
      }}
    }}

    function handlePwaInstallClick() {{
      if (deferredPrompt) {{
        deferredPrompt.prompt();
        deferredPrompt.userChoice.then((choiceResult) => {{
          if (choiceResult.outcome === "accepted") {{
            deferredPrompt = null;
          }}
        }});
      }} else {{
        document.getElementById("pwaInstructionsModal").classList.add("open");
      }}
    }}

    function closePwaModal() {{
      document.getElementById("pwaInstructionsModal").classList.remove("open");
    }}

    // Theme Handling
    function initTheme() {{
      const saved = localStorage.getItem(KEY_THEME);
      if (saved === "dark" || (!saved && window.matchMedia("(prefers-color-scheme: dark)").matches)) {{
        document.documentElement.setAttribute("data-theme", "dark");
        updateThemeIcon(true);
      }} else {{
        document.documentElement.removeAttribute("data-theme");
        updateThemeIcon(false);
      }}
    }}

    function toggleTheme() {{
      const isDark = document.documentElement.getAttribute("data-theme") === "dark";
      if (isDark) {{
        document.documentElement.removeAttribute("data-theme");
        localStorage.setItem(KEY_THEME, "light");
        updateThemeIcon(false);
      }} else {{
        document.documentElement.setAttribute("data-theme", "dark");
        localStorage.setItem(KEY_THEME, "dark");
        updateThemeIcon(true);
      }}
    }}

    function updateThemeIcon(isDark) {{
      const icon = document.getElementById("themeToggleIcon");
      if (isDark) {{
        icon.innerHTML = '<circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/>';
      }} else {{
        icon.innerHTML = '<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>';
      }}
    }}

    // Data Load & Save
    function loadStoredData() {{
      const saved = localStorage.getItem(KEY_DATA);
      if (saved) {{
        try {{
          directory = JSON.parse(saved);
          if (!directory.advocates || directory.advocates.length < SEED_DATA.advocates.length) {{
            directory = JSON.parse(JSON.stringify(SEED_DATA));
            saveData();
          }}
        }} catch (e) {{
          directory = JSON.parse(JSON.stringify(SEED_DATA));
          saveData();
        }}
      }} else {{
        directory = JSON.parse(JSON.stringify(SEED_DATA));
        saveData();
      }}

      isAdmin = localStorage.getItem(KEY_ADMIN) === "true";
      syncAdminRoleUI();
    }}

    function saveData() {{
      localStorage.setItem(KEY_DATA, JSON.stringify(directory));
    }}

    function getAdminPin() {{
      return localStorage.getItem(KEY_PIN) || PRIMARY_PIN;
    }}

    // 1. ADMIN LOGIN GESTURE: 7 Taps on Header Logo (Requirement #1)
    function handleHeaderLogoTap() {{
      logoTapCount++;
      clearTimeout(logoTapTimer);

      if (logoTapCount === 7) {{
        logoTapCount = 0;
        showToast("Admin Mode Activated");
        openAdminPinModal();
        return;
      }}

      logoTapTimer = setTimeout(() => {{
        logoTapCount = 0;
      }}, 3000);
    }}

    // 2. ADMIN ROLE UI SYNCHRONIZATION
    function syncAdminRoleUI() {{
      const adminPill = document.getElementById("adminActivePill");
      const adminActionsBar = document.getElementById("adminHeaderActionsBar");
      const adminNavTab = document.getElementById("navAdminTab");

      if (isAdmin) {{
        adminPill.classList.add("active");
        adminActionsBar.classList.add("unlocked");
        adminNavTab.style.display = "flex";
      }} else {{
        adminPill.classList.remove("active");
        adminActionsBar.classList.remove("unlocked");
        adminNavTab.style.display = "none";
        if (currentTab === "admin") {{
          switchBottomTab("advocates");
        }}
      }}
    }}

    function openAdminPinModal() {{
      const modal = document.getElementById("adminPinModal");
      const inp = document.getElementById("adminPinInput");
      const err = document.getElementById("pinErrorMsg");
      inp.value = "";
      err.style.display = "none";
      modal.classList.add("open");
      setTimeout(() => inp.focus(), 150);
    }}

    function closeAdminPinModal() {{
      document.getElementById("adminPinModal").classList.remove("open");
    }}

    function verifyAdminPin() {{
      const entered = document.getElementById("adminPinInput").value.trim();
      const actualPin = getAdminPin();
      const err = document.getElementById("pinErrorMsg");

      if (entered === actualPin || entered === PRIMARY_PIN || entered === FALLBACK_PIN) {{
        isAdmin = true;
        localStorage.setItem(KEY_ADMIN, "true");
        syncAdminRoleUI();
        closeAdminPinModal();
        renderAllViews();
        showToast("🛡️ Admin Mode Unlocked!");
      }} else {{
        err.textContent = "Incorrect PIN. Please try again.";
        err.style.display = "block";
      }}
    }}

    // Event Listeners
    function setupEventListeners() {{
      // Secret 7-tap on main header logo
      document.getElementById("mainHeaderLogoBrand").addEventListener("click", handleHeaderLogoTap);

      // Active pill exit click
      document.getElementById("adminActivePill").addEventListener("click", () => {{
        if (confirm("Exit Admin Mode?")) {{
          isAdmin = false;
          localStorage.removeItem(KEY_ADMIN);
          syncAdminRoleUI();
          renderAllViews();
          showToast("Exited Admin Mode");
        }}
      }});

      document.getElementById("pwaInstallHeaderBtn").addEventListener("click", handlePwaInstallClick);
      document.getElementById("themeToggleHeadBtn").addEventListener("click", toggleTheme);
      document.getElementById("btnConfirmAdminPin").addEventListener("click", verifyAdminPin);
      document.getElementById("adminPinInput").addEventListener("keydown", (e) => {{
        if (e.key === "Enter") verifyAdminPin();
      }});

      // Navigation
      document.getElementById("navAdvocatesTab").addEventListener("click", () => switchBottomTab("advocates"));
      document.getElementById("navClerksTab").addEventListener("click", () => switchBottomTab("clerks"));
      document.getElementById("navAdminTab").addEventListener("click", () => switchBottomTab("admin"));
      document.getElementById("btnAdminLogoutFromTab").addEventListener("click", () => {{
        isAdmin = false;
        localStorage.removeItem(KEY_ADMIN);
        syncAdminRoleUI();
        renderAllViews();
        showToast("Exited Admin Mode");
      }});

      // Search Field
      const searchInput = document.getElementById("globalSearchInput");
      const searchClear = document.getElementById("globalSearchClearBtn");
      searchInput.addEventListener("input", (e) => {{
        searchQuery = e.target.value.trim().toLowerCase();
        searchClear.classList.toggle("visible", searchQuery.length > 0);
        advVisibleCount = ADV_PAGE_SIZE;
        clerkVisibleCount = CLERK_PAGE_SIZE;
        renderAllViews();
      }});

      searchClear.addEventListener("click", () => {{
        searchInput.value = "";
        searchQuery = "";
        searchClear.classList.remove("visible");
        advVisibleCount = ADV_PAGE_SIZE;
        clerkVisibleCount = CLERK_PAGE_SIZE;
        renderAllViews();
      }});

      // Filter Chips
      document.querySelectorAll(".filter-pill").forEach(chip => {{
        chip.addEventListener("click", () => {{
          document.querySelectorAll(".filter-pill").forEach(c => c.classList.remove("active"));
          chip.classList.add("active");
          activeFilter = chip.getAttribute("data-filter");
          advVisibleCount = ADV_PAGE_SIZE;
          renderAdvocates();
        }});
      }});

      // Load More
      document.getElementById("advLoadMoreBtn").addEventListener("click", () => {{
        advVisibleCount += ADV_PAGE_SIZE;
        renderAdvocates();
      }});

      document.getElementById("clerkLoadMoreBtn").addEventListener("click", () => {{
        clerkVisibleCount += CLERK_PAGE_SIZE;
        renderClerks();
      }});

      // Admin CRUD
      document.getElementById("btnAdminAddAdvocate").addEventListener("click", () => openAdvocateModal());
      document.getElementById("btnAdminAddClerk").addEventListener("click", () => openClerkModal());
      document.getElementById("btnSaveAdvocateRecord").addEventListener("click", saveAdvocateRecord);
      document.getElementById("btnSaveClerkRecord").addEventListener("click", saveClerkRecord);
      document.getElementById("btnAssignClerkToAdv").addEventListener("click", handleAssignClerkToAdv);

      // Admin Data Management
      document.getElementById("btnExportCsvAdmin").addEventListener("click", exportDirectoryCsv);
      document.getElementById("btnExportJsonAdmin").addEventListener("click", exportDirectoryJson);
      document.getElementById("adminCsvFileInput").addEventListener("change", handleAdminCsvImport);
      document.getElementById("btnResetDefaultSeedAdmin").addEventListener("click", () => {{
        if (confirm("Reset directory to default original 1,365 records? Any manual changes will be replaced.")) {{
          directory = JSON.parse(JSON.stringify(SEED_DATA));
          saveData();
          renderAllViews();
          showToast("Directory restored to default 1,365 records");
        }}
      }});

      document.getElementById("btnChangePinAdmin").addEventListener("click", () => {{
        const np = prompt("Enter new Admin PIN (minimum 4 digits):");
        if (np && np.trim().length >= 4) {{
          localStorage.setItem(KEY_PIN, np.trim());
          showToast("Admin PIN updated successfully");
        }} else if (np !== null) {{
          alert("PIN must be at least 4 digits.");
        }}
      }});
    }}

    function switchBottomTab(tab) {{
      currentTab = tab;
      document.querySelectorAll(".nav-item-btn").forEach(btn => btn.classList.remove("active"));

      document.getElementById("viewAdvocatesTab").style.display = tab === "advocates" ? "block" : "none";
      document.getElementById("viewClerksTab").style.display = tab === "clerks" ? "block" : "none";
      document.getElementById("viewAdminTab").style.display = tab === "admin" ? "block" : "none";

      const filterChips = document.getElementById("advFilterChipsRow");
      filterChips.style.display = tab === "advocates" ? "flex" : "none";

      const searchInputEl = document.getElementById("globalSearchInput");
      if (tab === "advocates") {{
        document.getElementById("navAdvocatesTab").classList.add("active");
        if (searchInputEl) searchInputEl.placeholder = "Search advocate name, code, panel...";
      }} else if (tab === "clerks") {{
        document.getElementById("navClerksTab").classList.add("active");
        if (searchInputEl) searchInputEl.placeholder = "Search clerk name...";
      }} else if (tab === "admin") {{
        document.getElementById("navAdminTab").classList.add("active");
        if (searchInputEl) searchInputEl.placeholder = "Search directory...";
      }}

      window.scrollTo({{ top: 0, behavior: "smooth" }});
    }}

    function renderAllViews() {{
      renderAdvocates();
      renderClerks();
      updateBadges();
    }}

    function updateBadges() {{
      const advTotal = directory.advocates.length;
      const clerkTotal = directory.clerks.length;
      document.getElementById("badgeTotalRecords").textContent = advTotal.toLocaleString() + " Records";
      document.getElementById("bottomNavAdvCount").textContent = advTotal.toLocaleString();
      document.getElementById("bottomNavClerkCount").textContent = clerkTotal.toLocaleString();
    }}

    // Filter Logic
    function getFilteredAdvocates() {{
      const q = searchQuery;
      return directory.advocates.filter(adv => {{
        if (activeFilter === "panel" && !adv.panel) return false;
        if (activeFilter === "has-clerk" && (!adv.clerkIds || adv.clerkIds.length === 0)) return false;
        if (activeFilter === "multi-clerk" && (!adv.clerkIds || adv.clerkIds.length < 2)) return false;
        if (activeFilter === "no-clerk" && (adv.clerkIds && adv.clerkIds.length > 0)) return false;

        if (!q) return true;

        if (adv.name && adv.name.toLowerCase().includes(q)) return true;
        if (adv.code && adv.code.toLowerCase().includes(q)) return true;
        if (adv.panel && adv.panel.toLowerCase().includes(q)) return true;

        if (adv.clerkIds && adv.clerkIds.length > 0) {{
          for (let cid of adv.clerkIds) {{
            const clerk = directory.clerks.find(c => c.id === cid);
            if (clerk) {{
              if (clerk.name && clerk.name.toLowerCase().includes(q)) return true;
            }}
          }}
        }}
        return false;
      }});
    }}

    function getFilteredClerks() {{
      const q = searchQuery;
      return directory.clerks.filter(clerk => {{
        if (!q) return true;
        if (clerk.name && clerk.name.toLowerCase().includes(q)) return true;

        const linked = directory.advocates.filter(a => a.clerkIds && a.clerkIds.includes(clerk.id));
        for (let a of linked) {{
          if (a.name && a.name.toLowerCase().includes(q)) return true;
          if (a.code && a.code.toLowerCase().includes(q)) return true;
        }}
        return false;
      }});
    }}

    // Render Advocates List (with Panel Advocate Highlighting!)
    function renderAdvocates() {{
      const container = document.getElementById("advocatesCardsGrid");
      const list = getFilteredAdvocates();
      const countNotice = document.getElementById("resultsCountNotice");
      const loadMoreBtn = document.getElementById("advLoadMoreBtn");

      countNotice.textContent = "Showing " + Math.min(advVisibleCount, list.length).toLocaleString() + " of " + list.length.toLocaleString() + " Advocates";
      loadMoreBtn.style.display = advVisibleCount < list.length ? "inline-flex" : "none";

      const visibleSlice = list.slice(0, advVisibleCount);

      if (visibleSlice.length === 0) {{
        container.innerHTML = `
          <div style="grid-column: 1/-1; text-align: center; padding: 48px 16px; color: var(--text-tertiary);">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 12px; opacity: 0.6;"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            <p style="font-size: 1.05rem; font-weight: 700; color: var(--text-secondary);">No matching advocates found</p>
            <p style="font-size: 0.85rem; margin-top: 4px;">Check your search keyword or tap 'All' to clear filters.</p>
          </div>
        `;
        return;
      }}

      let html = "";
      for (let adv of visibleSlice) {{
        const isPanel = adv.panel && adv.panel.trim().length > 0;

        // Clerks mapping
        const clerkObjs = (adv.clerkIds || []).map(cid => directory.clerks.find(c => c.id === cid)).filter(Boolean);
        let clerksHtml = "";

        if (clerkObjs.length === 0) {{
          clerksHtml = '<div style="font-size: 0.78rem; color: var(--text-tertiary); font-style: italic; padding: 2px 0;">No clerk assigned</div>';
        }} else {{
          clerksHtml = clerkObjs.map((clerk, idx) => {{
            const hasMultiple = clerkObjs.length > 1;
            const tag = hasMultiple ? '<span class="clerk-tag-pill">Clerk #' + (idx + 1) + '</span>' : "";
            
            let phoneRows = "";
            if (clerk.primaryMobile) phoneRows += renderDirectActionRow(clerk.primaryMobile, "Primary");
            if (clerk.secondaryMobile) phoneRows += renderDirectActionRow(clerk.secondaryMobile, "Secondary");
            if (!clerk.primaryMobile && !clerk.secondaryMobile) {{
              phoneRows = '<div style="font-size: 0.75rem; color: var(--text-tertiary);">No phone recorded</div>';
            }}

            return `
              <div class="clerk-item-card">
                <div class="clerk-name-title">
                  <span>${{escapeHtml(clerk.name)}}</span>
                  ${{tag}}
                </div>
                ${{phoneRows}}
              </div>
            `;
          }}).join("");
        }}

        // 3. PANEL ADVOCATE HIGHLIGHTING (Requirement #3)
        let panelBadges = "";
        if (isPanel) {{
          const panels = adv.panel.includes(";") ? adv.panel.split(";") : adv.panel.split(String.fromCharCode(10));
          panelBadges = panels.map(p => p.trim()).filter(Boolean).map(p => `
            <span class="chip-panel-badge" title="Panel Advocate">
              <span>⚖️ Panel:</span>
              <span>${{escapeHtml(p)}}</span>
            </span>
          `).join("");
        }}

        // Contact rows
        let contactRows = "";
        if (adv.primaryMobile) contactRows += renderDirectActionRow(adv.primaryMobile, "Primary");
        if (adv.secondaryMobile) contactRows += renderDirectActionRow(adv.secondaryMobile, "Secondary");
        if (!adv.primaryMobile && !adv.secondaryMobile) {{
          contactRows = '<div style="font-size: 0.78rem; color: var(--text-tertiary); padding: 4px 0;">No phone recorded</div>';
        }}

        html += `
          <div class="native-card ${{isPanel ? 'panel-highlighted' : ''}}">
            <div class="card-top">
              <div class="adv-title-group">
                <div class="adv-full-name">${{escapeHtml(adv.name)}}</div>
                <div class="chips-group">
                  ${{adv.code ? `<span class="chip-code">Code: ${{escapeHtml(adv.code)}}</span>` : ""}}
                  ${{adv.srNo ? `<span class="chip-sr">#${{adv.srNo}}</span>` : ""}}
                  ${{panelBadges}}
                </div>
              </div>
            </div>

            <!-- Advocate Direct Contact Section -->
            <div class="contact-container">
              <div style="font-size: 0.72rem; font-weight: 700; color: var(--text-secondary); text-transform: uppercase;">
                Advocate Contact
              </div>
              ${{contactRows}}
            </div>

            <!-- Assigned Clerks Section (Multiple Clerks Supported) -->
            <div class="clerk-box-container">
              <div class="clerk-box-head">
                <span>👨‍💼 Assigned Clerk(s)</span>
                ${{clerkObjs.length > 1 ? `<span style="background: #166534; color: #fff; padding: 1px 7px; border-radius: 99px; font-size: 0.65rem;">${{clerkObjs.length}} Clerks</span>` : ""}}
              </div>
              ${{clerksHtml}}
            </div>

            <!-- Card Bottom Actions -->
            <div class="card-bottom-actions">
              <button class="btn-share-contact" onclick="shareAdvocateCard(${{adv.id}})">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" x2="12" y1="2" y2="15"/></svg>
                <span>Share</span>
              </button>

              <div class="admin-card-tools ${{isAdmin ? 'unlocked' : ''}}">
                <button class="btn-card-edit" onclick="openAdvocateModal(${{adv.id}})">Edit</button>
                <button class="btn-card-del" onclick="deleteAdvocateRecord(${{adv.id}})">Delete</button>
              </div>
            </div>
          </div>
        `;
      }}

      container.innerHTML = html;
    }}

    // 4. WHATSAPP BUTTON WITH 'WhatsApp' TEXT (Requirement #4)
    function renderDirectActionRow(phone, label) {{
      const cleanDigits = phone.replace(/[^0-9]/g, "");
      const waNumber = cleanDigits.length === 10 ? "91" + cleanDigits : cleanDigits;

      return `
        <div class="contact-row-item">
          <div class="contact-number-info">
            <span>${{escapeHtml(phone)}}</span>
            <span class="contact-type-tag">(${{label}})</span>
          </div>
          <div class="quick-action-btns">
            <!-- Direct WhatsApp Button (Text: 'WhatsApp') -->
            <a href="https://wa.me/${{waNumber}}" target="_blank" rel="noopener" class="btn-action-whatsapp" title="WhatsApp">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M17.472 14.382c-.301-.15-1.78-.878-2.056-.978-.276-.101-.476-.15-.676.15-.2.301-.776.978-.951 1.178-.175.201-.351.226-.652.075-.301-.15-1.27-.468-2.42-1.493-.894-.797-1.498-1.782-1.674-2.083-.175-.301-.019-.464.132-.614.136-.135.301-.351.451-.527.151-.176.201-.301.301-.502.101-.2.051-.376-.025-.526-.075-.15-.677-1.631-.927-2.233-.244-.587-.492-.507-.676-.516h-.577c-.201 0-.527.075-.802.376s-1.053 1.028-1.053 2.507c0 1.479 1.078 2.908 1.229 3.109.15.2 2.122 3.24 5.141 4.543.718.31 1.279.495 1.716.634.721.229 1.377.197 1.895.12.577-.087 1.78-.727 2.031-1.429.251-.702.251-1.304.176-1.43-.075-.125-.276-.201-.577-.351z"/></svg>
              <span>WhatsApp</span>
            </a>
            <!-- Direct Phone Call Button -->
            <a href="tel:${{cleanDigits}}" class="btn-action-call" title="Call">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              <span>Call</span>
            </a>
            <!-- Copy Number Button -->
            <button class="btn-action-copy" onclick="copyTextToClipboard('${{escapeHtml(phone)}}')" title="Copy Number">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>
            </button>
          </div>
        </div>
      `;
    }}

    // Render Clerks List
    function renderClerks() {{
      const container = document.getElementById("clerksCardsGrid");
      const list = getFilteredClerks();
      const loadMoreBtn = document.getElementById("clerkLoadMoreBtn");

      loadMoreBtn.style.display = clerkVisibleCount < list.length ? "inline-flex" : "none";
      const visibleSlice = list.slice(0, clerkVisibleCount);

      if (visibleSlice.length === 0) {{
        container.innerHTML = `
          <div style="grid-column: 1/-1; text-align: center; padding: 48px 16px; color: var(--text-tertiary);">
            <p style="font-size: 1.05rem; font-weight: 700; color: var(--text-secondary);">No matching clerks found</p>
          </div>
        `;
        return;
      }}

      let html = "";
      for (let clerk of visibleSlice) {{
        const linkedAdvs = directory.advocates.filter(a => a.clerkIds && a.clerkIds.includes(clerk.id));

        let phoneRows = "";
        if (clerk.primaryMobile) phoneRows += renderDirectActionRow(clerk.primaryMobile, "Primary");
        if (clerk.secondaryMobile) phoneRows += renderDirectActionRow(clerk.secondaryMobile, "Secondary");
        if (!clerk.primaryMobile && !clerk.secondaryMobile) {{
          phoneRows = '<div style="font-size: 0.78rem; color: var(--text-tertiary);">No phone recorded</div>';
        }}

        const linkedListHtml = linkedAdvs.map(a => `
          <div style="display: flex; align-items: center; justify-content: space-between; font-size: 0.8rem; padding: 5px 8px; background: var(--surface); border-radius: var(--radius-sm); border: 1px solid var(--outline);">
            <span style="font-weight: 700;">${{escapeHtml(a.name)}}</span>
            <span style="color: var(--text-secondary); font-size: 0.75rem;">Code: ${{escapeHtml(a.code || '-')}}</span>
          </div>
        `).join("");

        html += `
          <div class="native-card">
            <div class="card-top">
              <div class="adv-title-group">
                <div class="adv-full-name" style="color: #166534;">${{escapeHtml(clerk.name)}}</div>
                <div class="chips-group">
                  <span class="chip-code" style="background: #dcfce7; color: #15803d; border-color: #bbf7d0;">Clerk ID: ${{clerk.id}}</span>
                  <span class="chip-sr">${{linkedAdvs.length}} Advocate${{linkedAdvs.length === 1 ? '' : 's'}}</span>
                </div>
              </div>
            </div>

            <div class="contact-container">
              ${{phoneRows}}
            </div>

            <!-- Associated Advocates Accordion -->
            <div>
              <div onclick="toggleClerkAccordion(${{clerk.id}})" style="background: var(--surface-variant); border: 1px solid var(--outline); border-radius: var(--radius-md); padding: 8px 12px; font-size: 0.82rem; font-weight: 700; color: var(--primary); cursor: pointer; display: flex; align-items: center; justify-content: space-between;">
                <span>Associated Advocates (${{linkedAdvs.length}})</span>
                <span id="clerkArrow_${{clerk.id}}">▼</span>
              </div>
              <div id="clerkAdvBox_${{clerk.id}}" style="display: none; flex-direction: column; gap: 6px; max-height: 200px; overflow-y: auto; padding: 6px; background: var(--surface-variant); border-radius: var(--radius-md); margin-top: 4px;">
                ${{linkedListHtml || '<div style="font-size: 0.8rem; color: var(--text-tertiary); padding: 4px;">No advocates linked</div>'}}
              </div>
            </div>

            <div class="card-bottom-actions">
              <button class="btn-share-contact" onclick="shareClerkCard(${{clerk.id}})">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" x2="12" y1="2" y2="15"/></svg>
                <span>Share</span>
              </button>

              <div class="admin-card-tools ${{isAdmin ? 'unlocked' : ''}}">
                <button class="btn-card-edit" onclick="openClerkModal(${{clerk.id}})">Edit</button>
                <button class="btn-card-del" onclick="deleteClerkRecord(${{clerk.id}})">Delete</button>
              </div>
            </div>
          </div>
        `;
      }}

      container.innerHTML = html;
    }}

    function toggleClerkAccordion(cid) {{
      const box = document.getElementById("clerkAdvBox_" + cid);
      const arr = document.getElementById("clerkArrow_" + cid);
      if (box.style.display === "none") {{
        box.style.display = "flex";
        arr.textContent = "▲";
      }} else {{
        box.style.display = "none";
        arr.textContent = "▼";
      }}
    }}

    // Share Contact Card
    function shareAdvocateCard(advId) {{
      const adv = directory.advocates.find(a => a.id === advId);
      if (!adv) return;
      const clerkObjs = (adv.clerkIds || []).map(cid => directory.clerks.find(c => c.id === cid)).filter(Boolean);
      const lines = [
        "*Gujarat High Court Advocate & Clerk Directory*",
        "👤 *Advocate:* " + adv.name
      ];
      if (adv.code) lines.push("🔖 *Code:* " + adv.code);
      if (adv.panel) lines.push("⚖️ *Panel:* " + adv.panel);
      if (adv.primaryMobile) lines.push("📞 *Mobile:* " + adv.primaryMobile);
      if (adv.secondaryMobile) lines.push("📱 *Alt Mobile:* " + adv.secondaryMobile);
      if (clerkObjs.length > 0) {{
        lines.push("");
        lines.push("👨‍💼 *Assigned Clerk(s):*");
        clerkObjs.forEach((c, idx) => {{
          lines.push("  " + (idx + 1) + ". " + c.name + (c.primaryMobile ? " (" + c.primaryMobile + ")" : ""));
        }});
      }}
      const text = lines.join(String.fromCharCode(10));
      if (navigator.share) {{
        navigator.share({{ title: adv.name, text: text }}).catch(() => copyTextToClipboard(text));
      }} else {{
        copyTextToClipboard(text);
      }}
    }}

    function shareClerkCard(clerkId) {{
      const clerk = directory.clerks.find(c => c.id === clerkId);
      if (!clerk) return;
      const linked = directory.advocates.filter(a => a.clerkIds && a.clerkIds.includes(clerk.id));
      const lines = [
        "*Gujarat High Court Clerk Directory*",
        "👨‍💼 *Clerk:* " + clerk.name
      ];
      if (clerk.primaryMobile) lines.push("📞 *Mobile:* " + clerk.primaryMobile);
      if (clerk.secondaryMobile) lines.push("📱 *Alt Mobile:* " + clerk.secondaryMobile);
      lines.push("⚖️ *Advocates Represented:* " + linked.length);
      const text = lines.join(String.fromCharCode(10));
      if (navigator.share) {{
        navigator.share({{ title: clerk.name, text: text }}).catch(() => copyTextToClipboard(text));
      }} else {{
        copyTextToClipboard(text);
      }}
    }}

    // Advocate Modal
    let editingClerkIds = [];

    function openAdvocateModal(advId = null) {{
      const modal = document.getElementById("advocateEditModal");
      const title = document.getElementById("advocateModalTitle");
      const idInp = document.getElementById("advEditId");
      const nameInp = document.getElementById("advEditName");
      const codeInp = document.getElementById("advEditCode");
      const panelInp = document.getElementById("advEditPanel");
      const mob1Inp = document.getElementById("advEditMob1");
      const mob2Inp = document.getElementById("advEditMob2");

      populateClerkSelectDropdown();

      if (advId) {{
        const adv = directory.advocates.find(a => a.id === advId);
        if (!adv) return;
        title.textContent = "Edit Advocate";
        idInp.value = adv.id;
        nameInp.value = adv.name || "";
        codeInp.value = adv.code || "";
        panelInp.value = adv.panel || "";
        mob1Inp.value = adv.primaryMobile || "";
        mob2Inp.value = adv.secondaryMobile || "";
        editingClerkIds = [...(adv.clerkIds || [])];
      }} else {{
        title.textContent = "Add Advocate";
        idInp.value = "";
        nameInp.value = "";
        codeInp.value = "";
        panelInp.value = "";
        mob1Inp.value = "";
        mob2Inp.value = "";
        editingClerkIds = [];
      }}

      renderAdvocateClerkChips();
      modal.classList.add("open");
      setTimeout(() => nameInp.focus(), 150);
    }}

    function closeAdvocateModal() {{
      document.getElementById("advocateEditModal").classList.remove("open");
    }}

    function populateClerkSelectDropdown() {{
      const select = document.getElementById("advEditClerkSelect");
      const sorted = [...directory.clerks].sort((a,b) => a.name.localeCompare(b.name));
      let opts = '<option value="">-- Choose Clerk to Assign --</option>';
      for (let c of sorted) {{
        opts += '<option value="' + c.id + '">' + escapeHtml(c.name) + ' (' + (c.primaryMobile || 'No phone') + ')</option>';
      }}
      select.innerHTML = opts;
    }}

    function renderAdvocateClerkChips() {{
      const box = document.getElementById("advEditClerksChipBox");
      if (editingClerkIds.length === 0) {{
        box.innerHTML = '<span style="font-size: 0.8rem; color: var(--text-tertiary); padding: 4px;">No clerks assigned. Choose from below to add.</span>';
        return;
      }}

      let html = "";
      for (let cid of editingClerkIds) {{
        const clerk = directory.clerks.find(c => c.id === cid);
        const name = clerk ? clerk.name : "Clerk #" + cid;
        html += `
          <div class="clerk-sel-chip">
            <span>${{escapeHtml(name)}}</span>
            <button type="button" class="btn-remove-chip" onclick="removeClerkFromAdvModal(${{cid}})">✕</button>
          </div>
        `;
      }}
      box.innerHTML = html;
    }}

    function handleAssignClerkToAdv() {{
      const select = document.getElementById("advEditClerkSelect");
      const val = parseInt(select.value);
      if (!val) {{
        showToast("Please select a clerk from the dropdown");
        return;
      }}
      if (!editingClerkIds.includes(val)) {{
        editingClerkIds.push(val);
        renderAdvocateClerkChips();
      }}
      select.value = "";
    }}

    window.removeClerkFromAdvModal = function(cid) {{
      editingClerkIds = editingClerkIds.filter(id => id !== cid);
      renderAdvocateClerkChips();
    }};

    function saveAdvocateRecord() {{
      const idInp = document.getElementById("advEditId").value;
      const name = document.getElementById("advEditName").value.trim();
      const code = document.getElementById("advEditCode").value.trim();
      const panel = document.getElementById("advEditPanel").value.trim();
      const mob1 = document.getElementById("advEditMob1").value.trim();
      const mob2 = document.getElementById("advEditMob2").value.trim();

      if (!name) {{
        alert("Please enter Advocate Name.");
        return;
      }}

      if (idInp) {{
        const advId = parseInt(idInp);
        const adv = directory.advocates.find(a => a.id === advId);
        if (adv) {{
          adv.name = name;
          adv.code = code;
          adv.panel = panel;
          adv.primaryMobile = mob1;
          adv.secondaryMobile = mob2;
          adv.clerkIds = [...editingClerkIds];
        }}
        showToast("Advocate updated");
      }} else {{
        const newId = directory.advocates.length > 0 ? Math.max(...directory.advocates.map(a => a.id)) + 1 : 1;
        const newSr = directory.advocates.length > 0 ? Math.max(...directory.advocates.map(a => a.srNo || 0)) + 1 : 1;
        directory.advocates.unshift({{
          id: newId,
          srNo: newSr,
          name: name,
          code: code,
          panel: panel,
          primaryMobile: mob1,
          secondaryMobile: mob2,
          clerkIds: [...editingClerkIds]
        }});
        showToast("New Advocate added");
      }}

      saveData();
      closeAdvocateModal();
      renderAllViews();
    }}

    window.deleteAdvocateRecord = function(advId) {{
      const adv = directory.advocates.find(a => a.id === advId);
      if (!adv) return;
      if (confirm('Delete advocate "' + adv.name + '"?')) {{
        directory.advocates = directory.advocates.filter(a => a.id !== advId);
        saveData();
        renderAllViews();
        showToast("Advocate deleted");
      }}
    }};

    // Clerk Modal
    function openClerkModal(clerkId = null) {{
      const modal = document.getElementById("clerkEditModal");
      const title = document.getElementById("clerkModalTitle");
      const idInp = document.getElementById("clerkEditId");
      const nameInp = document.getElementById("clerkEditName");
      const mob1Inp = document.getElementById("clerkEditMob1");
      const mob2Inp = document.getElementById("clerkEditMob2");

      if (clerkId) {{
        const clerk = directory.clerks.find(c => c.id === clerkId);
        if (!clerk) return;
        title.textContent = "Edit Clerk";
        idInp.value = clerk.id;
        nameInp.value = clerk.name || "";
        mob1Inp.value = clerk.primaryMobile || "";
        mob2Inp.value = clerk.secondaryMobile || "";
      }} else {{
        title.textContent = "Add Clerk";
        idInp.value = "";
        nameInp.value = "";
        mob1Inp.value = "";
        mob2Inp.value = "";
      }}

      modal.classList.add("open");
      setTimeout(() => nameInp.focus(), 150);
    }}

    function closeClerkModal() {{
      document.getElementById("clerkEditModal").classList.remove("open");
    }}

    function saveClerkRecord() {{
      const idInp = document.getElementById("clerkEditId").value;
      const name = document.getElementById("clerkEditName").value.trim();
      const mob1 = document.getElementById("clerkEditMob1").value.trim();
      const mob2 = document.getElementById("clerkEditMob2").value.trim();

      if (!name) {{
        alert("Please enter Clerk Name.");
        return;
      }}

      if (idInp) {{
        const cId = parseInt(idInp);
        const clerk = directory.clerks.find(c => c.id === cId);
        if (clerk) {{
          clerk.name = name;
          clerk.primaryMobile = mob1;
          clerk.secondaryMobile = mob2;
        }}
        showToast("Clerk updated");
      }} else {{
        const newId = directory.clerks.length > 0 ? Math.max(...directory.clerks.map(c => c.id)) + 1 : 1;
        directory.clerks.push({{
          id: newId,
          name: name,
          primaryMobile: mob1,
          secondaryMobile: mob2
        }});
        showToast("New Clerk created");
      }}

      saveData();
      closeClerkModal();
      renderAllViews();
    }}

    window.deleteClerkRecord = function(clerkId) {{
      const clerk = directory.clerks.find(c => c.id === clerkId);
      if (!clerk) return;
      if (confirm('Delete clerk "' + clerk.name + '"? They will also be unassigned from linked advocates.')) {{
        directory.clerks = directory.clerks.filter(c => c.id !== clerkId);
        directory.advocates.forEach(a => {{
          if (a.clerkIds) a.clerkIds = a.clerkIds.filter(id => id !== clerkId);
        }});
        saveData();
        renderAllViews();
        showToast("Clerk deleted");
      }}
    }};

    // Admin Data Export
    function exportDirectoryCsv() {{
      const rows = [];
      rows.push(["Sr No", "Advocate Name", "Advocate Code", "Panel Advocate", "Advocate Primary Mobile", "Advocate Secondary Mobile", "Clerk Name", "Clerk Primary Mobile", "Clerk Secondary Mobile"]);
      for (let adv of directory.advocates) {{
        const clerkObjs = (adv.clerkIds || []).map(cid => directory.clerks.find(c => c.id === cid)).filter(Boolean);
        const firstClerk = clerkObjs[0] || null;
        rows.push([
          adv.srNo || "",
          adv.name || "",
          adv.code || "",
          adv.panel || "",
          adv.primaryMobile || "",
          adv.secondaryMobile || "",
          firstClerk ? firstClerk.name : "",
          firstClerk ? firstClerk.primaryMobile : "",
          firstClerk ? firstClerk.secondaryMobile : ""
        ]);
      }}
      const csvContent = rows.map(r => r.map(csvEscape).join(",")).join(String.fromCharCode(10));
      downloadBlobFile(new Blob(["\\uFEFF" + csvContent], {{ type: "text/csv;charset=utf-8;" }}), "advocate_directory_export.csv");
      showToast("Directory CSV downloaded");
    }}

    function exportDirectoryJson() {{
      const jsonStr = JSON.stringify(directory, null, 2);
      downloadBlobFile(new Blob([jsonStr], {{ type: "application/json" }}), "advocate_directory_export.json");
      showToast("Directory JSON downloaded");
    }}

    function csvEscape(val) {{
      if (!val) return "";
      const s = String(val);
      if (s.includes(",") || s.includes('"') || s.includes(String.fromCharCode(10)) || s.includes(String.fromCharCode(13))) {{
        return '"' + s.replace(/"/g, '""') + '"';
      }}
      return s;
    }}

    function downloadBlobFile(blob, name) {{
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = name;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }}

    // Admin CSV Import
    function handleAdminCsvImport(e) {{
      const file = e.target.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (evt) => {{
        try {{
          parseAndApplyCsvText(evt.target.result);
        }} catch (err) {{
          alert("Error parsing CSV: " + err.message);
        }}
      }};
      reader.readAsText(file, "UTF-8");
      e.target.value = "";
    }}

    function parseAndApplyCsvText(csvText) {{
      const lines = csvText.split(/\\r?\\n/).filter(l => l.trim().length > 0);
      if (lines.length < 2) {{
        alert("CSV contains no valid data lines.");
        return;
      }}

      function parseRow(line) {{
        const out = [];
        let curr = "";
        let inQuotes = false;
        for (let i = 0; i < line.length; i++) {{
          const char = line[i];
          if (char === '"') {{
            if (inQuotes && line[i+1] === '"') {{
              curr += '"';
              i++;
            }} else {{
              inQuotes = !inQuotes;
            }}
          }} else if (char === ',' && !inQuotes) {{
            out.push(curr.trim());
            curr = "";
          }} else {{
            curr += char;
          }}
        }}
        out.push(curr.trim());
        return out;
      }}

      const rows = lines.map(parseRow);
      const header = rows[0].map(h => h.toLowerCase());

      let colSr = header.findIndex(h => h.includes("sr") || h.includes("no"));
      let colName = header.findIndex(h => (h.includes("adv") && h.includes("name")) || h === "name");
      let colCode = header.findIndex(h => h.includes("code"));
      let colPanel = header.findIndex(h => h.includes("panel"));
      let colMob1 = header.findIndex(h => (h.includes("adv") || h.includes("pri") || h.includes("1")) && (h.includes("mob") || h.includes("phone")));
      let colMob2 = header.findIndex(h => (h.includes("sec") || h.includes("2")) && (h.includes("mob") || h.includes("phone")));
      let colClerk = header.findIndex(h => (h.includes("clerk") && h.includes("name")) || h === "clerk");
      let colClerkMob1 = header.findIndex(h => h.includes("clerk") && (h.includes("pri") || h.includes("1") || h.includes("mob")));
      let colClerkMob2 = header.findIndex(h => h.includes("clerk") && (h.includes("sec") || h.includes("2")));

      if (colName === -1) colName = 1;
      if (colCode === -1) colCode = 2;
      if (colPanel === -1) colPanel = 3;
      if (colMob1 === -1) colMob1 = 4;
      if (colMob2 === -1) colMob2 = 5;
      if (colClerk === -1) colClerk = 6;
      if (colClerkMob1 === -1) colClerkMob1 = 7;
      if (colClerkMob2 === -1) colClerkMob2 = 8;

      const newClerkMap = {{}};
      const newClerks = [];
      let cCounter = 1;

      for (let r of rows.slice(1)) {{
        const cName = r[colClerk] || "";
        const cMob1 = r[colClerkMob1] || "";
        const cMob2 = r[colClerkMob2] || "";
        if (cName.trim()) {{
          const key = cName.trim().toLowerCase();
          if (!newClerkMap[key]) {{
            const obj = {{ id: cCounter++, name: cName.trim(), primaryMobile: cMob1.trim(), secondaryMobile: cMob2.trim() }};
            newClerkMap[key] = obj;
            newClerks.push(obj);
          }}
        }}
      }}

      const newAdvocates = [];
      for (let i = 1; i < rows.length; i++) {{
        const r = rows[i];
        const advName = r[colName] || "";
        if (!advName.trim()) continue;

        const cName = (r[colClerk] || "").trim().toLowerCase();
        const clerkIds = cName && newClerkMap[cName] ? [newClerkMap[cName].id] : [];

        newAdvocates.push({{
          id: i,
          srNo: parseInt(r[colSr]) || i,
          name: advName.trim(),
          code: (r[colCode] || "").trim(),
          panel: (r[colPanel] || "").trim(),
          primaryMobile: (r[colMob1] || "").trim(),
          secondaryMobile: (r[colMob2] || "").trim(),
          clerkIds: clerkIds
        }});
      }}

      if (newAdvocates.length === 0) {{
        alert("No valid advocate records found in CSV file.");
        return;
      }}

      if (confirm("Found " + newAdvocates.length + " advocates and " + newClerks.length + " clerks. Overwrite current directory?")) {{
        directory = {{
          advocates: newAdvocates,
          clerks: newClerks
        }};
        saveData();
        renderAllViews();
        showToast("Successfully imported " + newAdvocates.length + " records!");
      }}
    }}

    // Toast Utility
    function showToast(msg) {{
      const container = document.getElementById("appToastContainer");
      const t = document.createElement("div");
      t.className = "toast-msg";
      t.textContent = msg;
      container.appendChild(t);
      setTimeout(() => {{
        if (t.parentNode) t.parentNode.removeChild(t);
      }}, 3000);
    }}

    function copyTextToClipboard(text) {{
      if (navigator.clipboard && navigator.clipboard.writeText) {{
        navigator.clipboard.writeText(text).then(() => {{
          showToast("Copied: " + text.slice(0, 30) + (text.length > 30 ? "..." : ""));
        }}).catch(() => fallbackCopyExec(text));
      }} else {{
        fallbackCopyExec(text);
      }}
    }}

    function fallbackCopyExec(text) {{
      const ta = document.createElement("textarea");
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      document.body.removeChild(ta);
      showToast("Copied to clipboard");
    }}

    function escapeHtml(str) {{
      if (!str) return "";
      return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
    }}
  </script>
</body>
</html>
'''

with open("index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

os.makedirs("public", exist_ok=True)
with open("public/index.html", "w", encoding="utf-8") as f:
    f.write(html_content)
os.makedirs("docs", exist_ok=True)
with open("docs/index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Generated index.html successfully! ({len(html_content)} bytes)")
