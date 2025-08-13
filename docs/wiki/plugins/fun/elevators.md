---
title: Elevators
description: Elevators Plugin
---

# Elevators

An elevator is a block that allows teleporting either directly upwards or downwards to another elevator. CivMC uses lodestones as elevators.

### Mechanics

When standing on an elevator, by default, jump to go to the elevator above, and shift to go to the elevator below. In the server config (`/config`) you can change this setting so that instead when you right click you go to the elevator above and left click to go to the elevator below. 

![Elevator Config](https://github.com/user-attachments/assets/699117fe-aafa-4096-b523-caa163c23d65)

To be considered valid, an elevator block must have at least two (2) empty air blocks above it (you are able to teleport to a lodestone if there is water in the two blocks above it). You can place blocks like a trapdoor and iron bars on a lodestone to restrict movement down to the lodestone, making it one-way to the elevator above only.

In this example, you can move from the bottom to the top lodestones but you cannot go from the top lodestones to the bottom ones.

![image](https://github.com/user-attachments/assets/f1be70f7-b436-4afb-830c-c9d0f15e2c0f)



[Reinforcing](https://civmc.net/wiki/plugins/essential/citadel.html) an elevator will not lock it to that [NameLayer](https://civmc.net/wiki/plugins/essential/namelayer.html) group. It will remain usable by all players.
