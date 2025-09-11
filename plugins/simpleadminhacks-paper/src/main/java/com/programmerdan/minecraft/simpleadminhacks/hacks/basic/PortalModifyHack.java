package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;

public class PortalModifyHack extends BasicHack {

    @AutoLoad
    private String targetWorld;
    @AutoLoad
    private String homeWorld;

    private final Map<Player, Location> teleports = new HashMap<>();

    public PortalModifyHack(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    //We want to go last incase any plugins want to cancel our attempt
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEnterPortal(EntityInsideBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getBlock().getType() != Material.END_PORTAL) {
            return;
        }
        World world = Bukkit.getWorld(targetWorld);
        if (world == null) {
            return;
        }
        Location to;
        switch (getTargetWorld(player).getEnvironment()) {
            case NETHER:
                to = new Location(getTargetWorld(player), player.getLocation().getX(), 125, player.getLocation().getZ());
                break;
            case NORMAL:
                to = getTargetWorld(player).getHighestBlockAt(player.getLocation(), HeightMap.WORLD_SURFACE).getLocation().toCenterLocation().add(0, 1, 0);
                break;
            default:
                return;
        }
        event.setCancelled(true);
        teleports.put(player, to);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        World world = Bukkit.getWorld(targetWorld);
        if (world == null) {
            return;
        }
        Player player = event.getPlayer();
        World targetWorld = getTargetWorld(player);
        if (targetWorld.getEnvironment() != World.Environment.NETHER) {
            return;
        }
        Location to = event.getTo();
        if (to.getBlockY() >= player.getWorld().getMinHeight() || to.getBlockY() == event.getFrom().getBlockY()) {
            return;
        }

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                int bx = to.getBlockX() + x;
                int bz = to.getBlockZ() + z;
                if (player.getWorld().getBlockAt(bx, player.getWorld().getMinHeight(), bz).getType() == Material.END_PORTAL) {
                    teleports.put(player, new Location(targetWorld, bx + 0.5, 125, bz + 0.5));
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        if (event.getTo().getWorld().getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (event.getTo().getY() >= 128) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        for (Map.Entry<Player, Location> entry : teleports.entrySet()) {
            Player player = entry.getKey();
            PlayerPortalEvent portalEvent = new PlayerPortalEvent(player, player.getLocation(), entry.getValue(), PlayerTeleportEvent.TeleportCause.END_PORTAL, 0, true, 0);
            Bukkit.getServer().getPluginManager().callEvent(portalEvent);
            //noinspection ConstantValue
            if (portalEvent.isCancelled() || portalEvent.getTo() == null || portalEvent.getTo().getWorld() == null) {
                continue;
            }
            player.teleport(portalEvent.getTo(), PlayerTeleportEvent.TeleportCause.END_PORTAL);
            if (portalEvent.getTo().getWorld().getName().equals(targetWorld)) {
                spawnExit(entry.getValue());
            }
        }
        teleports.clear();
    }

    private World getTargetWorld(Player player) {
        World target = Bukkit.getWorld(targetWorld);
        World home = Bukkit.getWorld(homeWorld);
        if (target == null) {
            return player.getWorld();
        }
        if (home == null) {
            return player.getWorld();
        }
        return player.getWorld().equals(target) ? home : target;
    }

    private void spawnExit(Location location) {
        if (location == null) {
            return;
        }
        //:Glad:
        if (location.getBlock().getRelative(0, -2, 0).getType() == Material.BEDROCK) {
            location.getBlock().getRelative(0, -2, 0).setType(Material.NETHERRACK);
        }
        ;
        if (location.getBlock().getRelative(0, -1, 0).getType() == Material.BEDROCK) {
            location.getBlock().getRelative(0, -1, 0).setType(Material.NETHERRACK);
        }
        location.getBlock().setType(Material.AIR);
        location.getBlock().getRelative(0, 1, 0).setType(Material.AIR);
        location.getBlock().getRelative(0, 2, 0).setType(Material.END_PORTAL);
    }
}
