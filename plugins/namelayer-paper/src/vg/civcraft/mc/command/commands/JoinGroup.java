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

public class JoinGroup extends PlayerCommand{

	public JoinGroup(String name) {
		super(name);
		setDescription("This command is used to join a password protected group.");
		setUsage("/groupsjoin <group> <password>");
		setIdentifier("groupsjoin");
		setArguments(2,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "How would this even work. Seriously my reddit account is rourke750, explain to me why " +
					"you would ever want to do this from console and I will remove this check.");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (g == null){
			p.sendMessage(ChatColor.RED + "Group is null.");
			return true;
		}
		if (g.isDisiplined()){
			p.sendMessage(ChatColor.RED + "This group is disiplined.");
			return true;
		}
		if (g.getPassword() == null){
			p.sendMessage(ChatColor.GREEN + "This group does not have a password, so you can't join it.");
			return true;
		}
		if (!g.getPassword().equals(args[1])){
			p.sendMessage(ChatColor.RED + "That password is incorrect");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		GroupPermission groupPerm = gm.getPermissionforGroup(g);
		PlayerType pType = groupPerm.getFirstWithPerm(PermissionType.JOIN_PASSWORD);
		if (pType == null){
			p.sendMessage(ChatColor.RED + "Someone derped. This group does not have the specified permission to let you join, sorry.");
			return true;
		}
		if (g.isMember(uuid, pType)){
			p.sendMessage(ChatColor.RED + "You are already a member.");
			return true;
		}
		if (g instanceof Private)
			((Private) g).addMember(uuid, pType);
		else
			g.addMember(uuid, pType);
		p.sendMessage(ChatColor.GREEN + "You have successfully been added to this group.");
		return true;
	}

}
