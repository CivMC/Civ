package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import com.untamedears.jukealert.util.JAUtility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class OpenContainerAction extends LoggableBlockAction {
	
	public static final String ID = "OPEN_CONTAINER";

	public OpenContainerAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getMaterial());
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Opened");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%Opened  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()), ChatColor.AQUA, material.toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
