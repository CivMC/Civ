package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class LeaveFieldAction extends LoggablePlayerAction {

	public static final String ID = "LEAVE";

	public LeaveFieldAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.SKELETON_SKULL);
		SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(getPlayer()));
		skullMeta.setDisplayName(ChatColor.GOLD + "Leave");
		is.setItemMeta(skullMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		boolean sameWorld = JAUtility.isSameWorld(snitch.getLocation(), reference);
		TextComponent comp = new TextComponent(
				String.format("%sLeave  %s%s  ", ChatColor.GOLD, ChatColor.GREEN, NameAPI.getCurrentName(getPlayer())));
		comp.addExtra(JAUtility.genTextComponent(snitch));
		comp.addExtra(
				String.format(" %s%s", ChatColor.YELLOW, JAUtility.formatLocation(snitch.getLocation(), !sameWorld)));
		return comp;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
