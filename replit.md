# TermuxMod — Project Overview

Fork dari Termux App v0.118.3 untuk tujuan modifikasi.
Repo GitHub: https://github.com/Kztutorial99/TermuxMod

## Stack
- Android (Java, Gradle, NDK/JNI)
- Multi-module: `app/`, `terminal-emulator/`, `terminal-view/`, `termux-shared/`
- Min SDK: 21, Target SDK: lihat `gradle.properties`

## User Preferences
- Bahasa komunikasi: Bahasa Indonesia
- Selalu push ke GitHub setelah perubahan signifikan
- Selalu jalankan safety-check sebelum dan sesudah edit apapun
- JANGAN propose/usulkan follow-up tasks di akhir setiap sesi — fitur ini dimatikan
- **WAJIB setelah setiap push:** pantau GitHub Actions sampai semua workflow selesai (status bukan `in_progress`). Jika ada `failure` → analisis log, fix segera, push ulang. Jangan lanjut ke item PLAN berikutnya sebelum build hijau semua.

## Arah Desain — WAJIB DIIKUTI SEMUA AGENT

Proyek ini bertujuan membuat TermuxMod dengan tampilan **modern & smooth**.

### DILARANG keras menggunakan:
- Material Design klasik (warna-warna flat default Android lama)
- Template klasik / boilerplate UI generik
- Emoji di dalam kode, UI, string resources, layout, atau komentar
- Pola UI kuno: AlertDialog default, ProgressDialog, Toast biasa, menu overflow klasik
- Warna hardcoded seperti `#FF4081`, `#3F51B5`, atau palet Material klasik
- Icon set lama (mdpi/hdpi drawable klasik yang pixelated)
- Font default Android (Roboto standar tanpa kustomisasi)

### WAJIB menggunakan pendekatan:
- Modern Android UI: smooth animation, custom transitions, fluid motion
- Palette gelap / dark-first dengan aksen yang kontras dan elegan
- Typography yang bersih dan terbaca
- Rounded corners, blur/glass efek jika memungkinkan
- Bottom sheet menggantikan dialog klasik
- Snackbar menggantikan Toast
- Ripple effect yang halus, bukan jarring
- Konsistensi visual di seluruh layar

---

## ATURAN WAJIB SEBELUM EDIT APAPUN

**BACA DULU:** `.agents/memory/termuxmod-crash-zones.md` dan `.agents/skills/termuxmod-safety/SKILL.md`

### Jangan pernah ubah:
1. `applicationId "com.termux"` di `app/build.gradle`
2. `android:sharedUserId` di `AndroidManifest.xml`
3. Semua PATH di `TermuxConstants.java` (`TERMUX_PREFIX_DIR_PATH`, `TERMUX_HOME_DIR_PATH`, dll.)
4. Nama JNI function `Java_com_termux_app_TermuxInstaller_getZip` di `termux-bootstrap.c`
5. `System.loadLibrary("termux-bootstrap")` dan `native byte[] getZip()` di `TermuxInstaller.java`
6. Urutan langkah di bootstrap installer (staging → symlinks → rename → second-stage)
7. `startForeground()` di `TermuxService.java`
8. `android:launchMode="singleTask"` di `AndroidManifest.xml`
9. Semua string Intent Actions di `TermuxConstants.java` (misal `"com.termux.service_execute"`)
10. Enum ordinal `ExecutionState` di `ExecutionCommand.java`
11. Environment vars `PATH`, `HOME`, `PREFIX`, `LD_LIBRARY_PATH` di `TermuxShellUtils.java`
12. Refleksi PID di `ShellUtils.getPid()`

### Sebelum setiap edit:
```bash
bash scripts/safety-check.sh
```
Jika ada yang FAIL → jangan lanjutkan edit.

### Setelah setiap edit:
```bash
bash scripts/safety-check.sh
```
Jika ada yang FAIL → rollback perubahan segera.
