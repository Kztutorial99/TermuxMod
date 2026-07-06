---
name: TermuxMod Crash Zones
description: Bagian-bagian source code TermuxMod yang TIDAK BOLEH diubah karena akan menyebabkan app crash. Wajib dibaca sebelum edit apapun di repo ini.
---

# TermuxMod — Zona Crash: Jangan Diubah

Repo: github.com/Kztutorial99/TermuxMod (fork Termux App v0.118.3)

---

## 🔴 1. applicationId & sharedUserId

**File:** `app/build.gradle`, `app/src/main/AndroidManifest.xml`

```
applicationId "com.termux"             ← JANGAN UBAH
android:sharedUserId="com.termux"      ← JANGAN UBAH
```

**Why:** Semua binary Linux (bash, python, gcc, dll.) punya path `/data/data/com.termux/files/usr` hardcoded di dalam ELF binary-nya. Ganti applicationId = semua binary tidak bisa jalan. Ganti sharedUserId = Android tolak install plugin dengan `INSTALL_FAILED_SHARED_USER_INCOMPATIBLE`.

---

## 🔴 2. TermuxConstants.java — Semua PATH

**File:** `termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java`

```java
TERMUX_PREFIX_DIR_PATH     = "/data/data/com.termux/files/usr"        ← JANGAN UBAH
TERMUX_BIN_PREFIX_DIR_PATH = "/data/data/com.termux/files/usr/bin"    ← JANGAN UBAH
TERMUX_HOME_DIR_PATH       = "/data/data/com.termux/files/home"       ← JANGAN UBAH
TERMUX_STAGING_PREFIX_DIR_PATH = "/data/data/com.termux/files/usr-staging"  ← JANGAN UBAH
TERMUX_TMP_PREFIX_DIR_PATH = "/data/data/com.termux/files/usr/tmp"    ← JANGAN UBAH
```

**Why:** Path ini tidak hanya di Java — dikompilasi ke dalam setiap binary bootstrap. Ubah satu saja: symlink rusak, interpreter shebang gagal, dynamic linker gagal. `STAGING` dipakai atomic install: kalau path salah, `renameTo()` gagal → app stuck loading selamanya.

---

## 🔴 3. JNI Native Bootstrap

**Files:**
- `app/src/main/cpp/termux-bootstrap.c`
- `app/src/main/cpp/termux-bootstrap-zip.S`
- `app/src/main/java/com/termux/app/TermuxInstaller.java`

```c
// termux-bootstrap.c
JNIEXPORT jbyteArray JNICALL
Java_com_termux_app_TermuxInstaller_getZip(JNIEnv* env, jclass clazz)
```
```java
// TermuxInstaller.java
System.loadLibrary("termux-bootstrap");   ← JANGAN UBAH
public static native byte[] getZip();     ← JANGAN UBAH
```
```asm
// termux-bootstrap-zip.S
.incbin "bootstrap.zip"    ← ZIP embedded langsung di binary
```

**Why:** `getZip()` adalah SATU-SATUNYA sumber bootstrap. Nama JNI function `Java_com_termux_app_TermuxInstaller_getZip` harus exact match package+class. Kalau package name berubah dari `com.termux.app` → nama JNI tidak match → `UnsatisfiedLinkError` → crash saat launch pertama. Kalau `loadLibrary` dihapus → instant crash.

---

## 🔴 4. TermuxInstaller.java — Urutan Bootstrap (WAJIB berurutan)

**File:** `app/src/main/java/com/termux/app/TermuxInstaller.java`

```
Urutan yang WAJIB dijaga:
1. Ekstrak zip ke STAGING dir  (bukan langsung ke PREFIX)
2. Buat semua symlinks di STAGING
3. atomic renameTo(STAGING → PREFIX)   ← kalau gagal = crash
4. Jalankan second-stage.sh
```

**Why:** Membalik urutan → prefix corrupt → setiap shell launch crash. Menghapus second-stage → `apt`/`pkg` tidak bisa jalan. Menghapus error handling → silent fail, user lihat app tapi tidak ada shell.

---

## 🔴 5. TermuxService.java — startForeground & WakeLock

**File:** `app/src/main/java/com/termux/app/TermuxService.java`

```java
// Di onCreate — JANGAN HAPUS/PINDAH
startForeground(TermuxConstants.TERMUX_APP_NOTIFICATION_ID, buildNotification());

// WakeLock lifecycle — JANGAN UBAH
mWakeLock.acquire();    // ACTION_WAKE_LOCK
mWakeLock.release();    // ACTION_WAKE_UNLOCK — harus ada di onDestroy juga
```

**Why:** Android 8+ WAJIB `startForeground()` dalam 5 detik setelah `startForegroundService()`. Terlambat/dihapus = `ForegroundServiceDidNotStartInTimeException` + ANR. WakeLock tidak di-release = Android force kill process.

---

## 🔴 6. AndroidManifest.xml — Komponen & launchMode

**File:** `app/src/main/AndroidManifest.xml`

```xml
<!-- JANGAN UBAH nama class komponen ini -->
android:name=".app.TermuxActivity"
android:launchMode="singleTask"         ← WAJIB singleTask, jangan hapus

android:name=".app.TermuxService"
android:name=".app.RunCommandService"
android:name=".filepicker.TermuxDocumentsProvider"
    android:authorities="com.termux.documents"    ← harus exact match TermuxConstants

<!-- Permissions WAJIB — jangan hapus -->
android.permission.FOREGROUND_SERVICE
android.permission.WAKE_LOCK
android.permission.INTERNET
```

**Why:** `launchMode="singleTask"` wajib — tanpa ini multiple Activity instance terbuat, `mTermuxService` binding jadi race condition → NPE crash. Plugin (Termux:Tasker, Termux:API) menggunakan explicit component name untuk bind ke `TermuxService` — kalau nama berubah → `ActivityNotFoundException`.

---

## 🔴 7. Intent Action Strings di TermuxConstants.java

**File:** `termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java`

```java
// JANGAN UBAH nilai string ini — plugin eksternal hardcode
ACTION_SERVICE_EXECUTE  = "com.termux.service_execute"
ACTION_STOP_SERVICE     = "com.termux.service_stop"
ACTION_WAKE_LOCK        = "com.termux.service_wake_lock"
ACTION_WAKE_UNLOCK      = "com.termux.service_wake_unlock"
EXTRA_ARGUMENTS         = "com.termux.execute.arguments"
EXTRA_WORKDIR           = "com.termux.execute.cwd"
EXTRA_BACKGROUND        = "com.termux.execute.background"
EXTRA_PENDING_INTENT    = "pendingIntent"    ← plain string, bukan package-prefixed
EXTRA_SESSION_ACTION    = "com.termux.execute.session_action"
```

**Why:** Semua plugin (Termux:Tasker terutama) mengirim intent dengan string ini hardcoded di APK mereka. Ubah string = intent tidak match = command tidak bisa dieksekusi. `EXTRA_PENDING_INTENT = "pendingIntent"` khusus — ini plain string, bukan package-prefixed — kalau diubah semua callback plugin gagal.

---

## 🔴 8. ExecutionCommand.java — State Machine

**File:** `termux-shared/src/main/java/com/termux/shared/models/ExecutionCommand.java`

```java
// JANGAN UBAH nilai enum atau urutannya
PRE_EXECUTION(0) → EXECUTING(1) → EXECUTED(2) → SUCCESS(3)
                                              ↘ FAILED(4)

// Aturan state machine — JANGAN UBAH logikanya:
// - State tidak bisa mundur (newState.getValue() < currentState.getValue())
// - SUCCESS adalah final state — tidak bisa diubah lagi
// - FAILED bisa di-set berkali-kali (untuk append error)
```

**Why:** Kalau ordinal berubah → state comparison rusak → command stuck → plugin tidak dapat hasil → timeout/ANR. Kalau `isPluginExecutionCommandWithPendingResult()` dihapus → PendingIntent tidak dikirim balik ke plugin → memori leak.

---

## 🔴 9. TermuxShellUtils.java — Environment Variables untuk Shell

**File:** `termux-shared/src/main/java/com/termux/shared/shell/TermuxShellUtils.java`

```java
// buildEnvironment() — JANGAN UBAH key-key ini
environment.add("HOME="    + TERMUX_HOME_DIR_PATH);        ← WAJIB
environment.add("PREFIX="  + TERMUX_PREFIX_DIR_PATH);      ← WAJIB
environment.add("PATH="    + TERMUX_BIN_PREFIX_DIR_PATH);  ← WAJIB
environment.add("TMPDIR="  + TERMUX_TMP_PREFIX_DIR_PATH);  ← WAJIB
environment.add("TERM=xterm-256color");                    ← WAJIB
environment.add("LD_LIBRARY_PATH=...");                    ← WAJIB
```

**Why:** `PATH` salah → `/bin/sh` tidak ketemu → setiap shell session crash saat launch. `LD_LIBRARY_PATH` salah → shared library tidak bisa di-load → binary crash dengan `SIGSEGV`. `TMPDIR` salah → python, gcc, dan tools lain yang butuh temp file crash.

---

## 🔴 10. ShellUtils.java — Refleksi PID (jangan diganti cara lain)

**File:** `termux-shared/src/main/java/com/termux/shared/shell/ShellUtils.java`

```java
// Satu-satunya cara dapat PID di Android — JANGAN UBAH
Field f = p.getClass().getDeclaredField("pid");
f.setAccessible(true);
return f.getInt(p);
```

**Why:** Menggunakan Java Reflection untuk akses field private `pid` di `java.lang.ProcessImpl`. Kalau dihapus → PID selalu -1 → `SIGKILL` tidak bisa dikirim ke proses yang benar → zombie processes yang tidak bisa di-kill.

---

## 🟡 BOLEH DIUBAH (dengan syarat)

| Bagian | Boleh | Syarat |
|--------|-------|--------|
| UI layout XML (warna, icon, tema) | ✅ | Jangan ubah ID resource yang direferensi di Java |
| String resources (`strings.xml`) | ✅ | Jangan hapus key yang dipakai di Java code |
| `versionName` di build.gradle | ✅ | Harus valid semver (x.y.z) |
| Notification text/icon | ✅ | Jangan ubah `TERMUX_APP_NOTIFICATION_ID` |
| Log messages (Logger.log*) | ✅ | Bebas |
| Tambah Activity/Fragment baru | ⚠️ | Jangan pakai nama class yang sudah ada |
| Tambah Permission baru | ⚠️ | Jangan hapus permission yang sudah ada |
| `minSdkVersion` | ⚠️ | Jangan naikkan > 24, jangan turunkan < 21 |
| CI/CD workflows | ⚠️ | Jangan ubah APK artifact naming pattern |

---

## ✅ ATURAN EMAS (ringkasan cepat)

1. **JANGAN ubah `applicationId = "com.termux"`** — dikompilasi ke binary Linux
2. **JANGAN ubah nama JNI function** — harus match package `com.termux.app.TermuxInstaller`
3. **JANGAN ubah urutan bootstrap** — staging → symlinks → atomic rename → second stage
4. **JANGAN ubah string Intent Actions** — plugin eksternal hardcode string ini
5. **JANGAN hapus `startForeground()`** — wajib Android 8+, tanpa ini = ANR
6. **JANGAN ubah `launchMode="singleTask"`** — tanpa ini NPE crash di service binding
7. **JANGAN ubah env vars** `PATH`, `HOME`, `PREFIX`, `LD_LIBRARY_PATH` di TermuxShellUtils
8. **JANGAN ubah enum ordinal** di `ExecutionCommand.ExecutionState`
9. **JANGAN hapus refleksi PID** di `ShellUtils.getPid()`
10. **JANGAN ubah nilai PATH constants** di `TermuxConstants.java`
