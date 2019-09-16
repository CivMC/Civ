package com.untamedears.JukeAlert.model.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.actions.impl.BlockBreakAction;
import com.untamedears.JukeAlert.model.actions.impl.BlockPlaceAction;
import com.untamedears.JukeAlert.model.actions.impl.EntryAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;

public class LoggedActionFactory {

	private Map<String, LoggedActionProvider> providers;

	public LoggedActionFactory() {
		this.providers = new HashMap<>();
		registerInternalProviders();
	}

	public void registerProvider(String identifier, LoggedActionProvider provider) {
		providers.put(identifier, provider);
	}
	
	public LoggedSnitchAction produce(String id, UUID player, Location location, long time, String victim) {
		LoggedActionProvider provider = providers.get(id);
		if (provider == null) {
			return null;
		}
		return provider.get(player, location, time, victim);
	}

	private void registerInternalProviders() {
		// java 8 is sexy
		registerProvider(EntryAction.ID, (player, loc, time, victim) -> new EntryAction(time, player));
		registerProvider(BlockBreakAction.ID,
				(player, loc, time, victim) -> new BlockBreakAction(time, player, loc, victim));
		registerProvider(BlockPlaceAction.ID,
				(player, loc, time, victim) -> new BlockPlaceAction(time, player, loc, victim));
		registerProvider(LoginAction.ID, (player, loc, time, victim) -> new LoginAction(time, player));
		registerProvider(LogoutAction.ID, (player, loc, time, victim) -> new LogoutAction(time, player));
	}

}
