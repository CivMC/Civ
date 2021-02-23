package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class MountEntityAction extends LoggablePlayerVictimAction {

	public static final String ID = "MOUNT_ENTITY";

	public MountEntityAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.SADDLE);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}
	
	@Override
	protected String getChatRepresentationIdentifier() {
		return "Mounted " + getVictim();
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
