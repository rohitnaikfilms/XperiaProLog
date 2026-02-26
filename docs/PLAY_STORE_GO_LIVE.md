# Xperia ProLog — Play Store Go-Live Checklist

## ✅ Code Readiness (Done)

- [x] **Zero TODOs** — All `TODO` comments removed from source
- [x] **R8 / ProGuard** — `isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-rules.pro` created
- [x] **Manifest hardened** — `allowBackup=false`, `hardwareAccelerated=true`, `configChanges` prevents activity restarts mid-recording, `keepScreenOn=true`
- [x] **OpenGL ES 3.0 required** — `<uses-feature android:glEsVersion="0x00030000">` filters incompatible devices
- [x] **Camera2 + MediaCodec pipeline** — Fully wired, no mocks
- [x] **Version set** — `versionCode = 1`, `versionName = "1.0.0"`

---

## 🔑 Signing (You Must Do)

> [!CAUTION]
> Google Play **requires** a release signing key. You cannot upload a debug APK.

1. Generate a keystore (if you don't have one):
   ```bash
   keytool -genkey -v -keystore xperia-prolog-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias xperia_prolog
   ```
2. Add signing config to `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../xperia-prolog-release.jks")
           storePassword = "YOUR_STORE_PASSWORD"
           keyAlias = "xperia_prolog"
           keyPassword = "YOUR_KEY_PASSWORD"
       }
   }
   ```
3. Uncomment `signingConfig = signingConfigs.getByName("release")` in the `release` buildType
4. **Back up your `.jks` file** — if you lose it, you can never update the app on Play Store

---

## 🎨 Store Assets (You Must Create)

| Asset | Spec | Status |
|---|---|---|
| App Icon | 512 × 512 PNG | ⬜ Create and place in `mipmap-*` dirs |
| Feature Graphic | 1024 × 500 PNG | ⬜ For Play Store listing |
| Screenshots | Min 2, 16:9 or 9:16 | ⬜ Capture from Xperia 1 V |
| Short Description | Max 80 chars | ⬜ e.g. "Professional 10-bit cinema camera for Sony Xperia" |
| Full Description | Max 4000 chars | ⬜ Feature list, supported devices |

---

## 📋 Play Console Requirements

- [ ] **Privacy Policy URL** — Required because the app uses Camera and Microphone. Host a page and link it in Play Console.
- [ ] **Content Rating** — Complete the IARC questionnaire in Play Console
- [ ] **Target Audience** — Select 18+ (professional filmmaking tool)
- [ ] **App Category** — Photography → Video Players & Editors
- [ ] **Contact Email** — Required for developer profile

---

## 🧪 Pre-Upload Testing

- [ ] **Build signed APK/AAB** — `./gradlew bundleRelease` (AAB preferred by Google)
- [ ] **Install on Xperia 1 V** — Test the full flow:
  1. Grant permissions
  2. Preview renders on screen
  3. Select wide-angle lens
  4. Select Cineon Log profile
  5. Record 2 minutes
  6. Verify file saves to `Movies/XperiaProLog/VID_*_CINEON_LOG_REC2020.mp4`
  7. Open the file in DaVinci Resolve and confirm 10-bit HEVC
- [ ] **Test on non-Sony device** — App should gracefully handle missing 10-bit support
- [ ] **Crash-free cold start** — Verify no crashes when permissions are denied

---

## 🚀 Upload Sequence

1. Go to [play.google.com/console](https://play.google.com/console)
2. Create new app → "Xperia ProLog"
3. Fill in Store Listing (descriptions, screenshots, feature graphic)
4. Complete Content Rating questionnaire
5. Set up Pricing (Free or Paid)
6. Upload the signed `.aab` to **Internal Testing** first
7. Test via internal link on your Xperia 1 V
8. Promote to **Production** once validated

> [!IMPORTANT]
> Google's first review typically takes 1-3 days. Camera apps with hardware permissions sometimes get additional scrutiny — make sure your Privacy Policy explicitly states what camera/mic data is used for (local recording only, no data transmitted).
