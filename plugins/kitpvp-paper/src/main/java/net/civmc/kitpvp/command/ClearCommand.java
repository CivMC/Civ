package net.civmc.kitpvp.command;

import net.civmc.kitpvp.KitApplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (player.getWorld().getName().startsWith("rankedarena.")) {
            player.sendMessage(Component.text("Clear command is deactivated in ranked arenas.", NamedTextColor.RED));
            return true;
        }
        KitApplier.reset(player);
        player.sendMessage(Component.text("Your inventory has been cleared", NamedTextColor.GREEN));
        return true;
    }
}
