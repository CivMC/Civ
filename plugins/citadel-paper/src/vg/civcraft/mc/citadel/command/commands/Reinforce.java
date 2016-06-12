package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Reinforce extends PlayerCommandMiddle {

	private GroupManager gm = NameAPI.getGroupManager();
	
	public Reinforce(String name) {
		super(name);
		setIdentifier("ctr");
		setDescription("Reinforce blocks under a group.");
		setUsage("/ctr <group>");
		setArguments(0,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Must be a player to use that command.");
			return true;
		}
		String groupName = null;
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		if(args.length == 0){
			groupName = gm.getDefaultGroup(uuid);
			if(groupName == null){
				sendAndLog(p, ChatColor.RED, "You need to reinforce to a group! Try /reinforce groupname. \n Or /create groupname if you don't have a group yet.");
				return true;
			}
		}
		else{
			groupName = args[0];
		}
		Group g = gm.getGroup(groupName);
		if (g == null){
			sendAndLog(p, ChatColor.RED, "That group does not exist.");
			return true;
		}
		PlayerType type = g.getPlayerType(uuid);
		if (!p.hasPermission("citadel.admin") && !p.isOp() && type == null){
			sendAndLog(p, ChatColor.RED, "You are not on this group.");
			return true;
		}
		if (!p.hasPermission("citadel.admin") && !p.isOp() && !gm.hasAccess(g.getName(), p.getUniqueId(), PermissionType.getPermission("REINFORCE"))){
			sendAndLog(p, ChatColor.RED, "You do not have permission to "
					+ "place a reinforcement on this group.");
			return true;
		}
		PlayerState state = PlayerState.get(p);
		if (state.getMode() == ReinforcementMode.REINFORCEMENT){
			sendAndLog(p, ChatColor.GREEN, state.getMode().name() + " has been disabled");
			state.reset();
		}
		else{
			sendAndLog(p, ChatColor.GREEN, "You are now in Reinforcement mode, hit blocks with a reinforcement material to secure them. \n Type /reinforce or /cto to turn this off when you are done.");
			state.setMode(ReinforcementMode.REINFORCEMENT);
			state.setGroup(g);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length == 0)
			return GroupTabCompleter.complete(null, PermissionType.getPermission("REINFORCE"), (Player)sender);
		else if (args.length == 1)
			return GroupTabCompleter.complete(args[0], PermissionType.getPermission("REINFORCE"), (Player)sender);
		else {
			return new ArrayList<String>();
		}
	}
}
