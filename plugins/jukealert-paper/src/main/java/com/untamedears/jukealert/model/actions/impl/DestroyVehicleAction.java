package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class DestroyVehicleAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "DESTROY_VEHICLE";

	public DestroyVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getVehicle());
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}
	
	/**
	 * @return Material of the vehicle destroyed
	 */
	public Material getVehicle() {
		return JAUtility.getVehicle(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Broke " + ItemUtils.getItemName(getVehicle());
	}

}
