# Backups TermuxMod

## 📁 Isi Folder Ini

### `agent-memory/`
Backup file memory agent yang berisi analisis lengkap zona crash TermuxMod.
- `MEMORY.md` — index pointer
- `termuxmod-crash-zones.md` — **10 zona kritis yang TIDAK BOLEH diubah** agar app tidak crash

---

## 🌿 Backup Branches (di GitHub)

Selain folder ini, terdapat juga backup dalam bentuk Git branch:

| Branch | Isi |
|--------|-----|
| `backup/original-source` | Source asli Termux App v0.118.3 murni (sebelum modifikasi apapun) |
| `backup/agent-memory` | Snapshot branch dengan memory agent |

Untuk akses backup source asli:
```bash
git checkout backup/original-source
```

---

## ⚠️ Wajib Baca Sebelum Edit

Baca `agent-memory/termuxmod-crash-zones.md` sebelum melakukan modifikasi apapun.
File ini berisi 10 bagian source code yang TIDAK BOLEH diubah karena akan menyebabkan app crash.
