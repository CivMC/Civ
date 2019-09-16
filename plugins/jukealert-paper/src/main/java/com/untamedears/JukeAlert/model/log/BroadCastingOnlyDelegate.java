package com.untamedears.JukeAlert.model.log;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class BroadCastingOnlyDelegate extends LoggingDelegate {

	@Override
	public void addAction(LoggedSnitchAction action) {
		switch (action.getIdentifier()) {
		case "ENTRY":
		case "LOGIN":
		case "LOGOUT":
			sendMessage(snitch.getGroup(), action.getChatRepresentation());
		}
	}

	@Override
	public List<LoggedSnitchAction> getFullLogs() {
		return Collections.emptyList();
	}

	@Override
	public void deleteAllLogs() {
		//we're not keeping any logs, so nothing to delete
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
	public void persist() {
		//nothing to do
	}

}
