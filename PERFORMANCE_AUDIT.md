# KeepJ Performance Audit

## Implemented

### Vault hot path
- Replaced per-query Room searches that decrypted every matching row with a single Room-backed decrypted snapshot.
- Search, category filtering, and sorting now operate on that snapshot off the UI dispatcher.
- Search still uses a short debounce so typing does not repeatedly recompute the result set.
- Search now correctly searches decrypted username, email, website, and notes instead of attempting SQL `LIKE` against encrypted ciphertext.
- Removed whole-list `filter` allocations from the composable's scroll-sensitive recomposition path.
- Static category data is remembered.
- Service icon initials are remembered by service name.

### Main-thread work
- PIN PBKDF2 derivation is now performed on `Dispatchers.Default`.
- Vault encryption/decryption remains off the UI dispatcher.
- Backup serialization/encryption/file work remains off the UI dispatcher.
- Compose state collection uses lifecycle-aware collection where applicable.

### Crypto overhead
- Vault Android Keystore key handle is cached for the lifetime of the crypto manager, avoiding repeated Keystore lookups for each field while keeping the key non-exportable.
- Biometric keys are intentionally not cached so biometric enrollment invalidation is respected safely.

### Motion preserved
- FAB width/height/corner changes now animate rather than snap.
- Navigation and press feedback remain intact.
- No infinite decorative animation was introduced.
- No broad animation removal was used as a shortcut for performance.

## Audit checks

- 41 Kotlin source files scanned.
- Kotlin delimiter sanity check: 0 structural errors.
- No `runBlocking`, `Thread.sleep`, or explicit main-dispatcher blocking operations found.
- No obsolete vault search/category repository calls remain.
- No suspicious hardcoded secrets or webhook patterns found.
- No image-loading library or bitmap processing path is present, so there is no image decoding hotspot to optimize.
- Release build already enables R8/minification and resource shrinking.

## Build limitation

A real Gradle compile could not be executed in this environment because the supplied `gradle-wrapper.jar` is missing `org.gradle.wrapper.IDownload`. The project declares Gradle 8.7, while the wrapper binary is incompatible/incomplete. Static validation was completed, but this is not a substitute for a real device/emulator run.

## Next production profiling pass

Once the project is opened in Android Studio or built on a machine with a valid Gradle 8.7 wrapper, profile a release-like build with:

1. Android Studio CPU Profiler / System Trace for startup and vault scrolling.
2. Compose recomposition counts for VaultScreen and VaultEntryItem.
3. Macrobenchmark for cold startup and vault-to-detail navigation.
4. JankStats / FrameTiming around list scrolling and navigation transitions.
5. Memory profiler with a large vault and repeated lock/unlock cycles.
6. Baseline Profile generation after the real navigation flow is finalized.
