package com.untamedears.jukealert.commands;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "jaclear")
public class ClearCommand extends StandaloneCommand {

	@Override
	public boolean execute(final CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Snitch snitch = JAUtility.findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getClearLogs());
		if (snitch == null) {
			sender.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to delete their logs!");
			return true;
		}
		SnitchLogAppender logAppender = (SnitchLogAppender) snitch.getAppender(SnitchLogAppender.class);
		if (logAppender == null) {
			sender.sendMessage(
					ChatColor.RED + "This " + snitch.getType().getName() + " does not keep any logs");
			return true;
		}
		logAppender.deleteLogs();
		sender.sendMessage(ChatColor.GREEN + "Deleted all logs for snitch " + JAUtility.genTextComponent(snitch));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}
}
