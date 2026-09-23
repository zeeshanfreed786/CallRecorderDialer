# Call Recorder Dialer

A minimal Android dialer with auto call recording. Kotlin, no Compose, plain
Views — kept simple on purpose since this can't be compiled/tested in the
environment that generated it (see "Building" below).

## Before you use this

**Legal.** Recording a call without the other party's knowledge is a
criminal offense in Pakistan under PECA 2016 §23 (unauthorized recording of
a person's voice/image) — the Supreme Court reaffirmed this in a March 2026
ruling. Sections 25/26 of the Telegraph Act 1885 and §19 of PECA cover
interception more broadly. If this is for personal note-taking on your own
calls, or business calls, worth a five-minute gut-check on that basis before
it goes on anyone's phone but yours. If you're outside Pakistan, the rule
varies by country/state (some are one-party consent, some require all
parties to know) — check locally.

**Technical.** There's no build of this that silently captures both sides
of a normal (non-speaker) call on every Android phone — that's not a gap in
this code, it's a deliberate platform restriction. Since Android 10, direct
access to the call-audio stream (`VOICE_CALL` source) is reserved for
pre-loaded system dialers (Samsung, MIUI, the Google Phone app). Since May
2022, Play policy also bans the Accessibility-API workaround third-party
apps used to use, specifically to stop silent recording. What's left, and
what this app uses, is the microphone (`AudioSource.MIC`). That reliably
works on any device with no special permission tier, but it mostly hears
your side of the call clearly; put the call on speakerphone for the other
side to come through — which is also, by nature, not really an invisible
mode of recording.

If you need studio-quality two-way audio on stock handset mode across
arbitrary phones, that specifically doesn't exist for a sideloaded app
anymore; only a device's own pre-installed dialer can do it, and only on
OEMs (Xiaomi/MIUI, some Samsung regions) that still ship the feature.

## What's included

- **Dialer** (`MainActivity`) — numeric keypad, dials via `ACTION_CALL`.
- **Auto recording** (`CallStateReceiver` + `RecordingService`) — watches
  system call state (works for calls placed from this app *or* your normal
  phone app) and records from ringing/dial to hang-up as a foreground
  service (Android requires the persistent "Recording call" notification
  for any app using the mic in the background — that's on your device only,
  the other party never sees it).
- **Recordings list** (`RecordingsActivity`) — play/delete, stored at
  `Android/data/com.zeeshan.callrecorder/files/recordings/` on the device,
  named `IN_<number>_<timestamp>.m4a` / `OUT_<number>_<timestamp>.m4a`.

## Building

This was written without access to the Android SDK/Gradle, so it hasn't
been compiled — I can't verify it builds clean on the first try.

### Option A: get a built APK without installing anything (GitHub Actions)

A workflow is included at `.github/workflows/build-apk.yml` that builds a
debug APK in the cloud and hands you a download link — no Android Studio
needed.

1. Create a new repo on GitHub (private is fine) and push this whole
   `CallRecorderDialer/` folder to it.
2. Go to the repo's **Actions** tab → **Build Debug APK** → **Run workflow**.
3. Once it finishes (a couple of minutes), open the run and download the
   `call-recorder-debug-apk` artifact — that's a zip containing
   `app-debug.apk`.
4. Copy that APK to your phone and install it (enable "install unknown
   apps" for whatever app you use to open it).

This produces a **debug-signed** APK — fine to install and run, not meant
for Play Store distribution.

### Option B: Android Studio (if you already have it installed)

1. Open the `CallRecorderDialer/` folder in Android Studio (Koala or newer).
2. If prompted about a missing Gradle wrapper jar, let Android Studio
   generate it. Accept any AGP/Gradle "upgrade available" prompt — it's
   safe to take the newer version.
3. Grant all requested permissions on first launch (mic, phone state, call
   log, call, notifications on Android 13+). If any are denied, recording
   silently won't start for that call — nothing crashes, it just skips it.
4. Run on a real device — the emulator doesn't have real telephony/audio to
   test call recording against.

Package name is `com.zeeshan.callrecorder` — rename via Android Studio's
refactor tool if you want your own. The launcher icon is a placeholder
(plain circle) — swap it via *Image Asset* in Android Studio when you're
ready to make this look finished.

## Known limitations

- Outgoing-call number is only captured when you dial from this app's own
  keypad. If you use your regular phone app to make the call, the outgoing
  recording is saved but labeled "Unknown" (Android removed the broadcast
  that exposed the dialed number to third-party apps back in API 29).
- No contacts integration, no call log view, no in-call UI (mute, hold,
  speaker toggle) — this is a recorder with a keypad bolted on, not a full
  phone-app replacement.
- `minSdk 24` (Android 7.0) / `targetSdk 35` (Android 15). Lower `minSdk` in
  `app/build.gradle.kts` if you truly need pre-2016 devices.
