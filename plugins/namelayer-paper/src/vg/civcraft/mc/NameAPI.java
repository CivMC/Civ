package vg.civcraft.mc;

import java.util.UUID;

import vg.civcraft.mc.database.AssociationList;

public class NameAPI {
	private static GroupManager groupManager;
	private static AssociationList associations = NameLayerPlugin.getAssociationList();
	
	public NameAPI(GroupManager man){
		groupManager = man;
	}
	
	public static UUID getUUID(String playerName) {
		return associations.getUUID(playerName);
	}
	
	public static String getCurrentName(UUID uuid) {
		return associations.getCurrentName(uuid);
	}
	
	public static GroupManager getGroupManager(){
		return groupManager;
	}
	
	public static AssociationList getAssociationList(){
		return associations;
	}
}
