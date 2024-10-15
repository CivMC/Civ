package net.civmc.kitpvp.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final SpawnProvider spawnProvider;

    public SpawnCommand(SpawnProvider spawnProvider) {
        this.spawnProvider = spawnProvider;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        Location spawn = spawnProvider.getSpawn();
        if (spawn != null) {
            player.teleport(spawn);
            player.sendMessage(Component.text("Teleported to spawn", NamedTextColor.GREEN));
        }
        return true;
    }
}
