package com.untamedears.JukeAlert.model.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.actions.impl.BlockBreakAction;
import com.untamedears.JukeAlert.model.actions.impl.BlockPlaceAction;
import com.untamedears.JukeAlert.model.actions.impl.EntryAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;

public class LoggedActionFactory {

	private Map<String, LoggedActionProvider> providers;
	private Map<String, Integer> identifierToInternal;

	public LoggedActionFactory() {
		this.providers = new HashMap<>();
		this.identifierToInternal = new HashMap<>();
		registerInternalProviders();
	}

	public void registerProvider(String identifier, LoggedActionProvider provider) {
		int internal = JukeAlert.getInstance().getDAO().getOrCreateActionID(identifier);
		if (internal != -1)  {
			providers.put(identifier, provider);
			identifierToInternal.put(identifier, internal);
		}
	}
	
	public LoggedSnitchAction produce(String id, UUID player, Location location, long time, String victim) {
		LoggedActionProvider provider = providers.get(id);
		if (provider == null) {
			return null;
		}
		return provider.get(player, location, time, victim);
	}
	
	public int getInternalID(String name) {
		Integer id = identifierToInternal.get(name);
		if (id == null) {
			return -1;
		}
		return id;
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
