package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommand;

import java.util.List;

public class GlobalStats extends PlayerCommand{

	public GlobalStats(String name) {
		super(name);
		setIdentifier("nlgls");
		setDescription("Get the amount of global groups.");
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

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
