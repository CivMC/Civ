package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.civmc.kitpvp.arena.data.Arena;
import java.util.List;

public record LoadedArena(
    PlayerProfile owner,
    Arena arena,
    List<PlayerProfile> invitedPlayers,
    int rankedId
) {
    public boolean ranked() {
        return rankedId >= 0;
    }
}
