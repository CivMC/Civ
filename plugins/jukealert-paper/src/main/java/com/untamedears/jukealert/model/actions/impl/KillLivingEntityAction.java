package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class KillLivingEntityAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "KILL_MOB";

	public KillLivingEntityAction(long time, Snitch snitch, UUID player, Location location,String victimName) {
		super(time, snitch, player, location, victimName);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	protected String getChatRepresentationIdentifier() {
		return "Killed " + getVictim();
	}

}
