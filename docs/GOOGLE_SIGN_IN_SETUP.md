# Google Sign-In Setup (TermuxMod)

The account screens (`LoginActivity` + `ProfileActivity`) use the Google Sign-In
API from `com.google.android.gms:play-services-auth`. To make sign-in work on a
build you have to register the app's signing certificate in Google Cloud.

## 1. Get the SHA-1 of your signing key

Debug builds use `app/dev_keystore.jks`:

```bash
keytool -list -v -keystore app/dev_keystore.jks -alias alias -storepass xrj45yWGLbsO7W0v
```

Copy the `SHA1:` value. Repeat for your release keystore.

## 2. Create the OAuth clients

1. Open <https://console.cloud.google.com/apis/credentials> and select (or create) a project.
2. Configure the OAuth consent screen (External, add your Google account as a test user).
3. Create credentials → OAuth client ID → **Android**:
   - Package name: `com.termux`
   - SHA-1: the fingerprint from step 1 (add one client per keystore).
4. Create credentials → OAuth client ID → **Web application**. Copy its client id.

## 3. Put the web client id in the app

Edit `app/src/main/res/values/strings_account.xml`:

```xml
<string name="google_oauth_web_client_id" translatable="false">1234567890-xxxxxxxx.apps.googleusercontent.com</string>
```

Leaving it empty still allows sign-in (name, email and photo work), but no
ID token is requested, so the account cannot later be verified by a backend.

## 4. Build and test

```bash
./gradlew assembleDebug
```

Open **Settings → Akun** in the app, tap *Lanjut dengan Google*, pick an account,
and the profile screen appears.

## What is stored

Everything stays on the device in the private `termux_account` SharedPreferences
file: Google account id, name, email, photo URL, plus the editable username and
bio. Signing out clears that file and revokes the local Google session.

Common errors:

- status code `10` (DEVELOPER_ERROR) → SHA-1 / package name mismatch in Google Cloud.
- status code `12501` → the user cancelled the account picker.
- status code `7` → no network connection.
