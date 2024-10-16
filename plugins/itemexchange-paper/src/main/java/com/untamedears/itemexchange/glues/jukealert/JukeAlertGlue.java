package com.untamedears.itemexchange.glues.jukealert;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.SuccessfulPurchaseEvent;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.DependencyGlue;

public final class JukeAlertGlue extends DependencyGlue {

	public JukeAlertGlue(final @NotNull ItemExchangePlugin plugin) {
		super(plugin, "JukeAlert");
	}

	private final Listener listener = new Listener() {
		@EventHandler(ignoreCancelled = true)
		public void triggerNearbySnitches(final SuccessfulPurchaseEvent event) {
			final Player purchaser = event.getPurchaser();
			final Location location = event.getTrade().getInventory().getLocation();
			final long now = System.currentTimeMillis();
			for (final Snitch snitch : JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(location)) {
				if (!purchaser.hasPermission("jukealert.vanish")) {
					snitch.processAction(new ShopPurchaseAction(snitch, purchaser.getUniqueId(), location, now));
				}
			}
		}
	};

	@Override
	protected void onDependencyEnabled() {
		JukeAlert.getInstance().getLoggedActionFactory().registerProvider(
				ShopPurchaseAction.IDENTIFIER,
				(snitch, player, location, timestamp, extra) ->
						new ShopPurchaseAction(snitch, player, location, timestamp));
		ItemExchangePlugin.getInstance().registerListener(this.listener);
	}

	@Override
	protected void onDependencyDisabled() {
		HandlerList.unregisterAll(this.listener);
	}

}
