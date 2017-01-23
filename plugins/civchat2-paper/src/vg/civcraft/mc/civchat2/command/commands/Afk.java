package vg.civcraft.mc.civchat2.command.commands;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Afk extends ChatCommand {

	public Afk(String name) {

		super(name);
		setIdentifier("afk");
		setDescription("Sets your afk status.");
		setUsage("/afk");
		setErrorOnTooManyArgs(false);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		Boolean isAfk = chatMan.setPlayerAfk(player(), !chatMan.isPlayerAfk(player()));

		if (isAfk) {
			msg(ChatStrings.chatAfk);
		} else {
			msg(ChatStrings.chatNotAfk);
		}
		logger.debug(String.format("Player %s changed afk status to %s.", getRealName(player()), isAfk.toString()));

		return true;
	}
}
