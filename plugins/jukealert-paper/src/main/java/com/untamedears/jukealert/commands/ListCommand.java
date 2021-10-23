package com.untamedears.jukealert.commands;

import com.google.common.base.Strings;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
						.map((snitch) -> {
							final DormantCullingAppender appender = snitch.getAppender(DormantCullingAppender.class);
							if (appender == null) {
								return null;
							}
							return new SnitchCache(snitch, appender.getTimeUntilCulling());
						})
						.filter(Objects::nonNull)
						.sorted(Comparator.comparingLong((entry) -> entry.timeUntilCulling))
						.map((entry) -> entry.snitch)
						.collect(Collectors.toList()))
				.syncLast((snitches) -> new SnitchOverviewGUI(player, snitches, "Your snitches",
						player.hasPermission("jukealert.admin")).showScreen())
				.execute();
		return true;
	}

	private static class SnitchCache {
		private final Snitch snitch;
		private final long timeUntilCulling;
		public SnitchCache(@Nonnull final Snitch snitch,
						   final long timeUntilCulling) {
			this.snitch = snitch;
			this.timeUntilCulling = timeUntilCulling;
		}
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String[] arguments) {
		final String last = ArrayUtils.isEmpty(arguments) ? "" : arguments[arguments.length - 1];
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
