---
title: Finale
description: Finale plugin
---

# Finale
The Finale plugin changes many aspects of PvP on CivMC such as reverting to pre 1.9 pvp, changing armor & weapons stats, buffing health potions and adding many new features/mechanics unique to the server.  

## Vanilla Changes
Finale changes many vanilla aspects of PvP, these changes are described below. 

### CPS limit
Unlike modern minecraft where attacks should be timed for maximum damage, CivMC mimics pre 1.8 pvp by making all weapon attackspeeds unlimited. Which means that clicking faster equals dealing more damage.
There is a limit however, clicking above 9 times a second (CPS) will cancel your attack and do no damage to your opponent.

### Damage Changes
a flat 20% reduction in damage is applied to all sources of damage except for: Swords, Arrows, Tridents and Fireworks. Critical hits do 18% less damage compared to vanilla. The strenght effect also gives 14% less extra damage. 
The sharpness enchant however gives a 23% boost in damage applied from the enchantment.

#### Weapon Damage
Because attack speed has no limit unlike vanilla, most weapons have had their damage output altered on CivMC.
Below is a list comparing Vanilla and CivMC damage values:
|       Weapon        | Vanilla | CivMC |     |    Weapon     | Vanilla | CivMC |
|:-------------------:|:-------:|:-----:| --- |:-------------:|:-------:|:-----:|
|     Wood Sword      |    4    |   3   |     |   Wood Axe    |    7    |   1   |
|     Gold Sword      |    4    |   4   |     |   Gold Axe    |    7    |   2   |
|     Stone Sword     |    5    |   5   |     |   Stone Axe   |    9    |   3   |
|     Iron Sword      |    6    |   6   |     |   Iron Axe    |    9    |   4   |
|    Diamond Sword    |    7    |   7   |     |  Diamond Axe  |    9    |   5   |
|   Netherite Sword   |    8    |   9   |     | Netherite Axe |   10    |   6   |
| Meteoric Iron Sword |    -    |   8   |     |    Trident    |   11    |   8   |


### Armor Changes
Netherite armor has also been altered slightly. See below how various pieces of equipment differ in the amount of armor and toughness they provide:
|     Armor piece      | Vanilla | CivMC |
|:--------------------:|:-------:|:-----:|
|   Netherite Helmet   |  A3/T3  | A4/T3 |
| Netherite Chestplate |  A8/T3  | A8/T3 |
|  Netherite Leggings  |  A6/T3  | A6/T3 |
|   Netherite Boots    |  A3/T3  | A4/T3 |

### Cooldowns
Some useable items have had cooldowns applied to them to prevent abuse and keep PvP balanced. The following items have cooldowns to their use: 
    
#### Pearls
Ender pearls have a cooldown of 16 seconds after use before one can be thrown again. This is to prevent spam throwing them to get away from combat easy.

#### Golden Apples
Golden apples have a cooldown of 15 second after use. This is to prevent constant easy healing and absorption while in combat making yourself invincible until your armor breaks. For the same reason enchanted golden apples have been disabled completely. 

#### Tridents
Tridents have a 10s cooldown after use, the riptide ability has a 60s cooldown. 

### Health potion buffs
Health restored by health potions has been given a 50% boost. A lvl 1 health potion that normally gives 4 health points (2 hearts) now gives 6 hp (3 hearths) And lvl 2 health potions that normally give 8 health points (4 hearts) now gives 12 hp (6 hearts)

### Disabled Enchants
Some enchants have been disabled completely through Finale. This only counts for Mending and Frost walker.

## New Features
Finale also adds certain new mechanics and features to PvP on CivMC. These are described below

### Warp Fruit (Currently only available on PvP server)
Chorus fruit serve a new purpose! Instead of teleporting your randomly they tp you back 7 seconds in time, for a maximum of 100 blocks. This allows players to escape from traps or being boxed in during a fight.
A cooldown of 180 seconds (3 minutes) is applied to warpfruit on use

While a player is consuming a warp fruit a green particle ring indicates to them where they will tp to. For other players this particle ring is purple instead.

### Shield Bash (Currently only available on PvP server)
Shields have the ability to bash. While right clicking with a shield to defend, the player activates the ability and dashes forward a couple blocks, if a damageable entity is hit this will do a small amount of damage and apply knockback to it/them.
A cooldown of 5 seconds is applied after activting this ability. Holding right click after activating it once will not activate the ability again after cooldown.

### Firework Crossbows (Currently only available on PvP server)
Using firework rockets with a crossbow makes the projectile explode with a 2 block radius on impact, destroying blocks and damaging reinforcements and bastions.
A single rocket fired this way does 5 reinforcement damage and/or 5 bastion damage.

A cooldown of 4 seconds is applied after firing. Preventing you from spamming rocket by preloading crosbows. 

### Enhanced Arrows (Currently only available on PvP server)
Potion tipped arrows explode in a radius on impact beyond a range of 10 blocks. This applies the effect to anyone in a 1 block radius of the impact, doing only 25% damage of a normal arrow.
There is no cooldown on this apart from how long it takes to draw a bow

### Ally system (Currently only available on PvP server)
Allies are marked with a blue nametag, players you have marked as allies don't take arrow damage fired by you. Nor have negative effects applied to them from tipped arrows fired by you.

The below commands apply to this feature:
- /ally add "player" (This marks a players as an ally to you)
- /ally remove "player" (This unmarks a player as an ally to you)
- /ally list (this shows a list of players marked as an ally to you)

### Small features
#### Netherite Axes
- Netherite axes do 2x extra damage to your opponents armor.

#### Netherite Armor Bonus
- Wearing a full set of netherite armor applies a permanent fire resistance effect 

#### Meteoric Iron Sword
- Opponents hit by a meteoric iron sword have slowness 1 applied to them for a second.
- Meteoric iron swords also instantly break cobwebs.

#### Meteoric Iron Armor
- Meteoric is equal to diamond armor in its base stats
- Every piece of meteoric iron armor equiped gives the wearer a 3% speed bonus, for a maximum of 12% when a full set is equiped.
- Wearing a full set of meteoric iron armor gives total immunity to fall damage.
- Meteoric Iron leggings have swift sneak 3 on them. An enchant exclusive to it as it cannot be found anywhere else.
