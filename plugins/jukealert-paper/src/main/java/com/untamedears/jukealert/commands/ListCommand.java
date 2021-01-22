package com.untamedears.jukealert.commands;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "jalist")
public class ListCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		List<String> groups;
		Player player = (Player) sender;
		GroupManager gm = NameAPI.getGroupManager();
		if (args.length == 0) {
			groups = gm.getAllGroupNames(player.getUniqueId());
		} else {
			groups = Arrays.asList(args);
		}
		List<Integer> groupIds = new ArrayList<>();
		for (String groupName : groups) {
			Group group = GroupManager.getGroup(groupName);
			if (group == null) {
				sender.sendMessage(ChatColor.RED + "The group " + groupName + " does not exist");
				continue;
			}
			if (!gm.hasAccess(group, player.getUniqueId(), JukeAlertPermissionHandler.getListSnitches())) {
				sender.sendMessage(
						ChatColor.RED + "You do not have permission to list snitches for the group " + group.getName());
				continue;
			}
			groupIds.addAll(group.getGroupIds());
		}
		sender.sendMessage(ChatColor.GREEN + "Retrieving snitches for a total of " + groupIds.size()
		+ " group instances. This may take a moment");
		//retrieve snitches async and show them sync again
		new BukkitRunnable() {

			@Override
			public void run() {
				List<Snitch> snitches = JukeAlert.getInstance().getDAO().loadSnitchesByGroupID(groupIds);
				new BukkitRunnable() {
					
					@Override
					public void run() {
						new SnitchOverviewGUI(player, snitches, "Your snitches", false).showScreen();;
						
					}
				}.runTask(JukeAlert.getInstance());
			}
		}.runTaskAsynchronously(JukeAlert.getInstance());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		String last;
		if (args.length == 0) {
			last = "";
		} else {
			last = args[args.length - 1];
		}
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
