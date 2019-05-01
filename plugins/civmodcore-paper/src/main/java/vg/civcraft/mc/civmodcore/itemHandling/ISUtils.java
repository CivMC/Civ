package vg.civcraft.mc.civmodcore.itemHandling;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ISUtils {

	public static void addLore(ItemStack is, String... lore) {
		ItemMeta im = is.getItemMeta();
		List<String> loreList = im.getLore();
		if (loreList == null) {
			loreList = new LinkedList<>();
		}
		for (String currLore : lore) {
			loreList.add(currLore);
		}
		im.setLore(loreList);
		is.setItemMeta(im);

	}

	public static void setLore(ItemStack is, String... lore) {
		ItemMeta im = is.getItemMeta();
		List<String> loreList = new LinkedList<>();
		for (String currLore : lore) {
			loreList.add(currLore);
		}
		im.setLore(loreList);
		is.setItemMeta(im);
	}

	public static void setName(ItemStack is, String name) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
	}

	public static String getName(ItemStack is) {
		return is.getItemMeta().getDisplayName();
	}

}
