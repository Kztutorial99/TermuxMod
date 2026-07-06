---
name: TermuxMod Layout Casting Bug
description: Crash ClassCastException saat root layout XML diubah dari custom View ke wrapper layout (LinearLayout dll.), tapi Java code masih cast inflate result langsung ke custom View.
---

# TermuxMod — Layout Casting Bug Pattern

## Aturan

Jika sebuah layout XML root-nya diubah dari custom View (mis. `ExtraKeysView`) ke wrapper seperti `LinearLayout`, semua Java code yang inflate layout itu dan langsung cast hasilnya ke custom View HARUS diupdate menggunakan `findViewById()`.

**Why:** `LayoutInflater.inflate()` mengembalikan root view dari XML. Kalau root berubah dari `ExtraKeysView` ke `LinearLayout`, cast `(ExtraKeysView) layout` langsung `ClassCastException` crash.

**How to apply:**
- Setiap kali mengubah root element di layout XML → grep seluruh codebase untuk inflate layout tersebut
- Cari pola: `inflater.inflate(R.layout.<nama>, ...) lalu cast langsung`
- Ganti cast langsung ke `layout.findViewById(R.id.<id_custom_view>)`

## Kasus yang Sudah Terjadi

**File layout:** `app/src/main/res/layout/view_terminal_toolbar_extra_keys.xml`
- Fase 2 mengubah root dari `<ExtraKeysView>` ke `<LinearLayout>` (bungkus divider + ExtraKeysView)
- `TerminalToolbarViewPager.java:46` masih cast `(ExtraKeysView) layout` → crash
- Fix: `layout.findViewById(R.id.terminal_toolbar_extra_keys)`
- Crash timestamp: 2026-07-06 13:52:40 UTC

## Grep Pattern untuk Cek

```bash
# Cari semua inflate yang mungkin terdampak perubahan layout root
grep -r "inflate(R.layout\." app/src/main/java/ --include="*.java" -l
# Lalu cek masing-masing apakah ada cast langsung setelah inflate
```
