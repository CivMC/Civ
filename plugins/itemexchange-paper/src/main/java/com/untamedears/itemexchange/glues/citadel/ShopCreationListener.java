package com.untamedears.itemexchange.glues.citadel;

import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import com.untamedears.itemexchange.glues.namelayer.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.group.Group;

final class ShopCreationListener implements Listener {
    @EventHandler(
        priority = EventPriority.NORMAL,
        ignoreCancelled = true
    )
    private void denyShopCreationIfNotGotPerms(
        final @NotNull BlockInventoryRequestEvent event
    ) {
        if (event.getPurpose() != BlockInventoryRequestEvent.Purpose.ACCESS) {
            return;
        }
        final Player requester = event.getRequester();
        if (requester == null) {
            return;
        }
        final Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(event.getBlock());
        if (reinforcement == null) {
            return;
        }
        final Group group = reinforcement.getGroup();
        if (!Permissions.testPermission(Permissions.CHESTS_PERMISSION, group, requester)) {
            return;
        }
        event.setCancelled(true);
    }
}
