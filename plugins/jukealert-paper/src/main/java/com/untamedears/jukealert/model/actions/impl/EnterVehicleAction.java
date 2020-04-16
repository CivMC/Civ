package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import com.untamedears.jukealert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class EnterVehicleAction extends LoggablePlayerVictimAction {

	public static final String ID = "ENTER_VEHICLE";
	
	public EnterVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getVehicle());
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Entered Vehicle");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sEntered Vehicle  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()), ChatColor.AQUA, getVehicle().toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

	/**
	 * @return Material of the entered vehicle
	 */
	public Material getVehicle() {
		return Material.valueOf(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
