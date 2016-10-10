package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.civchat2.database.DatabaseManager;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;

public class Tell extends ChatCommand {
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private DatabaseManager DBM = plugin.getDatabaseManager();

	public Tell(String name) {
		super(name);
		setIdentifier("tell");
		setDescription("Sends a private message to another player");
		setUsage("/tell <player> <message>");
		setSenderMustBePlayer(true);
		setErrorOnTooManyArgs(false);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		if (args.length == 0){
			UUID chattingWith = chatMan.getChannel(me());
			if (chattingWith != null) {
				chatMan.removeChannel(me());
				msg(ChatStrings.chatRemovedFromChat);
			}
			else {
				msg(ChatStrings.chatNotInPrivateChat);
			}
			return true;
		}

		Player receiver = argAsPlayer(0);
		if(receiver == null) {
			msg(ChatStrings.chatPlayerNotFound);
			return true;
		}

		if(! (receiver.isOnline())) {
			msg(ChatStrings.chatPlayerIsOffline);
			logger.debug(parse(ChatStrings.chatPlayerIsOffline));
			return true;
		}

		if(me().equals(receiver)) {
			msg(ChatStrings.chatCantMessageSelf);
			return true;
		}
		
		if(args.length >= 2) {
			//player and message
			StringBuilder builder = new StringBuilder();
			for (int x = 1; x < args.length; x++)
				builder.append(args[x] + " ");

			chatMan.sendPrivateMsg(me(), receiver, builder.toString());
			return true;
		}
		else if(args.length == 1) {
			if (DBM.isIgnoringPlayer(me().getUniqueId(), receiver.getUniqueId())) {
				msg(ChatStrings.chatNeedToUnignore);
				return true;
			}
			
			if (DBM.isIgnoringPlayer(receiver.getUniqueId(), me().getUniqueId())) {
				msg(ChatStrings.chatPlayerIgnoringYou);
				return true;
			}
			chatMan.addChatChannel(me(), receiver);
			msg(ChatStrings.chatNowChattingWith);
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length != 1) {
			return null;
		}

		return findPlayers(args[0]);
	}	
}
