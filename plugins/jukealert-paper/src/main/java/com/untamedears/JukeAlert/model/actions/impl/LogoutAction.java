package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.untamedears.JukeAlert.model.actions.PlayerAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class LogoutAction extends PlayerAction {
	
	public static final String ID = "LOGOUT";

	public LogoutAction(long time, UUID player) {
		super(time, player);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(Material.SKELETON_SKULL);
		SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(getPlayer()));
		skullMeta.setDisplayName(ChatColor.GOLD + "Logout");
		skullMeta.addEnchant(Enchantment.DURABILITY, 1, true);
		is.setItemMeta(skullMeta);
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}

	@Override
	public TextComponent getChatRepresentation() {
		return new TextComponent(String.format("%sLogout  %s%s", ChatColor.GOLD, ChatColor.GREEN, NameAPI.getCurrentName(getPlayer())));
	}

}
