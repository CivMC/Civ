package net.civmc.heliodor.command;

import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.heliodor.HeliodorGem;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.VeinCache;
import net.civmc.heliodor.vein.VeinSpawner;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class HeliodorDebugCommand implements CommandExecutor {

    private final VeinCache cache;
    private final VeinSpawner spawner;
    private final NamespacedKey oreLocationsKey;

    public HeliodorDebugCommand(VeinCache cache, VeinSpawner spawner, NamespacedKey oreLocationsKey) {
        this.cache = cache;
        this.spawner = spawner;
        this.oreLocationsKey = oreLocationsKey;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("givemeteoricironnugget")) {
                player.getInventory().addItem(MeteoricIron.METEORIC_IRON_NUGGET.createItem());
                return true;
            } else if (args[0].equalsIgnoreCase("givefinishedheliodorgem")) {
                player.getInventory().addItem(HeliodorGem.FINISHED_HELIODOR_GEM.createItem());
                return true;
            } else if (args[0].equalsIgnoreCase("giveheliodorgem")) {
                player.getInventory().addItem(HeliodorGem.createHeliodorGem(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
                return true;
            } else if (args[0].equalsIgnoreCase("listveins")) {
                for (Vein vein : cache.getVeins()) {
                    player.sendMessage(Component.text(vein.toString()));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("listores")) {
                PersistentDataContainer chunkPdc = player.getLocation().getChunk().getPersistentDataContainer();
                int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
                if (ints == null || ints.length == 0) {
                    player.sendMessage(Component.text("No ores in this chunk."));
                    return true;
                }
                for (int i = 0; i < ints.length; i += 3) {
                    player.sendMessage(Component.text(ints[i] + " " + ints[i + 1] + " " + ints[i + 2]));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("spawnmeteoricironvein")) {
                if (!spawner.checkValidMeteoricIronConfig()) {
                    player.sendMessage(Component.text("Invalid meteoric iron config. Cannot spawn"));
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(HeliodorPlugin.class), () -> {
                    if (!spawner.trySpawnMeteoricIron()) {
                        player.sendMessage(Component.text("Spawn attempt failed. Try again?"));
                    } else {
                        player.sendMessage(Component.text("Spawned meteoric iron vein"));
                    }
                });
                return true;
            }
        }
        return false;
    }
}
