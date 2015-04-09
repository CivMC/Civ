package vg.civcraft.mc.civchat2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class CivChat2Manager {	
	
	private CivChat2 plugin;
	//chatChannels in hashmap with (Player 1 name, player 2 name)
	private HashMap<String, String> chatChannels = new HashMap<String, String>();
	
	//groupChatChannels have (Message, GroupName)
	private HashMap<String, Group> groupChatChannels = new HashMap<String, Group>();
	
	//ignoreChannels have (Name, List of ignored groups)
	private HashMap<String, List<String>> ignoreChannels = new HashMap<String, List<String>>();
	
	//ignorePlayers have (recieversname, list of players they are ignoring
	private HashMap<String, List<String>> ignorePlayers = new HashMap<String, List<String>>();
	
	//replyList has (playerName, whotoreplyto)
	private HashMap<String, String> replyList = new HashMap<String, String>();
	
	
	//afk has player names in a list of who is afk
	private List<String> afk_player = new ArrayList<String>();
	
	private String afkMsg = ChatColor.AQUA + "That Player is currently AFK";
	private String ignoreMsg = ChatColor.YELLOW + "That Player is ignoring you";
	protected GroupManager gm = NameAPI.getGroupManager();

	
	
	public CivChat2Manager(CivChat2 pluginInstance){
		plugin = pluginInstance;
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
	
	public void groupChat(String chatGroupName, String chatMessage, String msgSender) {
		Player sender = Bukkit.getPlayer(NameAPI.getUUID(msgSender));
		Group chatGroup = gm.getGroup(chatGroupName);
		if(chatGroup == null){
			CivChat2.debugmessage("groupChat tried chatting to a group that doesn't exist: GroupName" + chatGroupName + 
					" Message: " + chatMessage + " Sender: " + msgSender);
			return;
		}
		String groupName = chatGroup.getName();
		List<UUID> groupMembers = chatGroup.getAllMembers();
		List<Player> recievers = new ArrayList<Player>();
		for(UUID u : groupMembers){
			//check if player is ignoring this group chat or is afk
			Player p = Bukkit.getPlayer(u);
			String playerName = NameAPI.getCurrentName(u);
			if(isAfk(playerName)){
				//player is afk do not include
				continue;
			}
			if(isIgnoringGroup(playerName, groupName)){
				//player is ignoring group do not include			
				continue;
			}
			if(p == sender){
				continue;
			}
			else{
				String grayMessage = ChatColor.GRAY + "[" + groupName + "] " + msgSender + ": ";
				String whiteMessage = ChatColor.WHITE + chatMessage;
				p.sendMessage(grayMessage + whiteMessage);
			}
		}		
	}
	

	public void removeGroupChat(String name) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Check if a player is ignoring a group chat channel
	 * @param name Player name of player to check
	 * @param chatChannel chatChannel to check
	 * @return Returns True if player is ignoring, false if not
	 */
	public boolean isIgnoringGroup(String name, String groupChatChannel) {
		if(ignoreChannels.containsKey(name)){
			List<String> ignored = ignoreChannels.get(name);
			if(ignored == null){
				return false;
			}
			if(ignored.contains(groupChatChannel)){
				return true;
			}
			else{
				return false;
			}
		}
		return false;
	}

	public void sendPrivateMsg(Player sender, Player receive, String chatMessage) {
		String senderName = sender.getName();
		String receiverName = receive.getName();
		
		String senderMessage = ChatColor.LIGHT_PURPLE + "To " + receiverName + ": "
				+ chatMessage;
		
		String receiverMessage = ChatColor.LIGHT_PURPLE + "From " + senderName
				+ ": " + chatMessage;
		
		CivChat2.debugmessage("ChatManager.sendPrivateMsg Sender: " + senderName + 
				" receiver: " + receiverName + " Message: " + chatMessage);
		if(isAfk(receive.getName())){
			receive.sendMessage(receiverMessage);
			sender.sendMessage(afkMsg);
			return;
		}
		else if(isIgnoringPlayer(receiverName, senderName)){
			//player is ignoring the sender
			sender.sendMessage(ignoreMsg);
			return;
		}
		CivChat2.debugmessage("Sending private chat message");
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
	private boolean isIgnoringPlayer(String receiverName, String senderName){
		if(ignorePlayers.containsKey(receiverName)){
			//they are ignoring people lets check who
			List<String> ignoredPlayers = ignorePlayers.get(receiverName);
			if(ignoredPlayers.contains(senderName)){
				return true;
			}
		}
		return false;
	}
	
	
	public void broadcastMessage(Player sender, String chatMessage,
			Set<Player> recipients) {
		// TODO Auto-generated method stub
		
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
		UUID uuid = NameAPI.getUUID(name);
		if(uuid == null){
			return null;
		}
		else{
			return uuid;
		}
	}
	public Group getGroupChatting(String name) {
		// TODO Auto-generated method stub
		return null;
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
			if(uuid == null){
				String errorMsg = "This should not be occuring... ERROR INFO: sender: [" 
						+ senderName + "] replyList Value: [" + replyeeName +"]";
				CivChat2.warningMessage(errorMsg);
				sender.sendMessage(ChatColor.RED + "Internal Error while performing reply");
			}
			else{
				return uuid;
			}
		}
		return null;
	}

}
