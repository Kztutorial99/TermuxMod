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

- Total item: 62
- Selesai: 33
- Sisa: 29
- Total fase: 20 (Fase 1-9 fondasi awal, Fase 10-20 perluasan rombak TermuxModern 2026)

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

- [x] **`app/src/main/res/drawable/session_background_selected.xml`**
  - Selector tetap sama (referensi ke current_session + session_ripple yang sudah modern)
  - Status: SELESAI (tidak perlu ubah) — 6 Jul 2026

- [x] **`app/src/main/res/drawable/session_background_black_selected.xml`**
  - Selector tetap sama (referensi ke current_session_black + session_ripple_black yang sudah modern)
  - Status: SELESAI (tidak perlu ubah) — 6 Jul 2026

- [x] **`app/src/main/res/drawable/session_ripple.xml`**
  - Ripple cyan #00E5CC 13% opacity + background dark #131320
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/res/drawable/current_session.xml`**
  - layer-list: background cyan 8% + garis vertikal aksen 3dp kiri
  - Status: SELESAI — 6 Jul 2026

### 5.2 Icon Drawables

- [x] **`app/src/main/res/drawable/ic_new_session.xml`**
  - Vector plus-in-square modern dua warna (outline #E8E8F0, plus #00E5CC)
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/res/drawable/ic_settings.xml`**
  - Hapus android:tint hitam, fillColor text-primary #E8E8F0
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/res/drawable/ic_service_notification.xml`**
  - Clean up whitespace, tetap monochrome putih
  - Status: SELESAI — 6 Jul 2026

### 5.3 Scroll Bar

- [x] **`app/src/main/res/drawable/terminal_scroll_shape.xml`**
  - Width: 3dp, corner radius: 2dp, warna muted #4D7B8899
  - Status: SELESAI — 6 Jul 2026

---

## FASE 6 — Notifikasi Service

> Target: notifikasi terasa bersih dan informatif.

### 6.1 Notification Style

- [x] **`app/src/main/java/com/termux/app/TermuxService.java`** (hanya bagian `buildNotification()`)
  - Notifikasi dengan BigTextStyle
  - Icon kecil monochrome yang baru (ic_service_notification, ic_notification_stop, ic_notification_wake_lock, ic_notification_wake_unlock)
  - Warna notifikasi: aksen utama #00E5CC
  - Tombol aksi: "Stop", "Wake Lock" dengan ikon + FLAG_IMMUTABLE Android 12+
  - Status: SELESAI — 6 Jul 2026

- [x] **`termux-shared/src/main/java/com/termux/shared/notification/NotificationUtils.java`**
  - Channel ID dengan nama yang proper, description, showBadge false
  - Importance dan behavior yang tepat (vibration off, sound null, lockscreen visibility public)
  - Status: SELESAI — 6 Jul 2026

---

## FASE 7 — Terminal View & Renderer

> Target: rendering terminal yang lebih smooth dan terasa premium.

### 7.1 Terminal Renderer

- [x] **`terminal-view/src/main/java/com/termux/view/TerminalRenderer.java`**
  - Font rendering: anti-alias aktif
  - Kursor: BAR slim ~2dp kiri, UNDERLINE slim ~2dp bawah (modern, tidak block penuh)
  - Selection highlight: warna aksen cyan #00E5CC dengan 30% opacity (0x4D)
  - Status: SELESAI — 6 Jul 2026

### 7.2 Text Selection Handle

- [x] **`terminal-view/src/main/java/com/termux/view/textselection/TextSelectionHandleView.java`**
  - Handle selection modern: warna diganti dari #2196F3 (Material biru) ke aksen #00E5CC
  - Diterapkan di drawable `text_select_handle_left_material.xml` dan `text_select_handle_right_material.xml`
  - Status: SELESAI — 6 Jul 2026

---

## FASE 8 — Animasi & Transisi

> Target: setiap perpindahan layar dan interaksi terasa smooth.

### 8.1 Activity Transitions

- [x] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (bagian transisi)
  - Enter: `overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)` di `onCreate()` (300ms ease-out)
  - Exit: `overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)` di `finishActivityIfNotFinishing()` (200ms ease-in)
  - Drawer open/close: `setDrawerParallaxEffect()` — terminal content bergeser translationX mengikuti slideOffset drawer (parallaxFactor 0.25)
  - Status: SELESAI — 6 Jul 2026

### 8.2 Shared Element & Motion

- [x] **`app/src/main/res/anim/`** (sudah ada)
  - `slide_in_up.xml` — translate Y 100%->0% 300ms decelerate_cubic + alpha fade in
  - `slide_out_down.xml` — translate Y 0%->100% 250ms accelerate_cubic + alpha fade out
  - `fade_in.xml` — alpha 0->1 300ms decelerate_quad
  - `fade_out.xml` — alpha 1->0 200ms accelerate_quad
  - Status: SELESAI — 6 Jul 2026 (sudah dibuat sebelumnya, sekarang digunakan di TermuxActivity)

---

## FASE 9 — TermuxActivity UI Logic

> Target: logika UI Activity utama menggunakan komponen modern.

### 9.1 Context Menu

- [x] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (bagian context menu & options menu)
  - Ganti context menu klasik (floating `ContextMenu`) ke `BottomSheetDialog` kustom dengan list pilihan (`bottom_sheet_context_menu.xml` + `item_context_menu_option.xml`)
  - Semua 12 opsi menu lama (Select URL, Share Transcript, Share Selected Text, Autofill, Reset Terminal, Kill Process, Styling, Toggle Keep Screen On, Help, Settings, Report Issue) dipindah ke sheet, termasuk state disabled (Kill Process) dan checked (Toggle Keep Screen On, pakai `ic_check_accent`)
  - Logika `onContextItemSelected` diekstrak ke `handleContextMenuAction()` agar dipakai bareng oleh sheet & fallback
  - Status: SELESAI — 6 Jul 2026

### 9.2 Toast → Snackbar

- [x] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (semua `Toast.makeText`/`Logger.showToast`)
  - `showToast()` di TermuxActivity sudah pakai Snackbar; satu pemanggilan tersisa (`toggleTerminalToolbar`) yang masih lewat `Logger.showToast` (Toast) diubah ke `showToast()` (Snackbar)
  - Status: SELESAI — 6 Jul 2026

- [x] **`app/src/main/java/com/termux/app/terminal/TermuxTerminalViewClient.java`** (semua Toast)
  - `Logger.showToast(mActivity, ...)` di `reportIssueFromTranscript()` diganti ke `mActivity.showToast(...)` (Snackbar); import `android.widget.Toast` yang sudah tidak terpakai dihapus
  - Status: SELESAI — 6 Jul 2026

---

## FASE 10 — Keyboard & Extra Keys Modernization

> Target: baris extra keys dan interaksi keyboard terasa premium, responsif, dengan feedback haptic dan visual modern.

### 10.1 Extra Keys Button Feedback

- [ ] **`termux-shared/src/main/java/com/termux/shared/terminal/io/extrakeys/ExtraKeysView.java`** (bagian pembuatan button/key)
  - Tambah `performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)` saat key ditekan
  - Tambah animasi scale kecil (0.95x) saat pressed, kembali ke 1x saat release
  - Status: belum dikerjakan

- [ ] **`termux-shared/src/main/java/com/termux/shared/terminal/io/extrakeys/ExtraKeysView.java`** (bagian layout row)
  - Smooth horizontal scroll untuk baris extra keys yang overflow (nested scroll behavior)
  - Spacing antar key konsisten dengan token `space_xs`/`space_sm` dari dimens.xml
  - Status: belum dikerjakan

### 10.2 Soft Keyboard Transition

- [ ] **`app/src/main/java/com/termux/app/terminal/TermuxTerminalViewClient.java`** (bagian toggle soft keyboard)
  - Animasi smooth saat keyboard show/hide menggunakan `WindowInsetsAnimation` (API 30+) dengan fallback aman di bawahnya
  - Status: belum dikerjakan

---

## FASE 11 — File Picker & Document Provider UI

> Target: pengalaman memilih dan membagikan file terasa modern, bukan dialog sistem polos tanpa konteks.

### 11.1 Document Provider Metadata

- [ ] **`app/src/main/java/com/termux/filepicker/TermuxDocumentsProvider.java`** (hanya bagian metadata tampilan/icon MIME, bukan logic path/akses file)
  - Lengkapi mapping icon berdasarkan tipe file (kode, gambar, arsip, teks) agar file picker sistem menampilkan ikon yang relevan
  - Status: belum dikerjakan

### 11.2 Share/Open With Bottom Sheet

- [ ] **File baru:** `app/src/main/java/com/termux/app/file/ShareOptionsBottomSheet.java` + layout `bottom_sheet_share_options.xml`
  - BottomSheet kustom untuk aksi "Share output" / "Open with" dari hasil command, menggantikan chooser sistem polos jika relevan
  - Tidak mengubah class `TermuxDocumentsProvider` yang sudah ada, hanya menambah pemanggil opsional
  - Status: belum dikerjakan

---

## FASE 12 — Styling & Theme Picker

> Target: alur pemilihan font/skema warna terminal terasa seperti theme gallery modern 2026, bukan sekadar redirect ke app lain.

### 12.1 Termux:Styling Entry Point

- [ ] **`app/src/main/java/com/termux/app/TermuxActivity.java`** (bagian pengecekan Termux:Styling terpasang/tidak)
  - Tambah kartu preview modern (MaterialCardView) sebelum redirect ke Termux:Styling atau F-Droid
  - Status: belum dikerjakan

### 12.2 In-App Color Scheme Preview

- [ ] **`app/src/main/java/com/termux/app/fragments/settings/termux/TerminalViewPreferencesFragment.java`**
  - Tambah preview swatch warna (background/foreground/cursor aktif) di bagian atas fragment sebelum daftar preference
  - Status: belum dikerjakan

---

## FASE 13 — Plugin & Background Task Feedback

> Target: eksekusi command lewat plugin (RunCommandService, Termux:Tasker) memberi feedback visual yang konsisten dengan desain 2026, bukan Toast/notifikasi polos.

### 13.1 RunCommandService Feedback

- [ ] **`app/src/main/java/com/termux/app/RunCommandService.java`** (hanya bagian feedback error ke user, bukan logic eksekusi command)
  - Ganti feedback error dari Toast/notifikasi default ke notifikasi bergaya sama seperti Fase 6 (BigTextStyle, warna aksen)
  - Status: belum dikerjakan

### 13.2 Plugin Error Notification Consistency

- [ ] **`termux-shared/src/main/java/com/termux/shared/termux/plugins/PluginUtils.java`** (hanya bagian pembuatan notifikasi error, bukan logic pengiriman PendingIntent)
  - Pastikan channel ID, warna, dan style notifikasi error plugin konsisten dengan `NotificationUtils` hasil Fase 6
  - Status: belum dikerjakan

---

## FASE 14 — Splash Screen & App Identity

> Target: kesan pertama membuka TermuxMod terasa modern sejak ikon dan splash, bukan layar putih polos bawaan Android lama.

### 14.1 Adaptive Icon Modern

- [ ] **`app/src/main/res/mipmap-anydpi-v26/`** dan drawable terkait (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`)
  - Adaptive icon dengan aksen palette TermuxMod 2026 (cyan-teal/violet di atas dasar gelap)
  - Status: belum dikerjakan

### 14.2 Splash Theme Tanpa Flicker Putih

- [ ] **`app/src/main/res/values/styles.xml`** (tambah splash theme, gunakan `androidx.core.splashscreen` bila kompatibel dengan minSdk 21)
  - Background splash gelap `#0A0A0F` dengan logo di tengah, tanpa flicker putih saat cold start
  - Status: belum dikerjakan

---

## FASE 15 — Onboarding / First-Run Experience

> Target: pengguna baru disambut walkthrough singkat modern, bukan langsung dilempar ke shell kosong tanpa konteks.

### 15.1 First Run Bottom Sheet

- [ ] **File baru:** `app/src/main/java/com/termux/app/onboarding/FirstRunBottomSheet.java` + layout terkait
  - BottomSheet 2-3 halaman (ViewPager2 + dots indicator) menjelaskan sesi, extra keys, dan settings
  - Trigger sekali via flag SharedPreferences baru (tidak mengubah flag/preference yang sudah ada)
  - Status: belum dikerjakan

---

## FASE 16 — Terminal Search Overlay

> Target: pencarian teks di scrollback terminal tersedia lewat overlay modern yang melayang di atas terminal.

### 16.1 Search Overlay Component

- [ ] **File baru:** `app/src/main/java/com/termux/app/terminal/TerminalSearchOverlay.java` + layout `view_terminal_search_overlay.xml`
  - Floating search bar di atas terminal dengan highlight hasil pencarian memakai warna aksen
  - Tombol next/prev/close dengan ripple halus, muncul/hilang dengan animasi fade dari Fase 8
  - Status: belum dikerjakan

---

## FASE 17 — Multi-Window & Orientation Polish

> Target: TermuxMod tetap terasa modern dan tidak pecah layout saat split-screen, foldable, atau rotasi.

### 17.1 Responsive Drawer & Margin

- [ ] **`app/src/main/res/layout/activity_termux.xml`** + qualifier baru (`layout-land/`, `layout-sw600dp/` dibuat bila belum ada)
  - Lebar drawer adaptif (280dp portrait penuh, menyempit proporsional saat split-screen sempit)
  - Status: belum dikerjakan

---

## FASE 18 — Accessibility Pass

> Target: semua komponen yang sudah dimodernisasi (Fase 1-9) punya content description dan kontras yang layak (WCAG AA minimum).

### 18.1 Content Description Audit

- [ ] Audit seluruh `ImageButton`/`ImageView` interaktif hasil Fase 2, 3, 5, 6 (drawer, dialog, notifikasi, session list)
  - Tambah `android:contentDescription` yang hilang, gunakan string resource baru bila perlu (jangan hapus key lama)
  - Status: belum dikerjakan

### 18.2 Contrast Verification

- [ ] Verifikasi rasio kontras teks vs background sesuai palette TermuxMod 2026 (`color_text_secondary` vs `color_surface`, dll.)
  - Dokumentasikan hasil pengukuran di `.agents/memory/termuxmod-design-direction.md`
  - Status: belum dikerjakan

---

## FASE 19 — Performance & Rendering Polish

> Target: seluruh animasi dan efek modern (Fase 7, 8) tidak menurunkan performa scroll/render terminal, terutama di device low-end.

### 19.1 Terminal Render Guard

- [ ] **`terminal-view/src/main/java/com/termux/view/TerminalView.java`** (bagian invalidate/redraw, bukan logic emulator)
  - Pastikan parallax drawer dan animasi baru tidak memicu overdraw berlebih pada TerminalView (gunakan hardware layer sementara saat animasi berjalan bila perlu)
  - Status: belum dikerjakan

### 19.2 Reduce Motion Preference

- [ ] **`app/src/main/java/com/termux/app/fragments/settings/termux/TerminalViewPreferencesFragment.java`**
  - Tambah preference baru "Reduce animations" agar user device low-end bisa menonaktifkan animasi berat (parallax, transisi slide)
  - Status: belum dikerjakan

---

## FASE 20 — Final QA & Consistency Audit

> Target: seluruh 19 fase sebelumnya konsisten satu sama lain, tidak ada elemen UI klasik tersisa. Fase penutup rombak TermuxModern 2026.

### 20.1 Full Visual Audit

- [ ] Review manual seluruh layar (Main, Settings, Help, Session list, Dialog, Notifikasi, Search overlay, Onboarding)
  - Pastikan tidak ada `AlertDialog`/`ProgressDialog`/`Toast`/warna Material klasik tersisa di mana pun
  - Catat temuan dan perbaikan di LOG PROGRES
  - Status: belum dikerjakan

### 20.2 Safety & Regression Final Check

- [ ] Jalankan `bash scripts/safety-check.sh` full pass setelah semua fase selesai
  - Build APK debug via CI, lakukan smoke test dasar (buka shell, jalankan command, buka drawer, buka settings, buka search overlay)
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
        └── Fase 10 (Keyboard & Extra Keys) ← bergantung Fase 2
        └── Fase 11 (File Picker) ← bergantung Fase 3
        └── Fase 12 (Styling & Theme Picker) ← bergantung Fase 4
        └── Fase 13 (Plugin Feedback) ← bergantung Fase 6
        └── Fase 14 (Splash & App Identity) ← bergantung Fase 1
        └── Fase 15 (Onboarding) ← bergantung Fase 3, 8
        └── Fase 16 (Search Overlay) ← bergantung Fase 7, 8
        └── Fase 17 (Multi-Window) ← bergantung Fase 2
        └── Fase 18 (Accessibility) ← bergantung Fase 2, 3, 5, 6
        └── Fase 19 (Performance) ← bergantung Fase 7, 8
        └── Fase 20 (QA Final) ← bergantung SEMUA fase 1-19
```

**Fase 1 harus selesai sebelum fase lain dimulai.**
Fase 2–7 bisa dikerjakan paralel setelah Fase 1 selesai.
Fase 8 dan 9 menutup gelombang pertama modernisasi (fondasi).
Fase 10–19 adalah gelombang kedua (perluasan rombak TermuxModern 2026) — bisa dikerjakan paralel sesuai dependency masing-masing di atas.
Fase 20 WAJIB dikerjakan paling akhir sebagai audit penutup setelah semua fase lain selesai.

---

## LOG PROGRES

| Tanggal | Agent | Item | Catatan |
|---------|-------|------|---------|
| 6 Jul 2026 | agent | Fase 6 & 7 | Sinkronisasi PLAN.md — checkbox belum ter-update walau kode sudah selesai (commit sebelumnya: f534be4, 5d49d5c). Status keseluruhan diperbaiki jadi Selesai: 28, Sisa: 14 |
| 6 Jul 2026 | agent | Fase 8.1 & 8.2 | Tambah overridePendingTransition enter/exit di TermuxActivity, drawer parallax listener (setDrawerParallaxEffect). File anim/ sudah ada sebelumnya, sekarang benar-benar dipakai. Status: Selesai 30, Sisa 12 |
| 6 Jul 2026 | agent | Perluasan PLAN | Tambah Fase 10-20 (Keyboard/Extra Keys, File Picker, Styling Picker, Plugin Feedback, Splash/App Identity, Onboarding, Search Overlay, Multi-Window, Accessibility, Performance, QA Final) — 20 item baru, dependency graph & status total diperbarui. Total item: 62, Selesai: 30, Sisa: 32. Belum ada kode yang diubah, murni dokumentasi/planning |
| 6 Jul 2026 | agent | Fase 9.1 & 9.2 | Context menu terminal diganti dari floating ContextMenu klasik ke BottomSheetDialog kustom (layout baru: bottom_sheet_context_menu.xml, item_context_menu_option.xml, ic_check_accent.xml). Toast tersisa (Logger.showToast) di TermuxActivity.java dan TermuxTerminalViewClient.java diganti ke showToast() berbasis Snackbar. Build Actions sukses (commit b2ec028). Status: Selesai 33, Sisa 29 |
