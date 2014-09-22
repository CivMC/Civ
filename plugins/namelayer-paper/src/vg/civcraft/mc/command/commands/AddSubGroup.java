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

public class AddSubGroup extends PlayerCommand{

	public AddSubGroup(String name) {
		super(name);
		setDescription("This command is used to add a subgroup to a group.");
		setUsage("/groupsaddsubgroup <main group> <sub group>");
		setIdentifier("groupsaddsubgroup");
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
		Group sub = gm.getGroup(args[1]);
		if (main == null){
			p.sendMessage(ChatColor.RED + "The group " + args[0] + " does not exist.");
			return true;
		}
		if (sub == null){
			p.sendMessage(ChatColor.RED + "The group " + args[1] + " does not exist.");
			return true;
		}
		GroupPermission perm = gm.getPermissionforGroup(main);
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = main.getPlayerType(uuid);
		if (!perm.isAccessible(PermissionType.SUBGROUP, type)){
			p.sendMessage(ChatColor.RED + "You dont have permission for that group.");
			return true;
		}
		if (main.isDisiplined() || sub.isDisiplined()){
			p.sendMessage(ChatColor.RED + "One of the groups is disiplined.");
			return true;
		}
		if (main.getType() != GroupType.PRIVATE){
			p.sendMessage(ChatColor.RED + "This group is not the right grouptype for adding subgroups.");
			return true;
		}
		Private pri = (Private) main;
		pri.addSubGroup(sub);
		
		if (sub instanceof Private){
			Private subGroup = (Private) sub;
			subGroup.setSuperGroup(pri);
			for (Group g: subGroup.getSubGroups()){
				pri.addSubGroup(g);
				for (UUID uu: pri.getAllMembers())
				pri.addMember(uu, PlayerType.SUBGROUP);
			}
		}
		p.sendMessage(ChatColor.GREEN + "The subgroup has successfully been added.");
		return true;
	}

}
