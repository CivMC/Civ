package com.untamedears.jukealert.commands;

import com.google.common.base.Strings;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "jalist")
public class ListCommand extends StandaloneCommand {

	@Override
	public boolean execute(final CommandSender sender, final String[] arguments) {
		final Player player = (Player) sender;
		boolean playerProvidedGroups = true;
		List<String> groupNames = null;
		if (ArrayUtils.isNotEmpty(arguments)) {
			groupNames = Arrays.asList(arguments);
			groupNames.removeIf(Strings::isNullOrEmpty);
		}
		if (CollectionUtils.isEmpty(groupNames)) {
			groupNames = NameAPI.getGroupManager().getAllGroupNames(player.getUniqueId());
			playerProvidedGroups = false;
		}
		final var groupIds = new IntArrayList();
		for (final String groupName : groupNames) {
			final Group group = GroupManager.getGroup(groupName);
			if (group == null) {
				if (playerProvidedGroups) {
					sender.sendMessage(ChatColor.RED + "The group " + groupName + " does not exist");
				}
				continue;
			}
			if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
					JukeAlertPermissionHandler.getListSnitches())) {
				if (playerProvidedGroups) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to list snitches "
							+ "for the group " + group.getName());
				}
				continue;
			}
			groupIds.addAll(group.getGroupIds());
		}
		if (groupIds.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "You do not have access to any group's snitches.");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Retrieving snitches for a total of " + groupNames.size()
				+ " group instances. This may take a moment.");
		JukeAlert.getInstance().getTaskChainFactory().newChain()
				.async((unused) -> JukeAlert.getInstance().getDAO().loadSnitchesByGroupID(groupIds).parallel()
						.map((snitch) -> new SnitchCache(snitch, snitch.getAppender(DormantCullingAppender.class)))
						.filter((entry) -> entry.appender != null)
						.sorted(Comparator.comparingLong((entry) -> entry.appender.getTimeUntilCulling()))
						.map((entry) -> entry.snitch)
						.collect(Collectors.toList()))
				.syncLast((snitches) -> new SnitchOverviewGUI(player, snitches, "Your snitches",
						player.hasPermission("jukealert.admin")).showScreen())
				.execute();
		return true;
	}

	@AllArgsConstructor
	@EqualsAndHashCode
	private static class SnitchCache {
		Snitch snitch;
		DormantCullingAppender appender;
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String[] arguments) {
		final String last = ArrayUtils.isEmpty(arguments) ? "" : arguments[arguments.length - 1];
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
