package vg.civcraft.mc.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.NameLayerPlugin;
import vg.civcraft.mc.command.PlayerCommand;

public class GlobalStats extends PlayerCommand{

	private boolean isRunning = false;
	public GlobalStats(String name) {
		super(name);
		setDescription("This command is used to get stats about groups and the sorts.");
		setUsage("/groupsglobalstats");
		setIdentifier("groupsglobalstats");
		setArguments(0,0);
	}

	@Override
	public boolean execute(final CommandSender sender, String[] args) {
		if (isRunning){
			sender.sendMessage(ChatColor.GREEN + "This command is already being run.  Try again later.");
			return true;
		}
		isRunning = true;
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				int count = NameLayerPlugin.getSaveManager().countGroups();
				sender.sendMessage(ChatColor.GREEN + "The amount of groups are: " + count);
			}
			
		});
		sender.sendMessage(ChatColor.GREEN + "Stats are being retrieved, please wait.");
		return true;
	}

}
