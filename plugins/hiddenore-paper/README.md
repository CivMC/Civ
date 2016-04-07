# HiddenOre

Massively configurable, low-impact plugin to allow post world generation ore balance and anti-xray plugin.

## Overview

Every time you break a block, naturally placed, there will be a lottery run. This will occasionally drop cool stuff instead of the block you broke.

Typical use case is for stone -- instead of dropping stone, occassionally drop Ore instead.

For example:

1:100,000 chance to find prismarine.

1:3000 chance to find a diamond.

1:10000 chance to find 10 diamonds.

Etc.

Mod is fully configurable with biome-specific settings and tool restrictions.

## Details

The raw technical details:

On block break, provided the user is not leveraging Silk Touch enchantment, checks the config to see if a lottery has been defined for this block.

You can specificy multiple drops, where each drop is computed as an independent probability against the break, and the sum of drops is spawned into the world.

Alternatively, you can indicate only a single drop-type is allowed. In this case a single random number is generated and tested against a probability distribution of possible drops. This allows replication of MC behavior (in terms of how mines are generated).

The chance to drop for each type of drop against a type of broken block is configurable.

A block can be configured to match all subtypes or just a specific set of subtypes (e.g. not regular stone but both andesite and diorite would be possible).

You can apply biome-level chance, level, and amount modifiers.

Drops can be restricted to specific Y levels and specific tools.

Included is a default config that effectively mirrors Minecraft Vanilla orespawn; it should be possible to generate a normal MC world with no ores or caves, but with this plugin allow effectively normal vanilla richness. Consider it the ultimate XRay defense; you cannot see what literally doesn't exist.

Supports tracking of breaks to prevent "gaming" the system for more drops. Extra event handlers watch for game attempts and directly penalize that "chunk".

Currently you cannot specify more then one config for a particular base type of Material (block type). 

You can specify custom messages for specific types of drops, allowing "uber"-finds to have a unique message

You can turn on or off the debug using /hiddenore debug true     or false. Be warned, if people are digging, this will spam the console badly.

Supports saving and loading of the tracking database, and fully adheres to /reload with no difficulties. 

I'm probably missing some other details but that's it for now.

