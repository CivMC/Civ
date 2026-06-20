package net.civmc.heliodor.command;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.heliodor.HeliodorGem;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
import net.civmc.heliodor.vein.VeinCache;
import net.civmc.heliodor.vein.VeinSpawner;
import net.civmc.heliodor.vein.data.Vein;
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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z")
        .withZone(ZoneId.systemDefault());

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
                player.getInventory().addItem(MeteoricIron.createMeteoricIronNugget());
                return true;
            } else if (args[0].equalsIgnoreCase("givefinishedheliodorgem")) {
                player.getInventory().addItem(HeliodorGem.createFinishedHeliodorGem());
                return true;
            } else if (args[0].equalsIgnoreCase("giveheliodorgem")) {
                player.getInventory().addItem(HeliodorGem.createHeliodorGem(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
                return true;
            } else if (args[0].equalsIgnoreCase("listveins")) {
                if (cache == null) {
                    player.sendMessage(Component.text("Veins are disabled"));
                } else {
                    for (Vein vein : cache.getVeins()) {
                        player.sendMessage(Component.text(vein.toString()));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("listores")) {
                if (oreLocationsKey == null) {
                    player.sendMessage(Component.text("Spawning is disabled"));
                    return true;
                }
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
                if (spawner == null) {
                    player.sendMessage(Component.text("Spawning is disabled"));
                    return true;
                }
                if (!spawner.checkValidMeteoricIronConfig()) {
                    player.sendMessage(Component.text("Invalid meteoric iron config. Cannot spawn"));
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(HeliodorPlugin.class), () -> {
                    if (spawner.trySpawnMeteoricIron() == null) {
                        player.sendMessage(Component.text("Spawn attempt failed. Try again?"));
                    } else {
                        player.sendMessage(Component.text("Spawned meteoric iron vein"));
                    }
                });
                return true;
            } else if (args[0].equalsIgnoreCase("forcepublicmeteor")) {
                if (spawner == null) {
                    player.sendMessage(Component.text("Spawning is disabled"));
                    return true;
                }
                spawner.forcePublicSpawn(vein -> {
                    if (vein == null) {
                        player.sendMessage(Component.text("Public meteor spawn attempt failed."));
                    } else {
                        player.sendMessage(Component.text("Forced public meteor spawn."));
                    }
                });
                return true;
            } else if (args[0].equalsIgnoreCase("nextpublicmeteor")) {
                if (spawner == null) {
                    sender.sendMessage(Component.text("Spawning is disabled"));
                    return true;
                }
                Long spawnAt = spawner.getNextPublicSpawnAt();
                if (spawnAt == null) {
                    sender.sendMessage(Component.text("Public meteor spawning is disabled or misconfigured."));
                    return true;
                }
                sender.sendMessage(Component.text("Next public meteor spawn is scheduled for "
                    + DATE_FORMAT.format(Instant.ofEpochMilli(spawnAt)) + " (in "
                    + formatDuration(Duration.ofMillis(Math.max(0L, spawnAt - System.currentTimeMillis()))) + ")."));
                return true;
            }
        }
        return false;
    }

    private static String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / 86_400L;
        long hours = totalSeconds % 86_400L / 3_600L;
        long minutes = totalSeconds % 3_600L / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
