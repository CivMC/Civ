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
import vg.civcraft.mc.namelayer.group.GroupType;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class AddSuperGroup extends PlayerCommand{

	public AddSuperGroup(String name) {
		super(name);
		setIdentifier("nlasg");
		setDescription("Add a supergroup to a group.");
		setUsage("/nlasg <main group> <super group>");
		setArguments(2,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "And it feels like I am just to close to touch you, but you are not a player.");
			return true;
		}
		Player p = (Player) sender;
		Group main = gm.getGroup(args[0]);
		Group superGroup = gm.getGroup(args[1]);
		if (main == null){
			p.sendMessage(ChatColor.RED + "The group " + args[0] + " does not exist.");
			return true;
		}
		if (superGroup == null){
			p.sendMessage(ChatColor.RED + "The group " + args[1] + " does not exist.");
			return true;
		}
		
		GroupPermission superGroupPerm = gm.getPermissionforGroup(superGroup);
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = main.getPlayerType(uuid);
		PlayerType superType = superGroup.getPlayerType(uuid);
		if (type == null || superType == null){
			p.sendMessage(ChatColor.RED + "You are not on one of the groups.");
			return true;
		}
		if (!superGroupPerm.isAccessible(superType, PermissionType.SUBGROUP)){
			p.sendMessage(ChatColor.RED + "You dont have permission for a group.");
			return true;
		}
		if (main.isDisciplined() || superGroup.isDisciplined()){
			p.sendMessage(ChatColor.RED + "One of the groups is disiplined.");
			return true;
		}
		if (main.getType() != GroupType.PRIVATE){
			p.sendMessage(ChatColor.RED + "This group is not the right grouptype for adding superGroupgroups.");
			return true;
		}
		PrivateGroup pri = (PrivateGroup) main;
		pri.setSuperGroup(superGroup);
		
		if (superGroup instanceof PrivateGroup){
			PrivateGroup Super = (PrivateGroup) superGroup;
			for (Group g: pri.getSubGroups()){
				Super.addSubGroup(g);
				for (UUID superMember: Super.getAllMembers())
					g.addMember(superMember, PlayerType.SUBGROUP);
			}
		}
		p.sendMessage(ChatColor.GREEN + "The super group has successfully been added.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.BLUE + "Fight me, bet you wont.\n Just back off you don't belong here.");
			return null;
		}

		if (args.length > 0)
			return GroupTabCompleter.complete(args[args.length - 1], PermissionType.SUBGROUP, (Player) sender);
		else{
			return  GroupTabCompleter.complete(null, PermissionType.SUBGROUP,(Player)sender);
		}
	}
}
