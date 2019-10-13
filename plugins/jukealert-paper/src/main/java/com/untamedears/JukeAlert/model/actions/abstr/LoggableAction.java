package com.untamedears.JukeAlert.model.actions.abstr;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.actions.ActionCacheState;
import com.untamedears.JukeAlert.model.actions.LoggedActionPersistence;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public interface LoggableAction {
	
	public IClickable getGUIRepresentation();
	
	public TextComponent getChatRepresentation(Location reference);
	
	public LoggedActionPersistence getPersistence();
	
	public void setID(int id);
	
	public int getID();
	
	public void setCacheState(ActionCacheState state);
	
	public ActionCacheState getCacheState();

}
