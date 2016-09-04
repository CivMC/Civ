package vg.civcraft.mc.civchat2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.database.DatabaseManager;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2FileLogger;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CivChat2Manager {	
	
	private CivChat2Config config;
	private CivChat2FileLogger chatLog;
	private CivChat2 instance;
	private DatabaseManager DBM;
	private final String sep = "|";
	
	
	//chatChannels in hashmap with (Player 1 name, player 2 name)
	private HashMap<String, String> chatChannels;
	
	//groupChatChannels have (Player, Group)
	private HashMap<String, String> groupChatChannels;
	
	//replyList has (playerName, whotoreplyto)
	private HashMap<String, String> replyList;
	
	//afk has player names in a list of who is afk
	private List<String> afk_player;
	
	private static final String AFKMSG = ChatColor.AQUA + "That Player is currently AFK";
	private static final String IGNOREMSG = ChatColor.YELLOW + "That Player is ignoring you";
	protected static final GroupManager GM = NameAPI.getGroupManager();

	private String defaultColor;
	
	
	public CivChat2Manager(CivChat2 pluginInstance){
		instance = pluginInstance;
		config = instance.getPluginConfig();
		chatLog = instance.getCivChat2FileLogger();
		DBM = instance.getDatabaseManager();
		defaultColor = config.getDefaultColor();
		chatChannels = new HashMap<String, String>();
		groupChatChannels  = new HashMap<String, String>();
		replyList = new HashMap<String, String>();
		afk_player = new ArrayList<String>();
	}
	
	
	/**
	 * Gets the channel for player to player chat
	 * @param name    Player name of the channel
	 * @return        Returns a String of channel name, null if doesn't exist
	 */
	
	public String getChannel(String name) {
		if(chatChannels.containsKey(name)){
			CivChat2.debugmessage("getChannel returning value: " + chatChannels.get(name));
			return chatChannels.get(name);
		}
		CivChat2.debugmessage("getChannel returning null");
		return null;
	}
	
	/**
	 * Removes the channel from the channel storage
	 * @param name    Player Name of the channel
	 * 
	 */
	public void removeChannel(String name) {
		if(chatChannels.containsKey(name)){
			CivChat2.debugmessage("removeChannel removing channel: " + name);
			chatChannels.remove(name);
		}	
	}
	
	/**
	 * Adds a channel for player to player chat, if player1 is 
	 * currently in a chatChannel this will overwrite it
	 * @param player1   Senders name
	 * @param player2   Recievers name
	 */
	public void addChatChannel(String player1, String player2) {
		if(getChannel(player1) != null){
			chatChannels.put(player1, player2);
			CivChat2.debugmessage("addChatChannel adding channel for P1: " + player1 + " P2: " + player2);
		}
		else{
			chatChannels.put(player1, player2);
			CivChat2.debugmessage("addChatChannel adding channel for P1: " + player1 + " P2: " + player2);
		}
		
	}
	
	/**
	 * Method to Send a message to a player on a different shard
	 * @param receive Name of the player receiving the message
	 * @param chatMessage Message to send to the player
	 */
	public void sendMsgAcrossShards(String receiver, String chatMessage){
		String receiverServer = MercuryAPI.getServerforPlayer(receiver).getServerName();
		//This separator needs to be changed to load from config.
        String sep = "|";
        MercuryAPI.sendMessage(receiverServer, "msg" + sep + receiver + sep + chatMessage.replace(sep, ""), "civchat2");
	}
	
	/**
	 * Method to Send private message between to players
	 * @param sender Player sending the message
	 * @param receiver Player Receiving the message
	 * @param chatMessage Message to send from sender to receive
	 */
	public void sendPrivateMsg(Player sender, Player receiver, String chatMessage) {		
		StringBuilder sb = new StringBuilder();
	
		String senderName = sender.getName();
		String receiverName = receiver.getName();
		
		String senderMessage = sb.append(ChatColor.LIGHT_PURPLE)
								.append("To ")
								.append(receiverName) 
								.append(": ")
								.append(chatMessage)
								.toString();
		sb.delete(0, sb.length());
		
		String receiverMessage = sb.append(ChatColor.LIGHT_PURPLE) 
									.append("From ") 
									.append(senderName)
									.append(": ") 
									.append(chatMessage)
									.toString();
		sb.delete(0, sb.length());
		
		CivChat2.debugmessage(sb.append("ChatManager.sendPrivateMsg Sender: " )
								.append( senderName)  
								.append(" receiver: ")
								.append( receiverName) 
								.append( " Message: ") 
								.append(chatMessage)
								.toString());
		sb.delete(0, sb.length());
		
		if(isAfk(receiver.getName())){
			receiver.sendMessage(receiverMessage);
			sender.sendMessage(AFKMSG);
			return;
		}
		else if(DBM.isIgnoringPlayer(receiverName, senderName)){
			//player is ignoring the sender
			sender.sendMessage(IGNOREMSG);
			return;
		} else if (DBM.isIgnoringPlayer(senderName, receiverName)){
			sender.sendMessage(ChatColor.YELLOW+"You need to unignore "+receiverName);
			return;
		}
		CivChat2.debugmessage("Sending private chat message");
		chatLog.logPrivateMessage(sender, chatMessage, receiver.getName());
		replyList.put(receiverName, senderName);
		replyList.put(senderName, receiverName);
		sender.sendMessage(senderMessage);
		receiver.sendMessage(receiverMessage);
	}
	
	/**
	 * Method to Send a private message to a player on a different shard
	 * @param sender Player sending the message on the current shard
	 * @param receiver Name of the player receiving the message on a different shard
	 * @param chatMessage Message to send from sender to receiver
	 */
	public void sendPrivateMsgAcrossShards(Player sender, String receiver, String chatMessage){
        String receiverServer = MercuryAPI.getServerforPlayer(receiver).getServerName();
        //This separator needs to be changed to load from config.
        String sep = "|";
       
        if (DBM.isIgnoringPlayer(receiver, sender.getName())){
            sender.sendMessage(ChatColor.YELLOW + "Player " + receiver +" is ignoring you");
            return;
        }
       
        if (DBM.isIgnoringPlayer(sender.getName(), receiver)){
            sender.sendMessage(ChatColor.YELLOW + "You need to unignore " + receiver);
            return;
        }
       
        StringBuilder sb = new StringBuilder();
       
        String senderMessage = sb.append(ChatColor.LIGHT_PURPLE)
                                .append("To ")
                                .append(receiver)
                                .append(": ")
                                .append(chatMessage)
                                .toString();
       
        sender.sendMessage(senderMessage);
       
        CivChat2.debugmessage(sb.append("ChatManager.sendPrivateMsg Sender: " )
                .append( sender.getName())  
                .append(" receiver: ")
                .append( receiver)
                .append( " Message: ")
                .append(chatMessage)
                .toString());
        CivChat2.debugmessage("Sending private chat message");
        chatLog.logPrivateMessage(sender, chatMessage, receiver);
       
        replyList.put(sender.getName(), receiver);
        MercuryAPI.sendMessage(receiverServer, "pm" + sep + sender.getName() + sep + receiver.trim()+sep + chatMessage.replace(sep, ""), "civchat2");
    }
	
	/**
	 * Method to Receive a private message from a player on a different shard
	 * @param sender Player sending the message on the current shard
	 * @param receiver Name of the player receiving the message on a different shard
	 * @param chatMessage Message to send from sender to receiver
	 */
    public void receivePrivateMsgAcrossShards(String sender, Player receiver, String chatMessage){
        if(isAfk(receiver.getName())){
            sendMsgAcrossShards(sender, AFKMSG);
        }
       
        StringBuilder sb = new StringBuilder();
       
        String receiverMessage = sb.append(ChatColor.LIGHT_PURPLE)
                                .append("From ")
                                .append(sender)
                                .append(": ")
                                .append(chatMessage)
                                .toString();
        sb.delete(0, sb.length());
       
        replyList.put(receiver.getName(), sender);
        receiver.sendMessage(receiverMessage);
    }	
	
	/**
	 * Method to broadcast a message in global chat
	 * @param sender Player who sent the message
	 * @param chatMessage Message to send
	 * @param recipients Players in range to receive the message
	 */
	public void broadcastMessage(Player sender, String chatMessage, Set<Player> recipients) {
		int range = config.getChatRange();
		int height = config.getYInc();	
		Location location = sender.getLocation();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		double scale = (config.getYScale())/1000;			
		double chatdist = 0;
		
		UUID uuid = NameAPI.getUUID(sender.getName());
		StringBuilder sb = new StringBuilder();

		//do height check
		if(y > height){
			//player is above chat increase range
			CivChat2.debugmessage("Player is above Y chat increase range");
			int above = y - height;
			int newRange = (int) (range + (range * (scale*above)));
			range = newRange;
			CivChat2.debugmessage(sb.append("New chatrange = [" )
									.append(range) 
									.append("]")
									.toString());
			sb.delete(0, sb.length());
		}
		
		Set<String> recivers = new HashSet<String>();
		for (Player receiver : recipients){
			if (!DBM.isIgnoringPlayer(receiver.getUniqueId(), sender.getUniqueId())) {
				//loop through players and send to those that are close enough
				ChatColor color = ChatColor.valueOf(defaultColor);
				int rx = receiver.getLocation().getBlockX();
				int ry = receiver.getLocation().getBlockY();
				int rz = receiver.getLocation().getBlockZ();
				
				chatdist = Math.sqrt(Math.pow(x- rx, 2) + Math.pow(y - ry, 2) + Math.pow(z - rz, 2));
				
				if(chatdist <= range){
					if(receiver.getWorld() != sender.getWorld()){
						//reciever is in differnt world dont send
						continue;
					} else {
						receiver.sendMessage(sb.append(color) 
												.append( NameAPI.getCurrentName(uuid)) 
												.append(": ") 
												.append( chatMessage)
												.toString());
						sb.delete(0, sb.length());
					}
				}
				recivers.add(receiver.getName());
			}
		}
		recivers.remove(sender.getName()); //remove the sender from the list
		chatLog.logGlobalMessage(sender, chatMessage, recivers);
	}

	/**
	 * Check if player is afk
	 * @param playername Name of the player to check
	 * @return True if player is afk, false if they are not
	 */
	public boolean isAfk(String playername){
		if(afk_player.contains(playername)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Method to toggle a players afk/not afk status
	 * @param playername the player to change state
	 */
	public void toggleAfk(String playername){
		Player p = Bukkit.getPlayer(NameAPI.getUUID(playername));
		if(afk_player.contains(playername)){
			//Player was afk bring them back now
			afk_player.remove(playername);
			String afkNoMoreMessage = ChatColor.BLUE + "You are no longer in AFK status";
			p.sendMessage(afkNoMoreMessage);
		}
		else{
			afk_player.add(playername);
			String afkMessage = ChatColor.BLUE + "You have enabled AFK, type /afk to remove afk status";
			p.sendMessage(afkMessage);
		}
	}
	
	/**
	 * Get a players UUID 
	 * @param name	Players Name
	 * @return Returns the players UUID
	 */
	public UUID getPlayerUUID(String name) {
		return NameAPI.getUUID(name);
	}
	
	/**
	 * Gets the player to send reply to
	 * @param sender the person sending reply command
	 * @return the UUID of the person to reply to, null if none
	 */
	public UUID getPlayerReply(Player sender) {
		String senderName = sender.getName();
		if(replyList.containsKey(senderName)){
			//sender has someone to reply too
			String replyeeName = replyList.get(senderName);
			UUID uuid = NameAPI.getUUID(replyeeName);
			StringBuilder sb = new StringBuilder();
			if(uuid == null){
				String errorMsg = sb.append("This should not be occuring... ERROR INFO: sender: [" )
									.append(senderName) 
									.append("] replyList Value: [")
									.append( replyeeName) 
									.append("]")
									.toString();
				sb.delete(0, sb.length());
				CivChat2.warningMessage(errorMsg);
				sender.sendMessage(sb.append(ChatColor.RED )
									.append( "Internal Error while performing reply")
									.toString());
				sb.delete(0, sb.length());
			}
			else{
				return uuid;
			}
		}
		return null;
	}

	/**
	 * Gets the player to send reply to
	 * @param sender the person sending reply command
	 * @return the UUID of the person to reply to, null if none
	 */
	public UUID getPlayerReply(String sender) {
		if(replyList.containsKey(sender)){
			String replyName = replyList.get(sender);
			UUID uuid = NameAPI.getUUID(replyName);
			if(uuid == null){
				StringBuilder sb = new StringBuilder();
				String errorMsg = sb.append("This should not be occuring... ERROR INFO: sender: [").append(sender)
						.append("] replyList Value: [").append(replyName).append("]").toString();
				sb.delete(0, sb.length());
				CivChat2.warningMessage(errorMsg);
			}
			return uuid;
		} else {
			return null;
		}
	}
	
	/**
	 * Add a player to the replyList 
	 * @param The name of the player using the reply command. 
	 * @param The name of the player that will receive the reply
	 */
	public void addPlayerReply(String player, String playerToReply){
		if(player != null && playerToReply != null){
			replyList.put(player, playerToReply);
		}
	}
	
	/**
	 * Method to add a groupchat channel
	 * @param name Player sending the message
	 * @param group Group sending the message to
	 */
	public void addGroupChat(String name, String groupName) {
		groupChatChannels.put(name, groupName);
	}

	/**
	 * Method to send a message to a group
	 * @param name Player sending the message
	 * @param groupMsg Message to send to the group
	 * @param group Group to send the message too
	 */
	public void sendGroupMsg(String name, String groupMsg, Group group) {
		if (CivChat2.getInstance().isMercuryEnabled()) {
			MercuryAPI.sendGlobalMessage("gc" + sep + name + sep + group.getName() + sep + groupMsg.replace(sep, ""), "civchat2");
		}
		StringBuilder sb = new StringBuilder();
		Player msgSender = Bukkit.getPlayer(NameAPI.getUUID(name));
		List<Player> members = new ArrayList<Player>();
		List<UUID> membersUUID = group.getAllMembers();
		for(UUID uuid : membersUUID){
			//only add online players to members
			Player toAdd = Bukkit.getPlayer(uuid);
			if(toAdd != null && toAdd.isOnline() && NameAPI.getGroupManager().hasAccess(
					group, toAdd.getUniqueId(), PermissionType.getPermission("READ_CHAT"))){
				members.add(toAdd);
			}
		}
		msgSender.sendMessage(sb.append(ChatColor.GRAY )
								.append("[" )
								.append( group.getName() )
								.append( "] ")
								.append( name )
								.append(": " )
								.append(ChatColor.WHITE)
								.append( groupMsg)
								.toString());
		sb.delete(0, sb.length());
		for(Player receiver: members){
			sb.delete(0, sb.length());
			if(DBM.isIgnoringGroup(receiver.getUniqueId(), group.getName())){
				continue;
			}
			if(DBM.isIgnoringPlayer(receiver.getName(), name)){
				continue;
			}
			if(receiver.getName().equals(name)){
				continue;
			} else {
				receiver.sendMessage(sb.append(ChatColor.GRAY )
										.append( "[" )
										.append( group.getName() )
										.append( "] ") 
										.append( name) 
										.append( ": " )
										.append( ChatColor.WHITE) 
										.append( groupMsg)
										.toString());
				sb.delete(0, sb.length());
			}
		}
		
		Set<String> players = new HashSet<String>();
		for(UUID uuid : membersUUID){
			if(MercuryAPI.getAllAccounts().contains(uuid)){
				players.add(NameAPI.getCurrentName(uuid));
			}
		}
		players.remove(name); //remove the sender from the list
		chatLog.logGroupMessage(msgSender, groupMsg, group.getName(), players);
	}
	
	public void sendGroupMsgFromOtherShard(String name, String groupName, String groupMsg) {
		Group g = NameAPI.getGroupManager().getGroup(groupName);
		if (g == null) {
			return;
		}
		List<Player> members = new ArrayList<Player>();
		List<UUID> membersUUID = g.getAllMembers();
		for(UUID uuid : membersUUID){
			Player toAdd = Bukkit.getPlayer(uuid);
			if (toAdd != null && toAdd.isOnline() && NameAPI.getGroupManager().hasAccess(
					g, toAdd.getUniqueId(), PermissionType.getPermission("READ_CHAT"))) {
				members.add(toAdd);
			}
		}
		StringBuilder sb = new StringBuilder();
		for(Player receiver: members){
			sb.delete(0, sb.length());
			if(DBM.isIgnoringGroup(receiver.getUniqueId(), g.getName())){
				continue;
			}
			if(DBM.isIgnoringPlayer(receiver.getName(), name)){
				continue;
			}
			else {
				receiver.sendMessage(sb.append(ChatColor.GRAY )
										.append( "[" )
										.append( g.getName() )
										.append( "] ") 
										.append( name) 
										.append( ": " )
										.append( ChatColor.WHITE) 
										.append( groupMsg)
										.toString());
				sb.delete(0, sb.length());
			}
		}
	}
	
	/**
	 * Method to remove player from groupchat
	 * @param name Playername to remove from chat
	 */
	public void removeGroupChat(String name) {
		if(groupChatChannels.containsKey(name)){
			groupChatChannels.remove(name);
		}
	}

	/**
	 * Method to get the group player is currently chatting in
	 * @param name Players name
	 * @return Group they are currently chatting in
	 */
	public String getGroupChatting(String name) {
		CivChat2.debugmessage("Checking if user is groupchatting name=[" + name + "]");
		if(groupChatChannels.containsKey(name)){
			return groupChatChannels.get(name);
		} else {
			return null;
		}
	}
}