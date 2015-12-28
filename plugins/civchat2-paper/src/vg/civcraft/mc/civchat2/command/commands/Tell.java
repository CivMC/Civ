package vg.civcraft.mc.civchat2.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.database.DatabaseManager;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class Tell extends PlayerCommand{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = (CivChat2CommandHandler) plugin.getCivChat2CommandHandler();
	private DatabaseManager DBM = plugin.getDatabaseManager();
	
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
			String chattingWith = chatMan.getChannel(player.getName());
			if (chattingWith != null) {
				chatMan.removeChannel(player.getName());
				player.sendMessage(ChatColor.GREEN + "You have been removed from private chat.");
			}
			else {
				player.sendMessage(ChatColor.RED + "You are not in a private chat");
			}
			return true;
		}
		
		if (CivChat2.getInstance().isMercuryEnabled()){
			for(String name : MercuryAPI.getAllPlayers()) {
				//iterate over names to find someone with a similar name to the one entered
				if (name.equalsIgnoreCase(args[0])  && !MercuryAPI.getServerforPlayer(name).equals(MercuryAPI.getServerforAccount(player.getUniqueId()))) {
					if(args.length == 1){
						if (DBM.isIgnoringPlayer(player.getName(), name) ){
							player.sendMessage(ChatColor.YELLOW + "You need to unignore " + name);
							return true;
						}
				        if (DBM.isIgnoringPlayer(name, player.getName())){
				            sender.sendMessage(ChatColor.YELLOW + "Player " + name +" is ignoring you");
				            return true;
				        }
						chatMan.removeChannel(player.getName());
						chatMan.addChatChannel(player.getName(), name);
						player.sendMessage(ChatColor.GREEN + "You are now chatting with " + name + " on another server.");
						return true;
					} else if(args.length >=2){
						StringBuilder builder = new StringBuilder();
						for (int x = 1; x < args.length; x++)
							builder.append(args[x] + " ");
						chatMan.sendPrivateMsgAcrossShards(player, name, builder.toString());
						return true;
					}
					break;
				}
			}
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
		if(args.length >= 2){
			//player and message
			StringBuilder builder = new StringBuilder();
			for (int x = 1; x < args.length; x++)
				builder.append(args[x] + " ");
				
			chatMan.sendPrivateMsg(player, receiver, builder.toString());
			return true;
		}
		else if(args.length == 1){
			if (DBM.isIgnoringPlayer(player.getUniqueId(), receiver.getUniqueId()) ){
				player.sendMessage(ChatColor.YELLOW+"You need to unignore "+receiver.getName());
				return true;
			}
	        if (DBM.isIgnoringPlayer(receiver.getUniqueId(), player.getUniqueId())){
	            sender.sendMessage(ChatColor.YELLOW + "Player " + receiver.getName() +" is ignoring you");
	            return true;
	        }
			chatMan.addChatChannel(player.getName(), receiver.getName());
			player.sendMessage(ChatColor.GREEN + "You are now chatting with " + receiver.getName() + ".");
			return true;
		}
		handler.helpPlayer(this, sender);
		
		return true;
		
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length != 1)
			return null;
		List<String> namesToReturn = new ArrayList<String>();
		if (plugin.isMercuryEnabled()) {
			Set<String> players = MercuryAPI.instance.getAllPlayers();
			for (String x: players) {
				if (x.toLowerCase().startsWith(args[0].toLowerCase()))
					namesToReturn.add(x);
			}
		}
		else {
			for (Player p: Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					namesToReturn.add(p.getName());
			}
		}
		return namesToReturn;
	}	

}
