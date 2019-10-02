package com.untamedears.JukeAlert.model.appender;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggablePlayerAction;
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.actions.impl.EntryAction;
import com.untamedears.JukeAlert.model.actions.impl.LoginAction;
import com.untamedears.JukeAlert.model.actions.impl.LogoutAction;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

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
		case EntryAction.ID:
		case LoginAction.ID:
		case LogoutAction.ID:
			sendMessage(getSnitch().getGroup(), log.getChatRepresentation());
		}
	}
	
	protected void sendMessage(Group group, TextComponent component) {
		GroupManager groupManager = NameAPI.getGroupManager();
		for (UUID uuid : group.getAllMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			if (groupManager.hasAccess(group, uuid, JukeAlertPermissionHandler.getSnitchAlerts())) {
				player.spigot().sendMessage(component);
			}
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

}
