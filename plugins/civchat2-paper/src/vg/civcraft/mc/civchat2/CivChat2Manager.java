package vg.civcraft.mc.civchat2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.zipper.CivChat2FileLogger;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class CivChat2Manager {	
	
	private CivChat2Config config;
	private CivChat2FileLogger chatLog;
	private CivChat2 instance;
	
	
	//chatChannels in hashmap with (Player 1 name, player 2 name)
	private HashMap<String, String> chatChannels;
	
	//groupChatChannels have (Player, Group)
	private HashMap<String, String> groupChatChannels;
	
	//ignorePlayers have (recieversname, list of players they are ignoring
	private HashMap<String, List<String>> ignorePlayers;
	
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
		chatLog.test();
		defaultColor = config.getDefaultColor();
		chatChannels = new HashMap<String, String>();
		groupChatChannels  = new HashMap<String, String>();
		ignorePlayers = new HashMap<String, List<String>>();
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
	 * Method to send message in a group
	 * @param chatGroupName Name of the namelayer group
	 * @param chatMessage Message to send to the groupees
	 * @param msgSender Player that sent the message
	 */
	public void groupChat(String chatGroupName, String chatMessage, String msgSender) {
		StringBuilder sb = new StringBuilder();
		Player sender = Bukkit.getPlayer(NameAPI.getUUID(msgSender));
		Group chatGroup = GroupManager.getGroup(chatGroupName);
		if(chatGroup == null){
			CivChat2.debugmessage(sb.append("groupChat tried chatting to a group that doesn't exist: GroupName" )
									.append( chatGroupName ) 
									.append(" Message: ") 
									.append( chatMessage) 
									.append( " Sender: ") 
									.append( msgSender)
									.toString());
			sb.delete(0, sb.length());
			return;
		}
		String groupName = chatGroup.getName();
		List<UUID> groupMembers = chatGroup.getAllMembers();
		for(UUID u : groupMembers){
			//check if player is ignoring this group chat or is afk
			Player p = Bukkit.getPlayer(u);
			String playerName = NameAPI.getCurrentName(u);
			if(isAfk(playerName)){
				//player is afk do not include
				continue;
			}
			if(isIgnoringGroup(playerName, GroupManager.getGroup(groupName))){
				//player is ignoring group do not include			
				continue;
			}
			if(p == sender){
				continue;
			}
			else{
				String grayMessage = sb.append(ChatColor.GRAY)
										.append( "[")
										.append(groupName)
										.append( "] ")
										.append( msgSender)
										.append( ": ")
										.toString();
				sb.delete(0, sb.length());
				String whiteMessage = sb.append(ChatColor.WHITE)
										.append(chatMessage)
										.toString();
				sb.delete(0, sb.length());
				p.sendMessage(sb.append(grayMessage)
								.append(whiteMessage)
								.toString());
				sb.delete(0, sb.length());
			}
		}		
	}
	

	/**
	 * Method to Send private message between to players
	 * @param sender Player sending the message
	 * @param receive Player Receiving the message
	 * @param chatMessage Message to send from sender to receive
	 */
	public void sendPrivateMsg(Player sender, Player receive, String chatMessage) {		
		StringBuilder sb = new StringBuilder();
	
		String senderName = sender.getName();
		String receiverName = receive.getName();
		
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
		
		if(isAfk(receive.getName())){
			receive.sendMessage(receiverMessage);
			sender.sendMessage(AFKMSG);
			return;
		}
		else if(isIgnoringPlayer(receiverName, senderName)){
			//player is ignoring the sender
			sender.sendMessage(IGNOREMSG);
			return;
		} else if (isIgnoringPlayer(senderName, receiverName)){
			sender.sendMessage(ChatColor.YELLOW+"You need to unignore "+receiverName);
			return;
		}
		CivChat2.debugmessage("Sending private chat message");
		chatLog.writeToChatLog(sender, chatMessage, "P MSG");
		replyList.put(receiverName, senderName);		
		sender.sendMessage(senderMessage);
		receive.sendMessage(receiverMessage);
	}

	
	/**
	 * Check to see if a receiver is ignoring a sender (P2P chat only)
	 * @param receiverName
	 * @param senderName
	 * @return True if they are ignored, False if not
	 */
	public boolean isIgnoringPlayer(String receiverName, String senderName){
		if(ignorePlayers.containsKey(receiverName)){
			//they are ignoring people lets check who
			List<String> ignoredPlayers = ignorePlayers.get(receiverName);
			if(ignoredPlayers.contains(senderName)){
				return true;
			}
		}
		return false;
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
		chatLog.writeToChatLog(sender, chatMessage, "GLOBAL");

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
		
		for (Player receiver : recipients){
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
		}
	
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
	 * Method to see if a user is ignoring a group
	 * @param name Player to check
	 * @param chatChannel Groupname to check... format is GROUP<groupname>
	 * @return true if ignoring, false otherwise
	 */
	public boolean isIgnoringGroup(String name, Group group) {
		String ignoreGroupName = "GROUP" + group.getName();
		if(ignorePlayers.containsKey(name)){
			//player is ignoring something
			List<String> ignored = ignorePlayers.get(name);
			if(ignored == null){
				return false;
			}
			if(ignored.contains(ignoreGroupName)){
				//player is ignoring the groupchat
				return true;
			}			
		}
		return false;
	}


	/**
	 * Method to add a player to ignorelist
	 * @param name Player adding a new ignoree
	 * @param ignore PlayerName to ignore
	 * @return true if player added, false if removed from list
	 */
	public boolean addIgnoringPlayer(String name, String ignore) {
		if(ignorePlayers.containsKey(name)){
			//player already ignoring stuff
			List<String> ignored = ignorePlayers.get(name);
			if(ignored.contains(ignore)){
				//take player out of list
				if(ignored.size() == 1){
					//take owner out of ignorePlayers
					ignorePlayers.remove(name);
					return false;
				}
				ignored.remove(ignore);
				ignorePlayers.put(name, ignored);
				return false;
			} else {
				//add player to list
				ignored.add(ignore);
				ignorePlayers.put(name, ignored);
				return true;
			}
		} else {
			//player not yet ignoring anything
			List<String> newIgnoree = new ArrayList<String>();
			newIgnoree.add(ignore);
			ignorePlayers.put(name, newIgnoree);
			return true;
		}
		
	}


	/**
	 * Method to toggle ignoring a group
	 * @param name Player toggling ignoree
	 * @param ignore Group to Ignore
	 * @return True if added to list, false if removed
	 */
	public boolean addIgnoringGroup(String name, String ignore) {
		String groupName = "GROUP" + ignore;
		if(ignorePlayers.containsKey(name)){
			//player already ignoring stuff
			List<String> ignored = ignorePlayers.get(name);
			if(ignored.contains(groupName)){
				//take player out of list
				ignored.remove(groupName);
				ignorePlayers.put(name, ignored);
				return false;
			} else {
				//add player to list
				ignored.add(groupName);
				ignorePlayers.put(name, ignored);
				return true;
			}
		} else {
			//player not yet ignoring anything
			List<String> newIgnoree = new ArrayList<String>();
			newIgnoree.add(groupName);
			ignorePlayers.put(name, newIgnoree);
			return true;
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
		StringBuilder sb = new StringBuilder();
		Player msgSender = Bukkit.getPlayer(NameAPI.getUUID(name));
		List<Player> members = new ArrayList<Player>();
		List<UUID> membersUUID = group.getAllMembers();
		for(UUID uuid : membersUUID){
			//only add online players to members
			Player toAdd = Bukkit.getPlayer(uuid);
			if(toAdd == null){
				//null getplayer return just ignore
			}
			else if(toAdd.isOnline()){
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
			CivChat2.debugmessage(sb.append("Checking if player is ignoring group or player.. Receiver: " )
									.append( receiver.getName() )
									.append(" Group: ") 
									.append( group.getName()) 
									.append( " Sender: ")
									.append( name)
									.toString());
			sb.delete(0, sb.length());
			if(isIgnoringGroup(receiver.getName(), group)){
				continue;
			}
			if(isIgnoringPlayer(receiver.getName(), name)){
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
		
		chatLog.writeToChatLog(msgSender, groupMsg, "GROUP MSG");
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

	
	/**
	 * Method to pass ignoredFile Contents to ChatManager
	 * @throws IOException 
	 */
	public void loadIgnoredPlayers(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		CivChat2.debugmessage("ChatMan is trying to loadignoredplayers file");
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		if(br.readLine() == null){
			CivChat2.debugmessage("IgnoreList File is empty...");
			br.close();
			return;
		}
		String line;
		while ((line = br.readLine()) != null) {
			CivChat2.debugmessage(sb.append("Reading Ignore List curLine: ")
									.append(line)
									.toString());
			sb.delete(0, sb.length());
			String parts[] = line.split(",");
			String owner = parts[0];
			CivChat2.debugmessage(sb.append("Owner=" )
									.append( owner )
									.append( " # of Ignorees: ") 
									.append( (parts.length - 1))
									.toString());
			sb.delete(0, sb.length());
			List<String> participants = new ArrayList<>();
			for (int x = 1; x < parts.length; x++) {
				participants.add(parts[x]);
				CivChat2.debugmessage(sb.append("Adding ignoree name=" )
										.append(parts[x])
										.toString());
				sb.delete(0, sb.length());
			}
			ignorePlayers.put(owner, participants);
		}
		if(ignorePlayers != null){
			CivChat2.debugmessage(sb.append("Loaded ignore list... [" )
									.append( ignorePlayers.size()) 
									.append( "] ignore entries")
									.toString());
			sb.delete(0, sb.length());
		}
		br.close();
		fis.close();
	}


	public void saveIgnoredFile(File ignoredPlayers) throws IOException {
		FileOutputStream fos = new FileOutputStream(ignoredPlayers);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		Set<String> names = this.ignorePlayers.keySet();
		bw.append("Ignored File");
		bw.append("\n");
		for(String playerName : names){
			bw.append(playerName);
			for(String ignoree : ignorePlayers.get(playerName)){
				bw.append(",");
				bw.append(ignoree);
			}
			bw.append("\n");
		}
		bw.flush();
		bw.close();		
	}
	
	public void chatManTest(){
		CivChat2.debugmessage("Class is accessing ChatMan as it should");
	}


	public List<String> getIgnoredPlayers(String name) {
		if(ignorePlayers.containsKey(name)){
			//they are ignoring people lets make a list
			List<String> ignorees = new ArrayList<String>();
			List<String> temp = ignorePlayers.get(name);
			for(String s : temp){
				if(!s.contains("GROUP")){
					ignorees.add(s);
				}
			}
			return ignorees;
		}
		return null;
	}


	public List<String> getIgnoredGroups(String name) {
		if(ignorePlayers.containsKey(name)){
			//they are ignoring people lets make a list
			List<String> ignorees = new ArrayList<String>();
			List<String> temp = ignorePlayers.get(name);
			for(String s : temp){
				if(s.contains("GROUP")){
					String groupName = s.replace("GROUP", "");
					ignorees.add(groupName);
				}
				else{
					
				}
			}
			if(ignorees.size() == 0){
				return null;
			}
			return ignorees;
		}
		return null;
	}

}
