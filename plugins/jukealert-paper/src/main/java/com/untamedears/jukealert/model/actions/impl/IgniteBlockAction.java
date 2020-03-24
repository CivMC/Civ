package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class IgniteBlockAction extends LoggablePlayerAction  {
	
	public static final String ID = "IGNITE_BLOCK";

	private Location location;
	
	public IgniteBlockAction(long time, Snitch snitch, UUID player, Location location) {
		super(time, snitch, player);
		this.location = location;
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(getPlayer(), location, time, null);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	/**
	 * @return Location the fire was put at
	 */
	public Location getLocation() {
		return location;
	}

}
