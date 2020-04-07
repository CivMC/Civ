package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import com.untamedears.jukealert.util.JAUtility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class DestroyVehicleAction extends LoggablePlayerVictimAction {
	
	public static String ID = "DESTROY_VEHICLE";

	public DestroyVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getVehicle());
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Broke Vehicle");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%Broke Vehicle  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()), ChatColor.AQUA, getVehicle().toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}
	
	/**
	 * @return Material of the vehicle destroyed
	 */
	public Material getVehicle() {
		return Material.valueOf(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
