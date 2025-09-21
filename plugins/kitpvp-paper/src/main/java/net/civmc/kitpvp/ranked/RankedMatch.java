package net.civmc.kitpvp.ranked;

import net.civmc.kitpvp.arena.LoadedArena;
import org.bukkit.entity.Player;
import java.time.Instant;

public record RankedMatch(Player player, double playerElo, Player opponent, double opponentElo, LoadedArena arena, Instant started, boolean unranked) {

}
