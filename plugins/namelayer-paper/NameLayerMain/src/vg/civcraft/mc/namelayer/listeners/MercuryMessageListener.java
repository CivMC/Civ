package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupAddInvitation;
import vg.civcraft.mc.namelayer.events.GroupInvalidationEvent;
import vg.civcraft.mc.namelayer.events.GroupRemoveInvitation;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	private NameLayerPlugin nl = NameLayerPlugin.getInstance();
	
	public MercuryMessageListener() {
		MercuryAPI.instance.registerPluginMessageChannel("namelayer");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("namelayer"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];	
		String group = message[1];
		if (reason.equals("recache")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group);
			Bukkit.getPluginManager().callEvent(e);
			if (gm.getGroup(group) != null) {
				gm.invalidateCache(group);
			}
		}
		else if (reason.equals("delete")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group);
			Bukkit.getPluginManager().callEvent(e);
			if (gm.getGroup(group) != null) {
				gm.invalidateCache(group);
			}
		}
		else if (reason.equals("merge")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			if (gm.getGroup(group) != null) {
				gm.invalidateCache(group);
			}
			if (gm.getGroup(message [2]) != null) {
				gm.invalidateCache(message [2]);
			}
		}
		else if (reason.equals("transfer")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			if (gm.getGroup(group) != null) {
				gm.invalidateCache(group);
			}
		}
		else if (reason.equals("addInvitation")){
			Group playerGroup = gm.getGroup(Integer.parseInt(group));
			PlayerType pType = PlayerType.getPlayerType(message[2]);
			UUID invitedPlayerUUID = UUID.fromString(message[3]);
			UUID inviterUUID = null;
			if(message.length >= 5){
				inviterUUID = UUID.fromString(message[4]);
			}
			GroupAddInvitation e = new GroupAddInvitation(playerGroup.getName(), pType, invitedPlayerUUID, inviterUUID);
			Bukkit.getPluginManager().callEvent(e);
			if (playerGroup != null) {
				InvitePlayer.sendInvitation(playerGroup, pType, invitedPlayerUUID, inviterUUID, false);
				playerGroup.addInvite(invitedPlayerUUID, pType, false);
			}
		}
		else if (reason.equals("removeInvitation")){
			Group playerGroup = gm.getGroup(Integer.parseInt(group));
			UUID invitedPlayerUUID = UUID.fromString(message[2]);
			GroupRemoveInvitation e = new GroupRemoveInvitation(playerGroup.getName(), invitedPlayerUUID);
			Bukkit.getPluginManager().callEvent(e);	
			if(playerGroup != null){
				playerGroup.removeInvite(invitedPlayerUUID, false);
				PlayerListener.removeNotification(invitedPlayerUUID, playerGroup);
			}
		}
	}
}
