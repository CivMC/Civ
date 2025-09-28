package net.civmc.kitpvp.ranked;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankedCommand implements CommandExecutor {

    private final RankedQueueManager rankedQueueManager;

    public RankedCommand(RankedQueueManager rankedQueueManager) {
        this.rankedQueueManager = rankedQueueManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (this.rankedQueueManager.isInQueue(player) || (args.length > 0 && args[0].equals("leave"))) {
            if (this.rankedQueueManager.leaveQueue(player)) {
                player.sendMessage(Component.text("You have left the ranked queue", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("You are not in the ranked queue", NamedTextColor.GRAY));
            }
        } else {
            this.rankedQueueManager.joinQueue(player, false);
        }

        return true;
    }
}
