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

public class SetPassword extends PlayerCommand{

	public SetPassword(String name) {
		super(name);
		setIdentifier("nlsp");
		setDescription("This command is used to set a password on a group.");
		setUsage("/nlsp <group> <password>");
		setArguments(1,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("You may not use this command, must be a pluer.");
			return true;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		Group g = gm.getGroup(args[0]);
		if (g == null){
			p.sendMessage(ChatColor.RED + "That group does not exist.");
		}
		
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null){
			p.sendMessage(ChatColor.RED + "You do not have access to that group.");
			return true;
		}
		
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if (!gPerm.isAccessible(pType, PermissionType.PASSWORD)){
			p.sendMessage(ChatColor.RED + "You do not have permission to modify that group.");
			return true;
		}

		String password = null;
		if (args.length == 2)
			password = args[1];
		g.setPassword(password);
		p.sendMessage(ChatColor.GREEN + "Password has been successfully set to: " + g.getPassword());
		return true;
	}

}
