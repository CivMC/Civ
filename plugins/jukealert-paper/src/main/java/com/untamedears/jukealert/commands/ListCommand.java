package com.untamedears.jukealert.commands;

import com.google.common.base.Strings;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
		final var groupIds = new ArrayList<Integer>();
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
		sender.sendMessage(ChatColor.GREEN + "Retrieving snitches for a total of " + groupIds.size()
				+ " group instances. This may take a moment.");
		new BukkitRunnable() {
			@Override
			public void run() {
				final List<Snitch> snitches = JukeAlert.getInstance().getDAO().loadSnitchesByGroupID(groupIds);
				snitches.removeIf(snitch -> !snitch.hasAppender(DormantCullingAppender.class));
				snitches.sort((lhs, rhs) -> {
					/** These should be present, if not look at {@link SnitchTypeManager#registerAppenderTypes()}! */
					final var thisAppender = lhs.getAppender(DormantCullingAppender.class);
					final var thatAppender = rhs.getAppender(DormantCullingAppender.class);
					// Since the time decreases the closer a snitch gets to culling, the values are flipped
					return Long.compare(
							thatAppender.getTimeUntilCulling(),
							thisAppender.getTimeUntilCulling());
				});
				new BukkitRunnable() {
					@Override
					public void run() {
						new SnitchOverviewGUI(player, snitches, "Your snitches",
								player.hasPermission("jukealert.admin")).showScreen();
					}
				}.runTask(JukeAlert.getInstance());
			}
		}.runTaskAsynchronously(JukeAlert.getInstance());
		return true;
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String[] arguments) {
		final String last = ArrayUtils.isEmpty(arguments) ? "" : arguments[arguments.length - 1];
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
