---
name: TermuxMod Material Components version limits
description: Library com.google.android.material yang dipakai TermuxMod (1.4.0) tidak punya semua widget Material terbaru — cek versi sebelum pakai widget baru di XML.
---

## Masalah

`app/build.gradle` memakai `com.google.android.material:material:1.4.0`. Versi ini TIDAK punya beberapa widget/class yang baru ditambahkan di rilis Material Components lebih baru, contohnya `com.google.android.material.bottomsheet.BottomSheetDragHandleView` (baru ada mulai versi 1.6.0).

Kalau widget seperti ini dipakai langsung di layout XML, app **tidak gagal compile** (karena resolusi class Android terjadi saat inflate runtime, bukan saat javac), tapi **crash saat runtime** dengan:

```
android.view.InflateException: ... Error inflating class com.google.android.material.bottomsheet.BottomSheetDragHandleView
Caused by: java.lang.ClassNotFoundException: com.google.android.material.bottomsheet.BottomSheetDragHandleView
```

Ini pernah menyebabkan crash nyata di device produksi (vivo V2205, Android 14) pada layout `bottom_sheet_bootstrap_progress.xml`, yang sudah lolos build CI (GitHub Actions "Build" sukses) karena error ini tidak terdeteksi saat compile — hanya muncul saat XML benar-benar di-inflate di HP.

**Why:** Build sukses di CI TIDAK berarti UI aman dari crash — resource/class Android di-resolve secara runtime lewat reflection, compiler Java tidak memvalidasi keberadaan class widget Material di classpath resource XML.

## Solusi yang dipakai

Ganti drag handle bottom sheet dengan `View` polos + drawable custom (`shape_drag_handle.xml`, sebuah `<shape>` rounded rectangle kecil `32dp x 4dp` dengan warna `@color/color_text_secondary`), bukan bump versi Material Components. Ini dipilih karena bump versi library berisiko efek samping lain yang lebih sulit diverifikasi tanpa test di device fisik.

## Atribut XML yang TIDAK ADA di Material 1.4.0

Selain widget class yang tidak ada, beberapa **atribut XML** MaterialButton juga tidak valid di 1.4.0 dan menyebabkan **AAPT build error** (bukan runtime crash):

- `app:insetTop="0dp"` → `attribute insetTop (aka com.termux:insetTop) not found`
- `app:insetBottom="0dp"` → `attribute insetBottom (aka com.termux:insetBottom) not found`

Ini berbeda dari ClassNotFoundException — ini **gagal compile** di CI dengan pesan AAPT. Solusi: hapus kedua atribut itu; button height sudah bisa dikontrol lewat `android:layout_height` + `android:singleLine="true"`.

## Cara mencegah ke depan

Sebelum memakai widget/class/atribut Material Components baru di layout XML manapun di TermuxMod:
1. Cek versi `com.google.android.material:material` di `app/build.gradle` (saat ini 1.4.0).
2. Cari tahu widget/atribut itu ditambahkan di versi berapa.
3. Jika versi project lebih lama → JANGAN pakai. Pakai alternatif manual.
4. Atribut yang fail saat AAPT → CI build error. Widget class yang tidak ada → CI build hijau tapi crash runtime.
