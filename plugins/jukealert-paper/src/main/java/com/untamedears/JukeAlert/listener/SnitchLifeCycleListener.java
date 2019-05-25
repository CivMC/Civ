package com.untamedears.JukeAlert.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.SnitchConfiguration;
import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;

public class SnitchLifeCycleListener implements Listener {

	@EventHandler
	public void createReinforcement(ReinforcementCreationEvent e) {
		SnitchConfiguration snitchConfig = JukeAlert.getInstance().getSnitchConfigManager()
				.getConfig(e.getBlockReinforced());
		if (snitchConfig == null) {
			return;
		}
		Snitch snitch = snitchConfig.createAt(e.getReinforcement().getLocation(), e.getPlayer(),
				e.getReinforcement().getGroup());
		JukeAlert.getInstance().getSnitchManager().addSnitch(snitch);
	}

}
