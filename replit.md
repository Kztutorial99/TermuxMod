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

## ⛔ ATURAN WAJIB SEBELUM EDIT APAPUN

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
