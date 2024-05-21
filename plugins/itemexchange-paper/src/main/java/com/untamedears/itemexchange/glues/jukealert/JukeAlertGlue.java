package com.untamedears.itemexchange.glues.jukealert;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.jukealert.JukeAlert;
import vg.civcraft.mc.civmodcore.events.PooledListeners;
import vg.civcraft.mc.civmodcore.gluing.DependencyGlue;

public final class JukeAlertGlue implements DependencyGlue {
    private final PooledListeners listeners = new PooledListeners();

    @Override
    public void enable() {
        JukeAlert.getInstance().getLoggedActionFactory().registerProvider(
            ShopPurchaseAction.IDENTIFIER,
            ShopPurchaseAction::provider
        );
        this.listeners.registerListener(ItemExchangePlugin.getInstance(), new PurchaseListener());
    }

    @Override
    public void disable() {
        this.listeners.clearListeners();
    }
}
