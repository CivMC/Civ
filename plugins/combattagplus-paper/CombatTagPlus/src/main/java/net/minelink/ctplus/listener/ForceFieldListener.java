package net.minelink.ctplus.listener;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.bukkit.block.BlockFace.*;

public final class ForceFieldListener implements Listener {

    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(NORTH, EAST, SOUTH, WEST);

    private static final int NUM_THREADS = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);

    private final CombatTagPlus plugin;

    private final Map<UUID, Set<Location>> previousUpdates = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS,
            new ThreadFactoryBuilder().setNameFormat("CombatTagPlus ForceField Thread #%d").build());

    private final Map<UUID, Lock> playerLocks = new HashMap<>();

    public ForceFieldListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void addPlayer(PlayerJoinEvent event) {
        playerLocks.put(event.getPlayer().getUniqueId(), new ReentrantLock());
    }

    @EventHandler
    public void removePlayer(PlayerQuitEvent event) {
        playerLocks.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateViewedBlocks(PlayerMoveEvent event) {
        // Do nothing if check is not active
        if (!plugin.getSettings().useForceFields()) return;

        // Do nothing if player hasn't moved over a whole block
        Location t = event.getTo();
        Location f = event.getFrom();
        if (t.getBlockX() == f.getBlockX() && t.getBlockY() == f.getBlockY() &&
                t.getBlockZ() == f.getBlockZ()) {
            return;
        }

        final Player player = event.getPlayer();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // Update the players force field perspective and find all blocks to stop spoofing
                UUID uuid = player.getUniqueId();

                // Stop processing if player has logged off
                if (!plugin.getPlayerCache().isOnline(uuid)) {
                    previousUpdates.remove(uuid);
                    return;
                }

                Set<Location> removeBlocks;
                Set<Location> changedBlocks = getChangedBlocks(player);
                Material forceFieldMaterial = Material.getMaterial(plugin.getSettings().getForceFieldMaterial());
                byte forceFieldMaterialDamage = plugin.getSettings().getForceFieldMaterialDamage();

                Lock lock = playerLocks.get(uuid);
                if (lock == null) return;

                lock.lock();
                try {
                    if (previousUpdates.containsKey(uuid)) {
                        removeBlocks = previousUpdates.get(uuid);
                    } else {
                        removeBlocks = new HashSet<>();
                    }

                    for (Location location : changedBlocks) {
                        player.sendBlockChange(location, forceFieldMaterial, forceFieldMaterialDamage);
                        removeBlocks.remove(location);
                    }

                    // Remove no longer used spoofed blocks
                    for (Location location : removeBlocks) {
                        Block block = location.getBlock();
                        player.sendBlockChange(location, block.getType(), block.getData());
                    }

                    previousUpdates.put(uuid, changedBlocks);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    private Set<Location> getChangedBlocks(Player player) {
        Set<Location> locations = new HashSet<>();

        // Do nothing if player is not tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return locations;

        // Find the radius around the player
        int r = plugin.getSettings().getForceFieldRadius();
        Location l = player.getLocation();
        Location loc1 = l.clone().add(r, 0, r);
        Location loc2 = l.clone().subtract(r, 0, r);
        int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
        int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

        // Iterate through all blocks surrounding the player
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                // Location corresponding to current loop
                Location location = new Location(l.getWorld(), (double) x, l.getY(), (double) z);

                // PvP is enabled here, no need to do anything else
                if (plugin.getHookManager().isPvpEnabledAt(location)) continue;

                // Check if PvP is enabled in a location surrounding this
                if (!isPvpSurrounding(location)) continue;

                for (int i = -r; i < r; i++) {
                    Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());

                    loc.setY(loc.getY() + i);

                    // Do nothing if the block at the location is not air
                    if (!loc.getBlock().getType().equals(Material.AIR)) continue;

                    // Add this location to locations
                    locations.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
        }

        return locations;
    }

    private boolean isPvpSurrounding(Location loc) {
        for (BlockFace direction : ALL_DIRECTIONS) {
            if (plugin.getHookManager().isPvpEnabledAt(loc.getBlock().getRelative(direction).getLocation())) {
                return true;
            }
        }

        return false;
    }

}
