package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.w3c.dom.Text;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

import java.util.List;

public class ItemMetaConverterHack extends BasicHack {

	public ItemMetaConverterHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void convertOnInventoryOpen(final InventoryOpenEvent event) {
		final var inventory = event.getInventory();
		if (inventory.getHolder() == null) {
			return; // GUI
		}
		for (final ItemStack item : inventory.getStorageContents()) {
			processItem(item);
		}
	}

	public static void processItem(final ItemStack item) {
		if (item == null) {
			return;
		}
		final var meta = item.getItemMeta();
		if (meta == null) {
			return;
		}
		boolean displayNameWasChanged = false;
		if (meta.hasDisplayName()) {
			final var currentDisplayName = meta.displayName();

			assert currentDisplayName != null;
			if (!ChatUtils.isBaseComponent(currentDisplayName)) {
				meta.displayName(currentDisplayName.compact());
				displayNameWasChanged = true;
			}
		}
		boolean loreWasChanged = false;
		if (meta.hasLore()) {
			final var currentLore = meta.lore();
			assert currentLore != null;
			for (int i = 0, l = currentLore.size(); i < l; i++) {
				final var loreLine = currentLore.get(i);
				if (!ChatUtils.isBaseComponent(loreLine)) {
					currentLore.set(i, Component.empty().append(loreLine.compact()));
					loreWasChanged = true;
				}
			}
			if (loreWasChanged) {
				meta.lore(currentLore);
			}
		}
		if (displayNameWasChanged || loreWasChanged) {
			item.setItemMeta(meta);
		}
	}
}
