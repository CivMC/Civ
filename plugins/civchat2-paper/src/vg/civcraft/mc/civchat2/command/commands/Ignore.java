package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Ignore extends ChatCommand {

	public Ignore(String name) {

		super(name);
		setIdentifier("ignore");
		setDescription("Toggles ignoring a player");
		setUsage("/ignore <player>");
		setArguments(1, 1);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		Player ignore = argAsPlayer(0);
		if (ignore == null) {
			msg(ChatStrings.chatPlayerNotFound);
			return true;
		}

		String ignoreName = getRealName(ignore);
		String name = getRealName(player());
		if (ignoreName == name) {
			msg(ChatStrings.chatCantIgnoreSelf);
			return true;
		}
		// Player added to the list
		if (!DBM.isIgnoringPlayer(name, ignoreName)) {
			DBM.addIgnoredPlayer(name, ignoreName);
			String debugMessage = "Player ignored another Player, Player: " + name + " IgnoredPlayer: " + ignoreName;
			logger.debug(debugMessage);
			msg(ChatStrings.chatNowIgnoring, ignoreName);
			return true;
		// Player removed from the list
		} else {
			DBM.removeIgnoredPlayer(name, ignoreName);
			String debugMessage = "Player un-ignored another Player, Player: " + name + " IgnoredPlayer: " + ignoreName;
			logger.debug(debugMessage);
			msg(ChatStrings.chatStoppedIgnoring, ignoreName);
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if (args.length != 1) {
			return null;
		}
		return findPlayers(args[0]);
	}
}
