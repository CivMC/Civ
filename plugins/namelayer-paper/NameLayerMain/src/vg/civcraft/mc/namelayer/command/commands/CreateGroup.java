package vg.civcraft.mc.namelayer.command.commands;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.group.groups.PublicGroup;

public class CreateGroup extends PlayerCommandMiddle{

	public CreateGroup(String name) {
		super(name);
		setIdentifier("nlcg");
		setDescription("Create a group (Public or Private). Password is optional.");
		setUsage("/nlcg <name> (GroupType- default PRIVATE) (password)");
		setArguments(1,3);
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
		
		if (NameLayerPlugin.getInstance().getGroupLimit() < gm.countGroups(p.getUniqueId()) + 1){
			p.sendMessage(ChatColor.RED + "You cannot create any more groups! Please delete an un-needed group before making more.");
			return true;
		}
		
		//enforce regulations on the name
		if (name.length() > 32) {
			p.sendMessage(ChatColor.RED + "The group name is not allowed to contain more than 32 characters");
			return true;
		}
		Charset latin1 = StandardCharsets.ISO_8859_1;
		boolean invalidChars = false;
		if (!latin1.newEncoder().canEncode(name)) {
			invalidChars = true;
		}
		
		for(char c:name.toCharArray()) {
			if (Character.isISOControl(c)) {
				invalidChars = true;
			}
		}
		
		if(invalidChars) {
			p.sendMessage(ChatColor.RED + "You used characters, which are not allowed");
			return true;
		}
		
		if (gm.getGroup(name) != null){
			p.sendMessage(ChatColor.RED + "That group is already taken.");
			return true;
		}
		String password = "";
		if (args.length == 3)
			password = args[2];
		else
			password = null;
		GroupType type = GroupType.PRIVATE;
		if (args.length == 2)
		{
			if(GroupType.getGroupType(args[1]) == null){
				p.sendMessage(ChatColor.RED + "You have entered an invalid GroupType, use /nllgt to list GroupTypes.");
				return true;
			}
			type = GroupType.getGroupType(args[1]);
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		Group g = null;
		switch(type){
		case PRIVATE:
			g = new PrivateGroup(name, uuid, false, password, -1);
			break;
		case PUBLIC:
			g = new PublicGroup(name, uuid, false, password, -1);
		default:
			g = new Group(name, uuid, false, password, type, -1);
		}
		int id = gm.createGroup(g);
		if (id == -1) { // failure
			p.sendMessage(ChatColor.RED + "That group is already taken or creation failed.");
			return true;
		}
		g.setGroupId(id);
		p.sendMessage(ChatColor.GREEN + "The group " + g.getName() + " was successfully created.");
		if (NameLayerPlugin.getInstance().getGroupLimit() == gm.countGroups(p.getUniqueId()))
			p.sendMessage(ChatColor.YELLOW + "You have reached the group limit with " + NameLayerPlugin.getInstance().getGroupLimit() + " groups! Please delete un-needed groups if you wish to create more.");
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}