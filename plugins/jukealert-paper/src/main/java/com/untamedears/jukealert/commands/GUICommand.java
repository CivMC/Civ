package com.untamedears.jukealert.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchLogGUI;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "ja")
public class GUICommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Snitch cursorSnitch = JAUtility.getSnitchUnderCursor(player);
		if (cursorSnitch != null && cursorSnitch.hasPermission(player, JukeAlertPermissionHandler.getReadLogs())) {
			SnitchLogGUI gui = new SnitchLogGUI(player, cursorSnitch);
			gui.showScreen();
			return true;
		}
		// No snitch under cursor, so search around player
		Collection<Snitch> snitches = JukeAlert.getInstance().getSnitchManager()
				.getSnitchesCovering(player.getLocation(), true);
		// Sort out the ones the player has no perms for
		Iterator<Snitch> iter = snitches.iterator();
		while (iter.hasNext()) {
			Snitch snitch = iter.next();
			if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getReadLogs())) {
				iter.remove();
			}
		}
		if (snitches.isEmpty()) {
			player.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
			return true;
		}
		if (snitches.size() == 1) {
			SnitchLogGUI gui = new SnitchLogGUI(player, snitches.iterator().next());
			gui.showScreen();
			return true;
		}
		SnitchOverviewGUI gui = new SnitchOverviewGUI(player, new ArrayList<>(snitches), "Nearby snitches", true);
		gui.showScreen();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}
}
