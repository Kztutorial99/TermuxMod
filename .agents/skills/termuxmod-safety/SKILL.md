---
name: termuxmod-safety
description: Wajib digunakan sebelum melakukan edit APAPUN di repo TermuxMod. Berisi daftar zona crash, aturan keamanan, arah desain modern, dan cara verifikasi. Gunakan skill ini setiap kali akan memodifikasi file di project TermuxMod (Termux App v0.118.3 fork).
---

# TermuxMod Safety — Baca Sebelum Edit Apapun

Repo ini adalah fork Android app (Termux v0.118.3). Banyak bagian yang saling terkait dengan binary Linux yang sudah dikompilasi — kesalahan kecil bisa membuat app tidak bisa build atau crash saat dijalankan.

---

## LANGKAH WAJIB SEBELUM EDIT

### 1. Baca crash zones
Buka dan baca penuh: `.agents/memory/termuxmod-crash-zones.md`
File ini berisi 10 zona kritis beserta alasan kenapa crash jika diubah.

### 2. Jalankan safety check
```bash
bash scripts/safety-check.sh
```
Semua check harus PASS sebelum mulai edit.

### 3. Edit dengan hati-hati
Hanya ubah bagian yang aman (lihat tabel di bawah).

### 4. Jalankan safety check lagi setelah edit
```bash
bash scripts/safety-check.sh
```
Jika ada yang FAIL → rollback segera dengan `git checkout -- <file>`.

---

## 10 ZONA CRASH — RINGKASAN CEPAT

| # | File | Yang Jangan Diubah | Akibat jika diubah |
|---|------|-------------------|-------------------|
| 1 | `app/build.gradle` | `applicationId "com.termux"` | Semua binary Linux tidak bisa jalan |
| 2 | `AndroidManifest.xml` | `sharedUserId`, nama komponen, `launchMode="singleTask"` | Install gagal, NPE crash |
| 3 | `TermuxConstants.java` | Semua `*_DIR_PATH` dan Intent action strings | Path corrupt, plugin tidak bisa komunikasi |
| 4 | `termux-bootstrap.c` | Nama fungsi JNI `Java_com_termux_app_TermuxInstaller_getZip` | UnsatisfiedLinkError saat launch |
| 5 | `TermuxInstaller.java` | `loadLibrary`, `native getZip()`, urutan bootstrap | Crash saat install pertama |
| 6 | `TermuxService.java` | `startForeground()`, WakeLock lifecycle | ANR / ForegroundServiceDidNotStartInTimeException |
| 7 | `TermuxShellUtils.java` | `PATH`, `HOME`, `PREFIX`, `LD_LIBRARY_PATH` env vars | Semua shell session crash |
| 8 | `ExecutionCommand.java` | Nilai enum `ExecutionState` (0–4) | Plugin tidak dapat hasil, timeout |
| 9 | `ShellUtils.java` | Refleksi field `pid` | Zombie process, tidak bisa di-kill |
| 10 | `termux-bootstrap-zip.S` | `.incbin "bootstrap.zip"` | Bootstrap ZIP hilang, app tidak bisa install |

Detail lengkap ada di `.agents/memory/termuxmod-crash-zones.md`.

---

## BAGIAN YANG AMAN DIUBAH

| Bagian | Syarat |
|--------|--------|
| UI layout XML (colors, drawables, themes) | Jangan ubah ID resource yang ada di Java |
| `strings.xml` | Jangan hapus key yang sudah ada |
| `versionName` | Format semver (x.y.z) |
| Log messages | Bebas |
| Tambah Activity/Fragment baru | Nama class baru, jangan timpa yang ada |
| Tambah Permission baru | Jangan hapus yang sudah ada |
| `README.md`, dokumentasi | Bebas |
| CI/CD workflow (non-destructive) | Jangan ubah APK artifact naming |

---

## PLAN MODERNISASI — CEK DULU SEBELUM KERJA

Sebelum mulai modifikasi UI apapun, baca **`PLAN.md`** di root project.

File ini berisi 42 item dalam 9 fase yang harus dikerjakan berurutan.
Cari item pertama yang masih `[ ]` dan lanjutkan dari sana.
Jangan kerjakan ulang item yang sudah `[x]`.

Setelah selesai 1 item:
```bash
bash scripts/safety-check.sh   # wajib
git add -A
git commit -m "ui: selesai [nama item]"
# push via GitHub API curl (BUKAN git push langsung)
```

### WAJIB setelah setiap push — Pantau Build:
```bash
# Cek sampai semua status BUKAN in_progress
curl -s -H "Authorization: token $GITHUB_TOKEN" \
  "https://api.github.com/repos/Kztutorial99/TermuxMod/actions/runs?per_page=5" \
  | jq '[.workflow_runs[] | {name:.name, status:.status, conclusion:.conclusion, commit:.head_sha[0:7]}]'
```
- Jika ada `"conclusion": "failure"` → ambil log, analisis, fix, push ulang
- **Jangan lanjut item PLAN berikutnya sebelum semua build success**
- Build Android butuh 5-10 menit, pantau minimal 2x polling

---

## STRUKTUR MODUL

```
app/                    ← Aplikasi utama (UI + logic)
terminal-emulator/      ← Library VT100/xterm emulator
terminal-view/          ← Android View render terminal
termux-shared/          ← Constants, utils, shell, models
backups/
  original-source/      ← Source asli Termux v0.118.3 (jangan edit)
  agent-memory/         ← Backup memory agent
```

---

## ARAH DESAIN — WAJIB DIIKUTI

Baca detail lengkap di `.agents/memory/termuxmod-design-direction.md`.

Ringkasan cepat:

**DILARANG:**
- Emoji di mana pun (kode, layout XML, strings.xml, komentar, log)
- Material Design klasik, template UI generik, warna flat default Android
- `AlertDialog` default, `ProgressDialog`, `Toast`
- Icon lama pixelated, Roboto default tanpa kustomisasi

**WAJIB:**
- Tema dark-first, aksen kontras dan elegan
- `BottomSheetDialog` ganti AlertDialog
- `Snackbar` ganti Toast
- Animasi smooth (`MotionLayout`, shared element transitions)
- Rounded corners, tipografi bersih, ripple halus
- Konsistensi visual di semua layar

---

## JIKA TIDAK YAKIN

Bandingkan file yang akan diedit dengan versi aslinya:
```bash
diff <file_yang_akan_diedit> backups/original-source/<path_yang_sama>
```

Jika ada perbedaan yang tidak disengaja → kembalikan ke versi backup.
