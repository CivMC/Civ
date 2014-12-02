package vg.civcraft.mc.namelayer.group;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public enum GroupType {

	PRIVATE,
	PUBLIC;
	
	public static GroupType getGroupType(String type){
		GroupType gType = null;
		try{
			gType = GroupType.valueOf(type.toUpperCase());
		} catch(IllegalArgumentException ex){}
		return gType;
	}
	
	public static void displayGroupTypes(Player p){
		String types = "";
		for (GroupType type: GroupType.values())
			types += type.name() + " ";
		p.sendMessage(ChatColor.RED +"That GroupType does not exists.\n" +
				"The current types are: " + types);
	}
	
}
