package vg.civcraft.mc.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameAPI;
import vg.civcraft.mc.command.PlayerCommand;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.groups.Private;
import vg.civcraft.mc.permission.GroupPermission;
import vg.civcraft.mc.permission.PermissionType;

public class RemoveSuperGroup extends PlayerCommand{

	public RemoveSuperGroup(String name) {
		super(name);
		setDescription("This command is used to remove a super group from a group.");
		setUsage("/groupsremovesuper <group>");
		setIdentifier("groupsremovesuper");
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
		
		if (g1.isDisiplined()){
			p.sendMessage(ChatColor.RED + "The main group has been disiplined.");
			return true;
		}
		
		GroupPermission gP1 = gm.getPermissionforGroup(g1);
		
		if (!gP1.isAccessible(PermissionType.SUBGROUP, pT1)){
			p.sendMessage(ChatColor.RED + "You dont have permission on the main group.");
			return true;
		}
		
		if (g1 instanceof Private){
			Private priv = (Private) g1;
			Group g2 = priv.getSuperGroup();
			priv.removeSuperGroup(g1);
			while (g2 != null && g2 instanceof Private){
				Private priv2 = (Private) g2;
				priv2.removeSubGroup(g1);
				g2 = priv2.getSuperGroup();
			}
			p.sendMessage(ChatColor.GREEN + "Super group has been removed.");
		}
		else
			p.sendMessage(ChatColor.RED + "That group cannot have super groups.");
		return true;
	}

}
