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

## Cara mencegah ke depan

Sebelum memakai widget/class Material Components baru (terutama nama-nama yang terdengar "baru"/eksperimental) di layout XML manapun di TermuxMod:
1. Cek versi `com.google.android.material:material` di `app/build.gradle`.
2. Cari tahu widget itu ditambahkan di versi berapa (biasanya lewat changelog Material Components di web).
3. Jika versi project lebih lama dari versi minimum widget tersebut → JANGAN pakai widget itu. Pakai alternatif manual (custom View + drawable, atau style lama yang sudah pasti ada), atau usulkan bump versi library ke user dengan penjelasan risikonya.
4. Ingat: CI build hijau tidak menjamin tidak ada crash runtime akibat ClassNotFoundException pada resource XML — untuk widget UI baru, verifikasi ketersediaan class di versi library, bukan cuma menunggu build sukses.
