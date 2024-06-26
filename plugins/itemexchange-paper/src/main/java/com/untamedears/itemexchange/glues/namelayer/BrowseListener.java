package com.untamedears.itemexchange.glues.namelayer;

import com.untamedears.itemexchange.events.BrowseOrPurchaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.Validation;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

final class BrowseListener implements Listener {
    @EventHandler(
        priority = EventPriority.NORMAL,
        ignoreCancelled = true
    )
    private void denyPurchaseIfNotGotPerms(
        final @NotNull BrowseOrPurchaseEvent event
    ) {
        final GroupModifier modifier = event.getTrade().getInput().getModifiers().get(GroupModifier.class);
        if (!Validation.checkValidity(modifier)) {
            return;
        }
        final Group group = GroupManager.getGroup(modifier.getGroupId());
        if (group == null) {
            return;
        }
        if (!Permissions.testPermission(Permissions.PURCHASE_PERMISSION, group, event.getBrowser())) {
            return;
        }
        event.limitToBrowsing();
    }
}
