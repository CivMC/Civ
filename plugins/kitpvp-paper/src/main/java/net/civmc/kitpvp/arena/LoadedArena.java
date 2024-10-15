package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.civmc.kitpvp.arena.data.Arena;

public record LoadedArena(
    PlayerProfile owner,
    Arena arena
) {

}
