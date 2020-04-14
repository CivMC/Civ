package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import com.untamedears.jukealert.util.JAUtility;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

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
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Ignited");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sIgnited  %s%s  %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
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
