package com.untamedears.JukeAlert.model.actions;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public abstract class LoggedSnitchAction {
	
	protected final long time;
	private CacheState state;
	
	public LoggedSnitchAction(long time) {
		this.time = time;
	}
	
	/**
	 * @return UNIX timestamp of when the action happened
	 */
	public long getTime() {
		return time;
	}
	
	public void setCacheState(CacheState state) {
		this.state = state;
	}
	
	public CacheState getCacheState() {
		return state;
	}
	
	/**
	 * @return Unique identifier for this type of action, one per class
	 */
	public abstract String getIdentifier();
	
	public abstract IClickable getGUIRepresentation();
	
	public abstract TextComponent getChatRepresentation();
	
	public abstract boolean hasPlayer();

}
