package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class Stats extends PlayerCommand{

	private List<Group> run = new ArrayList<Group>();

	public Stats(String name) {
		super(name);
		setIdentifier("cts");
		setDescription("Lists the stats about a certain group.");
		setUsage("/cts <group>");
		setArguments(0,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("meh");
			return true;
		}
		Player p = (Player) sender;
		if (!(p.isOp() || p.hasPermission("citadel.admin"))){
			Utility.sendAndLog(p, ChatColor.RED, "You do not have permission for this command.");
			return true;
		}
		if (args.length == 0){
			Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), new StatsMessageAllGroups(p));
			return true;
		}
		Group g = GroupManager.getGroup(args[0]);

		if (g == null){
			Utility.sendAndLog(p, ChatColor.RED, "This group does not exist.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("citadel.admin"))){
			Utility.sendAndLog(p, ChatColor.RED, "You are not on this group.");
			return true;
		}
		synchronized(run){
			if (run.contains(g)){
				Utility.sendAndLog(p, ChatColor.RED, "That group is already being searched for.");
				return true;
			}
			run.add(g);
		}
		Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), new StatsMessage(p, g));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return new ArrayList<String>();

		if (args.length == 0) {
			return GroupTabCompleter.complete(null, null, (Player)sender);
		} else if (args.length == 1) {
			return GroupTabCompleter.complete(args[0], null, (Player)sender);
		} else {
			return new ArrayList<String>();
		}

	}

	public class StatsMessage implements Runnable{

		private final Player p;
		private final Group g;

		public StatsMessage(Player p, Group g){
			this.p = p;
			this.g = g;
		}

		@Override
		public void run() {
			if (g == null || p == null) {
				return;
			}
			String message = "The amount of reinforcements on this group are: ";
			int count = Citadel.getCitadelDatabase().getReinCountForGroup(g.getName());
			message += Integer.toString(count);
			synchronized(run){
				run.remove(g);
			}
			if (p != null && !p.isOnline()) {// meh be safe
				return;
			}
			Utility.sendAndLog(p, ChatColor.GREEN, message);
		}

	}

	public class StatsMessageAllGroups implements Runnable{

		private final Player p;
		public StatsMessageAllGroups(Player p){
			this.p = p;
		}

		@Override
		public void run() {
			String message = "The amount of reinforcements on the server are: ";
			int count = Citadel.getCitadelDatabase().getReinCountForAllGroups();
			message += count;
			if (p != null && !p.isOnline()) // meh be safe
				return;
			Utility.sendAndLog(p, ChatColor.GREEN, message);
		}
	}
}
