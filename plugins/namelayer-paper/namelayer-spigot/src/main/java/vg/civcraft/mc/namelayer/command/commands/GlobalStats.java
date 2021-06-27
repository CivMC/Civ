package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;

@CommandAlias("nlgls")
@CommandPermission("namelayer.admin")
public class GlobalStats extends BaseCommandMiddle {

	@Syntax("/nlgls")
	@Description("Get the amount of global groups.")
	public void execute(final CommandSender sender) {
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				int count = NameLayerPlugin.getGroupManagerDao().countGroups();
				sender.sendMessage(ChatColor.GREEN + "The amount of groups are: " + count);
			}
			
		});
		sender.sendMessage(ChatColor.GREEN + "Stats are being retrieved, please wait.");
	}
}
