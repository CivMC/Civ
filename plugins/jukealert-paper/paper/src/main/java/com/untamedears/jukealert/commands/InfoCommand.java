package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.PlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;

public class InfoCommand extends BaseCommand {

	private static final String[] autocompleteCommands = { "next", "censor", "action=", "player=" };

	@CommandAlias("jainfo")
	@Description("Display information from a snitch")
	@Syntax("[page number_or_'next'] [censor] [action=action_type] [player=player_name]")
	public void execute(Player player, @Optional String pageNumber) {
		Snitch snitch = findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getReadLogs());
		if (snitch == null) {
			player.sendMessage(
					ChatColor.RED + " You do not own any snitches nearby or lack permission to view their logs!");
			return;
		}
		if (!snitch.hasAppender(SnitchLogAppender.class)) {
			player.sendMessage(ChatColor.RED + "This " + snitch.getType().getName() + " named " + snitch.getName()
					+ " can not save logs");
			return;
		}
		int offset = 0;
		String filterAction = null;
		String filterPlayer = null;
		if (pageNumber != null) {
			try {
				offset = Integer.parseInt(pageNumber);
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + pageNumber + " is not a number");
				return;
			}
		}
		int pageLength = JukeAlert.getInstance().getSettingsManager().getJaInfoLength(player.getUniqueId());
		sendSnitchLog(player, snitch, offset, pageLength, filterAction, filterPlayer);
	}

	public void sendSnitchLog(Player player, Snitch snitch, int offset, int pageLength, String actionType,
			String filterPlayerName) {
		SnitchLogAppender logAppender = (SnitchLogAppender) snitch.getAppender(SnitchLogAppender.class);
		List<LoggableAction> logs = new ArrayList<>(logAppender.getFullLogs());
		if (filterPlayerName != null) {
			UUID filterUUID = NameAPI.getUUID(filterPlayerName);
			if (filterUUID == null) {
				player.sendMessage(ChatColor.RED + filterPlayerName + " is not a player");
				return;
			}
			List<LoggableAction> logCopy = new LinkedList<>();
			for (LoggableAction log : logs) {
				if (!((SnitchAction) log).hasPlayer()) {
					continue;
				}
				PlayerAction playerAc = (PlayerAction) log;
				if (playerAc.getPlayer().equals(filterUUID)) {
					logCopy.add(log);
				}
			}
			logs = logCopy;
		}
		if (actionType != null) {
			List<LoggableAction> logCopy = new LinkedList<>();
			for (LoggableAction log : logs) {
				if (((SnitchAction) log).getIdentifier().equals(actionType)) {
					logCopy.add(log);
				}
			}
			logs = logCopy;
		}
		Collections.reverse(logs);
		int initialOffset = pageLength * offset;
		if (initialOffset >= logs.size()) {
			TextComponent reply = JAUtility.genTextComponent(snitch);
			reply.addExtra(ChatColor.GOLD + " has only " + logs.size() + " logs fitting your criteria");
			player.spigot().sendMessage(reply);
			return;
		}
		int currentPageSize = Math.min(pageLength, logs.size() - initialOffset);
		ListIterator<LoggableAction> iter = logs.listIterator(initialOffset);
		int currentSlot = 0;
		TextComponent reply = new TextComponent(ChatColor.GOLD + "--- Page " + offset + " for ");
		reply.addExtra(JAUtility.genTextComponent(snitch));
		player.spigot().sendMessage(reply);
		while (currentSlot++ < currentPageSize) {
			player.spigot().sendMessage(iter.next().getChatRepresentation(player.getLocation(), false));
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		List<String> completedArgs = new ArrayList<>();
		if (args.length > 0) {
			// Copy all of the arguments except the last one to the output
			for (int i = 0; i < args.length - 2; i++) {
				completedArgs.add(args[i]);
			}
			// Try to complete the last argument
			String lastArg = args[args.length - 1];
			for (String command : autocompleteCommands) {
				if (command.startsWith(lastArg)) {
					lastArg = command;
					break;
				}
			}
			// Add the last argument to the output
			completedArgs.add(lastArg);
		}
		return completedArgs;
	}
}
