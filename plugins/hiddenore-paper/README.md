# HiddenOre

Massively configurable, low-impact plugin to allow post world generation ore balance, via either drops, or ad-hoc generation of ores into the blocks of the world on-demand. It works as a wonderful anti-xray plugin, and as a powerful incentive tool for mining.

## Overview

Every time you break a block, naturally placed, there will be a lottery run. This will occasionally drop cool stuff instead of the block you broke, or generate new blocks around the miner.

Typical use case is for stone -- instead of dropping stone, occasionally drop ore instead, or generate awesome blocks around the miner.

For example:

1:100,000 chance to find prismarine.

1:3000 chance to find a diamond.

1:10000 chance to find a vein of 10 diamond ore.

Etc.

This plugin is fully configurable with biome-specific settings and tool restrictions, including a significant degree of options and specificity. 

## Details

The raw technical details:

On block break, checks the config to see if a lottery has been defined for this block. Optionally, a quick check for presence of Silk Touch is done.

You can specify multiple drops, where each drop is computed as an independent probability against the break, and the sum of drops is spawned into the world.

Alternatively, you can indicate only a single drop-type is allowed. In this case a single random number is generated and tested against a probability distribution of possible drops. This allows replication of Minecraft behavior (in terms of how mines are generated).

The chance to drop for each type of drop against a type of broken block is configurable.

A block can be configured to match all subtypes or just a specific set of subtypes (e.g. not regular stone but both andesite and diorite would be possible).

You can apply biome-level chance, level, and amount modifiers.

Drops can be restricted to specific Y levels.

Drops can be restricted to specific tools, with a high degree of configure-ability. Check `config.yml` and `config-advanced.yml`. 

Included is a default config that effectively mirrors Minecraft Vanilla orespawn; it should be possible to generate a normal MC world with no ores or caves, but with this plugin allow effectively normal vanilla riches. Consider it the ultimate XRay defense; you cannot see what literally doesn't exist.

Supports tracking of breaks to prevent "gaming" the system for more drops. Extra event handlers watch for game attempts and directly penalize that "chunk" (technically, the chunk slice).

You can specify more then one config per block type, to deal with subtypes even better. Note that in terms of drops, the first matching config to be encountered will be used; so keep that in mind. 

You can specify custom messages for specific types of drops, allowing "uber"-finds to have a unique message

You can turn on or off the debug using `/hiddenore debug true` or `false`. Be warned, if people are digging, this will spam the console _very_ badly.

Supports saving and loading of the tracking database, and fully adheres to /reload with no difficulties. 

I'm probably missing some other details but that's it for now.

