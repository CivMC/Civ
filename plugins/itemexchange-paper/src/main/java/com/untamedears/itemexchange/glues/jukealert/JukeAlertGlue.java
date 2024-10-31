package com.untamedears.itemexchange.glues.jukealert;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.SuccessfulPurchaseEvent;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.DependencyGlue;

import java.lang.reflect.InvocationTargetException;

public final class JukeAlertGlue extends DependencyGlue {

    private Class<?> shopPurchaseAction;

    public JukeAlertGlue(final @NotNull ItemExchangePlugin plugin) {
        super(plugin, "JukeAlert");
    }

    private final Listener listener = new Listener() {
        @EventHandler(ignoreCancelled = true)
        public void triggerNearbySnitches(final SuccessfulPurchaseEvent event) throws InvocationTargetException, InstantiationException, IllegalAccessException {
            final Player purchaser = event.getPurchaser();
            final Location location = event.getTrade().getInventory().getLocation();
            final long now = System.currentTimeMillis();
            for (final Snitch snitch : JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(location)) {
                if (!purchaser.hasPermission("jukealert.vanish")) {
                    snitch.processAction((SnitchAction) shopPurchaseAction.getConstructors()[0].newInstance(snitch, purchaser.getUniqueId(), location, now));
                }
            }
        }
    };

    @Override
    protected void onDependencyEnabled() {
        try {
            this.shopPurchaseAction = Class.forName("com.untamedears.itemexchange.glues.jukealert.ShopPurchaseAction");
            JukeAlert.getInstance().getLoggedActionFactory().registerProvider(
                (String) shopPurchaseAction.getDeclaredField("IDENTIFIER").get(null),
                (snitch, player, location, timestamp, extra) ->
                {
                    try {
                        return (LoggableAction) shopPurchaseAction.getConstructors()[0].newInstance(snitch, player, location, timestamp);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            ItemExchangePlugin.getInstance().registerListener(this.listener);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDependencyDisabled() {
        HandlerList.unregisterAll(this.listener);
    }

}
