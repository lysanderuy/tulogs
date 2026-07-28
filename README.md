# TuLogs

TuLogs is an Android sleep-tracking alarm clock that uses **NFC tags** instead of buttons to record when you actually go to bed and when you actually get up.

Instead of tapping "I'm asleep" on a screen (which you can lie about, or forget), you place two NFC stickers/tags in your home — one by your bed, one across the room (or wherever forces you to get up) — and TuLogs listens for scans:

- **Scan the bedtime tag** → starts a sleep session and begins tracking screen-off time (a proxy for when you actually fell asleep).
- **Scan the wake tag** → ends the session, stops any ringing alarm, and — if you woke up on your own before the alarm fired — cancels the rest of today's alarms automatically.

This turns "getting out of bed to silence the alarm" into the mechanism that both stops the alarm **and** logs the data, so the sleep log stays honest without extra effort.

## Product overview

| Feature | Description |
|---|---|
| **Alarms** | Create one-off or repeating (day-of-week) alarms. Alarms ring through a full-screen ringing activity even when the phone is locked. |
| **NFC tag dismiss** | An alarm only stops when the registered **wake** tag is scanned. Scanning the wrong tag is detected and reflected in the ringing UI. |
| **Sleep sessions** | Scanning the **bedtime** tag starts a session; a foreground service watches screen-off/screen-on events to estimate time-to-fall-asleep. Scanning the **wake** tag ends it. |
| **Sleep log / history** | Past sessions (bedtime, wake time, time asleep) are viewable on a weekly view. |
| **Tag registration** | A dedicated screen to register which physical NFC tag acts as "bedtime" and which acts as "wake," with scan-to-confirm feedback. |
| **Accounts** | Firebase Authentication (email/password) scopes alarms, tags, and sleep logs per user. |
| **Boot resilience** | Scheduled alarms are restored after a device reboot. |

### Typical flow

1. Sign in (Firebase Auth).
2. Register a **bedtime** tag and a **wake** tag under Tags.
3. Set an alarm under Alarms.
4. At night, tap the bedtime tag → sleep session starts, screen tracking begins.
5. Alarm rings at the scheduled time (full-screen, even locked) — or you wake up early.
6. Tap the wake tag → alarm/session stops; if it was before the alarm, remaining alarms for today are cancelled.
7. Review nights on the Week screen.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3) for UI, single-activity navigation via `androidx-navigation-compose`
- **Hilt** for dependency injection
- **Room** for local persistence (alarms, sleep logs, sleep tags), with schema export for migration testing
- **DataStore Preferences** for lightweight settings
- **Firebase Auth** for user accounts
- **AlarmManager** (exact alarms) + `BroadcastReceiver`/foreground `Service` for reliable alarm firing and ringing
- **NFC (`Ndef`/tag dispatch)** for bedtime/wake tag scanning, both foreground (`enableForegroundDispatch`) and cold-start (`NfcDispatchActivity` launched by the system's `TAG_DISCOVERED` intent filter)
- Min SDK 26, target/compile SDK 35, Java/Kotlin 17

## Project structure

```
app/src/main/java/com/lysanderuy/tulogs/
├── MainActivity.kt            # Single-activity host: nav graph, auth gating, NFC intent handling
├── TuLogsApplication.kt       # Hilt application entry point
│
├── alarm/                     # Alarm scheduling & ringing
│   ├── Alarm scheduling        (AlarmScheduler, AlarmReceiver, BootReceiver)
│   ├── Ringing UI/service       (RingingActivity, AlarmRingingService)
│   ├── AlarmOccurrence.kt      # Computed "next fire" occurrence for an Alarm
│   └── ScreenTrackingService.kt # Foreground service tracking screen on/off during a sleep session
│
├── nfc/                       # NFC tag scanning & dispatch
│   ├── NfcForegroundDispatcher.kt / NfcTagReader.kt  # Reading UID from a scanned tag
│   ├── NfcDispatchActivity.kt  # Entry point when a tag is scanned while app isn't foregrounded
│   ├── WakeTagHandler.kt       # What happens when the registered "wake" tag is scanned
│   └── BedtimeConfirmReceiver.kt / BedtimeNotificationHelper.kt
│
├── data/
│   ├── AlarmRepository.kt / SleepLogRepository.kt / SleepTagRepository.kt / AuthRepository.kt
│   └── local/                 # Room entities, DAOs, database, migrations, type converters
│       ├── Alarm.kt, SleepLog.kt, SleepTag.kt (+ Dao counterparts)
│       ├── TuLogsDatabase.kt, Migrations.kt, Coverters.kt
│
├── di/                        # Hilt modules (AuthModule, DatabaseModule)
│
├── ui/                        # Compose screens + ViewModels, one package per feature
│   ├── auth/       — sign in / sign up
│   ├── home/       — home dashboard, bottom navigation
│   ├── alarms/     — alarm list & editor
│   ├── tags/       — NFC tag registration
│   ├── week/       — weekly sleep history
│   ├── settings/   — account/settings
│   └── theme/      — Compose theme, color, typography
│
└── util/                      # Shared formatting helpers (e.g. sleep duration formatting)
```

Other notable paths:

- `app/schemas/` — Room-exported database schemas, used by migration tests in `androidTest`.
- `app/src/test/`, `app/src/androidTest/` — unit and instrumented tests.
- `gradle/libs.versions.toml` — centralized dependency version catalog.

## Building

```bash
./gradlew assembleDebug   # or gradlew.bat on Windows
```

Requires a `google-services.json` under `app/` (Firebase project config) — already present in this repo for the configured Firebase project.

### Tests

```bash
./gradlew test                 # unit tests
./gradlew connectedAndroidTest  # instrumented tests (Room migrations, etc.), requires a device/emulator
```
