# HiddenOre

Brought to you by the developers from https://www.reddit.com/r/Devoted and https://www.github.com/DevotedMC -- check out our server at play.devotedmc.com and our website at www.devotedmc.com .

Massively configurable, low-impact plugin to allow post world generation ore balance, via either drops, or ad-hoc generation of ores into the blocks of the world on-demand. It works as a wonderful anti-xray plugin, and as a powerful incentive tool for mining.

## Notes on Minecraft 1.14

Largely unchanged from 1.13, however it is worth noting that if you previouly used the durability match for "first hit", "last hit" or specific durability hit bonuses, you will need to recreate those configs, as durability is now in the metadata for items.

## Notes on Minecraft 1.13

Minecraft 1.13, and Bukkit/Spigot's implementation, introduces a wrinkle for my admittedly "deprecated" approach to sub types of materials; namely,
if you're defining a normal ore sequence, you can no longer just define a block config for "STONE" -- you now need to define a block config for
STONE, ANDESITE, DIORITE, and GRANITE, and if you use generation, you need to define allowed types for each.

The benefit with this approach, however, is even greater control over every detail of ore generation within your worlds. I wanted to include this note
at the top, however, to highlight the importance of this change. You can find an example of this done in detail, in config.yml.

Additional 1.13 notes are included below in the normal place.

## Overview

Every time you break a block, naturally placed, there will be a lottery run. This will occasionally drop cool stuff instead of the block you broke, or generate new blocks around the miner.

Typical use case is for stone -- instead of dropping stone, occasionally drop ore instead, or generate awesome blocks around the miner.

For example:

1:100,000 chance to find prismarine.

1:3000 chance to find a diamond.

1:10000 chance to find a vein of 10 diamond ore.

Etc.

This plugin is fully configurable with biome-specific settings, tool restrictions, and limited player state restrictions, allowing a significant degree of options and specificity. 

As of 1.5.2, you can upend this regime and construct intersecting probability fields via "Noise" generators, which allow a persistent hidden map of drop density regions. The probabilities are a bit harder to constrain, but still quite measurable with some trial and error. Unfortunately, due to the nature of noise generators, once you choose a config it will be difficult to rebalance smoothly -- e.g. player's discoveries of high density regions will likely be rendered invalid on a config change. The traditional probability approach doesn't really suffer from this as players would simply notice changes globally or within defined regions, and the "pain" would be shared, without impacting much secret knowledge. Regardless, both are excellent options for administrators and we're proud to finally support it on the mainline w/ all of our existing performance and anti-exploit features intact.

Note that unlike other branches, our noise-based generation approach is embedded in the existing configuration flow, and is handled as a new feature regime instead of a completely "other" element. This allows you to freely mix and match these "veins", drops, transforms, commands and the like without loss of functionality. As always, the goal is max features and control for you.

## Details

The raw technical details:

On block break, checks the config to see if a lottery has been defined for this block. Optionally, a quick check for presence of Silk Touch is done.

You can specify multiple drops, where each drop is computed as an independent probability against the break, and the sum of drops is spawned into the world.

Alternatively, you can indicate only a single drop-type is allowed. In this case a single random number is generated and tested against a probability distribution of possible drops. This allows replication of Minecraft behavior (in terms of how ores are generated).

The chance to drop for each type of drop against a type of broken block is configurable.

With Minecraft 1.13, subtype specification is no longer directly possible; future releases may add groups of native materials to emulate this prior behavior, but for now it has been removed.

You can apply biome-level chance, level, and amount modifiers.

Drops can be restricted to specific Y levels.

Drops can be restricted to specific tools, with a high degree of configure-ability. Check `config.yml` and `config-advanced.yml`. 

Drops chances can be modifed based on player potion / effect state and level of that state. Currently haste, mining fatigue, nausea, blindness, and luck / bad luck states are supported. See `config.yml`.

Included is a default config that effectively mirrors Minecraft Vanilla orespawn; it should be possible to generate a normal MC world with no ores or caves, but with this plugin allow effectively normal vanilla riches. Consider it the ultimate XRay defense; you cannot see what literally doesn't exist.

Supports tracking of breaks to prevent "gaming" the system for more drops. Extra event handlers watch for game attempts and directly penalize that "chunk" (technically, the chunk slice). An extra "highly localized" round robin list keeps track of recent breaks and places to _completely_ prevent break-place based attempts at exploits. Finally, a new tracker keeps track of _each block_ that is broken or interacted, and prevents it from being converted into ore or producing drops.

You can specify more then one config per block type, although as of Minecraft 1.13 only the first will be used. Also note that in terms of drops, the first matching config to be encountered will be used; so keep that in mind. 

You can specify custom messages for specific types of drops, allowing "uber"-finds to have a unique message.

You can turn on or off the debug using `/hiddenore debug true` or `false`. Be warned, if people are digging or pistons are active, this will spam the console _very_ badly.

Supports saving and loading of the tracking database, and fully adheres to /reload with no difficulties.

As of 1.4.2, full multiple world support, via name or UUID. 

As of 1.5.2, you can specify "veinNatures" which allow complex simplex-noise based distributions of ore probabilities. These are hard to compute as the mechanics aren't as straightforward, but the end result can be quite nice, allowing for layered, overlapping, or otherwise clever intersections of ores. Players like it too, as it allows a kind of density distribution, which once "found" can be leveraged. 

In mainline as with all our other features, the full suite of configuration options, including drops, transforms, commands, biome and state modifiers, etc can be applied to veinNature configs. See config-veins.yml for examples.

I'm probably missing some other details but that's it for now.

### TODO / In progress features:

* Configure which tool to "use" for cave dusting. Default right now is Diamond Pickaxe.

* Better documentation

### Feature Augment List:

**v1.6.0** Generaly update to 1.14.4. Thanks Maxopoly!

**v1.5.3** Some general fixes and improvements to block-level exploit tracking.

**v1.5.2** Adding in CivClassic style "veins", which are basically just distributions of ore backed by persistent noise fields instead of the classic HiddenOre probability distribution functions. The outcomes are probabilistically compatible, but the generation is quite a bit different. Check out config-veins.yml for a hopefully clear example. Note that due to fundamental implementation differences, configurations made for CivClassic veins are not portable to this implementation, as their implementation refactors the configs and introduces veins as a separate flow, with only some of the tradition drop or generation capabilities; our approach to veins preserves all the existing feature-rich environment and simply implements veins as a probability distribution modifier to existing drop configs (Expanding the features in 1.5.1 for global drop config design as well).

**v1.5.1** Adding in a few key QoL features for continued 1.13 support. Specifically (and see config.yml for examples and details) you can now specify drops in a global section, and pick those drops by name in the block drop configurations. You can also specify multiple blocks for whom the drop config should apply, all in the same block config. This should allow significantly improved "terseness" to configs, above and beyond what the prior subtype system allowed. Note you can mix and match global drops and local drop configs for a block, but you cannot mix single or multiple block types for a single block config.

**v1.5.0** Added Minecraft 1.13 support. Note that you WILL need to REDO your config. Although your config will load _as is_ it is unlikely to work as expected. Subtype support has been entirely removed and declarations of subtypes will be ignored. Spigot itself has renamed **many** materials to match their Minecraft normative style, and there is no backwards compatibility in HiddenOre. As of 1.5.0, no prior release of Minecraft is guaranteed active support, and will only be provided on a best-effort basis, if at all. IMPORTANT NOTE on config: All ItemStack serialization has been changed! There is a new `v:` specification. I've emulated it in the demonstration configs, but to avoid messy and unsupported prior version settings, re-export your serialized drops. 

**v1.4.2** Added full multiple world support. Standard config is used as default for any world that does not have a specific config. A new section, `worlds`
can be specified, each subsection is either the UUID or name of the world with a specific config. A single `blocks` subsection under the world identifier contains all the block configurations for that world. It is configured like the default, but within the world's blocks subsection. Check the configs for examples.

**v1.4.1** Live use of new tracker shows its disk use is much increased. Added a configuration option to explicitly disable it. Added config example of Command "drops" and some fixes.

**v1.4.0** New exploit tracker that tracks the actual blocks broken or exposed. This will fully prevent the "but I already checked that block" problem. Heuristic tracking is, for now, still active.

**v1.3.2** You can now run a command on block break. If you use reward crates, could gift, or custom /give, etc -- runs as SERVER so be careful. Use %player% to inject the player name into the command, or %uuid% to inject the player's Minecraft UUID.

**v1.3.1** Added a command for OPs only that generates all the drops configured. It has some quirks, but type /hiddenore generate to spawn the items.

**v1.2.9** Support for dusting the caves in your map with ore based on your active config; kind of an addendum to v1.2.7's feature. Can be used to prevent the "boring caves" problem when a world is otherwise devoid of ore from the 1.2.7 feature. Don't use your final config, do use a Generate only config, and do turn off drops if generate fails.

**v1.2.8** Support for altering drop chance based on 6 basic player effect states, generated by potion or beacon. Configurable by drop and biome (biome acts as override)

**v1.2.7** Experimental feature to allow stripping a world of ores during the generation phase. Fully configurable per-world by name; you can
  set it to replace any set of materials with a single material. Also includes a new anti-bypass method to directly target that "initial return" that can still occur from generators and place-break-place-break cycles
