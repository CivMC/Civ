package net.civmc.kitpvp.ranked;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EloCommand implements CommandExecutor {

    private final RankedPlayers players;

    public EloCommand(RankedPlayers players) {
        this.players = players;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        int elo = (int) Math.round(players.getElo(player.getUniqueId()));
        int rank = players.getRank(player.getUniqueId());

        player.sendMessage(Component.empty().append(Component.text("Elo: ", NamedTextColor.GOLD)).append(Component.text(elo, NamedTextColor.YELLOW)));

        String rankPlace = rank < 0 ? "unknown" : "#" + (rank + 1);
        player.sendMessage(Component.empty().append(Component.text("Rank: ", NamedTextColor.GOLD)).append(Component.text(rankPlace, NamedTextColor.YELLOW)));
        return true;
    }
}
