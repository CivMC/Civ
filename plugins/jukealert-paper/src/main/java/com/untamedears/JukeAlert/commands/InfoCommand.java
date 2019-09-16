package com.untamedears.JukeAlert.commands;

import static com.untamedears.JukeAlert.util.JAUtility.findLookingAtOrClosestSnitch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;
import com.untamedears.JukeAlert.model.actions.PlayerAction;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;
import com.untamedears.JukeAlert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.NameAPI;

@CivCommand(id = "jainfo")
public class InfoCommand extends StandaloneCommand {

	private static final String[] autocompleteCommands = { "next", "censor", "action=", "player=" };

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Snitch snitch = findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getReadLogs());
		if (snitch == null) {
			player.sendMessage(
					ChatColor.RED + " You do not own any snitches nearby or lack permission to view their logs!");
			return true;
		}
		int offset = 0;
		String filterAction = null;
		String filterPlayer = null;
		if (args.length == 1) {
			try {
				offset = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not a number");
				return true;
			}
		}
		sendSnitchLog(player, snitch, offset, 8, filterAction, filterPlayer);
		return true;
	}

	public void sendSnitchLog(Player player, Snitch snitch, int offset, int pageLength, String actionType,
			String filterPlayerName) {
		List<LoggedSnitchAction> logs = snitch.getLoggingDelegate().getFullLogs();
		if (filterPlayerName != null) {
			UUID filterUUID = NameAPI.getUUID(filterPlayerName);
			if (filterUUID == null) {
				player.sendMessage(ChatColor.RED + filterPlayerName + " is not a player");
				return;
			}
			List<LoggedSnitchAction> logCopy = new LinkedList<>();
			for (LoggedSnitchAction log : logs) {
				if (!log.hasPlayer()) {
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
			List<LoggedSnitchAction> logCopy = new LinkedList<>();
			for (LoggedSnitchAction log : logs) {
				if (log.getIdentifier().equals(actionType)) {
					logCopy.add(log);
				}
			}
			logs = logCopy;
		}
		int initialOffset = pageLength * offset;
		if (initialOffset >= logs.size()) {
			TextComponent reply = JAUtility.genTextComponent(snitch);
			reply.addExtra(ChatColor.GOLD + " has only " + logs.size() + " logs fitting your criteria");
			player.spigot().sendMessage(reply);
			return;
		}
		int currentPageSize = Math.min(pageLength, logs.size() - initialOffset);
		ListIterator<LoggedSnitchAction> iter = logs.listIterator(initialOffset);
		int currentSlot = 0;
		TextComponent reply = new TextComponent(ChatColor.GOLD + "--- Page " + offset + " for ");
		reply.addExtra(JAUtility.genTextComponent(snitch));
		player.spigot().sendMessage(reply);
		while (currentSlot++ < currentPageSize) {
			player.spigot().sendMessage(iter.next().getChatRepresentation());
		}
	}

	@Override
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
