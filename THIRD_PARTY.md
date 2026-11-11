# Third-party components

Combat Afloat uses the public APIs of Epic Fight and Sable. No upstream mod JARs, native binaries, sounds or textures are included in the release or source bundle. The dependency bootstrap retrieves original files and verifies SHA-256 checksums.

- Epic Fight Java code: GPL-3.0; its assets have a separate license. See the upstream project.
- Sable: PolyForm Shield License 1.0.0; Sable is an external dependency. See its upstream license for terms.
- The standard Gradle wrapper is included for reproducible builds under Apache License 2.0. Its license is in `gradle/LICENSE`.
- NeoForge, Sable Companion, Veil, JUnit and ASM retain their respective upstream licenses and are obtained as external dependencies.
