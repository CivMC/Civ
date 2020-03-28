package vg.civcraft.mc.namelayer.command.TabCompleters;

import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

/**
 * Created by isaac on 2/2/2015.
 */
public class InviteTabCompleter {
	public static List<String> complete(String lastArg, Player sender) {
		UUID uuid = NameAPI.getUUID(sender.getName());
		Set<Group> groups = PlayerListener.getNotifications(uuid);
		if (groups == null) {
			return Collections.emptyList();
		}
		List<String> groupsString = groups.stream().map(Group::getName).collect(Collectors.toList());
		if (lastArg == null) {
			return groupsString;
		}

		return StringUtil.copyPartialMatches(lastArg, groupsString, new ArrayList<>());
	}
}
