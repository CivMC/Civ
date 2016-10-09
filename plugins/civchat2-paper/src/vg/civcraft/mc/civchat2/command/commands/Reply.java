package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class Reply extends PlayerCommand{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	
	public Reply(String name) {
		super(name);
		setIdentifier("reply");
		setDescription("This command is used to reply to a message");
		setUsage("/reply <message>");
		setArguments(0,100);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatStrings.chatMustBePlayer);
			return true;
		}
		
		Player player = (Player) sender;
		String senderName = player.getName();
		UUID receiverUUID = chatMan.getPlayerReply(player);
		
		Player receiver = Bukkit.getPlayer(receiverUUID);
		if(receiver == null){
			sender.sendMessage(ChatStrings.chatNoOneToReplyTo);
			return true;
		}
		
		if(! (receiver.isOnline()) ){
			sender.sendMessage(ChatStrings.chatPlayerIsOffline);
			logger.debug(ChatStrings.chatPlayerIsOffline);
			return true;
		}
		
		if(player.getName().equals(receiver.getName())){
			CivChat2.warningMessage("Reply Command, Player Replying to themself??? Player: [" + senderName +"]");
			sender.sendMessage(ChatStrings.chatCantMessageSelf);
			return true;
		}
		
		if(args.length > 0){			
			StringBuilder sb = new StringBuilder();
			for(String s: args){
				sb.append(s + " ");
			}
			chatMan.sendPrivateMsg(player, receiver, sb.toString());
			return true;
		}
		else if (args.length == 0)
		{
			//player to chat with reply user
			String chatstart = String.format(ChatStrings.chatNowChattingWith, receiver.getName());
			player.sendMessage(chatstart);
			chatMan.removeChannel(senderName);
			chatMan.addChatChannel(senderName, receiver.getName());
			return true;
		}
		
		return false;
		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}	

}
