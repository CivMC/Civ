package net.civmc.zorweth;

import net.minelink.ctplus.event.CombatLogEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

public final class RocketCombatLogListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombatLog(final CombatLogEvent event) {
        // The source inventory has already been moved into the transfer payload.
        // Use the persistent marker so this also covers disconnects during preparation
        // and does not depend on the ordering of stasis cleanup during PlayerQuitEvent.
        if (event.getPlayer().getPersistentDataContainer()
            .has(RocketTransferKeys.SOURCE_TRANSFER_ID, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
