package net.civmc.kitpvp.ranked;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnrankedCommand implements CommandExecutor {

    private final RankedQueueManager rankedQueueManager;

    public UnrankedCommand(RankedQueueManager rankedQueueManager) {
        this.rankedQueueManager = rankedQueueManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (this.rankedQueueManager.isInUnrankedQueue(player) || (args.length > 0 && args[0].equals("leave"))) {
            if (this.rankedQueueManager.leaveUnrankedQueue(player)) {
                player.sendMessage(Component.text("You have left the unranked queue", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("You are not in the unranked queue", NamedTextColor.GRAY));
            }
        } else {
            this.rankedQueueManager.joinUnrankedQueue(player, false);
        }

        return true;
    }
}
