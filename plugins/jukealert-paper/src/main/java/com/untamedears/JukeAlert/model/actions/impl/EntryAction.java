package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.untamedears.JukeAlert.model.actions.PlayerAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class EntryAction extends PlayerAction {

	public EntryAction(long time, UUID player) {
		super(time, player);
	}

	@Override
	public String getIdentifier() {
		return "ENTRY";
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.SKELETON_SKULL);
		SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(getPlayer()));
		skullMeta.setDisplayName(ChatColor.GOLD + "Entry");
		is.setItemMeta(skullMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation() {
		return new TextComponent(
				String.format("%sEntry  %s%s", ChatColor.GOLD, ChatColor.GREEN, NameAPI.getCurrentName(getPlayer())));
	}

}
