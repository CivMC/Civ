package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import com.untamedears.jukealert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class BlockBreakAction extends LoggableBlockAction {
	
	public static final String ID = "BLOCK_BREAK";

	public BlockBreakAction(long time, Snitch snitch, UUID player, Location location, String materialString) {
		this(time, snitch, player, location, JAUtility.parseMaterial(materialString));
	}
	
	public BlockBreakAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getMaterial());
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Break");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sBreak  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, material.toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

}
