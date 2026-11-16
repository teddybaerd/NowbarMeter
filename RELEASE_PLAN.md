# First release checklist

- [x] Standard build with independent application ID.
- [x] Maintainer device tests: Android 17 Now Bar, Android 12 status icon, sampling interval, reboot autostart and extended standby.
- [x] Complete German resources, including formerly hardcoded update UI and notification-channel description.
- [x] Validate translation completeness and formatting placeholders.
- [x] Update README, attribution and concise release notes.
- [x] Verify final release build and German layout on a physical device: maintainer confirmed successful installation and operation.
- [x] Create and independently back up a dedicated persistent release signing key.
- [x] Configure environment-scoped signing secrets with build, signing and publication separated.
- [x] Build and verify the production-signed standard APK (run 43744818983).
- [x] Publish the first signed release after final checks: v1.4.0, publication run 43750998804.

The published APK is byte-for-byte identical to the device-tested APK: SHA-256 d80616e3bfad3714233e3e9659a5a87cb7a88f2535efed2df7f5fbf7f81dfbe2.

German translation requested and first continuation release published on 2026-11-15.

## Update 1.4.1: privacy and licenses

- [x] Persistent control for automatic GitHub update checks, loaded before any automatic request.
- [x] Manual check action, including previously skipped versions, with progress/result feedback.
- [x] Explicit cloud-backup opt-out and Android 12+ extraction exclusions.
- [x] Change notices on modified inherited source/configuration files, retaining original attribution.
- [x] Full Apache 2.0 text and generated dependency licenses/notices available offline in About.
- [x] Automated tests for preference loading and repeated automatic checks; APK content verification in CI.
- [ ] Build/lint checks pass and packaged licenses are reviewed.
- [ ] Production-signed APK tested by the maintainer as an update over 1.4.0.
- [ ] Publish stable after the device test.
