package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

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
		try {
			return Material.valueOf(victim);
		} catch (IllegalArgumentException e) {
			return SpawnEggUtils.getSpawnEgg(EntityType.valueOf(victim));
		}
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	protected String getChatRepresentationIdentifier() {
		return "Exited " + ItemUtils.getItemName(getVehicle());
	}

}
