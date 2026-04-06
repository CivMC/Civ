package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Prevents players from glitching through blocks by detecting cancelled block placements
 * and teleporting the player down to the ground if they are standing inside the cancelled
 * block's area.
 */
public final class AntiBlockglitch extends BasicHack {

    private static final double HITBOX_EXPANSION = 0.3;
    private static final int MAX_UP_EXPANSION = 3;
    private static final int MAX_DOWN_EXPANSION = 2;
    private static final int MAX_TELEPORT_DOWN = 3;
    private static final int TRACKING_DURATION = 30_000;

    private final Map<UUID, Deque<PlaceRecord>> cancelledPlacements = new HashMap<>();

    public AntiBlockglitch(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onDisable() {
        this.cancelledPlacements.clear();
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final long now = System.currentTimeMillis();

        final Deque<PlaceRecord> records = this.cancelledPlacements
            .computeIfAbsent(player.getUniqueId(), uuid -> new ArrayDeque<>());
        records.addLast(new PlaceRecord(block.getX(), block.getY(), block.getZ(), now));
        purgeOldRecords(records, now);

        checkAndTeleport(player, block);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.cancelledPlacements.remove(event.getPlayer().getUniqueId());
    }

    private void checkAndTeleport(final Player player, final Block placed) {
        final Location playerLoc = player.getLocation();

        // Player bounding box edges (feet position +/- hitbox expansion)
        final double playerMinX = playerLoc.getX() - HITBOX_EXPANSION;
        final double playerMaxX = playerLoc.getX() + HITBOX_EXPANSION;
        final double playerMinZ = playerLoc.getZ() - HITBOX_EXPANSION;
        final double playerMaxZ = playerLoc.getZ() + HITBOX_EXPANSION;
        final double playerFeetY = playerLoc.getY();

        // The cancelled block's expanded area
        final double blockMinX = placed.getX() - HITBOX_EXPANSION;
        final double blockMaxX = placed.getX() + 1 + HITBOX_EXPANSION;
        final double blockMinZ = placed.getZ() - HITBOX_EXPANSION;
        final double blockMaxZ = placed.getZ() + 1 + HITBOX_EXPANSION;
        final double blockMinY = placed.getY() - MAX_DOWN_EXPANSION;
        final double blockMaxY = placed.getY() + 1 + MAX_UP_EXPANSION;

        // Check if player overlaps with the expanded block area
        final boolean overlapsX = playerMaxX > blockMinX && playerMinX < blockMaxX;
        final boolean overlapsZ = playerMaxZ > blockMinZ && playerMinZ < blockMaxZ;
        final boolean overlapsY = playerFeetY >= blockMinY && playerFeetY < blockMaxY;

        if (!overlapsX || !overlapsZ || !overlapsY) {
            return;
        }

        // Find ground below the player (up to MAX_TELEPORT_DOWN blocks down)
        final Location groundLoc = findGround(placed.getLocation(), player);
        if (groundLoc == null) {
            return;
        }

        player.teleport(groundLoc, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION);
        plugin().info(player.getName() + " was teleported down due to cancelled block placement at "
            + placed.getX() + ", " + placed.getY() + ", " + placed.getZ());
    }

    /**
     * Finds a safe ground location below the player within {@link #MAX_TELEPORT_DOWN} blocks.
     * Returns null if no ground is found within range.
     */
    @org.jetbrains.annotations.Nullable
    private Location findGround(Location placed, final Player player) {
        final Location playerLoc = player.getLocation();
        final Block feetBlock = placed.getBlock();

        // Check blocks below the player's feet for solid ground
        for (int downOffset = 0; downOffset <= MAX_TELEPORT_DOWN; downOffset++) {
            final Block checkBlock = feetBlock.getRelative(BlockFace.DOWN, downOffset + 1);
            if (checkBlock.getType().isSolid()) {
                // Found solid ground; teleport location is on top of this block
                final Location target = new Location(
                    playerLoc.getWorld(),
                    playerLoc.getX(),
                    checkBlock.getY() + 1,
                    playerLoc.getZ(),
                    playerLoc.getYaw(),
                    playerLoc.getPitch()
                );

                // Verify the two blocks above ground are passable (room for player)
                final Block above1 = target.getBlock().getRelative(BlockFace.UP);
                final Block above2 = above1.getRelative(BlockFace.UP);
                if (!above1.getType().isSolid() && !above2.getType().isSolid()) {
                    return target;
                }
            }
        }
        return null;
    }

    private void purgeOldRecords(final Deque<PlaceRecord> records, final long now) {
        final long cutoff = now - TRACKING_DURATION;
        final Iterator<PlaceRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().timestamp() < cutoff) {
                iterator.remove();
            } else {
                break; // Records are in chronological order, remaining are still valid
            }
        }
    }

    private record PlaceRecord(int x, int y, int z, long timestamp) {

    }
}
