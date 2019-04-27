package vg.civcraft.mc.civchat2.command.commands;

import static vg.civcraft.mc.civchat2.ChatStrings.localChatFormat;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Exit extends ChatCommand {

	public Exit(String name) {
		super(name);
		setIdentifier("exit");
		setDescription("Moves to global chat");
		setUsage("/exit <message>");
		setErrorOnTooManyArgs(false);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			chatMan.removeChannel(player());
			chatMan.removeGroupChat(player());
			msg(ChatStrings.chatMovedToGlobal);
			return true;
		} else {
			StringBuilder chatMsg = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				chatMsg.append(args[i]);
				chatMsg.append(" ");
			}
			Set<Player> players = new HashSet<>(CivChat2.getInstance().getServer().getOnlinePlayers());
			chatMan.broadcastMessage(player(), chatMsg.toString(), localChatFormat, players);
			return true;
		}
	}
}
