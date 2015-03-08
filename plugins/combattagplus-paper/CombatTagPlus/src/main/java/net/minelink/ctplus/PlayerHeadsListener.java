package net.minelink.ctplus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.shininet.bukkit.playerheads.events.PlayerDropHeadEvent;

public class PlayerHeadsListener implements Listener {

    private final CombatTagPlus plugin;

    public PlayerHeadsListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void discardNpcHeads(PlayerDropHeadEvent event) {
        if (plugin.getNpcPlayerHelper().isNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

}
