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

## Next release: license notices

Requested by the maintainer on 2026-11-15. Planned work; not yet implemented.

- [ ] Add prominent change notices directly to modified inherited source/configuration files, retaining existing copyright and attribution notices.
- [ ] Include the complete Apache License 2.0 text with the distributed APK and make it accessible in the app's About/license information.
- [ ] Review bundled dependency licenses and any applicable NOTICE requirements; include the required third-party notices.
- [ ] Verify license and notice files in the final packaged APK before publishing the next release.
