package net.civmc.kitpvp.arena.data;

import java.util.List;

public interface ArenaDao {

    List<Arena> getArenas();

    boolean newArena(Arena arena);

    boolean deleteArena(String arenaName);

    boolean setDisplayName(String arenaName, String displayName);

    boolean setCategory(String arenaName, String category);
}
