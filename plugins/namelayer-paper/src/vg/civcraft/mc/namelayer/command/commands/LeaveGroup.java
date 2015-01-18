package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;

public class LeaveGroup extends PlayerCommand{

	public LeaveGroup(String name) {
		super(name);
		setDescription("This command is to leave a group");
		setUsage("/nlleg <group>");
		setIdentifier("nlleg");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Nope, be player");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (g == null){
			p.sendMessage(ChatColor.RED + "That group does not exit.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are not a member of this group.");
			return true;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disciplined.");
			return true;
		}
		g.removeMember(uuid);
		p.sendMessage(ChatColor.GREEN + "You have been removed from the group.");
		return true;
	}

}
