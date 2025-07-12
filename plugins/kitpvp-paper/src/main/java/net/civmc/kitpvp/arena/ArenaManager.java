package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.UUID;
import java.util.logging.Level;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.spawn.SpawnProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaManager {

    private static final SlimePropertyMap SLIME_PROPERTIES;

    static {
        SLIME_PROPERTIES = new SlimePropertyMap();
        SLIME_PROPERTIES.setValue(SlimeProperties.ALLOW_ANIMALS, false);
        SLIME_PROPERTIES.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        SLIME_PROPERTIES.setValue(SlimeProperties.DIFFICULTY, "hard");
    }

    private int maxArenas;
    private final JavaPlugin plugin;
    private final SpawnProvider spawn;
    private final MysqlLoader templateLoader;
    private final SequencedMap<UUID, LoadedArena> arenas = new LinkedHashMap<>();

    public ArenaManager(int maxArenas, JavaPlugin plugin, SpawnProvider spawn, MysqlLoader templateLoader) {
        this.maxArenas = maxArenas;
        this.plugin = plugin;
        this.spawn = spawn;
        this.templateLoader = templateLoader;
    }

    public void setMaxArenas(int maxArenas) {
        this.maxArenas = maxArenas;
    }

    public List<String> listArenas() throws IOException {
        return templateLoader.listWorlds();
    }

    public boolean hasArena(Player player) {
        return arenas.containsKey(player.getUniqueId());
    }

    public List<LoadedArena> getArenas() {
        return new ArrayList<>(arenas.sequencedValues());
    }

    public void deleteArena(PlayerProfile owner) {
        LoadedArena removedArena = arenas.remove(owner.getId());
        if (removedArena == null) {
            return;
        }
        Arena arena = removedArena.arena();
        String worldName = getArenaName(arena.name(), owner);
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Location spawn = this.spawn.getSpawn();
            for (Player worldPlayer : world.getPlayers()) {
                if (spawn != null) {
                    worldPlayer.sendMessage(Component.text("The arena you were in was deleted, so you have been teleported to spawn.", NamedTextColor.GREEN));
                    if (worldPlayer.getGameMode() == GameMode.SPECTATOR && !worldPlayer.hasPermission("kitpvp.admin")) {
                        worldPlayer.setGameMode(GameMode.SURVIVAL);
                    }
                    worldPlayer.teleport(spawn);
                } else {
                    worldPlayer.kick(Component.text("The arena you were in was deleted"));
                }
            }
        }
        Bukkit.unloadWorld(worldName, false);
    }

    public String getArenaName(String arena, PlayerProfile owner) {
        return "dynamicarena." + owner.getName() + "." + arena;
    }

    public boolean isArena(String worldName) {
        return worldName.startsWith("dynamicarena.");
    }

    public void createArena(Player player, Arena arena, boolean isPublic) {
        if (maxArenas >= 0 && this.arenas.size() >= maxArenas && !player.hasPermission("kitpvp.admin")) {
            player.sendMessage(Component.text("You cannot create an arena because there are too many arenas loaded", NamedTextColor.RED));
            return;
        }

        AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
        SlimeWorld slimeWorld;
        try {
            slimeWorld = api.readWorld(templateLoader, arena.name(), true, SLIME_PROPERTIES);
        } catch (UnknownWorldException e) {
            throw new RuntimeException(e);
        } catch (IOException | NewerFormatException | CorruptedWorldException e) {
            plugin.getLogger().log(Level.WARNING, "Unable to load arena", e);
            player.sendMessage(Component.text("Unable to load arena", NamedTextColor.RED));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            String worldName = getArenaName(arena.name(), player.getPlayerProfile());
            api.loadWorld(slimeWorld.clone(worldName), true);
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return;
            }

            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            world.setGameRule(GameRule.DO_VINES_SPREAD, false);
            world.setGameRule(GameRule.KEEP_INVENTORY, false);
            world.setFullTime(6000);

            arenas.put(player.getUniqueId(), new LoadedArena(player.getPlayerProfile(), arena, isPublic ? null : new ArrayList<>()));

            player.sendMessage(Component.text("Your world is ready. Open ", NamedTextColor.GOLD)
                .append(Component.text("/arena", NamedTextColor.YELLOW))
                .append(Component.text(" to go to your world.", NamedTextColor.GOLD)));
            if (!isPublic) {
                player.sendMessage(Component.text("Use ", NamedTextColor.GOLD)
                    .append(Component.text("/arena add <player>", NamedTextColor.YELLOW))
                    .append(Component.text(" to allow other players to join.", NamedTextColor.GOLD)));
            }
        });
    }
}
