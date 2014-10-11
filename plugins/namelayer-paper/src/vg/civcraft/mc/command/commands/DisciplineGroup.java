package vg.civcraft.mc.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.command.PlayerCommand;
import vg.civcraft.mc.group.Group;

public class DisciplineGroup extends PlayerCommand{

	public DisciplineGroup(String name) {
		super(name);
		setDescription("This command is used to disable a group from working.");
		setUsage("/groupsdisipline <group>");
		setIdentifier("groupsdisipline");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			sender.sendMessage(ChatColor.AQUA + "Meh, fine, just this one.");
		// checks and stuff should be in plugin.yml so going to assume that sender has perms
		// naaaaaaa
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (g == null){
			sender.sendMessage(ChatColor.RED + "Group does not exist.");
			return true;
		}
		if (!p.isOp() || !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "You do not have permission for this op command.");
			return true;
		}
		if (g.isDisiplined()){
			g.setDisiplined(true);
			sender.sendMessage(ChatColor.GREEN + "Group has been disabled.");
		}
		else
			g.setDisiplined(false);
		sender.sendMessage(ChatColor.GREEN + "Group has been enabled.");
		return true;
	}
}
