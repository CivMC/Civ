package net.civmc.kitpvp.spawn;

import net.civmc.kitpvp.KitPvpPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {

    private final SpawnProvider spawnProvider;

    public SetSpawnCommand(SpawnProvider spawnProvider) {
        this.spawnProvider = spawnProvider;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!player.hasPermission("kitpvp.admin")) {
            player.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return true;
        }

        Location location = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            if (spawnProvider.setSpawn(location)) {
                player.sendMessage(Component.text("Set spawn", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Could not set spawn", NamedTextColor.RED));
            }
        });
        return true;
    }
}
