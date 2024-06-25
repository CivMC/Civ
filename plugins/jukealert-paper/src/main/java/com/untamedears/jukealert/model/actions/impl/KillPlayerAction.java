package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

import java.util.UUID;

public class KillPlayerAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "KILL_PLAYER";

	public KillPlayerAction(long time, Snitch snitch, UUID player, Location location, UUID victim) {
		super(time, snitch, player, location, victim.toString());
	}
	
	public String getVictimName() {
		return NameAPI.getCurrentName(getVictimUUID());
	}
	
	public UUID getVictimUUID() {
		return UUID.fromString(victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
		is.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(true));
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}
	
	@Override
	public String getChatRepresentationIdentifier() {
		return "Killed " + getVictimName();
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
