# SONI CARD V1.0 (DEMO)

Барномаи Android: интерфейси бонки рақамӣ (Kotlin + Jetpack Compose + Material 3).
**Ин версия танҳо ДЕМО аст**: ҳамаи мавҷудӣ ва амалиётҳо тақлидӣ ва офлайн мебошанд. Пули воқеӣ, бонк ё API-и молиявӣ вуҷуд надорад.

Android app: digital-banking UI demo. Package `tj.soni.card`, min SDK 26, languages: Tajik (default), Russian, English.
**All money, cards and transactions are simulated. No real banking connectivity.**

---

## Роҳнамо барои навкорон (APK-ро бо GitHub месозем)

### 1. ZIP-ро боргирӣ кунед
`SONI-CARD-V1.0.zip`-ро ба телефон ё компютер захира кунед.

### 2. Кушодани ZIP
Файлро «Extract / Распаковать» кунед. Папкаи `SONI-CARD` пайдо мешавад, ки дар дохилаш `app`, `gradle`, `.github`, `README.md` ҳаст.

### 3. Репозитории GitHub созед
1. Ба github.com ворид шавед → **New repository**.
2. Ном: `SONI-CARD`. **Public** ё **Private** — ихтиёрӣ. Ҳеҷ файл илова накунед (README/.gitignore не).
3. **Create repository**.

### 4. Лоиҳаро боргузорӣ кунед
1. Дар саҳифаи репозиторий **uploading an existing file** -ро пахш кунед.
2. **Мундариҷаи** папкаи `SONI-CARD`-ро кашида ба саҳифа андозед (на худи папкаро, балки файлу папкаҳои дохили онро). Папкаи пинҳонии `.github` ҳатман бояд бошад.
3. Поёнтар **Commit changes** (шохаи `main`).

> Агар браузер папкаи `.github`-ро нишон надиҳад, Git-ро истифода баред:
> `git init && git add . && git commit -m "SONI CARD" && git branch -M main && git remote add origin <URL> && git push -u origin main`

### 5. Actions-ро кушоед
Дар репозиторий ҷадвали **Actions**-ро пахш кунед. Агар ҳисобшуда пурсад, **I understand my workflows, enable them** -ро интихоб кунед.

### 6. Build APK-ро иҷро кунед
Workflow **Android CI** худкор баъд аз боргузорӣ оғоз мешавад. Дастӣ: **Android CI → Run workflow → Run workflow**.
Сохтан тақрибан 5–12 дақиқа мегирад. Тики сабз = муваффақ; xатои сурх = матни хаторо ба мо фиристед.

### 7. SONI-CARD-APK-ро боргирӣ кунед
Дар саҳифаи иҷрои муваффақ, поёнтар **Artifacts** → **SONI-CARD-APK** -ро пахш кунед. ZIP боргирӣ мешавад; дар дохилаш `app-debug.apk`.

### 8. Насб дар Android
1. `app-debug.apk`-ро ба телефон интиқол диҳед ва кушоед.
2. Танзимот: «Allow installation from this source / Иҷозати насб аз ин манбаъ».
3. **Install** → **Open**.
4. Коди демои SMS: `123456` (дар экран низ нишон дода мешавад).

### 9. Навсозии барнома баъдтар
Файлҳоро дар репозиторий иваз кунед (ё `git push`) → Actions боз APK месозад → APK-и навро насб кунед (маълумот нигоҳ дошта мешавад, зеро номи бастаи `tj.soni.card` ва имзои debug як хел мемонад ҳангоми сохтан дар ҳамон муҳити GitHub; агар насб нашуд, барномаи кӯҳнаро нест кунед).

---

## Сохтор / Structure
```
app/src/main/java/tj/soni/card/
  MainActivity.kt           — AppCompatActivity (BiometricPrompt + per-app language)
  navigation/               — Routes, NavGraph (28 screens), auth guard
  ui/theme|components|screens
  data/local (DataStore), data/models, data/repository (DEMO in-memory data)
  domain/                   — validators, formatters, statistics
  security/                 — PBKDF2 PIN hash, Android Keystore AES-GCM, BiometricHelper
  localization/             — LocaleManager (tg / ru / en)
  viewmodel/AppViewModel.kt
app/src/test/               — unit tests (validators, stats, PIN hashing, demo repository)
```

## Security notes
- PIN is never stored: only a PBKDF2 hash with salt, additionally encrypted with an Android Keystore key.
- No real card numbers, CVV, bank passwords or SMS credentials are collected or generated. Cards show masked numbers only.
- Only permission: `USE_BIOMETRIC`. No INTERNET permission, no camera (the QR screen is a simulated DEMO viewfinder).
- Real transfers/deposits/QR payments stay disabled until authorized financial APIs and a compliant backend exist.

## Notes
- Tajik uses the Android language code `tg` (`values-tg`).
- Demo transactions reset when the app process restarts (in-memory demo repository). Settings, profile and PIN persist.
- Release builds are not signed; use the Debug APK for testing.

## Local build (optional)
Open the folder in Android Studio (JDK 17). Android Studio regenerates the official Gradle wrapper. Command line:
`./gradlew assembleDebug` and `./gradlew testDebugUnitTest`.
