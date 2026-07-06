[x] 1. Install the required packages
[x] 2. Restart the workflow to see if the project is working
[x] 3. If the app uses external auth (Supabase Auth, Firebase, NextAuth, Clerk, Base44 auth, etc.), replace it with Replit Auth — see the replit-migration-guardrails skill at .local/secondary_skills/replit-migration-guardrails/SKILL.md. Skip if the app has no login flow.
[x] 4. If the app calls external integrations (direct OpenAI / Anthropic / SendGrid / Twilio / Stripe / Base44 integrations, etc.), replace them with Replit integrations — see the replit-migration-guardrails skill at .local/secondary_skills/replit-migration-guardrails/SKILL.md. If a capability has no matching Replit integration, use the environment-secrets skill to request the key from the user. Skip if none apply.
[x] 5. Verify the project works end-to-end: use the testing agent (see the testing skill) to exercise the main flows, then use the feedback tool to screenshot and confirm with the user
[x] 6. Inform user the import is completed and they can start building, mark the import as completed using the complete_project_import tool

---

## FASE 1 — Fondasi Tema & Warna [SELESAI — 6 Jul 2026]

[x] 1.1a app/src/main/res/values/colors.xml — Palette TermuxMod 2026 diterapkan
[x] 1.1b termux-shared/src/main/res/values/colors.xml — Disinkronkan, warna lama dihapus
[x] 1.2a app/src/main/res/values/styles.xml — Parent diganti ke MaterialComponents.DayNight.NoActionBar, ShapeAppearance & BottomSheet ditambahkan
[x] 1.2b termux-shared/src/main/res/values/themes.xml — Semua style ke MaterialComponents, red_400 dihapus
[x] 1.2c termux-shared/src/main/res/values-night/themes.xml — Dark theme konsisten dengan palette 2026
[x] 1.3a app/src/main/res/values/dimens.xml — Dibuat baru: spacing & radius tokens
[x] 1.3b termux-shared/src/main/res/values/dimens.xml — Disinkronkan dengan spacing & radius tokens

Safety check: 37/37 PASS sebelum dan sesudah edit.
