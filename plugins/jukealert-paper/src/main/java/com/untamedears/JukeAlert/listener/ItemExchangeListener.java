package com.untamedears.JukeAlert.listener;

import static com.untamedears.JukeAlert.util.Utility.immuneToSnitch;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.untamedears.ItemExchange.events.IETransactionEvent;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

public class ItemExchangeListener implements Listener {

	private final JukeAlert plugin = JukeAlert.getInstance();

	SnitchManager snitchManager = plugin.getSnitchManager();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void exchangeEvent(IETransactionEvent event) {

		Player player = event.getPlayer();
		Location location = event.getExchangeLocation();
		World world = location.getWorld();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (!immuneToSnitch(snitch, accountId)) {
				snitch.imposeSnitchTax();
				if (snitch.shouldLog()) {
					plugin.getJaLogger().logSnitchExchangeEvent(snitch, player, location);
				}
			}
		}
	}
}
