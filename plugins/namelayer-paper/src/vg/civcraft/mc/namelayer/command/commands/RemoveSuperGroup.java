package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RemoveSuperGroup extends PlayerCommand{

	public RemoveSuperGroup(String name) {
		super(name);
		setIdentifier("nlrsg");
		setDescription("Remove a super group from a group.");
		setUsage("/nlrsg <group>");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Nope, nope, nope");
			return true;
		}
		Player p = (Player) sender;
		Group g1 = gm.getGroup(args[0]);
		UUID uuid = NameAPI.getUUID(p.getName());
		
		PlayerType pT1 = g1.getPlayerType(uuid);
		
		if (pT1 == null){
			p.sendMessage(ChatColor.RED + "You are not on the main group.");
			return true;
		}
		
		if (g1.isDisciplined()){
			p.sendMessage(ChatColor.RED + "The main group has been disiplined.");
			return true;
		}
		
		GroupPermission gP1 = gm.getPermissionforGroup(g1);
		
		if (!gP1.isAccessible(pT1, PermissionType.SUBGROUP)){
			p.sendMessage(ChatColor.RED + "You dont have permission on the main group.");
			return true;
		}
		
		if (g1 instanceof PrivateGroup){
			PrivateGroup priv = (PrivateGroup) g1;
			Group g2 = priv.getSuperGroup();
			priv.removeSuperGroup();
			while (g2 != null && g2 instanceof PrivateGroup){
				PrivateGroup priv2 = (PrivateGroup) g2;
				priv2.removeSubGroup(g1);
				g2 = priv2.getSuperGroup();
			}
			p.sendMessage(ChatColor.GREEN + "Super group has been removed.");
		}
		else
			p.sendMessage(ChatColor.RED + "That group cannot have super groups.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length > 0)
			return GroupTabCompleter.complete(args[0], PermissionType.SUBGROUP, (Player) sender);
		else{
			return GroupTabCompleter.complete(null, PermissionType.SUBGROUP, (Player)sender);
		}
	}
}
