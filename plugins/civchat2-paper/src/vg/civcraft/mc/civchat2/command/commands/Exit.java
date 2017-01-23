package vg.civcraft.mc.civchat2.command.commands;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Exit extends ChatCommand {

	public Exit(String name) {
		super(name);
		setIdentifier("exit");
		setDescription("Moves to global chat");
		setUsage("/exit");
		setErrorOnTooManyArgs(false);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		chatMan.removeChannel(player());
		chatMan.removeGroupChat(player());
		msg(ChatStrings.chatMovedToGlobal);
		return true;
	}
}
