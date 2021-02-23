package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

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
	public Material getMaterial() {
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
