# Finale 2.1 Draft

## Bug fixes

- Fixed no vehicle damage bug.
- Fixed erratic knockback.
- Fixed sprinting with no hunger bug.

*Below changes are experimental; some have already been written and being tested on Helios.*

## Allies

Bow & Arrows have been severely underpowered and in the context of larger-scale fights generally more of a liability than a viable weapon. This is partly because in these bigger fights, you're just as likely to hit your teammates as you are to hit the enemy.

By marking your friends as allies: 
- Their name becomes blue.
- They become visible while invisible.
- Projectile friendly-fire is disabled; projectiles will go through your allies.
- Their warp fruit animation particles become blue.

Melee friendly-fire is still enabled.

Commands:
- /ally add <player-name> - Mark another player as an ally.
- /ally remove <player-name> - Remove another player as an ally.
- /ally list - List your allies.

```
ally:
  enabled: true
  seeInvis: true # should you be able to see invisible allies?
  animateLink: # settings for animating adding/removing ally
    enabled: true
    maxDistance: 20
  storage:
    file: ally.sqlite
```

## Enhanced Tipped Arrows

Finale now has options to configure the duration of all tipped arrows.

```
tippedArrows:
    nightvision:
        name: Arrow of Night Vision
        type: NIGHT_VISION
        durations:
            normal: 440
            extended: 1200
            amplified: 220
        color:
            r: 3
            g: 32
            b: 126
    regen:
        name: Arrow of Regeneration
        type: REGEN
        durations: # duration in ticks (20 ticks = 1 second)
            normal: 100
            extended: 220
            amplified: 40
        color:
            r: 188
            g: 102
            b: 146
```

Another issue of the arrow is that they've always been pretty hard to hit, particularly when there is a large ping disparity between players, so most arrows don't do much damage and don't land on target.

After travelling [x] metres, tipped arrows will have a tiny area of effect of [y] metres.

```
enhancedArrows:
    enabled: true
    minDistance: 10 # how far should arrow travel for arrows to become area of effect?
    radius: 2
    damage: 1
    allyDamageReduction: 1 # how much damage to reduce against allied players? 1 = negate all damage, 0 = full friendly fire
    allyCollide: false # should arrows collide with allies?
```

## Warp Fruit

This isn't a silver bullet against trapping but it helps.

Warp fruit tracks your previous locations going back [x] seconds in the past. When you eat a chorus fruit, you teleport back to where you were [x] seconds ago. This ability then goes on cooldown for [y] seconds.

While you are eating the warp fruit, there is an animation and particles marking where you are and where you're going to teleport to. You also glow while eating (spectral).

During this animation, the particles will show as green to yourself, blue to allies, and purple to everyone else.

```
# the warp fruit will roll back to 7 seconds ago
warpFruit: # logSize * logInterval = how far back in time you go
    logSize: 35 # log queue size
    logInterval: 200ms # in milliseconds
    cooldown: 10s
    maxDistance: 100 # max warp distance
    spectralWhileChanneling: true
```

## New Offhands

Food and knockback 2 swords have been offhand favourites for a long time now; it's time we bring Tridents and Shields into the fold.

### Shield

In our pvp system, shields have had almost exclusively an ornamental value. The following changes will give it defensive value within the context of our system.

After activating block with your shield, you will boost forward in the direction you are looking and proportionally push back enemies in your way. This ability goes on cooldown for [x] seconds.

Another option is a passive resistance effect while a shield is equipped.

```
shield:
  bash:
    enabled: true
    activationResistance:
      enabled: true
      amplifier: 0
      duration: 200
  passive:
    enabled: false
    amplifier: 0
```

### Trident

In the vanilla game, if you throw your trident from your offhand and pick it back up (from loyalty or off the ground), it won't return to your offhand but simply be added back to your main inventory. An offhand that you keep having to put back in your offhand isn't a great offhand. So now, if you pick up your trident - of which you threw from your offhand - and your offhand slot is available, then it will return to your offhand. If your offhand slot is unavailable, then it goes to your main inventory like normal.

Riptide will go on cooldown for [x] seconds after use.

Tridents, in general, will go on cooldown for [y] seconds after use.

Tridents have a valid damage modifier:
```
damageModifiers:
    ...
    TRIDENT:
        multiplier: 1.0
        mode: DIRECT
    ...
```

## Crossbows

Fireworks have a valid damage modifier now, so damage from fireworks towards players can be adjusted.

```
damageModifiers:
    ...
    FIREWORK:
        multiplier: 1.0
        mode: DIRECT
    ...
```

Fireworks can damage reinforcements and bastion fields.
Firing fireworks from crossbows will go on cooldown for [x] seconds after use.

```
crossbow:
  reinforcementDamage: 5
  bastionDamage: 5
  cooldown: 4000ms
```

## Block Place Combat Restrictions

While in combat, the following restrictions can be placed:
- A blacklist of blocks that can't be placed.
- A whitelist of blocks that can be placed.
- A list of blocks that have a cooldown on placement.

```
blockPlacementRestrictions:
  enabled: true
  mode: BLACKLIST # BLACKLIST, WHITELIST
  blacklist:
    - COBWEB
    - OBSIDIAN
  whitelist:
    - WATER_BUCKET
    - COBBLESTONE
  cooldowns:
    cobweb:
      type: COBWEB
      cooldown: 5s
    obsidian:
      type: OBSIDIAN
      cooldown: 1s
```