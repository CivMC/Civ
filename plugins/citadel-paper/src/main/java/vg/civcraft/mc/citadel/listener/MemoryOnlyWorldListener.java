package vg.civcraft.mc.citadel.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import vg.civcraft.mc.citadel.Citadel;

public class MemoryOnlyWorldListener implements Listener {

    private final Citadel plugin;

    public MemoryOnlyWorldListener(final Citadel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event) {
        plugin.getReinforcementManager().removeMemoryOnlyReinforcements(event.getWorld());
    }
}
