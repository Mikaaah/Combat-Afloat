# Compatibility and installation

Combat Afloat targets **Minecraft 1.21.1 / NeoForge / Java 21**. Install the same addon version on the server and every client.

## Included source baseline

The initial repository import is **0.1.0-beta.2**, copied from the supplied source package. Its original gameplay source, build version and mod ID (`combatafloat`) are retained.

| Component | Declared requirement or recorded test version |
| --- | --- |
| Minecraft | Exactly 1.21.1 |
| NeoForge | Minimum 21.1.262; package checks used 21.1.262 |
| Java | 21 |
| Epic Fight | Exactly 21.17.3.1 |
| Sable | 2.0.5, below 2.0.6 |
| Create | Optional; package checks used 6.0.10 |
| Create Aeronautics | Optional; package checks used 1.3.2 |
| Sodium | Optional; graphical package checks used 0.8.13+mc1.21.1 |

Create and Aeronautics are optional dependencies for Combat Afloat itself. Their own dependencies still apply when installed. Sable's companion libraries and renderer dependencies come from Sable's normal installation.

## What changes on ships

Meteor Slam converts ship-local raycast endpoints to world coordinates and divides movement near hulls into smaller collision steps. Ship shockwaves retain Epic Fight's damage and stun behavior, while avoiding temporary fracture blocks on the deck and preserving ship attachments.

Impact effects retain a ship UUID and local position across delivery. Remote action positioning, trails, cloth anchors and fall-motion selection account for ship movement.

## Before reporting

Check the exact dependency versions and replace any previous Combat Afloat JAR. Include the action or skill, ship type, whether it was moving or tilted, perspective, other combat/rendering addons and reproduction steps.

[Validation scope](../VALIDATION.md) · [Report an issue](https://github.com/Mikaaah/Combat-Afloat/issues) · [README](../README.md)
