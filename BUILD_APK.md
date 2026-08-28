# Building the debug APK

Everything below is ready to go — the Gradle wrapper (`gradlew`) has been restored and
`.github/workflows/build-apk.yml` on branch `arena/01a04827-custodia` contains a working
CI build. Pick **one** of the two paths.

---

## Path A — GitHub Actions (no local tooling needed)

The workflow builds the APK on GitHub's servers and **commits `Custodia-debug.apk` back to
the branch automatically**.

> **One-time status:** GitHub Actions had to be enabled on this repo (Settings → Actions →
> General → "Allow all actions and reusable workflows"), and the workflow needs to be
> re-saved once by a human account so GitHub re-registers it (the automated token used by
> the coding agent is not allowed to modify files under `.github/workflows/`).

**Option 1 — via the web UI (30 seconds):**

1. Open: <https://github.com/codebyrahulsahu/Custodia/edit/arena/01a04827-custodia/.github/workflows/build-apk.yml>
2. Add a line at the end of the file, e.g. `# trigger`
3. Click **Commit changes…** → choose **Commit directly to the `arena/01a04827-custodia` branch**
   (not "create a new branch") → **Commit changes**

That push re-registers the workflow and starts the build immediately.
After ~10–15 minutes the APK is committed back to the branch as `Custodia-debug.apk`
(download it from the branch or from the run's *Summary* page under *Artifacts*).

**Option 2 — via git on your machine:**

```bash
git fetch origin
git checkout arena/01a04827-custodia
git pull origin arena/01a04827-custodia
printf '\n# trigger\n' >> .github/workflows/build-apk.yml
git commit -am "ci: re-register workflow" && git push origin
```

## Path B — build locally (Android Studio / command line)

Requires JDK 17+ and the Android SDK (Android Studio provides both).

```bash
git checkout arena/01a04827-custodia   # has the restored Gradle wrapper

# one-time: the build expects a debug keystore at the repo root (it is gitignored)
keytool -genkeypair -v \
  -keystore debug.keystore -alias androiddebugkey \
  -storepass android -keypass android \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"

./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

Or simply open the project in Android Studio and press **Run** — it generates the debug
keystore prompt automatically and builds the same APK.

---

## Notes

- No `google-services.json` is present: the google-services plugin is configured with
  `WARN` + passthrough, so the build succeeds without it (Firebase calls will fail at
  runtime until a real config is added).
- No `.env` is present: the secrets plugin falls back to `.env.example`
  (`GEMINI_API_KEY` stays unset; nothing in the code references it at build time).
- `debug.keystore` is generated with the standard Android debug credentials
  (`android` / `androiddebugkey`), so APKs from CI and local builds are
  signature-compatible with a normal debug setup.
