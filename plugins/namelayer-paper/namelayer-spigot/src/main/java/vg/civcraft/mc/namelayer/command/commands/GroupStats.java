package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nlgs")
@CommandPermission("namelayer.admin")
public class GroupStats extends BaseCommandMiddle {

	@Syntax("/nlgs <group>")
	@Description("Get stats about a group.")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("meh");
			return;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		UUID uuid = NameAPI.getUUID(p.getName());	
		
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return;
		}
		boolean hasPerm = NameAPI.getGroupManager().hasAccess(g, uuid, PermissionType.getPermission("GROUPSTATS"));
		if (!(p.isOp() || p.hasPermission("namelayer.admin")) && !hasPerm){
			p.sendMessage(ChatColor.RED + "You do not have permission to run that command.");
			return;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new StatsMessage(p, g));
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length > 0)
			return GroupTabCompleter.complete(args[args.length - 1], null, (Player) sender);
		else {
			return GroupTabCompleter.complete(null, null, (Player) sender);
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
			if (p != null && !p.isOnline()) // meh be safe
				return;
			p.sendMessage(message);
		}
		
	}
}
