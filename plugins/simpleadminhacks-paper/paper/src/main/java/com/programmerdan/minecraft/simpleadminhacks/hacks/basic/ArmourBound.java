package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

import java.util.Set;
import java.util.UUID;

public class ArmourBound extends BasicHack {

	private SimpleAdminHacks plugin;
	private NamespacedKey key;
	@AutoLoad
	private Set<String> whitelist;

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
		for (String string : whitelist) {
			if (Material.matchMaterial(string) == null) {
				this.plugin.getLogger().warning("ArmourBound found " + string + " as a whitelist item but couldn't match it, are you sure this is correct?");
			}
		}

		UUID boundUUID = getOrSetOwnerUUID(newItem, player);
		if (!boundUUID.equals(player.getUniqueId())) {
			player.closeInventory();
			newItem.setAmount(0);
			player.getWorld().dropItemNaturally(player.getLocation(), newItem);
			player.sendMessage(Component.text("This armor is not bound to you!").color(NamedTextColor.RED));
			return;
		}
		addBoundLore(newItem, player);
	}

	private void addBoundLore(ItemStack equippedItem, Player player) {
		ItemMeta item = equippedItem.getItemMeta();
		Component boundComponent = Component.text("This armor is bound to: ").color(NamedTextColor.WHITE).append(Component.text(player.getName()).color(NamedTextColor.GOLD));
		MetaUtils.addComponentLore(item, boundComponent);
		equippedItem.setItemMeta(item);
	}

	private UUID getOrSetOwnerUUID(ItemStack itemStack, Player player) {
		PersistentDataContainer itemContainer = itemStack.getItemMeta().getPersistentDataContainer();
		return UUID.fromString(itemContainer.getOrDefault(this.key, PersistentDataType.STRING, player.getUniqueId().toString()));
	}
}
