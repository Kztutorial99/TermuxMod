# TermuxMod — Rencana Modernisasi UX/UI 2026

> **Panduan untuk semua agent:** Baca file ini PERTAMA. Cek status setiap item sebelum mulai kerja.
> Jangan ulangi pekerjaan yang sudah `[x]`. Lanjutkan dari item `[ ]` berikutnya.
> Setelah selesai satu item, update status di file ini dan commit.

---

## Cara Agent Menggunakan File Ini

1. Baca PLAN.md ini dari atas ke bawah
2. Cari item pertama yang masih `[ ]` (belum dikerjakan)
3. Kerjakan item tersebut
4. Ubah `[ ]` menjadi `[x]` dan tambahkan catatan singkat
5. Commit: `git commit -m "plan: selesai [nama item]"`
6. Lanjutkan ke item `[ ]` berikutnya
7. Jalankan `bash scripts/safety-check.sh` sebelum & sesudah setiap fase

---

## Visi Desain

**TermuxMod Modern 2026** — terminal Android yang terasa premium, smooth, dan tidak seperti app lama.

| Prinsip | Implementasi |
|---------|-------------|
| Dark-first | Background `#0A0A0F`, surface `#12121A`, card `#1A1A2E` |
| Aksen elegan | Cyan-teal `#00E5CC` / violet `#7B61FF` sebagai aksen utama |
| Tipografi modern | Inter / JetBrains Mono untuk terminal, Inter untuk UI |
| Motion smooth | 300ms ease-out untuk semua transisi layar |
| Komponen modern | BottomSheet, Snackbar, MaterialCardView, ShapeableImageView |
| Tidak ada emoji | Di kode, layout, strings, komentar — tidak ada sama sekali |

---

## Palette Warna Resmi TermuxMod 2026

```
Background        #0A0A0F   (almost black, slight blue tint)
Surface           #12121A   (card background)
Surface Variant   #1A1A2E   (elevated surface)
Surface High      #222236   (toolbar, drawer)

Aksen Utama       #00E5CC   (cyan-teal)
Aksen Sekunder    #7B61FF   (violet/purple)
Aksen Error       #FF4757   (red, bukan Material merah)

Text Primary      #F0F0FF   (off-white)
Text Secondary    #8888AA   (muted)
Text Disabled     #444466   

Border/Divider    #2A2A3E
Ripple            #00E5CC22 (aksen utama 13% opacity)
```

---

## STATUS PROGRES KESELURUHAN

- Total item: 42
- Selesai: 14
- Sisa: 28

---

## FASE 1 — Fondasi Tema & Warna

> Target: semua warna dan tema dasar terganti. Fase ini HARUS selesai sebelum fase lain dimulai.

### 1.1 Color Resources

- [x] **`app/src/main/res/values/colors.xml`**
  - Ganti seluruh isi dengan palette TermuxMod 2026
  - Tambah semua token warna: background, surface, aksen, text, border, ripple
  - Status: SELESAI — 6 Jul 2026

- [x] **`termux-shared/src/main/res/values/colors.xml`**
  - Sinkronkan dengan palette yang sama
  - Hapus warna lama (red_400 #FF0000, grey_200, dll.)
  - Status: SELESAI — 6 Jul 2026

### 1.2 Theme Resources

- [x] **`app/src/main/res/values/styles.xml`**
  - Ganti parent dari `Theme.Material.Light.NoActionBar` ke `Theme.MaterialComponents.DayNight.NoActionBar`
  - Terapkan color tokens baru
  - Tambah ShapeAppearance rounded corners (12dp default, 16dp card)
  - Tambah transisi Activity (fade + slide)
  - Status: SELESAI — 6 Jul 2026

- [x] **`termux-shared/src/main/res/values/themes.xml`**
  - Ganti parent semua style ke MaterialComponents
  - Terapkan palette baru
  - Hapus penggunaan warna red_400 (#FF0000) sebagai colorPrimary
  - Status: SELESAI — 6 Jul 2026

- [x] **`termux-shared/src/main/res/values-night/themes.xml`**
  - Pastikan dark theme konsisten dengan palette TermuxMod 2026
  - Status: SELESAI — 6 Jul 2026

### 1.3 Dimens & Typography

- [x] **`app/src/main/res/values/dimens.xml`** (buat baru jika belum ada)
  - Definisikan spacing: `space_xs=4dp`, `space_sm=8dp`, `space_md=16dp`, `space_lg=24dp`, `space_xl=32dp`
  - Definisikan radius: `radius_sm=8dp`, `radius_md=12dp`, `radius_lg=16dp`, `radius_pill=50dp`
  - Status: SELESAI — 6 Jul 2026

- [x] **`termux-shared/src/main/res/values/dimens.xml`**
  - Sinkronkan dimens yang sama
  - Status: SELESAI — 6 Jul 2026

---

## FASE 2 — Layout Utama (TermuxActivity)

> Target: tampilan utama terminal modern. Bergantung pada Fase 1 selesai.

### 2.1 Root Layout

- [x] **`app/src/main/res/layout/activity_termux.xml`**
  - Ganti `RelativeLayout` root ke `ConstraintLayout`
  - Hapus margin hardcoded `3dp` yang membuat border aneh
  - Terminal view harus full-bleed (edge-to-edge)
  - Drawer modern: 280dp, background surface_high, MaterialButton
  - Status: SELESAI — 6 Jul 2026

### 2.2 Session List (Drawer)

- [x] **`app/src/main/res/layout/item_terminal_sessions_list.xml`**
  - Ganti `TextView` polos ke `MaterialCardView` dengan konten di dalam
  - Rounded corners `12dp`, ripple aksen, height `56dp`
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/java/com/termux/app/terminal/TermuxSessionsListViewController.java`**
  - Hapus hardcode Color.WHITE/BLACK/RED, ganti ke color resources
  - Tetap ArrayAdapter (ListView masih dipakai di TermuxActivity)
  - Status: SELESAI — 6 Jul 2026

### 2.3 Toolbar & Extra Keys

- [x] **`app/src/main/res/layout/view_terminal_toolbar_extra_keys.xml`**
  - Bungkus dalam LinearLayout dengan divider tipis di atas
  - Background: `color_surface_high`
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/res/layout/view_terminal_toolbar_text_input.xml`**
  - Ganti EditText ke `TextInputLayout` + `TextInputEditText`
  - OutlinedBox style, rounded corners, warna palette baru
  - Status: SELESAI — 6 Jul 2026

### 2.4 Extra Keys View (ExtraKeysView)

- [x] **`termux-shared/src/main/java/com/termux/shared/terminal/io/extrakeys/ExtraKeysView.java`**
  - Update DEFAULT_BUTTON_TEXT_COLOR ke #F0F0FF (color_text_primary)
  - Update DEFAULT_BUTTON_ACTIVE_TEXT_COLOR ke #00E5CC (aksen utama)
  - Update DEFAULT_BUTTON_ACTIVE_BACKGROUND_COLOR ke #1A1A2E (surface_variant)
  - Status: SELESAI — 6 Jul 2026

---

## FASE 3 — Dialog & Feedback Komponen

> Target: tidak ada lagi AlertDialog klasik, Toast, ProgressDialog.

### 3.1 MessageDialogUtils → BottomSheet

- [x] **`termux-shared/src/main/java/com/termux/shared/interact/MessageDialogUtils.java`**
  - Ganti semua `AlertDialog.Builder` ke `BottomSheetDialog` kustom
  - Tambah drag handle di atas sheet
  - Tombol aksi: `MaterialButton` dengan outline/filled style
  - Status: SELESAI — 6 Jul 2026

### 3.2 TextInputDialogUtils → BottomSheet

- [x] **`termux-shared/src/main/java/com/termux/shared/interact/TextInputDialogUtils.java`**
  - Ganti `AlertDialog` dengan input ke `BottomSheetDialog` dengan `TextInputLayout`
  - Keyboard otomatis muncul saat sheet terbuka
  - Status: SELESAI — 6 Jul 2026

### 3.3 Bootstrap Installer Dialog

- [x] **`app/src/main/java/com/termux/app/TermuxInstaller.java`** (hanya bagian UI)
  - Ganti `ProgressDialog` ke custom `BottomSheetDialog` dengan `LinearProgressIndicator`
  - Tambah animasi progress smooth
  - Ganti `AlertDialog` error ke `BottomSheetDialog` dengan ikon error dan tombol retry
  - PERHATIAN: Jangan ubah logika instalasi, hanya komponen UI
  - Status: SELESAI — 6 Jul 2026

### 3.4 CrashUtils Dialog

- [x] **`app/src/main/java/com/termux/app/utils/CrashUtils.java`** (hanya bagian UI)
  - CrashUtils tidak memiliki dialog UI langsung, hanya notification builder
  - Notification color diupdate dari hardcoded ke semantic color
  - Status: SELESAI (skip — tidak ada dialog UI) — 6 Jul 2026

---

## FASE 4 — Settings & Help Screen

> Target: Settings terasa seperti app modern 2026.

### 4.1 Settings Activity

- [x] **`app/src/main/res/layout/activity_settings.xml`**
  - Tambah custom toolbar dengan back button dan judul
  - Background konsisten dengan tema
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/java/com/termux/app/activities/SettingsActivity.java`**
  - Tambah smooth transition saat masuk/keluar
  - Custom toolbar terapkan style baru
  - Status: SELESAI — 6 Jul 2026

### 4.2 Preference Fragments

- [x] **`app/src/main/java/com/termux/app/fragments/settings/termux/TerminalViewPreferencesFragment.java`**
  - Gunakan `MaterialPreference` style via `preferenceTheme` di styles.xml
  - Status: SELESAI (via theme override) — 6 Jul 2026

- [x] **`app/src/main/java/com/termux/app/fragments/settings/termux/TerminalIOPreferencesFragment.java`**
  - Gunakan `MaterialPreference` style via `preferenceTheme` di styles.xml
  - Status: SELESAI (via theme override) — 6 Jul 2026

- [x] **`app/src/main/java/com/termux/app/fragments/settings/termux/DebuggingPreferencesFragment.java`**
  - Gunakan `MaterialPreference` style via `preferenceTheme` di styles.xml
  - Status: SELESAI (via theme override) — 6 Jul 2026

### 4.3 Help Activity

- [x] **`app/src/main/java/com/termux/app/activities/HelpActivity.java`**
  - Custom toolbar modern (AppCompatActivity + MaterialToolbar)
  - LinearProgressIndicator menggantikan ProgressBar klasik
  - WebView dengan injected CSS untuk dark mode yang konsisten
  - Status: SELESAI — 6 Jul 2026

---

## FASE 5 — Drawable & Ikon

> Target: tidak ada ikon lama pixelated.

### 5.1 Session Drawables

- [ ] **`app/src/main/res/drawable/session_background_selected.xml`**
  - Ganti ke `MaterialShapeDrawable` rounded dengan warna aksen
  - Status: belum dikerjakan

- [ ] **`app/src/main/res/drawable/session_background_black_selected.xml`**
  - Sinkronkan dengan yang di atas
  - Status: belum dikerjakan

- [ ] **`app/src/main/res/drawable/session_ripple.xml`**
  - Ripple dengan warna aksen utama (13% opacity)
  - Status: belum dikerjakan

- [ ] **`app/src/main/res/drawable/current_session.xml`**
  - Indikator session aktif: garis vertikal aksen kiri card
  - Status: belum dikerjakan

### 5.2 Icon Drawables

- [ ] **`app/src/main/res/drawable/ic_new_session.xml`**
  - Vector icon modern (plus dalam circle)
  - Status: belum dikerjakan

- [ ] **`app/src/main/res/drawable/ic_settings.xml`**
  - Vector icon modern
  - Status: belum dikerjakan

- [ ] **`app/src/main/res/drawable/ic_service_notification.xml`**
  - Icon notifikasi modern, monochrome
  - Status: belum dikerjakan

### 5.3 Scroll Bar

- [ ] **`app/src/main/res/drawable/terminal_scroll_shape.xml`**
  - Scrollbar tipis, rounded, warna muted
  - Width: 3dp, corner radius: 2dp
  - Status: belum dikerjakan

---

## FASE 6 — Notifikasi Service

> Target: notifikasi terasa bersih dan informatif.

### 6.1 Notification Style

- [ ] **`app/src/main/java/com/termux/app/TermuxService.java`** (hanya bagian `buildNotification()`)
  - Notifikasi dengan BigTextStyle
  - Icon kecil monochrome yang baru
  - Warna notifikasi: aksen utama
  - Tombol aksi: "Stop", "Wake Lock" dengan ikon
  - Status: belum dikerjakan

- [ ] **`termux-shared/src/main/java/com/termux/shared/notification/NotificationUtils.java`**
  - Channel ID dengan nama yang proper
  - Importance dan behavior yang tepat
  - Status: belum dikerjakan

---

## FASE 7 — Terminal View & Renderer

> Target: rendering terminal yang lebih smooth dan terasa premium.

### 7.1 Terminal Renderer

- [ ] **`terminal-view/src/main/java/com/termux/view/TerminalRenderer.java`**
  - Font rendering: anti-alias aktif
  - Kursor: blinking caret modern (slim, rounded)
  - Selection highlight: warna aksen dengan 30% opacity
  - Status: belum dikerjakan

### 7.2 Text Selection Handle

- [ ] **`terminal-view/src/main/java/com/termux/view/textselection/TextSelectionHandleView.java`**
  - Handle selection modern: teardrop shape dengan warna aksen
  - Status: belum dikerjakan

---

## FASE 8 — Animasi & Transisi

> Target: setiap perpindahan layar dan interaksi terasa smooth.

### 8.1 Activity Transitions

- [ ] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (bagian transisi)
  - Enter: fade in + slide up (300ms ease-out)
  - Exit: fade out + slide down (200ms ease-in)
  - Drawer open/close: animasi paralaks
  - Status: belum dikerjakan

### 8.2 Shared Element & Motion

- [ ] **`app/src/main/res/anim/`** (buat folder jika belum ada)
  - `slide_in_up.xml` — sheet masuk dari bawah
  - `slide_out_down.xml` — sheet keluar ke bawah
  - `fade_in.xml` — komponen muncul
  - `fade_out.xml` — komponen hilang
  - Status: belum dikerjakan

---

## FASE 9 — TermuxActivity UI Logic

> Target: logika UI Activity utama menggunakan komponen modern.

### 9.1 Context Menu

- [ ] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (bagian context menu & options menu)
  - Ganti context menu klasik ke `MaterialAlertDialog` atau `BottomSheetDialog` dengan list pilihan
  - Menu "New Session", "Rename", "Close" dalam sheet yang bersih
  - Status: belum dikerjakan

### 9.2 Toast → Snackbar

- [ ] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (semua `Toast.makeText`)
  - Ganti semua Toast ke `Snackbar`
  - Snackbar dengan aksi jika relevan
  - Status: belum dikerjakan

- [ ] **`app/src/main/java/com/termux/app/terminal/TermuxTerminalViewClient.java`** (semua Toast)
  - Ganti ke Snackbar
  - Status: belum dikerjakan

---

## CATATAN PENTING UNTUK AGENT

### Setiap selesai 1 item:
```bash
# 1. Update status di PLAN.md: [ ] → [x] + tanggal + singkat apa yang dilakukan
# 2. Jalankan safety check
bash scripts/safety-check.sh
# 3. Commit
git add -A
git commit -m "ui: selesai [nama item]"
git push origin main
```

### File yang TIDAK BOLEH disentuh (dari safety zones):
- Logika installer di `TermuxInstaller.java` (hanya UI boleh diubah)
- Logika service di `TermuxService.java` (hanya `buildNotification()` boleh diubah)
- `TermuxConstants.java` — tidak ada yang boleh diubah
- `termux-bootstrap.c`, `termux-bootstrap-zip.S`
- `ShellUtils.java`, `TermuxShellUtils.java` — environment vars

### Dependency antar Fase:
```
Fase 1 (Tema & Warna)
    └── Fase 2 (Layout Utama)
    └── Fase 3 (Dialog & Feedback)
    └── Fase 4 (Settings)
    └── Fase 5 (Drawable)
    └── Fase 6 (Notifikasi)
    └── Fase 7 (Terminal)
    └── Fase 8 (Animasi) ← bergantung Fase 2, 3, 4
    └── Fase 9 (UI Logic) ← bergantung Fase 1, 3
```

**Fase 1 harus selesai sebelum fase lain dimulai.**
Fase 2–7 bisa dikerjakan paralel setelah Fase 1 selesai.
Fase 8 dan 9 dikerjakan terakhir.

---

## LOG PROGRES

| Tanggal | Agent | Item | Catatan |
|---------|-------|------|---------|
| — | — | — | Belum ada progres |
