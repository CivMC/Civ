package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ItemMetaConverterHack extends BasicHack {
	
	@AutoLoad
	private boolean enabled;

	public ItemMetaConverterHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}
	
	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void invOpen(InventoryOpenEvent event) {
		if (!enabled) {
			return;
		}
		if (event.getInventory().getHolder() == null) {
			return; //GUI
		}
		Inventory inv = event.getInventory();
		for(ItemStack item : inv.getStorageContents()) {
			if (item == null) {
				continue;
			}
			if (!item.hasItemMeta()) {
				continue;
			}
			ItemMeta meta = item.getItemMeta();
			if (meta == null) {
				continue;
			}
			if (!meta.hasLore()) {
				continue;
			}
			List <BaseComponent[]> componentList = meta.getLoreComponents();
			for(BaseComponent[] componentArray : componentList) {
				for(BaseComponent component : componentArray) {
					if (!(component instanceof TextComponent)) {
						continue;
					}
					TextComponent text = (TextComponent) component;
					if (text.getText() == null || text.getText().length() == 0) {
						continue;
					}
					TextComponent copy = text.duplicate();
					if (copy.getExtra() != null) {
						copy.getExtra().clear();
					}
					List<BaseComponent> extra = text.getExtra();
					if (extra == null) {
						extra = new ArrayList<>();
						text.setExtra(extra);
					}
					extra.add(0, copy);
					text.setText("");
				}
			}
			meta.setLoreComponents(componentList);
			item.setItemMeta(meta);
		}
	}

}
