package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupStats extends PlayerCommand {

	public GroupStats(String name) {
		super(name);
		setDescription("This command is used to get stats about a group.");
		setUsage("/nlgroupsstats <group>");
		setIdentifier("nlgroupsstats");
		setArguments(1, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("meh");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		UUID uuid = NameAPI.getUUID(p.getName());
		
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return true;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new StatsMessage(p, g));
		
		return true;
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
			String message = ChatColor.GREEN + "This group is: " + g.getName() + ".\n";
			for (PlayerType type: PlayerType.values()){
				String names = "";
				for (UUID uu: g.getAllMembers(type))
					names += NameAPI.getCurrentName(uu) + ", ";
				if (!names.equals("")){
					names = names.substring(0, names.length()-2);
					names += ".";
					message += "The members for PlayerType " + type.name() + " are: " + names + "\n";
				}
				else
					message += "No members for the PlayerType " + type.name() + ".\n";
			}
			message += "That makes " + g.getAllMembers().size() + " members total.";
			if (!p.isOnline()) // meh be safe
				return;
			p.sendMessage(message);
		}
		
	}
}
