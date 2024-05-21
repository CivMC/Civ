package com.untamedears.itemexchange.glues.jukealert;

import com.untamedears.itemexchange.events.SuccessfulPurchaseEvent;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

final class PurchaseListener implements Listener {
    @EventHandler(
        priority = EventPriority.NORMAL,
        ignoreCancelled = true
    )
    private void triggerNearbySnitches(
        final @NotNull SuccessfulPurchaseEvent event
    ) {
        final Player purchaser = event.getPurchaser();
        final Location location = event.getTrade().getInventory().getLocation();
        final long now = System.currentTimeMillis();
        for (final Snitch snitch : JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(location)) {
            if (!purchaser.hasPermission("jukealert.vanish")) {
                snitch.processAction(new ShopPurchaseAction(snitch, purchaser.getUniqueId(), location, now));
            }
        }
    }
}
