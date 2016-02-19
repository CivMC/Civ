# CombatTagPlus

*CombatTagPlus is the last plugin you'll ever need to stop those dirty combat loggers!*

## Supports

* CraftBukkit & Spigot 1.7.9
* CraftBukkit & Spigot 1.7.10
* Spigot 1.7.10 / 1.8 Protocol Patch
* CraftBukkit & Spigot 1.8.0
* CraftBukkit & Spigot 1.8.3
* CraftBukkit & Spigot 1.8.4 / 1.8.5

## Features

* Spawn a NPC when a player logs off in combat
* Play an effect when NPC spawns
* Force NPC to always spawn when the player logs off, even out of combat
* Can instantly kill player when they log off in combat
* Safe Logout that optionally lets tagged player wait a timer without moving to safely log off
* Disallow flying while player is in combat
* Disallow block editing while player is in combat
* Disallow enderpearls while player is in combat
* Disallow teleporting while player is in combat
* Broadcast a PVP kill/death message
* Disable combat tagging for specific worlds
* Deny specific commands while player is in combat
* Disallow running back to safety while in combat (can also render a forcefield)
* Supports WorldGuard PVP deny flag (WorldGuard 5.x and 6.x both supported)
* Supports Factions PVP flag
  * _Most mainstream Faction builds are supported:_
  * Factions 2.7 (MassiveCore 2.7)
  * Factions 2.6 (MassiveCore 7.4)
  * FactionsUUID 1.6 (and HCFactions) by @drtshock
  * FactionsUUID 1.8 by @externo6
* Supports Towny PVP permission
* Works with BarAPI to show a boss bar counting down with the player's remaining combat timer
  * _These custom BarAPI builds by @vemacs are more compatible than the official ones:_
  * Spigot 1.8.0: [Download](http://ci.minelink.net/job/BarAPI/5/artifact/target/BarAPI.jar)
  * Spigot 1.7.10 / 1.8 Protocol Patch: [Download](http://ci.minelink.net/job/BarAPI/4/artifact/target/BarAPI.jar)

## Development Builds

You can acquire development builds of CombatTagPlus from the [Minelink Network CI server](http://ci.minelink.net/job/CombatTagPlus/).

## Installation

1. Obtain the latest version of CombatTagPlus from the [build server](http://ci.minelink.net/job/CombatTagPlus/).
2. Copy or move **CombatTagPlus.jar** into your server's *plugins* folder.
3. **Optionally** obtain and install a compatible version of BarAPI (see the *Features* section above for downloads)
4. Start the server. _This creates a new file **plugins/CombatTagPlus/config.yml**_
5. Edit the newly created configuration file with desired behavior.
6. If you made any changes, run the command **/ctreload** from console or as an operator.

## Permissions

| **Permission**           | **Description**                            | **Default** |
| -------------------------| ------------------------------------------ | ----------- |
| ctplus.reload            | Reload CombatTagPlus configuration         | operator    |
| ctplus.bypass.tag        | Bypass combat tagging                      | operator    |
| ctplus.bypass.command    | Bypass command restrictions in combat      | operator    |
| ctplus.bypass.blockedit  | Bypass block edit restrictions in combat   | operator    |
| ctplus.bypass.enderpearl | Bypass enderpearl restrictions in combat   | operator    |
| ctplus.bypass.flying     | Bypass flying restriction in combat        | operator    |
| ctplus.bypass.teleport   | Bypass teleportation restriction in combat | operator    |
| ctplus.check             | Check remaining combat timer               | everyone    |
| ctplus.logout            | Initiate a safe logout                     | everyone    |
| ctplus.notify.kill       | Receive kill/death message broadcast       | everyone    |

## Contributing

* 4-space indentation
* UNIX line endings
* Braces on the same line

Thanks :)

## License

CombatTagPlus is licensed to you freely under the terms of the [LGPL license](https://www.gnu.org/licenses/lgpl.html).

## Special Thanks To:

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/) supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/index.jsp) and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/index.jsp), innovative and intelligent tools for profiling Java and .NET applications.
