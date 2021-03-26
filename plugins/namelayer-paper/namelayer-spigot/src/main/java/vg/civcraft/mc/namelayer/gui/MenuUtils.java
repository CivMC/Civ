package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class MenuUtils {
	public static ItemStack toggleButton(boolean initState, String name, boolean canModify) {
		ItemStack is = null;
		if (initState) {
			is = AbstractGroupGUI.yesStack();
		} else {
			is = AbstractGroupGUI.noStack();
		}
		if (initState) {
			if (canModify) {
				ItemUtils.addLore(is, ChatColor.GOLD + "Currently turned on", ChatColor.AQUA + "Click to turn off");
			}
		}
		else {
			if  (canModify) {
				ItemUtils.addLore(is, ChatColor.GOLD + "Currently turned off", ChatColor.AQUA + "Click to turn on");
			}
		}
		if (!canModify) {
			ItemUtils.addLore(is, ChatColor.RED + "You don't have permission to", ChatColor.RED + "modify this setting");
		}
		ItemUtils.setDisplayName(is, name);
		return is;
	}
	
	public static ItemStack getPlayerSkull(UUID uuid) {
		return null; // TODO?
	}
	
	/**
	 * Doesn't work
	 *
	 * @param lore the lore to split
	 * @return a split list of lores
	 */
	public static List<String> splitLore(String lore) {
		System.out.println("Splitting " + lore);
		List<String> splitLore = new ArrayList<>();
		int maxLineLength = 50;
		StringBuilder sb = new StringBuilder();
		String [] split = lore.split(" ");
		for(int i = 0; i < split.length; i++) {
			String word = split [i];
			if ((sb.length() + word.length()) > maxLineLength) {
				//max line length reached
				if (sb.length() == 0) {
					//if empty, the word alone fills the line length so put it in anyway
					sb.append(word);
				}
				else {
					//include word in next run
					i--;
				}
				//add finished line
				splitLore.add(sb.toString());
				sb = new StringBuilder();
			}
			else {
				//just append, line not full yet
				sb.append(" ");
				sb.append(word);
			}
		}
		return splitLore;
	}
}
