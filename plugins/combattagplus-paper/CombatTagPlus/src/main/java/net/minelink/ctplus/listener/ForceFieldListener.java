package net.minelink.ctplus.listener;

import com.google.common.collect.ImmutableList;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.block.BlockFace.*;

public final class ForceFieldListener implements Listener {

    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(NORTH, EAST, SOUTH, WEST);

    private final CombatTagPlus plugin;

    private Map<UUID, List<Location>> previousUpdates = new HashMap<>();

    public ForceFieldListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void updateViewedBlocks(PlayerMoveEvent event) {
        // Do nothing if check is not active
        if (!plugin.getSettings().useForceFields()) return;

        // Do nothing if player hasn't moved over a whole block
        Location t = event.getTo();
        Location f = event.getFrom();
        if (!(t.getBlockX() != f.getBlockX() ||
                t.getBlockY() != f.getBlockY() ||
                t.getBlockZ() != f.getBlockZ())) {
            return;
        }

        // Update the players force field perspective and find all blocks to stop spoofing
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        List<Location> removeBlocks;
        List<Location> changedBlocks = getChangedBlocks(player);

        if (previousUpdates.containsKey(uuid)) {
            removeBlocks = previousUpdates.get(uuid);
        } else {
            removeBlocks = new ArrayList<>();
        }

        for (Location location : changedBlocks) {
            player.sendBlockChange(location, Material.STAINED_GLASS, (byte) 14);
            removeBlocks.remove(location);
        }

        // Prevent sneaky players crossing the force field
        if (!changedBlocks.isEmpty() && !plugin.isPvpEnabledAt(t) && plugin.isPvpEnabledAt(f)) {
            event.setTo(f.setDirection(t.getDirection()));
        }

        // Remove no longer used spoofed blocks
        for (Location location : removeBlocks) {
            Block block = location.getBlock();
            player.sendBlockChange(location, block.getType(), block.getData());
        }

        previousUpdates.put(uuid, changedBlocks);
    }

    private List<Location> getChangedBlocks(Player player) {
        List<Location> locations = new ArrayList<>();

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
                if (plugin.isPvpEnabledAt(location)) continue;

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
            if (!plugin.isPvpEnabledAt(loc.getBlock().getRelative(direction).getLocation())) continue;
            return true;
        }

        return false;
    }

}
