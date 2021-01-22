package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
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
		ItemStack is = new ItemStack(Material.FLINT_AND_STEEL);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}
	
	@Override
	protected Location getLocationForStringRepresentation() {
		return location;
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

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Ignited";
	}

}
