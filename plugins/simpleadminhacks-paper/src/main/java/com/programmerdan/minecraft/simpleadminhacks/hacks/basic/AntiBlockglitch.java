package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import io.papermc.paper.entity.TeleportFlag;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
    private static final int MAX_TELEPORT_DOWN = 2;
    private static final int TRACKING_DURATION = 800;

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
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) {
            return;
        }
        final Player player = event.getPlayer();
        final Deque<PlaceRecord> records = this.cancelledPlacements.get(player.getUniqueId());
        if (records == null || records.isEmpty()) {
            return;
        }

        purgeOldRecords(records, System.currentTimeMillis());

        final Location playerLoc = player.getLocation();
        final double playerMinX = playerLoc.getX() - HITBOX_EXPANSION;
        final double playerMaxX = playerLoc.getX() + HITBOX_EXPANSION;
        final double playerMinZ = playerLoc.getZ() - HITBOX_EXPANSION;
        final double playerMaxZ = playerLoc.getZ() + HITBOX_EXPANSION;
        final double playerFeetY = playerLoc.getY();

        for (PlaceRecord record : records) {
            final double blockMinX = record.x() - HITBOX_EXPANSION;
            final double blockMaxX = record.x() + 1 + HITBOX_EXPANSION;
            final double blockMinZ = record.z() - HITBOX_EXPANSION;
            final double blockMaxZ = record.z() + 1 + HITBOX_EXPANSION;
            final double blockMinY = record.y() - MAX_DOWN_EXPANSION;
            final double blockMaxY = record.y() + 1 + MAX_UP_EXPANSION;

            final boolean overlapsX = playerMaxX > blockMinX && playerMinX < blockMaxX;
            final boolean overlapsZ = playerMaxZ > blockMinZ && playerMinZ < blockMaxZ;
            final boolean overlapsY = playerFeetY >= blockMinY && playerFeetY < blockMaxY;

            if (!overlapsX || !overlapsZ || !overlapsY) {
                continue;
            }

            final Location groundLoc = findGround(player);
            if (groundLoc == null) {
                continue;
            }

            player.teleport(groundLoc, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION);
            plugin().info(player.getName() + " was teleported down due to cancelled block placement at "
                + record.x() + ", " + record.y() + ", " + record.z());
            records.clear();
            return;
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.cancelledPlacements.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Checks whether any solid block at the given Y level overlaps with the player's
     * hitbox footprint (feet position +/- {@link #HITBOX_EXPANSION} on X and Z).
     */
    private boolean isSupportedAt(final Player player, final int y) {
        final Location playerLoc = player.getLocation();
        final World world = playerLoc.getWorld();
        final int minBlockX = (int) Math.floor(playerLoc.getX() - HITBOX_EXPANSION);
        final int maxBlockX = (int) Math.floor(playerLoc.getX() + HITBOX_EXPANSION);
        final int minBlockZ = (int) Math.floor(playerLoc.getZ() - HITBOX_EXPANSION);
        final int maxBlockZ = (int) Math.floor(playerLoc.getZ() + HITBOX_EXPANSION);

        for (int bx = minBlockX; bx <= maxBlockX; bx++) {
            for (int bz = minBlockZ; bz <= maxBlockZ; bz++) {
                if (world.getBlockAt(bx, y, bz).getType().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds a safe ground location below the player within {@link #MAX_TELEPORT_DOWN} blocks.
     * Checks the full hitbox footprint at each Y level so that a player standing on the
     * edge of a block (within the 0.3 hitbox expansion) is correctly detected as supported.
     * Returns null if the player is already on solid ground or no safe ground is found.
     */
    @org.jetbrains.annotations.Nullable
    private Location findGround(final Player player) {
        final Location playerLoc = player.getLocation();
        final int feetBlockY = playerLoc.getBlockY();

        // If any block within the player's hitbox footprint at Y-1 is solid, the player
        // is already supported and does not need teleporting.
        if (isSupportedAt(player, feetBlockY - 1)) {
            return null;
        }

        // Search downward from the player's feet for solid ground
        for (int downOffset = 2; downOffset <= MAX_TELEPORT_DOWN + 1; downOffset++) {
            final int checkY = feetBlockY - downOffset;
            if (isSupportedAt(player, checkY)) {
                final Location target = new Location(
                    playerLoc.getWorld(),
                    playerLoc.getX(),
                    checkY + 1,
                    playerLoc.getZ(),
                    playerLoc.getYaw(),
                    playerLoc.getPitch()
                );

                // Verify the two blocks above ground are passable (room for player)
                final Block above1 = target.getBlock();
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
