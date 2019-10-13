package com.untamedears.JukeAlert.model.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.abstr.LoggableAction;
import com.untamedears.JukeAlert.model.actions.impl.BlockBreakAction;
import com.untamedears.JukeAlert.model.actions.impl.BlockPlaceAction;
import com.untamedears.JukeAlert.model.actions.impl.DismountEntityAction;
import com.untamedears.JukeAlert.model.actions.impl.EmptyBucketAction;
import com.untamedears.JukeAlert.model.actions.impl.EnterFieldAction;
import com.untamedears.JukeAlert.model.actions.impl.EnterVehicleAction;
import com.untamedears.JukeAlert.model.actions.impl.ExitVehicleAction;
import com.untamedears.JukeAlert.model.actions.impl.FillBucketAction;
import com.untamedears.JukeAlert.model.actions.impl.KillLivingEntityAction;
import com.untamedears.JukeAlert.model.actions.impl.KillPlayerAction;
import com.untamedears.JukeAlert.model.actions.impl.LeaveFieldAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;
import com.untamedears.JukeAlert.model.actions.impl.MountEntityAction;

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
		if (internal != -1) {
			providers.put(identifier, provider);
			identifierToInternal.put(identifier, internal);
		}
	}

	public LoggableAction produce(Snitch snitch, String id, UUID player, Location location, long time, String victim) {
		LoggedActionProvider provider = providers.get(id);
		if (provider == null) {
			return null;
		}
		return provider.get(snitch, player, location, time, victim);
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
		registerProvider(EnterFieldAction.ID,
				(snitch, player, loc, time, victim) -> new EnterFieldAction(time, snitch, player));
		registerProvider(LeaveFieldAction.ID,
				(snitch, player, loc, time, victim) -> new LeaveFieldAction(time, snitch, player));
		registerProvider(BlockBreakAction.ID,
				(snitch, player, loc, time, victim) -> new BlockBreakAction(time, snitch, player, loc, victim));
		registerProvider(BlockPlaceAction.ID,
				(snitch, player, loc, time, victim) -> new BlockPlaceAction(time, snitch, player, loc, victim));
		registerProvider(LoginAction.ID, (snitch, player, loc, time, victim) -> new LoginAction(time, snitch, player));
		registerProvider(LogoutAction.ID,
				(snitch, player, loc, time, victim) -> new LogoutAction(time, snitch, player));
		registerProvider(KillLivingEntityAction.ID,
				(snitch, player, loc, time, victim) -> new KillLivingEntityAction(time, snitch, player, loc, victim));
		registerProvider(KillPlayerAction.ID, (snitch, player, loc, time, victim) -> new KillPlayerAction(time, snitch,
				player, loc, UUID.fromString(victim)));
		registerProvider(FillBucketAction.ID, (snitch, player, loc, time, victim) -> new FillBucketAction(time, snitch,
				player, loc, Material.valueOf(victim)));
		registerProvider(EmptyBucketAction.ID, (snitch, player, loc, time, victim) -> new EmptyBucketAction(time,
				snitch, player, loc, Material.valueOf(victim)));
		registerProvider(EnterVehicleAction.ID,
				(snitch, player, loc, time, victim) -> new EnterVehicleAction(time, snitch, player, loc, victim));
		registerProvider(ExitVehicleAction.ID,
				(snitch, player, loc, time, victim) -> new ExitVehicleAction(time, snitch, player, loc, victim));
		registerProvider(MountEntityAction.ID,
				(snitch, player, loc, time, victim) -> new MountEntityAction(time, snitch, player, loc, victim));
		registerProvider(DismountEntityAction.ID,
				(snitch, player, loc, time, victim) -> new DismountEntityAction(time, snitch, player, loc, victim));
	}

}
