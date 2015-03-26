package vg.civcraft.mc.civchat2.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.command.CivChat2PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;

public class Tell extends CivChat2PlayerCommand{
	public Tell(String name) {
		super(name);
		setIdentifier("tell");
		setDescription("This command is used to send a message or chat with another player");
		setUsage("/tell <PlayerName> (message)");
		setArgs(1,2);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatColor.YELLOW + "You must be a player to perform that command.");
		}
		
		Player player = (Player) sender;
		
		Player receiver = Bukkit.getPlayer(NameAPI.getUUID(args[0]));
		if(!(receiver == null)){
			sender.sendMessage(ChatColor.RED + "There is no player with that name.");
			return true;
		}
		
		if(!receiver.isOnline()){
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
			chatMan.addChatChannel(player.getName(), receiver.getName());
			player.sendMessage(ChatColor.GREEN + "You are now chatting with " + receiver.getName() + ".");
			chatMan.sendPrivateMsg(player, receiver, args[1]);
		}
		else{
			chatMan.addChatChannel(player.getName(), receiver.getName());
			player.sendMessage(ChatColor.GREEN + "You are now chatting with " + receiver.getName() + ".");
		}
		
		
		return true;
		
	}	

}
