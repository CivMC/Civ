package vg.civcraft.mc.civchat2.utility;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CivChat2FileLogger {

	private Logger logger;

	public CivChat2FileLogger() {

		logger = LogManager.getLogger("ChatLogs");
	}

	public void logGlobalMessage(Player sender, String message, Set<String> recivers) {

		String reciversNames = getPlayersNames(recivers);
		Location playerLocation = sender.getLocation();
		String messageToLog = String.format(
				"Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [GLOBAL], Recivers: [%s]",
				sender.getName(), message, playerLocation.getBlockX(), playerLocation.getBlockY(),
				playerLocation.getBlockZ(), reciversNames);
		logger.info(messageToLog);
	}

	public void logPrivateMessage(Player sender, String message, String reciverName) {

		Location playerLocation = sender.getLocation();
		String messageToLog = String.format(
				"Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [PRIVATE], Reciver: [%s]",
				sender.getName(), message, playerLocation.getBlockX(), playerLocation.getBlockY(),
				playerLocation.getBlockZ(), reciverName);
		logger.info(messageToLog);
	}

	public void logGroupMessage(Player sender, String message, String groupName, Set<String> recivers) {

		String reciversNames = getPlayersNames(recivers);
		Location playerLocation = sender.getLocation();
		String messageToLog = String.format(
				"Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [GROUP], GroupName: [%s], Recivers: [%s]",
				sender.getName(), message, playerLocation.getBlockX(), playerLocation.getBlockY(),
				playerLocation.getBlockZ(), groupName, reciversNames);
		logger.info(messageToLog);
	}

	private String getPlayersNames(Set<String> players) {

		StringBuilder builder = new StringBuilder();
		for (String player : players) {
			builder.append(player).append(", ");
		}
		if (players.size() > 0) {
			builder.deleteCharAt(builder.length() - 2);
		}
		return builder.toString();
	}
}
