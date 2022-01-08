package com.github.devotedmc.hiddenore.commands;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.github.devotedmc.hiddenore.BlockConfig;
import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.DropConfig;
import com.github.devotedmc.hiddenore.DropItemConfig;
import com.github.devotedmc.hiddenore.HiddenOre;

/**
 * Management and maintenance commands
 * 
 * @author programmerdan
 */
public class CommandHandler implements CommandExecutor {

	final HiddenOre plugin;

	public CommandHandler(HiddenOre instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("hiddenore")) {
			if (sender.hasPermission(plugin.getCommand("hiddenore").getPermission())) {
				if (args.length >= 2) {
					if ("debug".equals(args[0])) {
						Config.isDebug = Boolean.parseBoolean(args[1]);
						sender.sendMessage("HiddenOre debug mode now " + (Config.isDebug ? "on" : "off"));
						return true;
					}
				}
				
				if (args.length >= 1) {
					if ("save".equals(args[0])) {
						plugin.getTracking().liveSave();
						sender.sendMessage("HiddenOre tracking forced live save scheduled");
						return true;
					} else if ("generate".equals(args[0])) {
						if (!(sender instanceof Player)) {
							sender.sendMessage("Cannot be run as console");
						}
						Player player = (Player) sender;
						double mult = 1;
						try {
							mult = Integer.parseInt(args[1]);
						} catch (Exception e) {mult = 1;}
						final double vmult = mult;
						final UUID world = player.getWorld().getUID();
						
						sender.sendMessage("Generating all drops, this could cause lag");
						
						Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
							@Override
							public void run() {
								long delay = 0l;
								Map<NamespacedKey, List<BlockConfig>> worldBlockConfigs = Config.instance.blockConfigs.getOrDefault(world, Config.instance.blockConfigs.get(null));
								if (worldBlockConfigs == null) {
									sender.sendMessage("No drops configured for blocks in this world.");
									return;
								}
								for (NamespacedKey blockConf : worldBlockConfigs.keySet()) {
									for (BlockConfig block : worldBlockConfigs.get(blockConf)) {
										for (String dropConf : block.getDrops()) {
											DropConfig drop = block.getDropConfig(dropConf);
											for (DropItemConfig item : drop.drops) {
												Bukkit.getScheduler().runTaskLater(plugin,  new Runnable() {
													@Override
													public void run() {
														sender.sendMessage(String.format("Block: %s, drop: %s", blockConf.toString(), dropConf));
														Item dropped = player.getWorld().dropItem(player.getLocation().add(0, 1.0, 0), item.render(vmult));
														dropped.setPickupDelay(20);
													}
												}, delay++);
											}
										}
									}
								}
							}
						});
						
						return true;
					}
				} else {
					Bukkit.getPluginManager().disablePlugin(plugin);
					Bukkit.getPluginManager().enablePlugin(plugin);
					sender.sendMessage("HiddenOre reloaded");
					return true;
				}
			}
		}

		return false;
	}
}
