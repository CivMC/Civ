package com.untamedears.itemexchange.glues.namelayer;

import com.untamedears.itemexchange.ItemExchangePlugin;
import java.util.List;
import vg.civcraft.mc.civmodcore.events.PooledListeners;
import vg.civcraft.mc.civmodcore.gluing.DependencyGlue;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class NameLayerGlue implements DependencyGlue {
    private final PooledListeners listeners = new PooledListeners();

    @Override
    public void enable() {
        PermissionType.registerPermission(
            Permissions.PURCHASE_PERMISSION,
            List.of(
                GroupManager.PlayerType.MEMBERS,
                GroupManager.PlayerType.MODS,
                GroupManager.PlayerType.ADMINS,
                GroupManager.PlayerType.OWNER
            ),
            "Determines whether players can purchase from shops limited to this group."
        );
        ItemExchangePlugin.modifierRegistrar().registerModifier(GroupModifier.TEMPLATE);
        this.listeners.registerListener(ItemExchangePlugin.getInstance(), new BrowseListener());
    }

    @Override
    public void disable() {
        this.listeners.clearListeners();
        ItemExchangePlugin.modifierRegistrar().deregisterModifier(GroupModifier.TEMPLATE);
    }
}
