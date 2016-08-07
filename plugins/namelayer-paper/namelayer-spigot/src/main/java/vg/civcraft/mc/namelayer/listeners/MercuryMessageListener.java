package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupAddInvitation;
import vg.civcraft.mc.namelayer.events.GroupInvalidationEvent;
import vg.civcraft.mc.namelayer.events.GroupRemoveInvitation;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MercuryMessageListener implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	
	public MercuryMessageListener() {
		MercuryAPI.registerPluginMessageChannel("namelayer");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("namelayer"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];	
		String groupname = message[1];
		if (reason.equals("recache")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, groupname);
			Bukkit.getPluginManager().callEvent(e);
			if (GroupManager.getGroup(groupname) != null) {
				GroupManager.invalidateCache(groupname);
			}
		}
		else if (reason.equals("create")){
			Group group = new Group(groupname, UUID.fromString(message[2]), Boolean.getBoolean(message[3]), 
					message[4], Integer.parseInt(message[5]));
			if (group != null){
				gm.createGroup(group, false);
			}
			
		}
		else if (reason.equals("delete")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, groupname);
			Bukkit.getPluginManager().callEvent(e);
			Group group = GroupManager.getGroup(groupname);
			if (group != null) {
				gm.deleteGroup(group.getName(), false);
			}
		}
		else if (reason.equals("discipline")){
			Group group = GroupManager.getGroup(groupname);
			if (group != null && message[2] != null) {
				group.setDisciplined(Boolean.getBoolean(message[2]));
			}
		}
		else if (reason.equals("merge")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, groupname, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			Group group = GroupManager.getGroup(groupname);
			Group toMerge = GroupManager.getGroup(message[2]);
			if ((group != null) && (toMerge != null)){
				gm.mergeGroup(group, toMerge, false);
			}
		}
		else if (reason.equals("transfer")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			UUID newowner = UUID.fromString(message[2]);
			Group group = GroupManager.getGroup(groupname);
			if (group != null) {
				gm.transferGroup(group, newowner, false);
			}
		}
		else if (reason.equals("addInvitation")){
			Group playerGroup = GroupManager.getGroup(Integer.parseInt(groupname));
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
			}
		}
		else if (reason.equals("removeInvitation")){
			Group playerGroup = GroupManager.getGroup(Integer.parseInt(groupname));
			UUID invitedPlayerUUID = UUID.fromString(message[2]);
			GroupRemoveInvitation e = new GroupRemoveInvitation(playerGroup.getName(), invitedPlayerUUID);
			Bukkit.getPluginManager().callEvent(e);	
			if(playerGroup != null){
				playerGroup.removeInvite(invitedPlayerUUID, false);
				PlayerListener.removeNotification(invitedPlayerUUID, playerGroup);
			}
		}
		else if (reason.equals("defaultGroup")) {
			Group group = GroupManager.getGroup(groupname);
			UUID uuid = UUID.fromString(message [2]);
			if (group != null && uuid != null){
				NameLayerPlugin.getDefaultGroupHandler().setDefaultGroup(uuid, group, false);
			}
		} 
		else if (reason.equals("addMember")){
			Group group = GroupManager.getGroup(groupname);
			UUID uuid = UUID.fromString(message[2]);
			PlayerType type = PlayerType.getPlayerType(message[3]);
			if (group != null && uuid != null){
				group.addMember(uuid, type, false);
			}
		}
		else if (reason.equals("removeMember")){
			Group group = GroupManager.getGroup(groupname);
			UUID uuid = UUID.fromString(message[2]);
			if (group != null && uuid != null){
				group.removeMember(uuid, false);
			}
		}
		else if (reason.equals("setOwner")){
			Group group = GroupManager.getGroup(groupname);
			UUID uuid = UUID.fromString(message[2]);
			if (group != null && uuid != null){
				group.setOwner(uuid, false);
			}
		}
		else if (reason.equals("setPass")){
			Group group = GroupManager.getGroup(groupname);
			String pass = message[2];
			if (group != null && pass != null){
				group.setPassword(pass, false);
			}
		}
		else if (reason.equals("link")){
			Group supgroup = GroupManager.getGroup(groupname);
			Group subgroup = GroupManager.getGroup(message[2]);
			if (supgroup != null && subgroup != null){
				Group.link(supgroup, subgroup, false);
			}
		}
		else if (reason.equals("unlink")){
			Group supgroup = GroupManager.getGroup(groupname);
			Group subgroup = GroupManager.getGroup(message[2]);
			if (supgroup != null && subgroup != null){
				Group.unlink(supgroup, subgroup, false);
			}
		}
		else if (reason.equals("permadd")){
			Group group = GroupManager.getGroup(groupname);
			PlayerType ptype = PlayerType.valueOf(message[2]);
			PermissionType permt = PermissionType.getPermission(message[3]);
			if (group != null){
				GroupPermission perms = gm.getPermissionforGroup(group);
				perms.addPermission(ptype, permt, false);
			}
		}
		else if (reason.equals("permrem")){
			Group group = GroupManager.getGroup(groupname);
			PlayerType ptype = PlayerType.valueOf(message[2]);
			PermissionType permt = PermissionType.getPermission(message[3]);
			if (group != null){
				GroupPermission perms = gm.getPermissionforGroup(group);
				perms.removePermission(ptype, permt, false);
			}
		}
		else if (reason.equals("blAdd")){
			BlackList bl = NameLayerPlugin.getBlackList();
			UUID uuid = UUID.fromString(message[2]);
			if (bl != null && uuid != null){
				bl.addBlacklistMember(groupname, uuid, false);
			}
		}
		else if (reason.equals("blRem")){
			BlackList bl = NameLayerPlugin.getBlackList();
			UUID uuid = UUID.fromString(message[2]);
			if (bl != null && uuid != null){
				bl.removeBlacklistMember(groupname, uuid, false);
			}
		}
	}
}
