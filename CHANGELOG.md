# 0.1.0-beta.2

- Fixed a client startup crash: Epic Fight's client payload handler is an interface, and its mixin must also be an interface.
- Fixed Meteor Slam treating ship-local raycast hits as world-space movement destinations and fall distances.
- Added smaller movement steps for Meteor Slam near ship hulls, preventing high-speed descent from passing through a thin deck.
- Added regression checks for mixin interface/class agreement and ship-raycast coordinate conversion.
- Completed graphical first/third person combat and two real Meteor Slam casts with Sodium 0.8.13 loaded, including a translated/tilted deck. Screenshots and telemetry included.

Minecraft 1.21.1, NeoForge, Epic Fight 21.17.3.1, Sable 2.0.5. Separate multiplayer, sustained powered flight and the full TNK3 addon combination remain unverified.

# 0.1.0-beta.1

Initial ship shockwave, terrain protection, impact networking, interpolation and trail compatibility. Server-side tests passed, but graphical testing had not been performed. Superseded by beta 2 because client testing uncovered additional failures.
