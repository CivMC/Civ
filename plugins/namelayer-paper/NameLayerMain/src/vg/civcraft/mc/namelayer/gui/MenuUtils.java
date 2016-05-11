package vg.civcraft.mc.namelayer.gui;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

public class MenuUtils {
	public static ItemStack toggleButton(boolean initState, String name, boolean canModify) {
		ItemStack is = new ItemStack(Material.INK_SACK);
		if (initState) {
			is.setDurability((short) 10); //dye green
			if (canModify) {
			ISUtils.addLore(is, ChatColor.GOLD + "Currently turned on", ChatColor.AQUA + "Click to turn off");
			}
		}
		else {
			is.setDurability((short) 1); //dye red
			if  (canModify) {
			ISUtils.addLore(is, ChatColor.GOLD + "Currently turned off", ChatColor.AQUA + "Click to turn on");
			}
		}
		if (!canModify) {
			ISUtils.addLore(is, ChatColor.RED + "You don't have permission to", ChatColor.RED + "modify this setting");
		}
		ISUtils.setName(is, name);
		return is;
	}
	
	public static ItemStack getPlayerSkull(UUID uuid) {
		return null; // TODO?
	}
}
