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

    public void logGlobalMessage(Player sender, String message, Set<String> receivers) {

        String reciversNames = getPlayersNames(receivers);
        Location playerLocation = sender.getLocation();
        String messageToLog = String.format(
            "Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [GLOBAL], Receivers: [%s]",
            sender.getName(), message, playerLocation.getBlockX(), playerLocation.getBlockY(),
            playerLocation.getBlockZ(), reciversNames);
        logger.info(messageToLog);
    }

    public void logRemotePrivateMessage(String sender, String message, String receiverName) {
        String messageToLog = String.format(
            "Sender: [%s], Message: [%s], Location: REMOTE, Channel: [PRIVATE], Receiver: [%s]",
            sender, message, receiverName);
        logger.info(messageToLog);
    }

    public void logPrivateMessage(Player sender, String message, String receiverName) {
        Location playerLocation = sender.getLocation();
        String messageToLog = String.format(
            "Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [PRIVATE], Receiver: [%s]",
            sender.getName(), message, playerLocation.getBlockX(), playerLocation.getBlockY(),
            playerLocation.getBlockZ(), receiverName);
        logger.info(messageToLog);
    }

    public void logRemoteGroupMessage(String sender, String message, String groupName, Set<String> receivers) {
        String reciversNames = getPlayersNames(receivers);
        String messageToLog = String.format(
            "Sender: [%s], Message: [%s], Location: REMOTE, Channel: [GROUP], GroupName: [%s], Receivers: [%s]",
            sender, message, groupName, reciversNames);
        logger.info(messageToLog);
    }

    public void logGroupMessage(Player sender, String message, String groupName, Set<String> receivers) {
        String reciversNames = getPlayersNames(receivers);
        Location playerLocation = sender.getLocation();
        String messageToLog = String.format(
            "Sender: [%s], Message: [%s], Location: [%d, %d, %d], Channel: [GROUP], GroupName: [%s], Receivers: [%s]",
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
