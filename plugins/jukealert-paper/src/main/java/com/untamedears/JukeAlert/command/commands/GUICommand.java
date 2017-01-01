package com.untamedears.JukeAlert.command.commands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.gui.SnitchLogGUI;
import com.untamedears.JukeAlert.gui.SnitchOverviewGUI;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.util.Utility;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GUICommand extends PlayerCommand {

	public GUICommand() {

		super("JaGUI");
		setDescription("Opens the JukeAlert GUI");
		setUsage("/ja");
		setArguments(0, 0);
		setIdentifier("ja");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.AQUA + "The technology is not there yet");
			return true;
		}
		Player player = (Player) sender;
		Snitch cursorSnitch = Utility.getSnitchUnderCursor(player);
		if (cursorSnitch != null
		                 && Utility.doesSnitchExist(cursorSnitch, true)
		                 && NameAPI.getGroupManager().hasAccess(cursorSnitch.getGroup(), player.getUniqueId(),
		                                                        PermissionType.getPermission("READ_SNITCHLOG"))) {
			SnitchLogGUI gui = new SnitchLogGUI(player, cursorSnitch);
			gui.showScreen();
			return true;
		}
		// No snitch under cursor, so search around player
		Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(
			player.getWorld(), player.getLocation());
		// Remove the ones the player has no perms for
		Iterator<Snitch> iter = snitches.iterator();
		while (iter.hasNext()) {
			Snitch s = iter.next();
			if (!NameAPI.getGroupManager().hasAccess(
					s.getGroup(), player.getUniqueId(), PermissionType.getPermission("READ_SNITCHLOG"))) {
				iter.remove();
			}
		}
		if (snitches.size() == 0) {
			player.sendMessage(
				ChatColor.RED + " You do not own any snitches nearby or lack permission to view their logs!");
			return true;
		}
		if (snitches.size() == 1) {
			SnitchLogGUI gui = new SnitchLogGUI(player, snitches.iterator().next());
			gui.showScreen();
			return true;
		}
		SnitchOverviewGUI gui = new SnitchOverviewGUI(player, new LinkedList<Snitch>(snitches));
		gui.showScreen();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
