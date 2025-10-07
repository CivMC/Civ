package net.civmc.kitpvp.ranked;

import net.civmc.kitpvp.arena.LoadedArena;
import org.bukkit.entity.Player;
import java.time.Instant;
import java.util.Objects;

public final class RankedMatch {
    private final Player player;
    private final double playerElo;
    private final Player opponent;
    private final double opponentElo;
    private final LoadedArena arena;
    private final Instant started;
    private final boolean unranked;

    private double playerDamageDealt;
    private double opponentDamageDealt;

    public RankedMatch(Player player, double playerElo, Player opponent, double opponentElo, LoadedArena arena, Instant started, boolean unranked) {
        this.player = player;
        this.playerElo = playerElo;
        this.opponent = opponent;
        this.opponentElo = opponentElo;
        this.arena = arena;
        this.started = started;
        this.unranked = unranked;
    }

    public Player player() {
        return player;
    }

    public double playerElo() {
        return playerElo;
    }

    public Player opponent() {
        return opponent;
    }

    public double opponentElo() {
        return opponentElo;
    }

    public LoadedArena arena() {
        return arena;
    }

    public Instant started() {
        return started;
    }

    public boolean unranked() {
        return unranked;
    }

    public double getPlayerDamageDealt() {
        return playerDamageDealt;
    }

    public double getOpponentDamageDealt() {
        return opponentDamageDealt;
    }

    public void addPlayerDamageDealt(double playerDamageDealt) {
        this.playerDamageDealt += playerDamageDealt;
    }

    public void addOpponentDamageDealt(double opponentDamageDealt) {
        this.opponentDamageDealt += opponentDamageDealt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RankedMatch) obj;
        return Objects.equals(this.player, that.player) &&
            Double.doubleToLongBits(this.playerElo) == Double.doubleToLongBits(that.playerElo) &&
            Objects.equals(this.opponent, that.opponent) &&
            Double.doubleToLongBits(this.opponentElo) == Double.doubleToLongBits(that.opponentElo) &&
            Objects.equals(this.arena, that.arena) &&
            Objects.equals(this.started, that.started) &&
            this.unranked == that.unranked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, playerElo, opponent, opponentElo, arena, started, unranked);
    }

    @Override
    public String toString() {
        return "RankedMatch[" +
            "player=" + player + ", " +
            "playerElo=" + playerElo + ", " +
            "opponent=" + opponent + ", " +
            "opponentElo=" + opponentElo + ", " +
            "arena=" + arena + ", " +
            "started=" + started + ", " +
            "unranked=" + unranked + ']';
    }


}
