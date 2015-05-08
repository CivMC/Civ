package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.namelayer.command.PlayerCommand;

public class Tell extends PlayerCommand{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = CivChat2.getCivChat2CommandHandler();
	
	public Tell(String name) {
		super(name);
		setIdentifier("tell");
		setDescription("This command is used to send a message or chat with another player");
		setUsage("/tell <PlayerName> (message)");
		setArguments(0,100);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatColor.YELLOW + "You must be a player to perform that command.");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (args.length == 0){
			chatMan.removeChannel(player.getName());
			player.sendMessage(ChatColor.GREEN + "You have been removed from private chat.");
			return true;
		}
		
		UUID receiverUUID = chatMan.getPlayerUUID(args[0].trim());
		Player receiver = Bukkit.getPlayer(receiverUUID);
		if(receiver == null){
			sender.sendMessage(ChatColor.RED + "There is no player with that name.");
			return true;
		}
		
		if(! (receiver.isOnline()) ){
			String offlinemsg = ChatColor.RED + "Error: Player is offline.";
			sender.sendMessage(offlinemsg);
			logger.debug(offlinemsg);
			return true;
		}
		
		if(player.getName().equals(receiver.getName())){
			sender.sendMessage(ChatColor.RED + "Error: You cannot send a message to yourself.");
			return true;
		}
		if(args.length == 2){
			//player and message
			StringBuilder builder = new StringBuilder();
			for (int x = 1; x < args.length; x++)
				builder.append(args[x] + " ");
				
			chatMan.sendPrivateMsg(player, receiver, builder.toString());
			return true;
		}
		else if(args.length == 1){
			chatMan.addChatChannel(player.getName(), receiver.getName());
			player.sendMessage(ChatColor.GREEN + "You are now chatting with " + receiver.getName() + ".");
			return true;
		}
		handler.helpPlayer(this, sender);
		
		return true;
		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}	

}
