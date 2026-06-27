package net.civmc.zorweth.mechanics;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderSignal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public final class EnderEyeListener implements Listener {

    private final String worldName;
    private final List<PortalPosition> portals;

    public EnderEyeListener(final String worldName, final List<PortalPosition> portals) {
        this.worldName = worldName;
        this.portals = portals;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnderEyeSpawn(final EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof EnderSignal eye)) {
            return;
        }

        final World world = event.getEntity().getWorld();
        if (!world.getName().equals(this.worldName)) {
            return;
        }

        Location closestPortal = null;
        double closestDistanceSquared = Double.MAX_VALUE;
        for (final PortalPosition portal : this.portals) {
            final Location location = new Location(world, portal.x(), world.getMinHeight(), portal.z());
            final double distanceSquared = location.distanceSquared(eye.getLocation());
            if (distanceSquared < closestDistanceSquared) {
                closestPortal = location;
                closestDistanceSquared = distanceSquared;
            }
        }

        if (closestPortal != null) {
            eye.setTargetLocation(closestPortal);
        }
    }
}
