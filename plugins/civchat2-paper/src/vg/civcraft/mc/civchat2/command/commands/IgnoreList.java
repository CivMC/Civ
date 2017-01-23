package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.namelayer.NameAPI;

public class IgnoreList extends ChatCommand {

	public IgnoreList(String name) {

		super(name);
		setIdentifier("ignorelist");
		setDescription("Lists the players & groups you are ignoring");
		setUsage("/ignorelist");
		setArguments(0, 0);
		setSenderMustBePlayer(true);
		setErrorOnTooManyArgs(false);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		List<UUID> players = DBM.getIgnoredPlayers(player().getUniqueId());
		List<String> groups = DBM.getIgnoredGroups(player().getUniqueId());

		// No players ignored
		if (players == null || players.size() == 0) {
			msg(ChatStrings.chatNotIgnoringAnyPlayers);
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
			msg(msg);
		}

		// No groups ignored
		if (groups == null || groups.size() == 0) {
			msg(ChatStrings.chatNotIgnoringAnyGroups);
			return true;
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
			msg(msg);
			return true;
		}
	}
}
