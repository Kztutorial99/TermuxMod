---
name: TermuxMod ActionBar Conflict Crash
description: Crash IllegalStateException saat Activity pakai theme dengan built-in action bar tapi juga memanggil setSupportActionBar(toolbar).
---

# ActionBar Conflict Crash — IllegalStateException

## Aturan

Setiap Activity yang memanggil `setSupportActionBar(toolbar)` **WAJIB** menggunakan theme dengan parent `NoActionBar`. Jika theme Activity menyediakan built-in window action bar dan kode juga memanggil `setSupportActionBar()`, app crash saat Activity dibuka.

**Why:** Android/AppCompat tidak mengizinkan dua action bar sekaligus — satu dari window decor (theme) dan satu dari Toolbar yang di-set manual.

**How to apply:**
- Di `AndroidManifest.xml`, pastikan setiap Activity yang pakai `setSupportActionBar()` menggunakan `android:theme="@style/Theme.Termux"` (bukan `Theme.AppCompat.Light.DarkActionBar`, `Theme.Material.Light.DarkActionBar`, atau varian lain yang punya built-in action bar).
- `Theme.Termux` sudah extend `Theme.MaterialComponents.DayNight.NoActionBar` — aman dipakai bersama `setSupportActionBar()`.
- Tema yang BERBAHAYA (menyebabkan crash jika dipakai di Activity yang call `setSupportActionBar`):
  - `@style/Theme.AppCompat.Light.DarkActionBar`
  - `@android:style/Theme.Material.Light.DarkActionBar`
  - Semua varian `AppCompat` atau `Material` tanpa suffix `NoActionBar`

## Crash yang sudah difix

- `SettingsActivity` — crash saat buka Settings. Fix: ganti theme ke `Theme.Termux` di Manifest.
- `HelpActivity` — pola sama, potensi crash yang sama. Fix: ganti theme ke `Theme.Termux` di Manifest.
