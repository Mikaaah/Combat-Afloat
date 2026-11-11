# Validation scope

The initial GitHub source import preserves the supplied **0.1.0-beta.2** source, mod metadata and dependency pins. No gameplay implementation was changed for this repository setup.

## Recorded package checks

The 9 November 2026 source package records:

- Java 21 compilation and release packaging.
- Four JUnit tests covering binary mixin contracts and ship-motion math.
- 68 dedicated-server assertions with NeoForge 21.1.262, Epic Fight 21.17.3.1, Sable 2.0.5, Create 6.0.10 and Aeronautics 1.3.2.
- Rendered first- and third-person combat, two complete Meteor Slam casts and another translated/tilted deck pose, including a client with Sodium 0.8.13 loaded.
- Damage, landing, bounded coordinates and preservation of the test deck.

These are the supplied package's historical results. The repository's automated build checks unit tests and packaging; it does not run a new graphical playthrough.

## Coverage limits

The graphical session used one client and its integrated server. Physics stepping on the test deck was paused to hold a controlled pose. Separate multiplayer, sustained powered Aeronautics flight, shaders, Punchy/Combat Punchy, Weapons of Miracles and the full TNK3 combination were not covered by that session. Audible output was not checked.

Combat Afloat does not repair previously damaged saves or player records.

[Compatibility guide](docs/COMPATIBILITY.md) · [Current build runs](https://github.com/Mikaaah/Combat-Afloat/actions)
