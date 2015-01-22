package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class DeleteGroup extends PlayerCommand{

	public DeleteGroup(String name) {
		super(name);
		setIdentifier("nldg");
		setDescription("This command is used to delete a group.");
		setUsage("/nldg <group>");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.DARK_AQUA + "I grow tired of this, NO.");
			return true;
		}
		Player p = (Player) sender;
		String x = args[0];
		Group g = gm.getGroup(x);
		if (g == null){
			p.sendMessage(ChatColor.RED + "That group does not exist.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null && !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "You are not on that group.");
			return true;
		}
		if (g.isDisciplined() && !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "Group is disiplined.");
			return true;
		}
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if (!gPerm.isAccessible(pType, PermissionType.DELETE) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You do not have permission to run that command.");
			return true;
		}
		if(gm.deleteGroup(g.getName()))
			p.sendMessage(ChatColor.GREEN + "Group was successfully deleted.");
		else
			p.sendMessage(ChatColor.GREEN + "Group is now disciplined."
					+ " Check back later to see if group is deleted.");
		return true;
	}

}
