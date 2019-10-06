package com.untamedears.JukeAlert.model.appender;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggablePlayerAction;
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.actions.impl.EnterFieldAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;
import com.untamedears.JukeAlert.util.JASettingsManager;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

public class BroadcastEntryAppender extends AbstractSnitchAppender {
	
	public final static String ID = "broadcast"; 

	public BroadcastEntryAppender(Snitch snitch) {
		super(snitch);
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (action.isLifeCycleEvent() || !action.hasPlayer()) {
			return;
		}
		LoggablePlayerAction log = (LoggablePlayerAction) action;
		if (snitch.hasPermission(log.getPlayer(), JukeAlertPermissionHandler.getSnitchImmune())) {
			return;
		}
		switch (action.getIdentifier()) {
		case EnterFieldAction.ID:
		case LoginAction.ID:
		case LogoutAction.ID:
			for (UUID uuid : snitch.getGroup().getAllMembers()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				JASettingsManager settings = JukeAlert.getInstance().getSettingsManager();
				if (settings.doesIgnoreAllAlerts(uuid)) {
					continue;
				}
				if (settings.doesIgnoreAlert(snitch.getGroup().getName(), uuid)) {
					continue;
				}
				if (snitch.hasPermission(uuid, JukeAlertPermissionHandler.getSnitchAlerts())) {
					player.spigot().sendMessage(log.getChatRepresentation(player.getLocation()));
				}
			}
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

}
