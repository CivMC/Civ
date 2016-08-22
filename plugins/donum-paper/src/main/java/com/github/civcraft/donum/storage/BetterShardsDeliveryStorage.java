package com.github.civcraft.donum.storage;

import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.bettershards.BetterShardsAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.civcraft.donum.Donum;

public class BetterShardsDeliveryStorage extends IDeliveryStorage {

	@Override
	public void loadDeliveryInventory(UUID uuid) {
		ConfigurationSection config = BetterShardsAPI.getConfigurationSection(Donum.getInstance(), uuid);
		ItemMap im = new ItemMap();
		for (String key : config.getKeys(false)) {
			ItemStack is = config.getItemStack(key);
			im.addItemStack(is);
		}
		for(ItemMap merge : Donum.getManager().getDAO().popStagedAdditions(uuid)) {
			im.merge(merge);
		}
		Donum.getManager().setDeliveryInventory(uuid, im);
		postLoad(im, uuid);
	}

	@Override
	public void updateDeliveryInventory(UUID uuid, ItemMap im, boolean async) {
		//async is ignored here, because this always has to be sync
		ConfigurationSection config = BetterShardsAPI.getConfigurationSection(Donum.getInstance(), uuid);
		//clear all old entries
		for (String key : config.getKeys(false)) {
			config.set(key, null);
		}
		int count = 0;
		for (Entry<ItemStack, Integer> entry : im.getEntrySet()) {
			ItemStack is = entry.getKey().clone();
			is.setAmount(entry.getValue());
			//yaml doesnt allow int as keys, so we have to add a string
			config.set("bla" + String.valueOf(count), is);
			count++;
		}
	}
}
