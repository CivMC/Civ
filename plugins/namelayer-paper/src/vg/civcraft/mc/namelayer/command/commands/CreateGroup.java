package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;

public class CreateGroup extends PlayerCommand{

	public CreateGroup(String name) {
		super(name);
		setDescription("This command is used to create a group (Public or Private). Password is optional.");
		setUsage("/nlgroupscreate <name> <GroupType> (password)");
		setIdentifier("nlgroupscreate");
		setArguments(2,3);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.DARK_BLUE + "Nice try console man, you can't bring me down. The computers won't win. " +
					"Dis a player commmand back off.");
			return true;
		}
		Player p = (Player) sender;
		String name = args[0];
		if (gm.getGroup(name) != null){
			p.sendMessage(ChatColor.RED + "That group is already taken.");
			return true;
		}
		String password = "";
		if (args.length == 3)
			password = args[2];
		else
			password = null;
		GroupType type = GroupType.getGroupType(args[1]);
		if (type == null){
			GroupType.displayGroupTypes(p);
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		Group g = null;
		switch(type){
		case PRIVATE:
			g = new PrivateGroup(name, uuid, false, password);
			break;
		default:
			g = new Group(name, uuid, false, password, type);
		}
		gm.createGroup(g);
		p.sendMessage(ChatColor.GREEN + "The group " + g.getName() + " was successfully created.");
		return true;
	}

}
