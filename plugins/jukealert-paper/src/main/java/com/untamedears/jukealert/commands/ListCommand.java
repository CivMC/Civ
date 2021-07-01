package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

public class ListCommand extends BaseCommand {

	@CommandAlias("jalist")
	@Syntax("[group]")
	@Description("Lists all snitches you have access to if given no arguments or the ones on the given groups")
	public void execute(final Player player, @Optional final String targetGroup1, @Optional final String targetGroup2, @Optional final String targetGroup3) {
		boolean playerProvidedGroups = true;
		List<String> groupNames = null;
		if (!(targetGroup1 == null) && !(targetGroup2 == null) && !(targetGroup3 == null)) {
			groupNames.add(targetGroup1);
			groupNames.add(targetGroup2);
			groupNames.add(targetGroup3);
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
					player.sendMessage(ChatColor.RED + "The group " + groupName + " does not exist");
				}
				continue;
			}
			if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
					JukeAlertPermissionHandler.getListSnitches())) {
				if (playerProvidedGroups) {
					player.sendMessage(ChatColor.RED + "You do not have permission to list snitches "
							+ "for the group " + group.getName());
				}
				continue;
			}
			groupIds.addAll(group.getGroupIds());
		}
		if (groupIds.isEmpty()) {
			player.sendMessage(ChatColor.GREEN + "You do not have access to any group's snitches.");
			return;
		}
		player.sendMessage(ChatColor.GREEN + "Retrieving snitches for a total of " + groupNames.size()
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
							thisAppender.getTimeUntilCulling(),
							thatAppender.getTimeUntilCulling());
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
	}

	public List<String> tabComplete(final CommandSender sender, final String[] arguments) {
		final String last = ArrayUtils.isEmpty(arguments) ? "" : arguments[arguments.length - 1];
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
