package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class ExitVehicleAction extends LoggablePlayerVictimAction {

	public static final String ID = "EXIT_VEHICLE";
	
	public ExitVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getVehicle());
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	/**
	 * @return Material of the exited vehicle
	 */
	public Material getVehicle() {
		return JAUtility.getVehicle(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public String getChatRepresentationIdentifier() {
		return "Exited " + ItemUtils.getItemName(getVehicle());
	}

}
