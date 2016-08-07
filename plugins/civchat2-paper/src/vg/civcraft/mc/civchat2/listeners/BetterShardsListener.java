package vg.civcraft.mc.civchat2.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import vg.civcraft.mc.bettershards.events.PlayerChangeServerEvent;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;

public class BetterShardsListener implements Listener{
	private static CivChat2 plugin;
	private static CivChat2Manager chatMan;
	private static final String sep = "|";
	
	public BetterShardsListener(CivChat2 plugin) {
		this.plugin = plugin;
		chatMan = plugin.getCivChat2Manager();
	}
	
	
	@EventHandler
	public void PlayerChangeServer(PlayerChangeServerEvent event){
		if (event.getPlayerUUID() == null) {
			return;
		}
		String playerName = NameAPI.getCurrentName(event.getPlayerUUID());
		UUID playerToReplyUUID = chatMan.getPlayerReply(playerName);
		if (playerToReplyUUID != null) {
			String playerToReply = NameAPI.getCurrentName(playerToReplyUUID);
			if(playerToReply != null){
				MercuryAPI.sendMessage(event.getServerTravelingTo(), "channel" + sep + "reply" + sep + playerName + sep + playerToReply, "civchat2");
			}
		}
		String playerChat = chatMan.getChannel(playerName);
		if(playerChat != null){
			MercuryAPI.sendMessage(event.getServerTravelingTo(), "channel" + sep + "player" + sep + playerName + sep + playerChat, "civchat2");
			chatMan.removeChannel(playerName);
			return;
		}
		String groupChat = chatMan.getGroupChatting(playerName);
		if(groupChat != null){
			MercuryAPI.sendMessage(event.getServerTravelingTo(), "channel" + sep + "group" + sep + playerName + sep + groupChat, "civchat2");
			chatMan.removeGroupChat(playerName);
		}
	}
}
