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

## Next release: privacy controls and license notices

License work requested on 2026-11-15; privacy controls added on 2026-11-16. Deliver these together in the next update. Planned work; not yet implemented.

### Privacy controls

- [ ] Add a persistent setting to disable automatic GitHub update checks. When disabled, opening the app and changing the skipped-version preference must not initiate update requests.
- [ ] Provide an explicit manual update-check action that works on demand, including when automatic checks are disabled. Explain the GitHub connection in the settings.
- [ ] Explicitly disable Android cloud backup for app settings and imported fonts. Configure and verify the manifest and Android 12+ data-extraction rules. Treat device-to-device transfer separately; do not claim that disabling cloud backup necessarily disables manufacturer migration tools.
- [ ] Update German and English interface strings and privacy documentation; verify automatic-check behavior and backup configuration before release.

### License notices

- [ ] Add prominent change notices directly to modified inherited source/configuration files, retaining existing copyright and attribution notices.
- [ ] Include the complete Apache License 2.0 text with the distributed APK and make it accessible in the app's About/license information.
- [ ] Review bundled dependency licenses and any applicable NOTICE requirements; include the required third-party notices.
- [ ] Verify license and notice files in the final packaged APK before publishing the next release.
