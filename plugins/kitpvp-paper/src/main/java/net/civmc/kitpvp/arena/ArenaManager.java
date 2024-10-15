package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI;
import com.infernalsuite.aswm.api.exceptions.CorruptedWorldException;
import com.infernalsuite.aswm.api.exceptions.NewerFormatException;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.UUID;
import java.util.logging.Level;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.spawn.SpawnProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

    private final JavaPlugin plugin;
    private final SpawnProvider spawn;
    private final MysqlLoader templateLoader;
    private final SequencedMap<UUID, LoadedArena> arenas = new LinkedHashMap<>();

    public ArenaManager(JavaPlugin plugin, SpawnProvider spawn, MysqlLoader templateLoader) {
        this.plugin = plugin;
        this.spawn = spawn;
        this.templateLoader = templateLoader;
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

    public void deleteArena(PlayerProfile owner, Player player) {
        Arena arena = arenas.remove(owner.getId()).arena();
        String worldName = getArenaName(arena.name(), player.getPlayerProfile());
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Location spawn = this.spawn.getSpawn();
            for (Player worldPlayer : world.getPlayers()) {
                if (spawn != null) {
                    player.sendMessage(Component.text("The arena you were in was deleted, so you have been teleported to spawn.", NamedTextColor.GREEN));
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

    public void createArena(Player player, Arena arena) {
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
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setFullTime(6000);

            arenas.put(player.getUniqueId(), new LoadedArena(player.getPlayerProfile(), arena));

            player.sendMessage(Component.text("Your world is ready. Open ", NamedTextColor.GOLD)
                .append(Component.text("/arena", NamedTextColor.YELLOW))
                .append(Component.text(" to go to your world.", NamedTextColor.GOLD)));
        });
    }
}
