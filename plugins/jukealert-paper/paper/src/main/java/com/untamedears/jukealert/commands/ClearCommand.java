package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClearCommand extends BaseCommand {

	@CommandAlias("jaclear")
	@Description("Deletes all logs a snitch has")
	public void execute(final Player sender, String[] args) {
		Snitch snitch = JAUtility.findLookingAtOrClosestSnitch(sender, JukeAlertPermissionHandler.getClearLogs());
		if (snitch == null) {
			sender.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to delete their logs!");
			return;
		}
		SnitchLogAppender logAppender = (SnitchLogAppender) snitch.getAppender(SnitchLogAppender.class);
		if (logAppender == null) {
			sender.sendMessage(
					ChatColor.RED + "This " + snitch.getType().getName() + " does not keep any logs");
			return;
		}
		logAppender.deleteLogs();
		sender.sendMessage(ChatColor.GREEN + "Deleted all logs for snitch " + JAUtility.genTextComponent(snitch));
	}
}
