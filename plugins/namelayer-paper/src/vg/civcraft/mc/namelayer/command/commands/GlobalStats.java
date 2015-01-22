package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommand;

public class GlobalStats extends PlayerCommand{

	public GlobalStats(String name) {
		super(name);
		setIdentifier("nlgls");
		setDescription("This command is used to get stats about groups and the sorts.");
		setUsage("/nlgls");
		setArguments(0,0);
	}

	@Override
	public boolean execute(final CommandSender sender, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				int count = NameLayerPlugin.getGroupManagerDao().countGroups();
				sender.sendMessage(ChatColor.GREEN + "The amount of groups are: " + count);
			}
			
		});
		sender.sendMessage(ChatColor.GREEN + "Stats are being retrieved, please wait.");
		return true;
	}

}
