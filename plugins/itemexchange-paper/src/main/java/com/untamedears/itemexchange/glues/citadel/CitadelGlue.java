package com.untamedears.itemexchange.glues.citadel;

import com.untamedears.itemexchange.ItemExchangePlugin;
import vg.civcraft.mc.civmodcore.gluing.DependencyGlue;
import vg.civcraft.mc.civmodcore.events.PooledListeners;

public final class CitadelGlue implements DependencyGlue {
    private final PooledListeners listeners = new PooledListeners();

    @Override
    public void enable() {
        this.listeners.registerListener(ItemExchangePlugin.getInstance(), new ShopCreationListener());
    }

    @Override
    public void disable() {
        this.listeners.clearListeners();
    }
}
