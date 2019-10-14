package com.untamedears.jukealert.model.actions.abstr;

import org.bukkit.Location;

import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public interface LoggableAction {
	
	IClickable getGUIRepresentation();
	
	TextComponent getChatRepresentation(Location reference);
	
	LoggedActionPersistence getPersistence();
	
	void setID(int id);
	
	int getID();
	
	void setCacheState(ActionCacheState state);
	
	ActionCacheState getCacheState();

}
