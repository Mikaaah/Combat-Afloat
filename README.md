<p align="center"><img src="https://i.imgur.com/RxiYjzR.png" alt="Combat Afloat" width="100%"></p>

<h1 align="center">Combat Afloat</h1>

<p align="center"><strong>Take Epic Fight aboard.</strong></p>

<p align="center"><a href="docs/COMPATIBILITY.md">Compatibility guide</a> · <a href="CHANGELOG.md">Changelog</a> · <a href="https://github.com/Mikaaah/Combat-Afloat/issues">Report an issue</a> · <a href="https://github.com/Mikaaah/Combat-Afloat/actions">Development builds</a> · <a href="https://discord.gg/CRYk3uDPVt">Discord</a></p>

<p align="center"><img src="https://img.shields.io/badge/Minecraft-1.21.1-5b8731?style=flat-square" alt="Minecraft 1.21.1"> <img src="https://img.shields.io/badge/Loader-NeoForge-d77828?style=flat-square" alt="NeoForge"> <img src="https://img.shields.io/badge/Java-21-7f384a?style=flat-square" alt="Java 21"></p>

**Combat Afloat** connects Epic Fight with **Sable's ship physics**, including ships built with **Create Aeronautics**. It keeps ship coordinates, combat movement and effects working together on deck.

## Fight on deck

- **Ship-aware Meteor Slam:** correct ship-local targeting and movement, with smaller collision steps near hulls to catch thin decks.
- **Anchored shockwaves and impacts:** keep damage checks, particles and sound positions aligned with the ship.
- **Deck protection:** skip temporary fracture blocks and protect ship attachments from the shockwave's block-breaking branch.
- **Combat presentation:** ship-aware action positioning, weapon trails, cloth anchors and fall-motion selection.
- **Normal terrain behavior:** ordinary ground retains Epic Fight's existing behavior.

The bouncing terrain-block effect is skipped on ships. Combat Afloat does not add new weapons or combat styles.

## Install

Install matching Combat Afloat versions on **the server and every client**. Keep Epic Fight and Sable installed on both sides.

| Component | Requirement |
| --- | --- |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.262 or newer |
| Java | 21 |
| Epic Fight | 21.17.3.1 |
| Sable | 2.0.5 |
| Create / Aeronautics | Optional for this addon; checked with Create 6.0.10 and Aeronautics 1.3.2 |

Use the versions appropriate to your installed build. The [compatibility guide](docs/COMPATIBILITY.md) records the included source baseline, dependency details and test coverage. Development JARs are attached to successful [build runs](https://github.com/Mikaaah/Combat-Afloat/actions).

## Support and coverage

The included source has recorded integrated-client combat and Meteor Slam checks, including a translated and tilted test deck. Separate multiplayer, sustained powered flight and the complete TNK3 combat-addon combination remain outside that recorded coverage. See [validation details](VALIDATION.md).

[Report a bug](https://github.com/Mikaaah/Combat-Afloat/issues/new?template=bug_report.yml) with exact versions, the affected ship/action and steps to reproduce it.

## License and credits

[GPL-3.0-only](LICENSE). [Epic Fight](https://github.com/Antikythera-Studios/epicfight) and [Sable](https://github.com/ryanhcode/sable) remain separate dependencies with their own licenses. See [third-party notices](THIRD_PARTY.md).

## Community

Share builds, ask questions and follow development in the **Towns & Kingdoms Modding Hub**.

[![Join the Towns & Kingdoms Discord](https://i.imgur.com/Ojgcccm.png)](https://discord.gg/CRYk3uDPVt)

[Discord](https://discord.gg/CRYk3uDPVt) · [All projects](https://mikaaah.github.io/TownsAndKingdoms/projects/)
