package vg.civcraft.mc.civchat2.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.DatabaseManager;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public abstract class ChatCommand extends PlayerCommand {

	protected CivChat2 plugin = CivChat2.getInstance();

	protected CivChat2Manager chatMan = plugin.getCivChat2Manager();

	protected CivChat2Log logger = CivChat2.getCivChat2Log();

	protected DatabaseManager DBM = plugin.getDatabaseManager();

	public ChatCommand(String name) {

		super(name);
		setArguments(0, 0);
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {

		return null;
	}

	protected Player argAsPlayer(int index) {

		try {
			return Bukkit.getPlayer(NameAPI.getUUID(getArgs()[index].trim()));
		} catch (Exception ex) {
			return null;
		}
	}

	protected Group argAsGroup(int index) {

		try {
			return GroupManager.getGroup(getArgs()[index].trim());
		} catch (Exception ex) {
			return null;
		}
	}

	protected String getRealName(Player player) {

		try {
			return NameAPI.getCurrentName(player.getUniqueId());
		} catch (Exception ex) {
			return null;
		}
	}

	protected String getRealName(String name) {

		try {
			return NameAPI.getCurrentName(NameAPI.getUUID(name));
		} catch (Exception ex) {
			return null;
		}
	}

	protected List<String> findPlayers(String pattern) {

		List<String> players = new ArrayList<String>();
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(pattern.toLowerCase())) {
				players.add(p.getName());
			}
		}
		return players;
	}

	protected List<String> findGroups(String pattern) {

		List<String> groupsToReturn = new ArrayList<String>();
		GroupManager gm = NameAPI.getGroupManager();
		List<String> groups = gm.getAllGroupNames(player().getUniqueId());
		for (String group:groups) {
			if (group.toLowerCase().startsWith(pattern.toLowerCase())) {
				groupsToReturn.add(group);
			}
		}
		return groupsToReturn;
	}
}
