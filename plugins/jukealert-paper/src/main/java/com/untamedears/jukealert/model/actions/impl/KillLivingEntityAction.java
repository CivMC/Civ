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

public class KillLivingEntityAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "KILL_MOB";

	public KillLivingEntityAction(long time, Snitch snitch, UUID player, Location location,String victimName) {
		super(time, snitch, player, location, victimName);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GOLD + "Killed Mob");
		is.setItemMeta(itemMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sKilled Mob  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, getVictim(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
