[x] 1. Install the required packages
[x] 2. Restart the workflow to see if the project is working
[x] 3. If the app uses external auth — Skip (Android app, no login flow)
[x] 4. If the app calls external integrations — Skip (tidak ada)
[x] 5. Verify the project works end-to-end — safety check 37/37 PASS
[x] 6. Import completed

---

## FASE 1 — Fondasi Tema & Warna [SELESAI — 6 Jul 2026]

[x] 1.1a app/src/main/res/values/colors.xml — Palette TermuxMod 2026
[x] 1.1b termux-shared/src/main/res/values/colors.xml — Sinkronkan, hapus lama
[x] 1.2a app/src/main/res/values/styles.xml — MaterialComponents.DayNight.NoActionBar
[x] 1.2b termux-shared/src/main/res/values/themes.xml — MaterialComponents, hapus red_400
[x] 1.2c termux-shared/src/main/res/values-night/themes.xml — Dark theme konsisten
[x] 1.3a app/src/main/res/values/dimens.xml — Dibuat baru: spacing & radius tokens
[x] 1.3b termux-shared/src/main/res/values/dimens.xml — Sinkronkan tokens

Safety check: 37/37 PASS

---

## FASE 2 — Layout Utama (TermuxActivity) [SELESAI — 6 Jul 2026]

[x] 2.1  activity_termux.xml — ConstraintLayout, hapus margin 3dp, drawer modern 280dp
[x] 2.2a item_terminal_sessions_list.xml — MaterialCardView, rounded 12dp, height 56dp
[x] 2.2b TermuxSessionsListViewController.java — Hapus hardcode Color, pakai color resources
[x] 2.3a view_terminal_toolbar_extra_keys.xml — Divider atas + background surface_high
[x] 2.3b view_terminal_toolbar_text_input.xml — TextInputLayout OutlinedBox
[x] 2.4  ExtraKeysView.java — Update default button colors ke palette 2026
[x] TermuxActivity.java — Cast RelativeLayout -> ConstraintLayout (import diperbarui)

Safety check: 37/37 PASS
