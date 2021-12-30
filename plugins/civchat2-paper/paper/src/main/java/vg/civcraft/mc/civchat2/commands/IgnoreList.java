package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.namelayer.NameAPI;

public class IgnoreList extends BaseCommand {

	@CommandAlias("ignorelist")
	@Description("Lists the players and groups you are ignoring")
	public void execute(Player player) {
		CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
		List<UUID> players = db.getIgnoredPlayers(player.getUniqueId());
		List<String> groups = db.getIgnoredGroups(player.getUniqueId());

		// No players ignored
		if (players == null || players.isEmpty()) {
			player.sendMessage(ChatStrings.chatNotIgnoringAnyPlayers);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<a>Ignored Players: \n<n>");
			for (UUID playerUUID : players) {
				String playerName = NameAPI.getCurrentName(playerUUID);
				if (playerName != null) {
					sb.append(playerName);
					sb.append(", ");
				}
			}
			String msg = sb.toString();
			if (msg.endsWith(", ")) {
				msg = msg.substring(0, msg.length() - 2);
			}
			player.sendMessage(msg);
		}

		// No groups ignored
		if (groups == null || groups.isEmpty()) {
			player.sendMessage(ChatStrings.chatNotIgnoringAnyGroups);
			return;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<a>Ignored Groups: \n<n>");
			for (String s : groups) {
				sb.append(s);
				sb.append(", ");
			}
			String msg = sb.toString();
			if (msg.endsWith(", ")) {
				msg = msg.substring(0, msg.length() - 2);
			}
			player.sendMessage(msg);
		}
	}
}
