package vg.civcraft.mc.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameAPI;
import vg.civcraft.mc.command.PlayerCommand;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.GroupType;
import vg.civcraft.mc.group.groups.Private;
import vg.civcraft.mc.permission.GroupPermission;
import vg.civcraft.mc.permission.PermissionType;

public class AddSuperGroup extends PlayerCommand{

	public AddSuperGroup(String name) {
		super(name);
		setDescription("This command is used to add a supergroup to a group.");
		setUsage("/groupsaddsupergroup <main group> <super group>");
		setIdentifier("groupsaddsupergroup");
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
		if (!superGroupPerm.isAccessible(PermissionType.SUBGROUP, superType)){
			p.sendMessage(ChatColor.RED + "You dont have permission for a group.");
			return true;
		}
		if (main.isDisiplined() || superGroup.isDisiplined()){
			p.sendMessage(ChatColor.RED + "One of the groups is disiplined.");
			return true;
		}
		if (main.getType() != GroupType.PRIVATE){
			p.sendMessage(ChatColor.RED + "This group is not the right grouptype for adding superGroupgroups.");
			return true;
		}
		Private pri = (Private) main;
		pri.setSuperGroup(superGroup);
		
		if (superGroup instanceof Private){
			Private Super = (Private) superGroup;
			for (Group g: pri.getSubGroups()){
				Super.addSubGroup(g);
				for (UUID superMember: Super.getAllMembers())
					g.addMember(superMember, PlayerType.SUBGROUP);
			}
		}
		p.sendMessage(ChatColor.GREEN + "The super group has successfully been added.");
		return true;
	}

}
