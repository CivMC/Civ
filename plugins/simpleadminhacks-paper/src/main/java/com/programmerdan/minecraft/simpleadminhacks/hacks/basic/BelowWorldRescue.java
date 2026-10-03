package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public final class BelowWorldRescue extends BasicHack {

    private static final int RESCUE_DEPTH = 20;

    public BelowWorldRescue(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        final Location to = event.getTo();
        final World world = to.getWorld();
        if (to.getY() >= world.getMinHeight() - RESCUE_DEPTH) {
            return;
        }

        final Location surface = to.clone();
        surface.setY(world.getHighestBlockYAt(to, HeightMap.WORLD_SURFACE) + 1);
        if (player.teleport(surface)) {
            player.setVelocity(new Vector());
            player.setFallDistance(0);
        }
    }

}
