package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

public class ArmourBound extends BasicHack {

	private SimpleAdminHacks plugin;
	private NamespacedKey key;
	@AutoLoad
	private List<String> whitelist;

	public ArmourBound(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		this.plugin = plugin;
		this.key = new NamespacedKey(plugin, "SAH_ArmourBound");
	}

	@EventHandler
	public void onArmourEquip(PlayerArmorChangeEvent event){
		Player player = event.getPlayer();
		ItemStack newItem = event.getNewItem();
		if (newItem == null) {
			return;
		}
		if (newItem.getType() == Material.AIR) {
			return;
		}
		EquipmentSlot realSlot = getRealSlot(event.getSlotType());
		if (realSlot == null) {
			return;
		}
		//From here, we take over from the copy, get the actual itemstack ourselves
		newItem = player.getInventory().getItem(realSlot);
		if (!whitelist.contains(newItem.getType().toString())) {
			return;
		}
		ItemMeta meta = newItem.getItemMeta();
		PersistentDataContainer itemContainer = meta.getPersistentDataContainer();
		if (itemContainer.get(this.key, PersistentDataType.STRING) == null) {
			itemContainer.set(this.key, PersistentDataType.STRING, player.getUniqueId().toString());
			Component boundComponent = Component.text("This armor is bound to: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE).append(Component.text(player.getName()).color(NamedTextColor.GOLD));
			MetaUtils.addComponentLore(meta, boundComponent);
			newItem.setItemMeta(meta);
			return;
		}
		UUID boundUUID = UUID.fromString(itemContainer.get(this.key, PersistentDataType.STRING));
		if (!boundUUID.equals(player.getUniqueId())) {
			player.closeInventory();
			player.getWorld().dropItemNaturally(player.getLocation(), newItem);
			player.getInventory().getItem(realSlot).setAmount(0);
			player.sendMessage(Component.text("This armor is not bound to you!").color(NamedTextColor.RED));
		}
	}

	private EquipmentSlot getRealSlot(PlayerArmorChangeEvent.SlotType slotType) {
		switch (slotType) {
			case HEAD:
				return EquipmentSlot.HEAD;
			case CHEST:
				return EquipmentSlot.CHEST;
			case LEGS:
				return EquipmentSlot.LEGS;
			case FEET:
				return EquipmentSlot.FEET;
			default:
				return null;
		}
	}
}
