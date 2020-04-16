package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import com.untamedears.jukealert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class MountEntityAction extends LoggablePlayerVictimAction {

	public static final String ID = "MOUNT_ENTITY";

	public MountEntityAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.SADDLE);
		ItemAPI.setDisplayName(is,String.format("%Mounted %s%s", ChatColor.GOLD, ChatColor.AQUA, getVictim()));
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sMounted  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, getVictim(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
