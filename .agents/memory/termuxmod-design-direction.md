---
name: TermuxMod Design Direction
description: Arah desain dan UI/UX yang WAJIB diikuti semua agent saat memodifikasi TermuxMod. Berisi larangan dan keharusan dalam hal visual, animasi, komponen, dan gaya kode UI.
---

# TermuxMod — Design Direction

Proyek ini bukan Termux biasa. Tujuannya: tampilan **modern, smooth, premium** — jauh dari kesan Android klasik.

**Why:** User secara eksplisit melarang semua elemen UI klasik dan meminta pendekatan modern penuh untuk seluruh modifikasi UX/UI TermuxMod.

---

## DILARANG — Jangan gunakan ini sama sekali

| Kategori | Yang dilarang |
|----------|--------------|
| Komponen | `AlertDialog` default, `ProgressDialog`, `Toast`, menu overflow klasik |
| Warna | Palet Material klasik (`#3F51B5`, `#FF4081`, dll.), warna flat generik |
| Emoji | Di kode, UI, layout XML, string resources, komentar, log — di mana pun |
| Template | Boilerplate UI generik, Activity template default Android Studio |
| Icon | Drawable klasik pixelated (mdpi/hdpi lama) |
| Font | Roboto default tanpa kustomisasi |
| Pola desain | Toolbar klasik tanpa elevation/blur, list item tanpa animasi |
| Material versi lama | Material Design 1 / awal Material Design 2 |

---

## WAJIB — Gunakan pendekatan ini

| Kategori | Pendekatan |
|----------|-----------|
| Tema | Dark-first, aksen kontras dan elegan |
| Animasi | Smooth transitions, `MotionLayout`, shared element transitions |
| Dialog | `BottomSheetDialog` / `BottomSheetDialogFragment` |
| Notifikasi in-app | `Snackbar` dengan action |
| Warna | Palette custom gelap, kontras tinggi, konsisten |
| Corners | Rounded corners (`MaterialShapeDrawable`, `ShapeAppearance`) |
| Efek | Blur/glass jika API memungkinkan, subtle gradient |
| Typography | Custom typeface atau Google Fonts yang modern dan terbaca |
| Touch feedback | Ripple halus, tidak jarring |
| Konsistensi | Satu design language yang sama di semua layar |

---

## How to apply

- Setiap kali menulis atau memodifikasi layout XML → tidak ada komponen klasik
- Setiap kali menulis Java UI code → ganti Toast dengan Snackbar, ganti AlertDialog dengan BottomSheet
- Setiap kali menambah warna → gunakan palette yang sudah ada di project, bukan hardcode Material klasik
- Setiap kali menambah animasi → smooth, tidak abrupt
- Tidak ada emoji di mana pun dalam kode
