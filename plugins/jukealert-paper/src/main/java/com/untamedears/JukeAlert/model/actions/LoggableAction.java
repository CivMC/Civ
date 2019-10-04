package com.untamedears.JukeAlert.model.actions;

import org.bukkit.Location;

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
