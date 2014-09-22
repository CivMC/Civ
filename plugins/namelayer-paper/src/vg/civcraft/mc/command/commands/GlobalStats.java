package vg.civcraft.mc.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.command.PlayerCommand;

public class GlobalStats extends PlayerCommand{

	private boolean isRunning = false;
	public GlobalStats(String name) {
		super(name);
		setDescription("This command is used to get stats about groups and the sorts.");
		setUsage("/groupsstats");
		setIdentifier("groupsstats");
		setArguments(0,0);
	}

	@Override
	public boolean execute(final CommandSender sender, String[] args) {
		if (isRunning){
			sender.sendMessage(ChatColor.GREEN + "This command is already being run.  Try again later.");
			return true;
		}
		isRunning = true;
		Bukkit.getScheduler().runTaskAsynchronously(NameTrackerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				int count = NameTrackerPlugin.getSaveManager().countGroups();
				sender.sendMessage(ChatColor.GREEN + "The amount of groups are: " + count);
			}
			
		});
		sender.sendMessage(ChatColor.GREEN + "Stats are being retrieved, please wait.");
		return true;
	}

}
