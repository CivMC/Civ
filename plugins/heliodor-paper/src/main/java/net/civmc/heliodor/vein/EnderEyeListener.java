package net.civmc.heliodor.vein;

import net.civmc.heliodor.vein.data.VerticalBlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderSignal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import java.util.List;

public class EnderEyeListener implements Listener {
    private final String worldName;
    private final List<VerticalBlockPos> positions;

    public EnderEyeListener(String worldName, List<VerticalBlockPos> positions) {
        this.worldName = worldName;
        this.positions = positions;
    }

    @EventHandler
    public void on(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof EnderSignal eye)) {
            return;
        }

        World world = event.getEntity().getWorld();
        if (!world.getName().equals(worldName)) {
            return;
        }

        Location closestLocation = null;
        double distanceSquared = Double.MAX_VALUE;

        if (positions.isEmpty()) {
            return;
        }

        for (VerticalBlockPos position : positions) {
            Location portal = new Location(world, position.x(), world.getMinHeight(), position.z());
            double portalDistanceSquared = portal.distanceSquared(eye.getLocation());
            if (portalDistanceSquared < distanceSquared) {
                closestLocation = portal;
                distanceSquared = portalDistanceSquared;
            }
        }

        eye.setTargetLocation(closestLocation);
    }

}
