package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import com.untamedears.jukealert.util.JAUtility;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

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
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Killed Player");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sKilled Player  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, getVictimName(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
