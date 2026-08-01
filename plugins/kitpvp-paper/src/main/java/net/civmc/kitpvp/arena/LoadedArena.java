package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.civmc.kitpvp.arena.data.Arena;
import java.util.List;

public final class LoadedArena {

    private final PlayerProfile owner;
    private final Arena arena;
    private final List<PlayerProfile> invitedPlayers;
    private final int rankedId;
    private ArenaGravity gravity;
    private boolean gravityLocked;

    public LoadedArena(PlayerProfile owner, Arena arena, List<PlayerProfile> invitedPlayers, int rankedId) {
        this.owner = owner;
        this.arena = arena;
        this.invitedPlayers = invitedPlayers;
        this.rankedId = rankedId;
        this.gravity = ArenaGravity.MAIN;
    }

    public PlayerProfile owner() {
        return owner;
    }

    public Arena arena() {
        return arena;
    }

    public List<PlayerProfile> invitedPlayers() {
        return invitedPlayers;
    }

    public int rankedId() {
        return rankedId;
    }

    public boolean ranked() {
        return rankedId >= 0;
    }

    public ArenaGravity gravity() {
        return gravity;
    }

    public boolean gravityLocked() {
        return gravityLocked;
    }

    public boolean setGravity(ArenaGravity gravity) {
        if (gravityLocked) {
            return false;
        }
        this.gravity = gravity;
        return true;
    }

    public void lockGravity() {
        this.gravityLocked = true;
    }
}
