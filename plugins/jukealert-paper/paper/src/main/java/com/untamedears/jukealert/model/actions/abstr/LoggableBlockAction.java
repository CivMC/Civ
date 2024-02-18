package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public abstract class LoggableBlockAction extends LoggablePlayerAction {

	protected final Location location;
	protected final Material material;

	public LoggableBlockAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player);
		this.location = location;
		this.material = material;
	}

	/**
	 * @return Where the block was for which the action occured
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return Material of the block this action is about
	 */

	/*
	Some items cannot be held in the inventory, such as FIRE or POWDER_SNOW, therefore we have a check here to return
	the correct inventory item to represent.
	 */
	public Material getMaterial() {
		if (material == Material.POWDER_SNOW) {
			return Material.POWDER_SNOW_BUCKET;
		}
		if (material == Material.FIRE) {
			return Material.FLINT_AND_STEEL;
		}
		if (material == Material.SWEET_BERRY_BUSH) {
			return Material.SWEET_BERRIES;
		}
		return material;
	}

	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(getPlayer(), location, time, material.name());
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is;
		try {
			is = new ItemStack(getMaterial());
		} catch (Exception e) {
			is = new ItemStack(Material.STONE);
			ItemUtils.addLore(is,
					String.format("%sMaterial: %s%s", ChatColor.GOLD, ChatColor.AQUA, ItemUtils.getItemName(getMaterial())));
		}
		super.enrichGUIItem(is);
		ItemUtils.addLore(is, ChatColor.GOLD + JAUtility.formatLocation(location, false));
		return new DecorationStack(is);
	}
	
	@Override
	protected Location getLocationForStringRepresentation() {
		return location;
	}
}
