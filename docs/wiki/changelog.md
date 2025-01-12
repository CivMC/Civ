---
title: Changelog
description: Server changelog
---

# Changelog
## Civ 1.23.1
* Bastion config is now public - RedDevel
* Add patreon to tab list footer - RedDevel
* Add city spawns for Meracydia, Zephyria, Papal states, and Hell. Update city spawn for temporal isles - RedDevel
* Add Kingtell, Gwua, and S0uthw3st heads to fossils - RedDevel
* Factory mod changes!
* Add "melt ice" recipe to make brewing in the nether easier - RedDevel
* Add tinted glass recipe to glass dying factory - RedDevel
* Fix behive recipe - RedDevel
* Change dark prismarine to use black dye - RedDevel
* Add a new factory, the Copper Workshop - Walkers
* Update ViaVersion to support clients as recent as 1.20.1 - RedDevel
* Amethyst drops will now only drop in the stone layer - RedDevel
* Update chunk limits for a number of blocks and entities - RedDevel
* Add Tinted Glass to shop relays - Gjum
* Change randomspawn to choose spawnpoints async. This should fix a lot of recent lag spikes - Okx
* Update player kill message to properly reflect the item used, changed color to dark red to not collide with wordbank colors, and Add /config option to let you choose the wording of your message. You can now choose "for", "while", "", "using", "by", and "with" - Walkers

## Civ 1.22.0

* Fix randomspawning in the nether - Orinnari
* Fix dormant snitches re-activating - Okx
* Fix supervanish announcements - Ahri
* Change wordbank icon in factory, make NBT data count - Diet_Cola
    This is a semi-breaking change, any wordbanks you previously had that used an item with nbt data will now be different. You can recreate that original recipe by using a blank item
* Don't show downgrade command on pearls - Drekamor
* Fix OTT timeouts and requests - Orinnari
* Fix pearl saving with admin commands - Diet_Cola
* Remove better chairs, add Gsit - RedDevel
    Sitting should be a lot better now, and can /lay and /bellyflop
    Adds particle trails for patrons
* Lower stone and iron acid maturation times - RedDevel
* Update dogfacts announcements - RedDevel
* Add city spawns for Volterra, Shockton, Imperia, update Pavia and AmboisePalace - Ahri
* Buff netherite tool bonuses - RedDevel
    Netherite axes will now give you more bonus logs
    Netherite shovels will now give you bonus drops
    Netherite hoes will now have a chance to drop cobwebs while breaking leaves in swamps
* Kill messages are now broadcast globally - RedDevel
* Overhall brewery - RedDevel


## Civ 1.21.7

* Add city spawns for Venne, Transylvania, Lambat, Cliqueston, and Aeros - Ahri
* Update tab list to include RedDevel as Admin - Ahri
* disable safeorebreak by default - RedDevel
* Increase bonus from netherite pickaxe - RedDevel
* Add Amethyst to hiddenore - RedDevel


## Civ 1.21.4

* Fix factories running while chest is full or at 0% health - GordonFreemanQ
* Notify the player every time they attempt to break an ore with protection on - Awoo
* Add safe ore break for more ores - Gjum
* Fix pearl essence cost display - RedDevel
* Correct voting time cooldown display - RedDevel
* Add wordbank - RedDevel
* Add discord link to vpn/multiaccount bans - RedDevel
* Add bio factory with recipes for lots of non-renewable plants - RedDevel
* Add endstone recipe - RedDevel
* Remove queue server - Soundtech


## Civ 1.19.0

* Prevent using /cardinal inside of a vehicle - Diet_Cola
* Fix off by one and member range error in Namelayer GUI - Tuomasz_
* Lengthen reinforcement maturation and acid times - RektTangle/CaptainKlutz
    Stone/Netherbrick: 30m
    Iron/Gold: 4h
    Diamond/Gilded Blackstone: 12h
* Add pigstep and otherside to the fossil table - RedDevel
* Add cityspawn for MTA - Wingzero54
* Rewrite welcome book for newfirends - RedDevel
* Add /config option to prevent breaking certain ores without silk touch - Okx
* Don't allow randomspawning on trapdoors - Gjum


## Civ 1.18.6

* Fix config error causing randomspawn to default to world spawn - Soundtech
* Update Banstick to 2.0.1 - Soundtech
* Update Bastion to 3.0.1 - Soundtech
* Update FactoryMod to 3.0.3 - Soundtech
* Update Namelayer to 3.0.3 - Soundtech
* Update RandomSpawn to 3.0.2 - Soundtech
* Adjust algorithm for circle spawning to not bias towards the center - Gjum
* Fix Vulcan kicks when bridging over open air - Soundtech


## Civ 1.18.5

* Fix gold forge duplicate recipe - smal
* Fix link to wiki rules in dogfact - Blob
* Add player run newsletter to dogfacts - specificlanguage
* Disable random percentage of mobs turning into bees - Orinnari
* Fix world border in nether not having the reinforcement buffer zone it's supposed to - Diet_Cola
* Fix randomspawning in the nether spawing you in lava and on the roof - SoundTech
* Add city spawns for Estalia, Free Danzilonan Republic, Regentsburgh, Pridelands, Amboise Palace, Pacem, Icarus, and Concord (cdm) - Blob
* Fix NovaCeasar's player head being spelled incorrectly -SoundTech


## Civ 1.18.4

* Add Blob's donator head to fossil drops - Wingzero
* Fix regression in backups, to reduce size.


## Civ 1.18.3

* Added End Rod recipe to Aesthetics Factory -Walkers
* Buffed Basic XP to make it less brutal for starter groups - Ahri
* Remove string from XP as there was not supposed to be a breedable mob drop - Diet_Cola
* Fix an issue with offhand logic -Diet_Cola
* Fix an issue with banned players who are pearled costing essence -Diet_Cola
* Tweaks and improvements to globalmute -Diet_Cola
* Fix an issue that caused reinforced jukeboxes not to drop music discs when broken -melncat
* Add missing admin heads, and added patron heads for high tier donors - Wingzero
* Double the player render distance - Wingzero
* Added city spawns! 17 city spawns currently, note that players must be online around the spawn area for a new player to have a chance to spawn there - Wingzero
* You can now use multiple pages to create a printing note to give more flexibility with formatting, color, etc - melncat
* Fixed hex color code issue with printing press -melncat
* Books and printing plates now track print generation: Original, Copy of Original, Copy of Copy, Tattered. There is only one original, and books have a finite re-printable life which gives inherent value to earlier print editions! -melncat


## Civ 1.18.0

* There are now multiple tiers of acid blocks. Gold, Diamond, and Netherite. While gold still only acids upwards, Diamond will acid upwards and downwards, and Netherite will acid in every direction. Diamond and Netherite blocks take more time to acid than gold blocks, twice as long and four times as long respectively. This multiplier is applied to the acid time for the reinforcment level. - Soundtech


## Civ 1.17.1

* Allow players within range of their pearl - ?
* Automatically add new players to [!] - ?
* Add chat color gradient based on distance - ?
* Add Soul Sand -> glass smeltery recipe (The factory mod config is also now public) - ?
* Divided Dirt! into Dirt!, Gravel!, Sand!, Cobblestone!, and Terracotta! in fossil cracking. Each is at 20% of the original Dirt! rate. - ?
* Update ExilePearl to 2.1.1 - ?
* Players will now drop pearls when going through portals - ?
* Prison Pearled players can now pickup and interact with pearls - ?
* Fix combat loggers not dropping pearls - ?
* Update SimpleAdminHacks to 2.1.1 - ?
* Don't allow reparing armor that's bound differently, or removing binding from armor when repairing - ?


## Civ 1.17.0

* Added 60 new brews - Rektangle
* Tweaks to brew and brew ingredient's names - Rektangle
* Increased essence costs across the board for factorymod - ?
* Added essence cost for making bastions - ?
