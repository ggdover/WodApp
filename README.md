# WodApp

Android app built with Kotlin and Jetpack Compose.

## Prerequisites

| Tool | Role |
|------|------|
| **Podman** | Builds the APK in a container so the toolchain (JDK, Android SDK) does not depend on what is installed on the host. |
| **adb** (on the host) | Installs the built APK on a physical device over USB (or TCP/IP). Only needed for the install step, not for the build. |

Install Podman and the Android platform tools (`adb`) using your OS package manager or the [Android SDK Platform-Tools](https://developer.android.com/tools/releases/platform-tools) zip.

## Build the debug APK with Podman

All commands below assume your shell’s current directory is this folder (`WodApp`), i.e. the one that contains `gradlew` and `app/`.

### How the container knows what to build

The **image** (`ghcr.io/cirruslabs/android-sdk:36-ndk`) is generic: it ships a JDK and a preinstalled Android SDK (API 36, NDK, etc.). It does **not** contain your app source.

Your **project** is what defines the build. The flag `-v "$PWD:/workspace:rw"` bind-mounts the `WodApp` directory from the host into the container at `/workspace`, so the container sees the same files you have locally: `gradlew`, `gradle/`, `app/`, `build.gradle.kts`, and so on. The `-w /workspace` flag sets the shell’s working directory there before the command runs.

The **command** at the end of `podman run` — `./gradlew --no-daemon assembleDebug` — is what actually starts the build:

1. **`gradlew`** is the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) checked into this repo. It runs the Gradle version pinned in `gradle/wrapper/gradle-wrapper.properties` (and downloads that distribution on first use, with network access).
2. **`assembleDebug`** is a standard Android Gradle Plugin task from your `app/build.gradle.kts` / AGP setup; it compiles the app and packages the debug APK.

So: the image supplies **where** to compile (toolchain); your repo supplies **what** to compile and **how** (Gradle scripts). Running the README commands from `WodApp` is intended to produce a working debug APK, assuming network access for dependencies, acceptable SDK licenses in the image, and no permission/SELinux issues on the bind mount (see Troubleshooting). It is not guaranteed on every host without those conditions.

### 1. Choose a pinned SDK image (reproducible builds)

The project targets **compileSdk 36** (see `app/build.gradle.kts`). Use an Android SDK image that includes API 36, for example:

```text
ghcr.io/cirruslabs/android-sdk:36-ndk
```

To pin an **exact** image so every machine uses the same bits:

1. Pull once: `podman pull ghcr.io/cirruslabs/android-sdk:36-ndk`
2. Record the digest: `podman image inspect --format '{{.Digest}}' ghcr.io/cirruslabs/android-sdk:36-ndk`
3. In scripts or docs, reference `ghcr.io/cirruslabs/android-sdk:36-ndk@sha256:<digest>`

Updating the pinned digest is a deliberate change when you want to move to a newer SDK image.

### 2. Run the Gradle build inside the container

```bash
cd /path/to/wod_app/WodApp

podman run --rm \
  -v "$PWD:/workspace:rw" \
  -w /workspace \
  ghcr.io/cirruslabs/android-sdk:36-ndk \
  ./gradlew --no-daemon assembleDebug
```

Notes:

- **`--no-daemon`** avoids leaving a Gradle daemon inside the container (typical for one-shot CI-style builds).
- **Volume flag**: `:rw` is enough on many setups. If rootless Podman cannot write build outputs into the bind mount, try appending **`U`** (e.g. `:rw,U`) so the mount’s ownership matches the container user, or **`Z`** on SELinux systems (e.g. `:rw,Z`).
- First run downloads the Gradle distribution and dependencies; later runs are faster. Optional: add a named volume for Gradle caches, e.g. `-v wodapp-gradle:/root/.gradle`, if you want faster rebuilds (cache is then shared by name on that machine, not necessarily bit-identical across PCs).

On success, the debug APK is:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Release APK (optional)

Unsigned / default release output (no signing config in this template):

```bash
podman run --rm \
  -v "$PWD:/workspace:rw" \
  -w /workspace \
  ghcr.io/cirruslabs/android-sdk:36-ndk \
  ./gradlew --no-daemon assembleRelease
```

Output:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

For Play Store or serious distribution you will add a signing config; that is outside this minimal flow.

## Install on a phone from the host (adb only)

1. On the phone: enable **Developer options** and **USB debugging**.
2. Connect USB and accept the debugging prompt on the device.
3. On the **host** (not inside Podman):

   ```bash
   adb devices
   ```

   You should see your device as `device`, not `unauthorized` or `offline`.

4. Install the debug APK you built (path relative to `WodApp`):

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   `-r` replaces an existing install with the same application id.

### Wireless debugging (optional)

If you use Android’s wireless debugging pairing, use `adb connect <ip>:<port>` then the same `adb install` command.

## Troubleshooting

| Issue | What to try |
|--------|----------------|
| `gradlew`: Permission denied | `chmod +x gradlew` |
| SDK license errors in the container | Run once interactively: `podman run --rm -it -v "$PWD:/workspace:rw" -w /workspace ghcr.io/cirruslabs/android-sdk:36-ndk bash` then `yes \| sdkmanager --licenses` and retry the build. |
| `adb` does not see the device | Another USB cable/port; on Linux, `adb kill-server` then `adb start-server`; install udev rules for your vendor if the device stays unauthorized. |
| SELinux / permission errors with `-v` | Try `:rw,Z` or `:rw,U` (see build notes above). |
